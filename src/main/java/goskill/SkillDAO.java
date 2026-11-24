package goskill.dao;

import goskill.db.DatabaseConnection;
import goskill.model.Skill;

import java.sql.*;
import java.util.*;

public class SkillDAO {

    public List<Skill> findAll() throws SQLException {
        return query("SELECT skill_id, nama_skill, kategori, NULL AS level FROM skills "
                   + "ORDER BY kategori, nama_skill", null);
    }

    public List<Skill> findByTalent(int talentId) throws SQLException {
        return query("SELECT s.skill_id, s.nama_skill, s.kategori, ts.level FROM talent_skills ts "
                   + "JOIN skills s ON s.skill_id=ts.skill_id WHERE ts.talent_id=? ORDER BY s.nama_skill", talentId);
    }

    public List<Skill> findByProject(int projectId) throws SQLException {
        return query("SELECT s.skill_id, s.nama_skill, s.kategori, NULL AS level FROM project_skills ps "
                   + "JOIN skills s ON s.skill_id=ps.skill_id WHERE ps.project_id=? ORDER BY s.nama_skill", projectId);
    }

    public Set<Integer> idsByTalent(int talentId) throws SQLException {
        Set<Integer> ids = new HashSet<>();
        for (Skill s : findByTalent(talentId)) ids.add(s.id());
        return ids;
    }

    public Set<Integer> idsByProject(int projectId) throws SQLException {
        Set<Integer> ids = new HashSet<>();
        for (Skill s : findByProject(projectId)) ids.add(s.id());
        return ids;
    }

    /** Tambah skill ke talenta. Jika nama skill belum ada di daftar, skill baru dibuat (kategori 'Lainnya'). */
    public void addToTalent(int talentId, String namaSkill, String level) throws SQLException {
        namaSkill = namaSkill == null ? "" : namaSkill.trim();
        if (namaSkill.isEmpty()) throw new IllegalArgumentException("Nama skill wajib diisi");

        try (Connection c = DatabaseConnection.getConnection()) {
            int skillId = -1;
            try (PreparedStatement ps = c.prepareStatement("SELECT skill_id FROM skills WHERE nama_skill=?")) {
                ps.setString(1, namaSkill);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) skillId = rs.getInt(1); }
            }
            if (skillId < 0) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO skills(nama_skill) VALUES(?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, namaSkill);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { k.next(); skillId = k.getInt(1); }
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO talent_skills(talent_id,skill_id,level) VALUES(?,?,?) "
                  + "ON DUPLICATE KEY UPDATE level=VALUES(level)")) {
                ps.setInt(1, talentId);
                ps.setInt(2, skillId);
                ps.setString(3, level);
                ps.executeUpdate();
            }
        }
    }

    public void removeFromTalent(int talentId, int skillId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM talent_skills WHERE talent_id=? AND skill_id=?")) {
            ps.setInt(1, talentId);
            ps.setInt(2, skillId);
            ps.executeUpdate();
        }
    }

    private List<Skill> query(String sql, Integer param) throws SQLException {
        List<Skill> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new Skill(rs.getInt("skill_id"), rs.getString("nama_skill"),
                        rs.getString("kategori"), rs.getString("level")));
            }
        }
        return list;
    }
}
