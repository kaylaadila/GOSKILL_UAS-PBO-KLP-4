package goskill.service;

import goskill.dao.SkillDAO;
import goskill.dao.UserDAO;
import goskill.model.Project;
import goskill.model.Skill;
import goskill.model.Talent;

import java.sql.SQLException;
import java.util.*;

/**
 * Skill matching sederhana (bukan AI):
 * Skor = (jumlah skill proyek yang dimiliki talenta / total skill yang dibutuhkan proyek) x 100%
 */
public class MatchingService {
    public record TalentMatch(Talent talent, int skor) { }
    public record ProjectMatch(Project project, int skor, String skillDibutuhkan) { }

    private final SkillDAO skillDAO = new SkillDAO();
    private final UserDAO userDAO = new UserDAO();

    /** Logika murni (mudah di-unit-test dengan JUnit, mis. TC-09: 2 dari 4 skill = 50). */
    public static int hitungSkor(Set<Integer> skillTalent, Set<Integer> skillProyek) {
        if (skillProyek.isEmpty()) return 0;
        long cocok = skillProyek.stream().filter(skillTalent::contains).count();
        return (int) Math.round(cocok * 100.0 / skillProyek.size());
    }

    public int skor(int talentId, int projectId) throws SQLException {
        return hitungSkor(skillDAO.idsByTalent(talentId), skillDAO.idsByProject(projectId));
    }

    /** Daftar talenta diurutkan dari skor tertinggi (untuk klien). */
    public List<TalentMatch> rekomendasiTalenta(int projectId) throws SQLException {
        Set<Integer> butuh = skillDAO.idsByProject(projectId);
        List<TalentMatch> hasil = new ArrayList<>();
        for (Talent t : userDAO.findAllTalents())
            hasil.add(new TalentMatch(t, hitungSkor(skillDAO.idsByTalent(t.getTalentId()), butuh)));
        hasil.sort(Comparator.comparingInt(TalentMatch::skor).reversed()
                .thenComparing(Comparator.comparingDouble((TalentMatch m) -> m.talent().getRating()).reversed()));
        return hasil;
    }

    /** Daftar proyek diurutkan dari skor tertinggi (untuk talenta). */
    public List<ProjectMatch> proyekUntukTalenta(int talentId, List<Project> proyek) throws SQLException {
        Set<Integer> milik = skillDAO.idsByTalent(talentId);
        List<ProjectMatch> hasil = new ArrayList<>();
        for (Project p : proyek) {
            List<Skill> butuh = skillDAO.findByProject(p.id());
            Set<Integer> ids = new HashSet<>();
            StringJoiner nama = new StringJoiner(", ");
            for (Skill s : butuh) { ids.add(s.id()); nama.add(s.nama()); }
            hasil.add(new ProjectMatch(p, hitungSkor(milik, ids), nama.toString()));
        }
        hasil.sort(Comparator.comparingInt(ProjectMatch::skor).reversed());
        return hasil;
    }
}
