package goskill.model;

public class Talent extends User {
    private final int talentId;
    private final String universitas;
    private final String jurusan;

    public Talent(int userId, String nama, String email, double rating,
                  int talentId, String universitas, String jurusan) {
        super(userId, nama, email, rating);
        this.talentId = talentId;
        this.universitas = universitas;
        this.jurusan = jurusan;
    }

    public int getTalentId() { return talentId; }
    public String getUniversitas() { return universitas; }
    public String getJurusan() { return jurusan; }

    @Override public String getRole() { return "TALENT"; }
    @Override public String getDashboardTitle() {
        return "GoSkill - Talenta: " + getNama() + " (" + universitas + ")";
    }
}
