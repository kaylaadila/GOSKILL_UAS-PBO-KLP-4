package goskill.model;

import java.time.LocalDate;

/** Data proyek (record = kelas data immutable). status: OPEN / IN_PROGRESS / COMPLETED / CANCELLED */
public record Project(int id, String judul, String deskripsi, double anggaran,
                      LocalDate deadline, String status, String namaKlien, String namaTalent) { }
