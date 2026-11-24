package goskill.model;

/** level hanya terisi untuk skill milik talenta (PEMULA/MENENGAH/MAHIR), selain itu null. */
public record Skill(int id, String nama, String kategori, String level) {
    @Override public String toString() { return level == null ? nama : nama + " (" + level + ")"; }
}
