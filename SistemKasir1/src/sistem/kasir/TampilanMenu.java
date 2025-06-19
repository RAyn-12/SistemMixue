package sistem.kasir;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TampilanMenu extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnTambah, btnEdit, btnHapus, btnKembali;
    private String username;
    private int role;
    private static final Color MIXUE_RED = new Color(213, 0, 0);
    private static final Color MIXUE_WHITE = Color.WHITE;
    private static final Color MIXUE_YELLOW = new Color(255, 193, 7);
    private static final Color MIXUE_BLACK = Color.BLACK;
    private static final Color MIXUE_GRAY = new Color(245, 245, 245);

    public TampilanMenu(String username, int role) {
        this.username = username;
        this.role = role;
        initComponents();
    }

    private void initComponents() {
        setTitle("Mixue Cashier System - Menu");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel sidebar = createSidebar();
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(MIXUE_WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(new Object[]{"ID", "Nama", "Harga", "Stok", "Image Path"}, 0);
        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? MIXUE_WHITE : MIXUE_GRAY);
                return c;
            }
        });
        JTableHeader header = table.getTableHeader();
        header.setBackground(MIXUE_RED);
        header.setForeground(MIXUE_WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(MIXUE_WHITE);
        btnTambah = createStyledButton("Tambah", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
        btnEdit = createStyledButton("Edit", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
        btnHapus = createStyledButton("Hapus", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
        btnKembali = createStyledButton("Kembali", MIXUE_GRAY, MIXUE_BLACK, MIXUE_YELLOW);
        buttonPanel.add(btnTambah);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnKembali);

        content.add(tableScroll, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);

        loadProducts();

        btnTambah.addActionListener(e -> new FormTambahProduk(this).setVisible(true));
        btnEdit.addActionListener(e -> editProduct());
        btnHapus.addActionListener(e -> deleteProduct());
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

    private void loadProducts() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nama_produk, harga, stok, image_path FROM produk")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama_produk"),
                    rs.getDouble("harga"),
                    rs.getInt("stok"),
                    rs.getString("image_path")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("SQL Error: " + ex.getMessage());
        }
    }

    private void editProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih produk terlebih dahulu!", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (Integer) model.getValueAt(row, 0);
        new FormEditProduk(this, id).setVisible(true);
    }

    private void deleteProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih produk terlebih dahulu!", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (Integer) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus produk ini?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM produk WHERE id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
                loadProducts();
                JOptionPane.showMessageDialog(this, "Produk dihapus!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("SQL Error: " + ex.getMessage());
            }
        }
    }

    public void refreshTable() {
        loadProducts();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(7, 1, 10, 10));
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
            btnMenu.addActionListener(e -> System.out.println("Menu clicked: " + e.getActionCommand()));
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