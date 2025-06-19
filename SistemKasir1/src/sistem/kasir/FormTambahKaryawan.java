package sistem.kasir;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class FormTambahKaryawan extends JFrame {
    private JTextField txtNama, txtUsername, txtPassword;
    private JComboBox<String> comboRole;
    private JButton btnSimpan, btnBatal;
    private String username;
    private int role;

    public FormTambahKaryawan(String username, int role) {
        this.username = username;
        this.role = role;
        initComponents();
    }

    private void initComponents() {
        setTitle("Tambah Karyawan");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblNama = new JLabel("Nama:");
        txtNama = new JTextField();
        JLabel lblUsername = new JLabel("Username:");
        txtUsername = new JTextField();
        JLabel lblPassword = new JLabel("Password:");
        txtPassword = new JTextField();
        JLabel lblRole = new JLabel("Role:");
        comboRole = new JComboBox<>(new String[]{"Admin", "Kasir"});
        btnSimpan = new JButton("Simpan");
        btnBatal = new JButton("Batal");

        panel.add(lblNama);
        panel.add(txtNama);
        panel.add(lblUsername);
        panel.add(txtUsername);
        panel.add(lblPassword);
        panel.add(txtPassword);
        panel.add(lblRole);
        panel.add(comboRole);
        panel.add(btnSimpan);
        panel.add(btnBatal);

        add(panel);

        btnSimpan.addActionListener(e -> simpanKaryawan());
        btnBatal.addActionListener(e -> dispose());
    }

    private void simpanKaryawan() {
        String nama = txtNama.getText();
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String roleDisplay = (String) comboRole.getSelectedItem();
        // Map display role to database ENUM
        String roleDb = "Admin".equals(roleDisplay) ? "admin" : "kasir";

        if (nama.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO users (nama, username, password, role) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, nama);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, roleDb);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Karyawan berhasil ditambahkan!");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}