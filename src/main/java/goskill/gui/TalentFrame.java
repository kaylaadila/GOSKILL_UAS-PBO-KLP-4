package goskill.gui;

import goskill.dao.*;
import goskill.model.*;
import goskill.service.MatchingService;
import goskill.service.MatchingService.ProjectMatch;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TalentFrame extends JFrame {
    private final Talent talent;
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ApplicationDAO appDAO = new ApplicationDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final SkillDAO skillDAO = new SkillDAO();
    private final PortfolioDAO portfolioDAO = new PortfolioDAO();
    private final MatchingService matching = new MatchingService();

    // tab 1: cari proyek (diurutkan berdasarkan kecocokan skill)
    private final JTextField txtCari = new JTextField(20);
    private final DefaultTableModel mProyek =
        GuiUtil.model("ID", "Judul", "Klien", "Anggaran", "Skill Dibutuhkan", "Kecocokan");
    private final JTable tProyek = new JTable(mProyek);
    private List<ProjectMatch> proyek = List.of();

    // tab 2: penawaran saya
    private final DefaultTableModel mTawar =
        GuiUtil.model("Proyek", "Harga", "Hari", "Status Penawaran", "Status Proyek");
    private final JTable tTawar = new JTable(mTawar);
    private List<Penawaran> tawaran = List.of();

    // tab 3: skill saya
    private final DefaultTableModel mSkill = GuiUtil.model("Skill", "Kategori", "Level");
    private final JTable tSkill = new JTable(mSkill);
    private List<Skill> skills = List.of();

    // tab 4: portofolio saya
    private final DefaultTableModel mPort = GuiUtil.model("Judul", "Kategori", "Deskripsi", "Tautan");
    private final JTable tPort = new JTable(mPort);
    private List<Portfolio> portfolios = List.of();

    public TalentFrame(Talent talent) {
        super(talent.getDashboardTitle());
        this.talent = talent;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Cari Proyek", buildCari());
        tabs.addTab("Penawaran Saya", buildTawaran());
        tabs.addTab("Skill Saya", buildSkill());
        tabs.addTab("Portofolio Saya", buildPortfolio());
        add(tabs, BorderLayout.CENTER);
        add(new JLabel(String.format("  Rating Anda: %.2f", talent.getRating())), BorderLayout.SOUTH);

        setSize(900, 480);
        setLocationRelativeTo(null);
        cari();
        muatTawaran();
        muatSkill();
        muatPortfolio();
    }

    // ================= Tab 1: Cari Proyek =================
    private JPanel buildCari() {
        JButton bCari = new JButton("Cari"), bAjukan = new JButton("Ajukan Penawaran"),
                bLogout = new JButton("Logout");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Kata kunci:")); top.add(txtCari); top.add(bCari); top.add(bAjukan); top.add(bLogout);

        JPanel p = new JPanel(new BorderLayout());
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(tProyek), BorderLayout.CENTER);

        bCari.addActionListener(e -> cari());
        txtCari.addActionListener(e -> cari());
        bAjukan.addActionListener(e -> ajukan());
        bLogout.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
        return p;
    }

    private void cari() {
        GuiUtil.run(this, () -> {
            proyek = matching.proyekUntukTalenta(talent.getTalentId(),
                projectDAO.findOpen(txtCari.getText().trim()));
            mProyek.setRowCount(0);
            for (ProjectMatch pm : proyek) {
                Project p = pm.project();
                mProyek.addRow(new Object[]{p.id(), p.judul(), p.namaKlien(),
                    GuiUtil.rupiah(p.anggaran()), pm.skillDibutuhkan(), pm.skor() + "%"});
            }
        });
    }

    private void ajukan() {
        int r = tProyek.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Pilih proyek dulu"); return; }
        Project pr = proyek.get(r).project();

        JTextField harga = new JTextField(String.valueOf((long) pr.anggaran())),
                   hari = new JTextField("7"), pesan = new JTextField();
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.add(new JLabel("Harga (Rp)")); p.add(harga);
        p.add(new JLabel("Estimasi (hari)")); p.add(hari);
        p.add(new JLabel("Pesan")); p.add(pesan);

        if (JOptionPane.showConfirmDialog(this, p, "Penawaran: " + pr.judul(),
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            GuiUtil.run(this, () -> {
                double h; int d;
                try {
                    h = Double.parseDouble(harga.getText().trim());
                    d = Integer.parseInt(hari.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Harga dan hari harus berupa angka");
                }
                appDAO.apply(pr.id(), talent.getTalentId(), h, d, pesan.getText().trim());
                JOptionPane.showMessageDialog(this, "Penawaran terkirim (PENDING)");
                muatTawaran();
            });
        }
    }

    // ================= Tab 2: Penawaran Saya =================
    private JPanel buildTawaran() {
        JButton bUlasan = new JButton("Beri Ulasan"), bRefresh = new JButton("Refresh");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(bUlasan); top.add(bRefresh);

        JPanel p = new JPanel(new BorderLayout());
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(tTawar), BorderLayout.CENTER);

        bRefresh.addActionListener(e -> { muatTawaran(); cari(); });
        bUlasan.addActionListener(e -> {
            int r = tTawar.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Pilih penawaran dulu"); return; }
            Penawaran a = tawaran.get(r);
            if (!a.status().equals("ACCEPTED")) {
                JOptionPane.showMessageDialog(this, "Hanya untuk proyek yang penawarannya diterima");
                return;
            }
            GuiUtil.reviewDialog(this, reviewDAO, a.projectId(), talent.getUserId());
        });
        return p;
    }

    private void muatTawaran() {
        GuiUtil.run(this, () -> {
            tawaran = appDAO.findByTalent(talent.getTalentId());
            mTawar.setRowCount(0);
            for (Penawaran a : tawaran)
                mTawar.addRow(new Object[]{a.judulProyek(), GuiUtil.rupiah(a.harga()), a.hari(),
                    a.status(), a.statusProyek()});
        });
    }

    // ================= Tab 3: Skill Saya =================
    private JPanel buildSkill() {
        JButton bTambah = new JButton("+ Tambah Skill"), bHapus = new JButton("Hapus Skill");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(bTambah); top.add(bHapus);

        JPanel p = new JPanel(new BorderLayout());
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(tSkill), BorderLayout.CENTER);

        bTambah.addActionListener(e -> tambahSkill());
        bHapus.addActionListener(e -> {
            int r = tSkill.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Pilih skill dulu"); return; }
            GuiUtil.run(this, () -> {
                skillDAO.removeFromTalent(talent.getTalentId(), skills.get(r).id());
                muatSkill();
                cari();
            });
        });
        return p;
    }

    private void tambahSkill() {
        GuiUtil.run(this, () -> {
            // combo bisa diketik: pilih dari daftar atau tulis skill baru
            JComboBox<String> nama = new JComboBox<>();
            for (Skill s : skillDAO.findAll()) nama.addItem(s.nama());
            nama.setEditable(true);
            JComboBox<String> level = new JComboBox<>(new String[]{"PEMULA", "MENENGAH", "MAHIR"});
            level.setSelectedItem("MENENGAH");

            JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
            p.add(new JLabel("Skill (pilih / ketik baru)")); p.add(nama);
            p.add(new JLabel("Level")); p.add(level);

            if (JOptionPane.showConfirmDialog(this, p, "Tambah Skill", JOptionPane.OK_CANCEL_OPTION)
                    == JOptionPane.OK_OPTION) {
                skillDAO.addToTalent(talent.getTalentId(), String.valueOf(nama.getSelectedItem()),
                    (String) level.getSelectedItem());
                muatSkill();
                cari();
            }
        });
    }

    private void muatSkill() {
        GuiUtil.run(this, () -> {
            skills = skillDAO.findByTalent(talent.getTalentId());
            mSkill.setRowCount(0);
            for (Skill s : skills) mSkill.addRow(new Object[]{s.nama(), s.kategori(), s.level()});
        });
    }

    // ================= Tab 4: Portofolio Saya =================
    private JPanel buildPortfolio() {
        JButton bTambah = new JButton("+ Tambah Portofolio"), bHapus = new JButton("Hapus");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(bTambah); top.add(bHapus);

        JPanel p = new JPanel(new BorderLayout());
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(tPort), BorderLayout.CENTER);

        bTambah.addActionListener(e -> tambahPortfolio());
        bHapus.addActionListener(e -> {
            int r = tPort.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Pilih portofolio dulu"); return; }
            GuiUtil.run(this, () -> {
                portfolioDAO.delete(portfolios.get(r).id(), talent.getTalentId());
                muatPortfolio();
            });
        });
        return p;
    }

    private void tambahPortfolio() {
        JTextField judul = new JTextField(), kategori = new JTextField(),
                   deskripsi = new JTextField(), tautan = new JTextField();
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.add(new JLabel("Judul proyek")); p.add(judul);
        p.add(new JLabel("Kategori")); p.add(kategori);
        p.add(new JLabel("Deskripsi")); p.add(deskripsi);
        p.add(new JLabel("Link hasil kerja")); p.add(tautan);

        if (JOptionPane.showConfirmDialog(this, p, "Tambah Portofolio", JOptionPane.OK_CANCEL_OPTION)
                == JOptionPane.OK_OPTION) {
            GuiUtil.run(this, () -> {
                portfolioDAO.add(talent.getTalentId(), judul.getText(), deskripsi.getText().trim(),
                    kategori.getText().trim(), tautan.getText().trim());
                muatPortfolio();
            });
        }
    }

    private void muatPortfolio() {
        GuiUtil.run(this, () -> {
            portfolios = portfolioDAO.findByTalent(talent.getTalentId());
            mPort.setRowCount(0);
            for (Portfolio p : portfolios)
                mPort.addRow(new Object[]{p.judul(), p.kategori(), p.deskripsi(), p.tautan()});
        });
    }
}
