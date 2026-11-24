CREATE DATABASE IF NOT EXISTS goskill;
USE goskill;

CREATE TABLE IF NOT EXISTS users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  nama_lengkap VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('TALENT','CLIENT','ADMIN') NOT NULL,
  status_akun ENUM('AKTIF','NONAKTIF','DIBLOKIR') NOT NULL DEFAULT 'AKTIF',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS talents (
  talent_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL UNIQUE,
  universitas VARCHAR(100),
  jurusan VARCHAR(100),
  rating_rata DECIMAL(3,2) DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS clients (
  client_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL UNIQUE,
  nama_usaha VARCHAR(100),
  bidang_usaha VARCHAR(100),
  rating_rata DECIMAL(3,2) DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS projects (
  project_id INT AUTO_INCREMENT PRIMARY KEY,
  client_id INT NOT NULL,
  talent_id INT NULL,
  judul VARCHAR(150) NOT NULL,
  deskripsi TEXT,
  anggaran DECIMAL(12,2) NOT NULL,
  deadline DATE,
  status ENUM('OPEN','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'OPEN',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (client_id) REFERENCES clients(client_id),
  FOREIGN KEY (talent_id) REFERENCES talents(talent_id)
);

CREATE TABLE IF NOT EXISTS applications (
  application_id INT AUTO_INCREMENT PRIMARY KEY,
  project_id INT NOT NULL,
  talent_id INT NOT NULL,
  harga_penawaran DECIMAL(12,2) NOT NULL,
  estimasi_hari INT NOT NULL,
  pesan TEXT,
  status ENUM('PENDING','ACCEPTED','REJECTED') NOT NULL DEFAULT 'PENDING',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (project_id, talent_id),
  FOREIGN KEY (project_id) REFERENCES projects(project_id),
  FOREIGN KEY (talent_id) REFERENCES talents(talent_id)
);

CREATE TABLE IF NOT EXISTS reviews (
  review_id INT AUTO_INCREMENT PRIMARY KEY,
  project_id INT NOT NULL,
  reviewer_id INT NOT NULL,
  reviewee_id INT NOT NULL,
  rating TINYINT NOT NULL,
  komentar TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (project_id, reviewer_id),
  FOREIGN KEY (project_id) REFERENCES projects(project_id),
  FOREIGN KEY (reviewer_id) REFERENCES users(user_id),
  FOREIGN KEY (reviewee_id) REFERENCES users(user_id)
);

-- ===== Skills, Portfolio, Matching =====
CREATE TABLE IF NOT EXISTS skills (
  skill_id INT AUTO_INCREMENT PRIMARY KEY,
  nama_skill VARCHAR(50) NOT NULL UNIQUE,
  kategori VARCHAR(50) DEFAULT 'Lainnya'
);

CREATE TABLE IF NOT EXISTS talent_skills (
  talent_id INT NOT NULL,
  skill_id INT NOT NULL,
  level ENUM('PEMULA','MENENGAH','MAHIR') NOT NULL DEFAULT 'MENENGAH',
  PRIMARY KEY (talent_id, skill_id),
  FOREIGN KEY (talent_id) REFERENCES talents(talent_id),
  FOREIGN KEY (skill_id) REFERENCES skills(skill_id)
);

CREATE TABLE IF NOT EXISTS project_skills (
  project_id INT NOT NULL,
  skill_id INT NOT NULL,
  PRIMARY KEY (project_id, skill_id),
  FOREIGN KEY (project_id) REFERENCES projects(project_id),
  FOREIGN KEY (skill_id) REFERENCES skills(skill_id)
);

CREATE TABLE IF NOT EXISTS portfolios (
  portfolio_id INT AUTO_INCREMENT PRIMARY KEY,
  talent_id INT NOT NULL,
  judul VARCHAR(100) NOT NULL,
  deskripsi TEXT,
  kategori VARCHAR(50),
  tautan VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (talent_id) REFERENCES talents(talent_id)
);

INSERT IGNORE INTO skills(nama_skill, kategori) VALUES
 ('HTML','Web Development'), ('CSS','Web Development'), ('JavaScript','Web Development'),
 ('PHP','Web Development'), ('Java','Pemrograman'), ('Python','Pemrograman'),
 ('Flutter','Mobile Development'), ('Android','Mobile Development'),
 ('Graphic Design','Desain'), ('UI/UX Design','Desain'), ('Figma','Desain'),
 ('Adobe Photoshop','Desain'), ('Illustration','Desain'), ('Branding','Desain'),
 ('Video Editing','Multimedia'), ('Data Analysis','Data'), ('Excel','Data'),
 ('Penulisan','Konten'), ('Penerjemahan','Konten'), ('Digital Marketing','Pemasaran');
