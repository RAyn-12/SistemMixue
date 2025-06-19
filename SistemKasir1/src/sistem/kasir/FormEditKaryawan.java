package sistem.kasir;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class FormEditKaryawan extends JFrame {
    private JTextField txtNama, txtUsername, txtPassword;
    private JComboBox<String> comboRole;
    private JButton btnSimpan, btnHapus, btnBatal;
    private String username;
    private int role;
    private int idKaryawan;

    public FormEditKaryawan(String username, int role, int idKaryawan) {
        this.username = username;
        this.role = role;
        this.idKaryawan = idKaryawan;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setTitle("Edit Karyawan");
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
        btnHapus = new JButton("Hapus");
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
        panel.add(btnHapus);
        panel.add(btnBatal);

        add(panel);

        btnSimpan.addActionListener(e -> simpanKaryawan());
        btnHapus.addActionListener(e -> hapusKaryawan());
        btnBatal.addActionListener(e -> dispose());
    }

    private void loadData() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            stmt.setInt(1, idKaryawan);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                txtNama.setText(rs.getString("nama"));
                txtUsername.setText(rs.getString("username"));
                txtPassword.setText(rs.getString("password"));
                String roleDb = rs.getString("role");
                comboRole.setSelectedItem("admin".equals(roleDb) ? "Admin" : "Kasir");
            } else {
                JOptionPane.showMessageDialog(this, "Karyawan tidak ditemukan!");
                dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void simpanKaryawan() {
        String nama = txtNama.getText();
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String roleDisplay = (String) comboRole.getSelectedItem();
        String roleDb = "Admin".equals(roleDisplay) ? "admin" : "kasir";

        if (nama.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE users SET nama = ?, username = ?, password = ?, role = ? WHERE id = ?")) {
            stmt.setString(1, nama);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, roleDb);
            stmt.setInt(5, idKaryawan);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Karyawan berhasil diperbarui!");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void hapusKaryawan() {
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus karyawan ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                stmt.setInt(1, idKaryawan);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Karyawan berhasil dihapus!");
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
}