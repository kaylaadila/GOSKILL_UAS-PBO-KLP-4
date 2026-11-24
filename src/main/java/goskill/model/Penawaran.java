package goskill.model;

/** Data pengajuan penawaran. status: PENDING / ACCEPTED / REJECTED */
public record Penawaran(int id, int projectId, int talentId, String namaTalent, double harga, int hari,
                        String pesan, String status, String judulProyek, String statusProyek) { }
