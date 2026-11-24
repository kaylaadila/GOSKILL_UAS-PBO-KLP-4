package goskill.dao;

import goskill.db.DatabaseConnection;
import goskill.model.Project;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {
    private static final String BASE =
        "SELECT p.*, c.nama_usaha AS nama_klien, u.nama_lengkap AS nama_talent "
      + "FROM projects p JOIN clients c ON c.client_id=p.client_id "
      + "LEFT JOIN talents t ON t.talent_id=p.talent_id "
      + "LEFT JOIN users u ON u.user_id=t.user_id ";

    public void create(int clientId, String judul, String deskripsi, double anggaran,
                       LocalDate deadline, List<Integer> skillIds) throws SQLException {
        if (judul.isBlank()) throw new IllegalArgumentException("Judul wajib diisi");
        if (anggaran <= 0) throw new IllegalArgumentException("Anggaran harus lebih dari 0");
        if (skillIds.isEmpty()) throw new IllegalArgumentException("Pilih minimal 1 skill yang dibutuhkan");
        String sql = "INSERT INTO projects(client_id,judul,deskripsi,anggaran,deadline) VALUES(?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int projectId;
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, clientId);
                    ps.setString(2, judul);
                    ps.setString(3, deskripsi);
                    ps.setDouble(4, anggaran);
                    ps.setDate(5, Date.valueOf(deadline));
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { k.next(); projectId = k.getInt(1); }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO project_skills(project_id,skill_id) VALUES(?,?)")) {
                    for (int s : skillIds) { ps.setInt(1, projectId); ps.setInt(2, s); ps.addBatch(); }
                    ps.executeBatch();
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public List<Project> findOpen(String keyword) throws SQLException {
        String like = "%" + keyword + "%";
        return query(BASE + "WHERE p.status='OPEN' AND (p.judul LIKE ? OR p.deskripsi LIKE ?) "
                          + "ORDER BY p.created_at DESC", like, like);
    }

    public List<Project> findByClient(int clientId) throws SQLException {
        return query(BASE + "WHERE p.client_id=? ORDER BY p.created_at DESC", clientId);
    }

    /** Klien menerima satu penawaran: lainnya ditolak, proyek jadi IN_PROGRESS (satu transaksi). */
    public void accept(int applicationId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int projectId, talentId;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT project_id, talent_id FROM applications WHERE application_id=?")) {
                    ps.setInt(1, applicationId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Penawaran tidak ditemukan");
                        projectId = rs.getInt(1);
                        talentId = rs.getInt(2);
                    }
                }
                exec(c, "UPDATE applications SET status='ACCEPTED' WHERE application_id=?", applicationId);
                exec(c, "UPDATE applications SET status='REJECTED' WHERE project_id=? AND application_id<>?",
                     projectId, applicationId);
                int n = exec(c, "UPDATE projects SET status='IN_PROGRESS', talent_id=? "
                              + "WHERE project_id=? AND status='OPEN'", talentId, projectId);
                if (n == 0) throw new IllegalArgumentException("Proyek tidak lagi berstatus OPEN");
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void complete(int projectId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            int n = exec(c, "UPDATE projects SET status='COMPLETED' "
                          + "WHERE project_id=? AND status='IN_PROGRESS'", projectId);
            if (n == 0) throw new IllegalArgumentException("Hanya proyek IN_PROGRESS yang bisa diselesaikan");
        }
    }

    // ---- helper ----
    private int exec(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            return ps.executeUpdate();
        }
    }

    private List<Project> query(String sql, Object... params) throws SQLException {
        List<Project> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate("deadline");
                    list.add(new Project(rs.getInt("project_id"), rs.getString("judul"),
                        rs.getString("deskripsi"), rs.getDouble("anggaran"),
                        d == null ? null : d.toLocalDate(), rs.getString("status"),
                        rs.getString("nama_klien"), rs.getString("nama_talent")));
                }
            }
        }
        return list;
    }
}
