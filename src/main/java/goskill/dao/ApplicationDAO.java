package goskill.dao;

import goskill.db.DatabaseConnection;
import goskill.model.Penawaran;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {
    private static final String BASE =
        "SELECT a.*, u.nama_lengkap, p.judul, p.status AS p_status "
      + "FROM applications a JOIN talents t ON t.talent_id=a.talent_id "
      + "JOIN users u ON u.user_id=t.user_id JOIN projects p ON p.project_id=a.project_id ";

    public void apply(int projectId, int talentId, double harga, int hari, String pesan) throws SQLException {
        if (harga <= 0 || hari <= 0) throw new IllegalArgumentException("Harga dan estimasi hari harus > 0");
        String sql = "INSERT INTO applications(project_id,talent_id,harga_penawaran,estimasi_hari,pesan) VALUES(?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, talentId);
            ps.setDouble(3, harga);
            ps.setInt(4, hari);
            ps.setString(5, pesan);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062)
                throw new IllegalArgumentException("Kamu sudah mengajukan penawaran pada proyek ini");
            throw e;
        }
    }

    public List<Penawaran> findByProject(int projectId) throws SQLException {
        return query(BASE + "WHERE a.project_id=? ORDER BY a.created_at", projectId);
    }

    public List<Penawaran> findByTalent(int talentId) throws SQLException {
        return query(BASE + "WHERE a.talent_id=? ORDER BY a.created_at DESC", talentId);
    }

    private List<Penawaran> query(String sql, int param) throws SQLException {
        List<Penawaran> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Penawaran(rs.getInt("application_id"), rs.getInt("project_id"), rs.getInt("talent_id"),
                        rs.getString("nama_lengkap"), rs.getDouble("harga_penawaran"),
                        rs.getInt("estimasi_hari"), rs.getString("pesan"), rs.getString("status"),
                        rs.getString("judul"), rs.getString("p_status")));
                }
            }
        }
        return list;
    }
}
