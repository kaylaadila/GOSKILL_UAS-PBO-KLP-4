package goskill.dao;

import goskill.db.DatabaseConnection;
import goskill.model.Portfolio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PortfolioDAO {

    public void add(int talentId, String judul, String deskripsi, String kategori, String tautan) throws SQLException {
        if (judul == null || judul.isBlank()) throw new IllegalArgumentException("Judul portofolio wajib diisi");
        String sql = "INSERT INTO portfolios(talent_id,judul,deskripsi,kategori,tautan) VALUES(?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, talentId);
            ps.setString(2, judul.trim());
            ps.setString(3, deskripsi);
            ps.setString(4, kategori);
            ps.setString(5, tautan);
            ps.executeUpdate();
        }
    }

    public List<Portfolio> findByTalent(int talentId) throws SQLException {
        List<Portfolio> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT * FROM portfolios WHERE talent_id=? ORDER BY created_at DESC")) {
            ps.setInt(1, talentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new Portfolio(rs.getInt("portfolio_id"), rs.getString("judul"),
                        rs.getString("deskripsi"), rs.getString("kategori"), rs.getString("tautan")));
            }
        }
        return list;
    }

    public void delete(int portfolioId, int talentId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM portfolios WHERE portfolio_id=? AND talent_id=?")) {
            ps.setInt(1, portfolioId);
            ps.setInt(2, talentId);
            ps.executeUpdate();
        }
    }
}
