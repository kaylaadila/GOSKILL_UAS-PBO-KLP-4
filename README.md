# GoSkill - Demo Implementasi (Java Swing + JDBC + MySQL)
 
## Kelompok 4
| No | Nama | NIM |
|---|---|---|
| 1 | Muhammad Hafidz Zuliesky | 2108107010045 |
| 2 | Kayla Adila Farija Atika | 250810701100067 |
| 3 | Muhammad Rayyan | 250810701100113 |
| 4 | Putri Rahmatul 'Ulya | 250810701100030 |
 
## Deskripsi
GoSkill adalah aplikasi desktop marketplace freelance yang mempertemukan **talenta** (mahasiswa atau freelancer
pemula) dengan **klien** (pelaku usaha atau perorangan yang membutuhkan jasa). Klien memasang proyek beserta
anggaran dan skill yang dibutuhkan, lalu talenta mengajukan penawaran. Sistem menghitung **skor kecocokan skill**
antara talenta dan proyek sehingga klien mudah memilih talenta yang paling sesuai. Setelah penawaran diterima,
proyek berjalan hingga selesai, lalu kedua pihak saling memberi ulasan dan rating.
 
Fitur utama:
- Registrasi dan login dengan dua peran (Talent dan Client), password disimpan dalam bentuk hash.
- Klien: membuat proyek, melihat rekomendasi talenta, menerima penawaran, menandai proyek selesai, memberi ulasan.
- Talenta: mengelola skill dan portofolio, mencari proyek dengan skor kecocokan, mengajukan penawaran, memberi ulasan.
- Skill Matching: skor = skill proyek yang dimiliki talenta / total skill proyek x 100%.
Aplikasi ini dibuat sebagai contoh implementasi konsep Pemrograman Berorientasi Objek (inheritance, polymorphism,
encapsulation) dengan pola berlapis dan basis data MySQL melalui JDBC.
 
## Arsitektur
GUI (Swing) -> Service -> DAO -> JDBC -> MySQL
 
## Kebutuhan
- JDK 17 atau lebih baru (cek: `javac -version`; download di adoptium.net)
- MySQL (XAMPP atau Laragon)
- Driver MySQL: https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar
- Terminal PowerShell (mudah lewat VS Code: Ctrl + `)
## Cara menjalankan (Windows, PowerShell)
 
1. **Ekstrak zip** dengan klik kanan -> Extract All. Jangan buka file langsung dari dalam zip.
2. **Taruh driver** `mysql-connector-j-8.4.0.jar` ke folder `lib/` (buat folder `lib` jika belum ada).
3. **Nyalakan MySQL** (XAMPP Control Panel -> Start di baris MySQL sampai hijau). Biarkan menyala selama program jalan.
4. **Import database**: buka `http://localhost/phpmyadmin` -> tab Import -> pilih `schema.sql` -> Go.
5. **Cek password MySQL** di `src/main/java/goskill/db/DatabaseConnection.java`.
   Default `root` dengan password kosong (cocok untuk XAMPP). Ubah `PASSWORD` jika MySQL kalian memakai password,
   lalu compile ulang.
6. **Cek source ada**. Di terminal, pada folder yang berisi `pom.xml`:
```
Get-ChildItem -Recurse src -Filter *.java | Measure-Object
```
   `Count` harus **21**. Jika 0, zip belum terekstrak lengkap (ulangi langkah 1).
7. **Compile**:
```
mkdir out -Force
javac -encoding UTF-8 -cp "lib/*" -d out (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName)
```
   Jika berhasil, tidak ada tulisan apa pun.
8. **Jalankan**:
```
java -cp "out;lib/*" goskill.MainApp
```
 
Lain kali cukup langkah 3 dan 8. Compile ulang (langkah 7) hanya jika kode diubah.
 
> `run.bat` tidak disarankan karena gagal di javac versi baru. Pakai perintah di atas.
 
## Mac / Linux
Langkah 1-6 sama, lalu jalankan `./run.sh`.
 
## Jika memakai Maven / IDE
Buka folder sebagai Maven project (`pom.xml` sudah berisi `mysql-connector-j`), lalu run `goskill.MainApp`
atau `mvn compile exec:java`.
 
## Error yang sering muncul
| Error | Penyebab dan solusi |
|---|---|
| `no source files` | Folder `src` kosong. Ekstrak ulang zip lengkap. |
| `Could not find or load main class` | Compile belum berhasil. Ulangi langkah 7. |
| `package com.mysql ... does not exist` / `No suitable driver` | `.jar` belum ada di `lib/` atau namanya salah. |
| `Communications link failure` | MySQL belum menyala atau port salah (default 3306). |
| `Access denied for user 'root'` | Password di `DatabaseConnection.java` salah. |
| `Unknown database 'goskill'` | `schema.sql` belum di-import. |
 
## Skenario demo
1. Daftar akun CLIENT (mis. Warung Kopi) dan akun TALENT (mis. mahasiswa). Email harus berbeda.
2. TALENT: tab **Skill Saya** dan **Portofolio Saya**, isi beberapa data.
3. Login CLIENT -> Buat Proyek (anggaran > 0, pilih minimal satu skill dengan Ctrl+klik).
4. Logout, login TALENT -> Cari Proyek (lihat kolom Kecocokan) -> Ajukan Penawaran.
5. Login CLIENT -> pilih proyek -> Lihat Penawaran -> terima (status IN_PROGRESS).
6. CLIENT -> Tandai Selesai (COMPLETED) -> Beri Ulasan.
7. TALENT -> tab Penawaran Saya -> Beri Ulasan. Rating rata-rata ter-update.
## Pemetaan ke proposal
- Inheritance/abstraction : User (abstract) -> Talent, Client
- Polymorphism            : getDashboardTitle(), pemilihan dashboard di LoginFrame
- Encapsulation           : atribut private + getter
- DAO + JDBC              : UserDAO, ProjectDAO, ApplicationDAO, ReviewDAO (PreparedStatement)
- Password                : SHA-256 + salt (TC-19)
- Service                 : MatchingService (logika pencocokan dipisah dari DAO dan GUI)
- Transaksi               : proses terima penawaran
- Aturan uji              : email duplikat ditolak (TC-02), anggaran <= 0 ditolak (TC-07),
                            penawaran ganda ditolak (TC-11), satu penawaran diterima dan lainnya REJECTED (TC-12),
                            ulasan hanya jika COMPLETED (TC-15), rating 1-5 (TC-16)
## Fitur Skills, Portofolio, dan Skill Matching
- Talenta: tab **Skill Saya** (tambah/hapus, bisa ketik skill baru, ada level) dan tab **Portofolio Saya**.
- Klien: saat **Buat Proyek** wajib memilih skill yang dibutuhkan.
- **MatchingService**: Skor = skill proyek yang dimiliki talenta / total skill proyek x 100%.
  - Talenta: daftar proyek diurutkan dari kecocokan tertinggi.
  - Klien: tombol **Rekomendasi Talenta** dan **Lihat Penawaran** (diurutkan skor), plus tombol
    **Lihat Skill & Portofolio** untuk menilai talenta sebelum menerima penawaran.
- TC-09 (2 dari 4 skill = 50%) bisa diuji lewat `MatchingService.hitungSkor(...)` (static).
> Jika database dari versi sebelumnya sudah ada, jalankan ulang `schema.sql` (aman, memakai IF NOT EXISTS).
> Proyek lama tanpa skill akan punya kecocokan 0%.
 
## Belum ada di demo (pengembangan lanjutan)
Role Admin, chat, notifikasi, Strategy Pattern (biaya), Observer Pattern (notifikasi status).
 
