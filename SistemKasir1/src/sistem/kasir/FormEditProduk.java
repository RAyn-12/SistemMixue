package sistem.kasir;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;

public class FormEditProduk extends JDialog {
    private JTextField txtNama, txtHarga, txtStok, txtImagePath;
    private JButton btnChooseImage, btnSimpan, btnBatal;
    private TampilanMenu parent;
    private int productId;
    private static final Color MIXUE_RED = new Color(213, 0, 0);
    private static final Color MIXUE_WHITE = Color.WHITE;
    private static final Color MIXUE_YELLOW = new Color(255, 193, 7);
    private static final Color MIXUE_BLACK = Color.BLACK;
    private static final Color MIXUE_GRAY = new Color(245, 245, 245);

    public FormEditProduk(TampilanMenu parent, int productId) {
        super(parent, "Edit Produk", true);
        this.parent = parent;
        this.productId = productId;
        initComponents();
        loadProduct();
    }

    private void initComponents() {
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new GridLayout(5, 2, 10, 10));
        content.setBackground(MIXUE_WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(new JLabel("Nama Produk:"));
        txtNama = new JTextField(20);
        txtNama.setFont(new Font("Arial", Font.PLAIN, 14));
        txtNama.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        content.add(txtNama);

        content.add(new JLabel("Harga:"));
        txtHarga = new JTextField(20);
        txtHarga.setFont(new Font("Arial", Font.PLAIN, 14));
        txtHarga.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        content.add(txtHarga);

        content.add(new JLabel("Stok:"));
        txtStok = new JTextField(20);
        txtStok.setFont(new Font("Arial", Font.PLAIN, 14));
        txtStok.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        content.add(txtStok);

        content.add(new JLabel("Gambar:"));
        JPanel imagePanel = new JPanel(new BorderLayout(5, 0));
        imagePanel.setBackground(MIXUE_WHITE);
        txtImagePath = new JTextField(15);
        txtImagePath.setFont(new Font("Arial", Font.PLAIN, 14));
        txtImagePath.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        txtImagePath.setEditable(false);
        btnChooseImage = createStyledButton("Pilih", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
        imagePanel.add(txtImagePath, BorderLayout.CENTER);
        imagePanel.add(btnChooseImage, BorderLayout.EAST);
        content.add(imagePanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(MIXUE_WHITE);
        btnSimpan = createStyledButton("Simpan", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
        btnBatal = createStyledButton("Batal", MIXUE_GRAY, MIXUE_BLACK, MIXUE_YELLOW);
        buttonPanel.add(btnSimpan);
        buttonPanel.add(btnBatal);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(content, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        btnChooseImage.addActionListener(e -> chooseImage());
        btnSimpan.addActionListener(e -> saveProduct());
        btnBatal.addActionListener(e -> dispose());
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
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
        return button;
    }

    private void loadProduct() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT nama_produk, harga, stok, image_path FROM produk WHERE id = ?")) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                txtNama.setText(rs.getString("nama_produk"));
                txtHarga.setText(String.valueOf(rs.getDouble("harga")));
                txtStok.setText(String.valueOf(rs.getInt("stok")));
                txtImagePath.setText(rs.getString("image_path") != null ? rs.getString("image_path") : "");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("SQL Error: " + ex.getMessage());
        }
    }

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            txtImagePath.setText(selectedFile.getName());
        }
    }

    private void saveProduct() {
        String nama = txtNama.getText().trim();
        String hargaStr = txtHarga.getText().trim();
        String stokStr = txtStok.getText().trim();
        String imagePath = txtImagePath.getText().trim();

        if (nama.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi semua field!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double harga = Double.parseDouble(hargaStr);
            int stok = Integer.parseInt(stokStr);
            if (harga <= 0 || stok < 0) {
                JOptionPane.showMessageDialog(this, "Harga dan stok harus valid!", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String targetImagePath = null;
            if (!imagePath.isEmpty() && !imagePath.equals(txtImagePath.getText())) {
                File sourceFile = new File(new JFileChooser().getFileSystemView().getDefaultDirectory(), imagePath);
                targetImagePath = "product" + System.currentTimeMillis() + "." + getFileExtension(imagePath);
                File targetFile = new File("src/resources/images/" + targetImagePath);
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                targetImagePath = txtImagePath.getText();
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE produk SET nama_produk = ?, harga = ?, stok = ?, image_path = ? WHERE id = ?")) {
                stmt.setString(1, nama);
                stmt.setDouble(2, harga);
                stmt.setInt(3, stok);
                stmt.setString(4, targetImagePath);
                stmt.setInt(5, productId);
                stmt.executeUpdate();
                parent.refreshTable();
                JOptionPane.showMessageDialog(this, "Produk diperbarui!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Harga dan stok harus berupa angka!", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error: " + ex.getMessage());
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }
}