package sistem.kasir;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;

public class TampilanPengaturan extends JFrame {
    private JTextField txtStoreName, txtStoreAddress, txtStoreContact;
    private JPasswordField txtOldPassword, txtNewPassword, txtConfirmPassword;
    private JButton btnSaveStore, btnChangePassword, btnBackup, btnRestore, btnKembali;
    private String username;
    private int role;

    private static final Color MIXUE_RED = new Color(213, 0, 0);
    private static final Color MIXUE_WHITE = Color.WHITE;
    private static final Color MIXUE_YELLOW = new Color(255, 193, 7);
    private static final Color MIXUE_BLACK = Color.BLACK;
    private static final Color MIXUE_GRAY = new Color(245, 245, 245);

    public TampilanPengaturan(String username, int role) {
        this.username = username;
        this.role = role;
        initComponents();
    }

    private void initComponents() {
        setTitle("Mixue Cashier System - Pengaturan");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel sidebar = createSidebar();
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(MIXUE_GRAY);
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 20));

        JPanel settingsPanel = new JPanel(new GridLayout(2, 1, 10, 20));
        settingsPanel.setBackground(MIXUE_GRAY);

        // Informasi Toko
        JPanel storePanel = new JPanel(new GridLayout(4, 2, 10, 10));
        storePanel.setBackground(MIXUE_GRAY);
        storePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(MIXUE_GRAY), "Informasi Toko",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), MIXUE_RED));

        storePanel.add(new JLabel("Nama Toko:"));
        txtStoreName = new JTextField(20);
        txtStoreName.setFont(new Font("Arial", Font.PLAIN, 14));
        txtStoreName.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        storePanel.add(txtStoreName);

        storePanel.add(new JLabel("Alamat:"));
        txtStoreAddress = new JTextField(20);
        txtStoreAddress.setFont(new Font("Arial", Font.PLAIN, 14));
        txtStoreAddress.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        storePanel.add(txtStoreAddress);

        storePanel.add(new JLabel("Kontak:"));
        txtStoreContact = new JTextField(20);
        txtStoreContact.setFont(new Font("Arial", Font.PLAIN, 14));
        txtStoreContact.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        storePanel.add(txtStoreContact);

        btnSaveStore = createStyledButton("Simpan Informasi Toko", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
        storePanel.add(new JLabel(""));
        storePanel.add(btnSaveStore);

        // Ubah Password
        JPanel passwordPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        passwordPanel.setBackground(MIXUE_GRAY);
        passwordPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(MIXUE_GRAY), "Ubah Password",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14), MIXUE_RED));

        passwordPanel.add(new JLabel("Password Lama:"));
        txtOldPassword = new JPasswordField(20);
        txtOldPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtOldPassword.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        passwordPanel.add(txtOldPassword);

        passwordPanel.add(new JLabel("Password Baru:"));
        txtNewPassword = new JPasswordField(20);
        txtNewPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtNewPassword.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        passwordPanel.add(txtNewPassword);

        passwordPanel.add(new JLabel("Konfirmasi Password:"));
        txtConfirmPassword = new JPasswordField(20);
        txtConfirmPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtConfirmPassword.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        passwordPanel.add(txtConfirmPassword);

        btnChangePassword = createStyledButton("Ubah Password", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
        passwordPanel.add(new JLabel(""));
        passwordPanel.add(btnChangePassword);

        settingsPanel.add(storePanel);
        settingsPanel.add(passwordPanel);

        // Panel Tombol Bawah
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(MIXUE_GRAY);
        if (role == 1) {
            btnBackup = createStyledButton("Backup Database", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
            btnRestore = createStyledButton("Restore Database", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
            buttonPanel.add(btnBackup);
            buttonPanel.add(btnRestore);
        }
        btnKembali = createStyledButton("Kembali", MIXUE_GRAY, MIXUE_BLACK, MIXUE_YELLOW);
        buttonPanel.add(btnKembali);

        content.add(settingsPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);

        loadStoreInfo();

        btnSaveStore.addActionListener(e -> saveStoreInfo());
        btnChangePassword.addActionListener(e -> changePassword());
        if (role == 1) {
            btnBackup.addActionListener(e -> backupDatabase());
            btnRestore.addActionListener(e -> restoreDatabase());
        }
        btnKembali.addActionListener(e -> {
            new TampilanDashboard(username, role).setVisible(true);
            dispose();
        });
    }

    private JButton createStyledButton(String text, Color bg, Color fg, Color hover) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
        return button;
    }

    private void loadStoreInfo() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT store_name, store_address, store_contact FROM settings WHERE id = 1")) {
            if (rs.next()) {
                txtStoreName.setText(rs.getString("store_name"));
                txtStoreAddress.setText(rs.getString("store_address"));
                txtStoreContact.setText(rs.getString("store_contact"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveStoreInfo() {
        String storeName = txtStoreName.getText().trim();
        String storeAddress = txtStoreAddress.getText().trim();
        String storeContact = txtStoreContact.getText().trim();

        if (storeName.isEmpty() || storeAddress.isEmpty() || storeContact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi semua field!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE settings SET store_name = ?, store_address = ?, store_contact = ?, updated_at = NOW() WHERE id = 1")) {
            stmt.setString(1, storeName);
            stmt.setString(2, storeAddress);
            stmt.setString(3, storeContact);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO settings (id, store_name, store_address, store_contact, updated_at) VALUES (1, ?, ?, ?, NOW())");
                insertStmt.setString(1, storeName);
                insertStmt.setString(2, storeAddress);
                insertStmt.setString(3, storeContact);
                insertStmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Informasi toko disimpan!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changePassword() {
        String oldPassword = new String(txtOldPassword.getPassword());
        String newPassword = new String(txtNewPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi semua kolom password!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Password baru tidak sama!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getString("password").equals(oldPassword)) {
                try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET password = ? WHERE username = ?")) {
                    updateStmt.setString(1, newPassword);
                    updateStmt.setString(2, username);
                    updateStmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Password telah diubah!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    txtOldPassword.setText("");
                    txtNewPassword.setText("");
                    txtConfirmPassword.setText("");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Password lama salah!", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backupDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("sistem_kasir_backup_" + System.currentTimeMillis() + ".sql"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File backupFile = fileChooser.getSelectedFile();
            try {
                String mysqlDumpPath = "C:\\xampp\\mysql\\bin\\mysqldump.exe";
                ProcessBuilder pb = new ProcessBuilder(
                        mysqlDumpPath, "-u", "root", "--password=", "sistem_kasir", "--result-file", backupFile.getAbsolutePath());
                pb.redirectErrorStream(true);
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    JOptionPane.showMessageDialog(this, "Backup berhasil disimpan di " + backupFile.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal membuat backup! Periksa path mysqldump atau kredensial.", "Backup Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | InterruptedException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Backup Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void restoreDatabase() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Pilih File SQL untuk Restore");
    int userSelection = fileChooser.showOpenDialog(this);
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File sqlFile = fileChooser.getSelectedFile();

        try {
            String mysqlPath = "C:\\xampp\\mysql\\bin\\mysql.exe";
            ProcessBuilder pb = new ProcessBuilder(mysqlPath, "-u", "root", "--password=", "sistem_kasir");
            Process process = pb.start();

            OutputStream os = process.getOutputStream();
            FileInputStream fis = new FileInputStream(sqlFile);
            byte[] buffer = new byte[1000];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
            os.close();
            fis.close();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                JOptionPane.showMessageDialog(this, "Restore berhasil dari file: " + sqlFile.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Restore gagal! Pastikan file SQL valid dan database 'sistem_kasir' sudah ada.", "Restore Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Restore Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new GridLayout(7, 1, 10, 10));
        sidebar.setBackground(MIXUE_RED);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidebar.setPreferredSize(new Dimension(200, 600));

        JButton btnKasir = createStyledButton("Kasir", MIXUE_WHITE, MIXUE_RED, MIXUE_YELLOW);
        JButton btnMenu = role == 1 ? createStyledButton("Menu", MIXUE_WHITE, MIXUE_RED, MIXUE_YELLOW) : null;
        JButton btnKaryawan = role == 1 ? createStyledButton("Karyawan", MIXUE_WHITE, MIXUE_RED, MIXUE_YELLOW) : null;
        JButton btnStok = role == 1 ? createStyledButton("Stok Gudang", MIXUE_WHITE, MIXUE_RED, MIXUE_YELLOW) : null;
        JButton btnLaporan = role == 1 ? createStyledButton("Laporan", MIXUE_WHITE, MIXUE_RED, MIXUE_YELLOW) : null;
        JButton btnPengaturan = role == 1 ? createStyledButton("Pengaturan", MIXUE_WHITE, MIXUE_RED, MIXUE_YELLOW) : null;
        JButton btnLogout = createStyledButton("Logout", MIXUE_WHITE, MIXUE_RED, MIXUE_YELLOW);

        sidebar.add(btnKasir);
        if (role == 1) {
            sidebar.add(btnMenu);
            sidebar.add(btnKaryawan);
            sidebar.add(btnStok);
            sidebar.add(btnLaporan);
            sidebar.add(btnPengaturan);
        }
        sidebar.add(btnLogout);

        btnKasir.addActionListener(e -> {
            new TampilanKasir(username, role, "").setVisible(true);
            dispose();
        });
        if (role == 1) {
            btnMenu.addActionListener(e -> {
                new TampilanMenu(username, role).setVisible(true);
                dispose();
            });
            btnKaryawan.addActionListener(e -> {
                new TampilanKaryawan(username, role).setVisible(true);
                dispose();
            });
            btnStok.addActionListener(e -> {
                new TampilanStokGudang(username, role, "").setVisible(true);
                dispose();
            });
            btnLaporan.addActionListener(e -> {
                new TampilanLaporan(username, role).setVisible(true);
                dispose();
            });
            btnPengaturan.addActionListener(e -> {
                new TampilanPengaturan(username, role).setVisible(true);
                dispose();
            });
        }
        btnLogout.addActionListener(e -> {
            new FormLogin2().setVisible(true);
            dispose();
        });

        return sidebar;
    }
}
