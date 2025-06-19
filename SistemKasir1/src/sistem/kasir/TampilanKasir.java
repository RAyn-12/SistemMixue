package sistem.kasir;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class TampilanKasir extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblTotal;
    private JButton btnBayar, btnKembali;
    private String username;
    private int role;
    private ArrayList<Object[]> transaksi;
    private static final Color MIXUE_RED = new Color(213, 0, 0); // #D50000
    private static final Color MIXUE_WHITE = Color.WHITE; // #FFFFFF
    private static final Color MIXUE_YELLOW = new Color(255, 193, 7); // #FFC107
    private static final Color MIXUE_BLACK = Color.BLACK; // #000000
    private static final Color MIXUE_GRAY = new Color(245, 245, 245); // #F5F5F5

    public TampilanKasir(String username, int role, String additionalParam) {
        this.username = username;
        this.role = role;
        this.transaksi = new ArrayList<>();
        initComponents();
    }

    private void initComponents() {
        setTitle("Mixue Cashier System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Auto-maximize

        JPanel sidebar = createSidebar();
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(MIXUE_WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Product selection panel
        JPanel productPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        productPanel.setBackground(MIXUE_WHITE);
        productPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane productScroll = new JScrollPane(productPanel);
        productScroll.setPreferredSize(new Dimension(0, 400));
        productScroll.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        loadProducts(productPanel);

        // Cart table
        model = new DefaultTableModel(new Object[]{"Kode", "Nama", "Harga", "Jumlah", "Subtotal"}, 0);
        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30);
        // Custom renderer for alternating row colors
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
        tableScroll.setPreferredSize(new Dimension(0, 200));
        tableScroll.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));

        // Total and buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setBackground(MIXUE_WHITE);
        lblTotal = new JLabel("Total: Rp 0");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotal.setForeground(MIXUE_RED);
        btnBayar = createStyledButton("Bayar", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
        btnKembali = createStyledButton("Kembali", MIXUE_GRAY, MIXUE_BLACK, MIXUE_YELLOW);
        bottomPanel.add(lblTotal);
        bottomPanel.add(btnBayar);
        bottomPanel.add(btnKembali);

        content.add(productScroll, BorderLayout.CENTER);
        content.add(tableScroll, BorderLayout.SOUTH);
        content.add(bottomPanel, BorderLayout.PAGE_END);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);

        btnBayar.addActionListener(e -> prosesPembayaran(e));
        btnKembali.addActionListener(e -> {
            System.out.println("Kembali clicked: " + e.getActionCommand());
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

    private void loadProducts(JPanel productPanel) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nama_produk, harga, image_path FROM produk WHERE stok > 0")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nama = rs.getString("nama_produk");
                double harga = rs.getDouble("harga");
                String imagePath = rs.getString("image_path");

                // Create product card
                JPanel card = new JPanel(new BorderLayout(5, 5));
                card.setBackground(MIXUE_WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MIXUE_GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                card.setPreferredSize(new Dimension(180, 240));

                // Image
                JLabel lblImage = new JLabel();
                if (imagePath != null) {
                    try {
                        ImageIcon icon = new ImageIcon(getClass().getResource("/resources/images/" + imagePath));
                        Image scaledImage = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                        lblImage.setIcon(new ImageIcon(scaledImage));
                    } catch (Exception e) {
                        lblImage.setText("No Image");
                        lblImage.setForeground(MIXUE_RED);
                        System.err.println("Failed to load image: " + imagePath);
                    }
                } else {
                    lblImage.setText("No Image");
                    lblImage.setForeground(MIXUE_RED);
                }
                lblImage.setHorizontalAlignment(JLabel.CENTER);
                card.add(lblImage, BorderLayout.NORTH);

                // Name and price
                JLabel lblNama = new JLabel(nama, SwingConstants.CENTER);
                lblNama.setFont(new Font("Arial", Font.BOLD, 14));
                lblNama.setForeground(MIXUE_RED);
                JLabel lblHarga = new JLabel("Rp " + String.format("%.2f", harga), SwingConstants.CENTER);
                lblHarga.setFont(new Font("Arial", Font.PLAIN, 12));
                lblHarga.setForeground(MIXUE_YELLOW);
                JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
                infoPanel.setBackground(MIXUE_WHITE);
                infoPanel.add(lblNama);
                infoPanel.add(lblHarga);
                card.add(infoPanel, BorderLayout.CENTER);

                // Quantity selector
                JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
                qtyPanel.setBackground(MIXUE_WHITE);
                JButton btnMinus = createStyledButton("-", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
                JTextField txtQty = new JTextField("0", 3);
                txtQty.setHorizontalAlignment(JTextField.CENTER);
                txtQty.setFont(new Font("Arial", Font.PLAIN, 14));
                txtQty.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
                JButton btnPlus = createStyledButton("+", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);

                qtyPanel.add(btnMinus);
                qtyPanel.add(txtQty);
                qtyPanel.add(btnPlus);
                card.add(qtyPanel, BorderLayout.SOUTH);

                // Quantity actions
                btnPlus.addActionListener(e -> {
                    int qty = Integer.parseInt(txtQty.getText());
                    qty++;
                    txtQty.setText(String.valueOf(qty));
                    addToCart(id, nama, harga, String.valueOf(qty));
                });
                btnMinus.addActionListener(e -> {
                    int qty = Integer.parseInt(txtQty.getText());
                    if (qty > 0) {
                        qty--;
                        txtQty.setText(String.valueOf(qty));
                        if (qty == 0) {
                            removeFromCart(id);
                        } else {
                            updateCart(id, qty);
                        }
                    }
                });

                productPanel.add(card);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("SQL Error: " + ex.getMessage());
        }
    }

    private void addToCart(int id, String nama, double harga, String qtyStr) {
        try {
            int jumlah = Integer.parseInt(qtyStr);
            System.out.println("Adding to cart: ID=" + id + ", Nama=" + nama + ", Jumlah=" + jumlah);

            if (jumlah <= 0) {
                System.out.println("Jumlah <= 0, not adding to cart");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT stok FROM produk WHERE id = ?")) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int stok = rs.getInt("stok");
                    System.out.println("Available stock: " + stok);
                    if (stok >= jumlah) {
                        // Check if product is already in cart
                        for (int i = 0; i < model.getRowCount(); i++) {
                            if ((Integer) model.getValueAt(i, 0) == id) {
                                int existingQty = (Integer) model.getValueAt(i, 3);
                                if (stok >= jumlah) {
                                    model.setValueAt(jumlah, i, 3);
                                    model.setValueAt(jumlah * harga, i, 4);
                                    updateTransaksi(id, jumlah);
                                    System.out.println("Updated existing item in cart");
                                    updateTotal();
                                    table.repaint();
                                    return;
                                } else {
                                    JOptionPane.showMessageDialog(this, "Stok tidak cukup untuk jumlah tambahan!", "Stock Error", JOptionPane.WARNING_MESSAGE);
                                    System.out.println("Insufficient stock for additional quantity");
                                    return;
                                }
                            }
                        }
                        // Add new item
                        double subtotal = harga * jumlah;
                        model.addRow(new Object[]{id, nama, harga, jumlah, subtotal});
                        transaksi.add(new Object[]{id, jumlah});
                        System.out.println("Added new item to cart");
                        updateTotal();
                        table.repaint();
                    } else {
                        JOptionPane.showMessageDialog(this, "Stok tidak cukup!", "Stock Error", JOptionPane.WARNING_MESSAGE);
                        System.out.println("Insufficient stock");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Produk tidak ditemukan!", "Product Error", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Product not found");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka!", "Input Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("NumberFormatException: " + ex.getMessage());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("SQL Error: " + ex.getMessage());
        }
    }

    private void updateCart(int id, int newQty) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((Integer) model.getValueAt(i, 0) == id) {
                double harga = (Double) model.getValueAt(i, 2);
                model.setValueAt(newQty, i, 3);
                model.setValueAt(newQty * harga, i, 4);
                updateTransaksi(id, newQty);
                updateTotal();
                table.repaint();
                System.out.println("Updated cart: ID=" + id + ", NewQty=" + newQty);
                return;
            }
        }
    }

    private void removeFromCart(int id) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((Integer) model.getValueAt(i, 0) == id) {
                model.removeRow(i);
                transaksi.removeIf(item -> (Integer) item[0] == id);
                updateTotal();
                table.repaint();
                System.out.println("Removed from cart: ID=" + id);
                return;
            }
        }
    }

    private void updateTransaksi(int id, int jumlah) {
        for (Object[] item : transaksi) {
            if ((Integer) item[0] == id) {
                item[1] = jumlah;
                return;
            }
        }
        transaksi.add(new Object[]{id, jumlah});
    }

    private void updateTotal() {
        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += (Double) model.getValueAt(i, 4);
        }
        lblTotal.setText("Total: Rp " + String.format("%.2f", total));
    }

    private void prosesPembayaran(ActionEvent e) {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang kosong!", "Cart Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement stmtTransaksi = conn.prepareStatement(
                "INSERT INTO transaksi (tanggal, id_user, total) VALUES (NOW(), ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            int idUser = getUserId(username);
            double total = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                total += (Double) model.getValueAt(i, 4);
            }
            stmtTransaksi.setInt(1, idUser);
            stmtTransaksi.setDouble(2, total);
            stmtTransaksi.executeUpdate();
            ResultSet rs = stmtTransaksi.getGeneratedKeys();
            int idTransaksi = rs.next() ? rs.getInt(1) : 0;

            PreparedStatement stmtDetail = conn.prepareStatement(
                "INSERT INTO detail_transaksi (id_transaksi, id_produk, jumlah, subtotal) VALUES (?, ?, ?, ?)"
            );
            for (Object[] item : transaksi) {
                int idProduk = (Integer) item[0];
                int jumlah = (Integer) item[1];
                double subtotal = jumlah * getHargaProduk(idProduk);
                stmtDetail.setInt(1, idTransaksi);
                stmtDetail.setInt(2, idProduk);
                stmtDetail.setInt(3, jumlah);
                stmtDetail.setDouble(4, subtotal);
                stmtDetail.executeUpdate();

                PreparedStatement stmtUpdateStok = conn.prepareStatement(
                    "UPDATE produk SET stok = stok - ? WHERE id = ?"
                );
                stmtUpdateStok.setInt(1, jumlah);
                stmtUpdateStok.setInt(2, idProduk);
                stmtUpdateStok.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Pembayaran berhasil!", "Success", JOptionPane.INFORMATION_MESSAGE);
            model.setRowCount(0);
            transaksi.clear();
            updateTotal();
            refreshProductPanel();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("SQL Error: " + ex.getMessage());
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
        }
    }

    private void refreshProductPanel() {
        JPanel contentPanel = (JPanel) getContentPane().getComponent(1);
        JPanel productPanel = (JPanel) ((JScrollPane) contentPanel.getComponent(0)).getViewport().getView();
        productPanel.removeAll();
        loadProducts(productPanel);
        productPanel.revalidate();
        productPanel.repaint();
    }

    private int getUserId(String username) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("id") : 0;
        }
    }

    private double getHargaProduk(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT harga FROM produk WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("harga") : 0;
        }
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
            System.out.println("Kasir clicked: " + e.getActionCommand());
        });

        if (role == 1) {
            btnMenu.addActionListener(e -> {
                System.out.println("Menu clicked: " + e.getActionCommand());
                new TampilanMenu(username, role).setVisible(true);
                dispose();
            });

            btnKaryawan.addActionListener(e -> {
                System.out.println("Karyawan clicked: " + e.getActionCommand());
                new TampilanKaryawan(username, role).setVisible(true);
                dispose();
            });

            btnStok.addActionListener(e -> {
                System.out.println("Stok clicked: " + e.getActionCommand());
                new TampilanStokGudang(username, role, "").setVisible(true);
                dispose();
            });

            btnLaporan.addActionListener(e -> {
                System.out.println("Laporan clicked: " + e.getActionCommand());
                new TampilanLaporan(username, role).setVisible(true);
                dispose();
            });

            btnPengaturan.addActionListener(e -> {
                System.out.println("Pengaturan clicked: " + e.getActionCommand());
                new TampilanPengaturan(username, role).setVisible(true);
                dispose();
            });
        }

        btnLogout.addActionListener(e -> {
            System.out.println("Logout clicked: " + e.getActionCommand());
            new FormLogin2().setVisible(true);
            dispose();
        });

        return sidebar;
    }
}