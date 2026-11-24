package goskill.dao;

import goskill.db.DatabaseConnection;

import java.sql.*;

public class ReviewDAO {

    /** Ulasan hanya boleh jika proyek COMPLETED, dan pemberi ulasan adalah klien/talenta proyek tsb. */
    public void add(int projectId, int reviewerUserId, int rating, String komentar) throws SQLException {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating harus 1-5");

        try (Connection c = DatabaseConnection.getConnection()) {
            String status; int clientUser, talentUser;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT p.status, cu.user_id AS cu, tu.user_id AS tu FROM projects p "
                  + "JOIN clients cl ON cl.client_id=p.client_id JOIN users cu ON cu.user_id=cl.user_id "
                  + "JOIN talents ta ON ta.talent_id=p.talent_id JOIN users tu ON tu.user_id=ta.user_id "
                  + "WHERE p.project_id=?")) {
                ps.setInt(1, projectId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Proyek belum memiliki talenta terpilih");
                    status = rs.getString("status");
                    clientUser = rs.getInt("cu");
                    talentUser = rs.getInt("tu");
                }
            }
            if (!"COMPLETED".equals(status))
                throw new IllegalArgumentException("Ulasan hanya bisa diberikan setelah proyek COMPLETED");
            if (reviewerUserId != clientUser && reviewerUserId != talentUser)
                throw new IllegalArgumentException("Kamu bukan pihak dalam proyek ini");
            int reviewee = (reviewerUserId == clientUser) ? talentUser : clientUser;

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO reviews(project_id,reviewer_id,reviewee_id,rating,komentar) VALUES(?,?,?,?,?)")) {
                ps.setInt(1, projectId);
                ps.setInt(2, reviewerUserId);
                ps.setInt(3, reviewee);
                ps.setInt(4, rating);
                ps.setString(5, komentar);
                ps.executeUpdate();
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) throw new IllegalArgumentException("Kamu sudah memberi ulasan untuk proyek ini");
                throw e;
            }
            // perbarui rating rata-rata penerima ulasan
            for (String tabel : new String[]{"talents", "clients"}) {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE " + tabel + " SET rating_rata=(SELECT AVG(rating) FROM reviews WHERE reviewee_id=?) "
                      + "WHERE user_id=?")) {
                    ps.setInt(1, reviewee);
                    ps.setInt(2, reviewee);
                    ps.executeUpdate();
                }
            }
        }
    }
}
