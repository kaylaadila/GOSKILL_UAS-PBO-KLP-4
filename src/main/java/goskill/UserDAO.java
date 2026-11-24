package goskill.dao;

import goskill.db.DatabaseConnection;
import goskill.model.*;
import goskill.util.PasswordUtil;

import java.sql.*;

public class UserDAO {

    /** extra1/extra2: Talent = universitas/jurusan, Client = nama usaha/bidang usaha. */
    public void register(String nama, String email, String password, String role,
                         String extra1, String extra2) throws SQLException {
        if (nama.isBlank() || email.isBlank() || password.length() < 6)
            throw new IllegalArgumentException("Data tidak lengkap / kata sandi minimal 6 karakter");

        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO users(nama_lengkap,email,password_hash,role) VALUES(?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, nama);
                    ps.setString(2, email);
                    ps.setString(3, PasswordUtil.hash(password));
                    ps.setString(4, role);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { k.next(); userId = k.getInt(1); }
                }
                String sql = role.equals("TALENT")
                    ? "INSERT INTO talents(user_id,universitas,jurusan) VALUES(?,?,?)"
                    : "INSERT INTO clients(user_id,nama_usaha,bidang_usaha) VALUES(?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, extra1);
                    ps.setString(3, extra2);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                if (e.getErrorCode() == 1062) throw new IllegalArgumentException("Email sudah terdaftar");
                throw e;
            }
        }
    }

    /** Mengembalikan Talent/Client, atau null jika email/kata sandi salah. */
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT u.*, t.talent_id, t.universitas, t.jurusan, t.rating_rata AS t_rating, "
                   + "c.client_id, c.nama_usaha, c.bidang_usaha, c.rating_rata AS c_rating "
                   + "FROM users u LEFT JOIN talents t ON t.user_id=u.user_id "
                   + "LEFT JOIN clients c ON c.user_id=u.user_id WHERE u.email=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                if (!PasswordUtil.verify(password, rs.getString("password_hash"))) return null;
                if (!"AKTIF".equals(rs.getString("status_akun")))
                    throw new IllegalArgumentException("Akun tidak aktif / diblokir");

                int uid = rs.getInt("user_id");
                String nama = rs.getString("nama_lengkap");
                if ("TALENT".equals(rs.getString("role"))) {
                    return new Talent(uid, nama, email, rs.getDouble("t_rating"),
                        rs.getInt("talent_id"), rs.getString("universitas"), rs.getString("jurusan"));
                } else if ("CLIENT".equals(rs.getString("role"))) {
                    return new Client(uid, nama, email, rs.getDouble("c_rating"),
                        rs.getInt("client_id"), rs.getString("nama_usaha"), rs.getString("bidang_usaha"));
                }
                throw new IllegalArgumentException("Peran ADMIN belum tersedia di demo ini");
            }
        }
    }

    /** Semua talenta aktif (untuk rekomendasi talenta oleh klien). */
    public java.util.List<Talent> findAllTalents() throws SQLException {
        java.util.List<Talent> list = new java.util.ArrayList<>();
        String sql = "SELECT u.user_id, u.nama_lengkap, u.email, t.talent_id, t.universitas, t.jurusan, t.rating_rata "
                   + "FROM talents t JOIN users u ON u.user_id=t.user_id WHERE u.status_akun='AKTIF'";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new Talent(rs.getInt("user_id"), rs.getString("nama_lengkap"), rs.getString("email"),
                    rs.getDouble("rating_rata"), rs.getInt("talent_id"),
                    rs.getString("universitas"), rs.getString("jurusan")));
        }
        return list;
    }
}
