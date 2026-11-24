package goskill.model;

public class Client extends User {
    private final int clientId;
    private final String namaUsaha;
    private final String bidangUsaha;

    public Client(int userId, String nama, String email, double rating,
                  int clientId, String namaUsaha, String bidangUsaha) {
        super(userId, nama, email, rating);
        this.clientId = clientId;
        this.namaUsaha = namaUsaha;
        this.bidangUsaha = bidangUsaha;
    }

    public int getClientId() { return clientId; }
    public String getNamaUsaha() { return namaUsaha; }
    public String getBidangUsaha() { return bidangUsaha; }

    @Override public String getRole() { return "CLIENT"; }
    @Override public String getDashboardTitle() {
        return "GoSkill - Klien: " + getNama() + " (" + namaUsaha + ")";
    }
}
