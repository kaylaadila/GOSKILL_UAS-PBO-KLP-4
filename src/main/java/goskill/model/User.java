package goskill.model;

/** Kelas abstrak: Talent dan Client mewarisi User (inheritance + abstraction). */
public abstract class User {
    private final int userId;
    private final String nama;
    private final String email;
    private final double rating;

    protected User(int userId, String nama, String email, double rating) {
        this.userId = userId;
        this.nama = nama;
        this.email = email;
        this.rating = rating;
    }

    public int getUserId() { return userId; }
    public String getNama() { return nama; }
    public String getEmail() { return email; }
    public double getRating() { return rating; }

    /** Polymorphism: tiap peran punya judul dashboard berbeda. */
    public abstract String getRole();
    public abstract String getDashboardTitle();
}
