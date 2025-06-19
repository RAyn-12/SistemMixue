package sistem.kasir;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TampilanDashboard extends JFrame {
    private JLabel lblWelcome, lblDateTime, lblDailySales, lblWeeklySales, lblAnnualSales, 
                   lblDailyTransactions, lblTopProduct;
    private String username;
    private int role;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    
    // Warna tema Mixue
    private static final Color MIXUE_RED = new Color(213, 0, 0);
    private static final Color MIXUE_WHITE = Color.WHITE;
    private static final Color MIXUE_YELLOW = new Color(255, 193, 7);
    private static final Color MIXUE_BLACK = Color.BLACK;
    private static final Color MIXUE_GRAY = new Color(245, 245, 245);
    private static final Color MIXUE_RED_LIGHT = new Color(255, 102, 102);

    public TampilanDashboard(String username, int role) {
        this.username = username;
        this.role = role;
        initComponents();
        loadDashboardData();
        startClock();
        animateWelcome();
    }

    private void initComponents() {
        setTitle("Mixue Cashier System - Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel sidebar = createSidebar();
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(MIXUE_GRAY);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = createHeaderPanel();
        JPanel salesPanel = createSalesPanel();

        content.add(headerPanel, BorderLayout.NORTH);
        content.add(salesPanel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);
    }

    // Membuat panel header dengan selamat datang dan jam
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, MIXUE_RED, getWidth(), getHeight(), MIXUE_WHITE);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        headerPanel.setLayout(new BorderLayout(10, 10));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(0, 150));

        lblWelcome = new JLabel("Selamat Datang!", SwingConstants.LEFT);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblWelcome.setForeground(MIXUE_WHITE);
        lblWelcome.setIcon(new JLabel("🍦 ").getIcon());

        lblDateTime = new JLabel("", SwingConstants.RIGHT);
        lblDateTime.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDateTime.setForeground(MIXUE_WHITE);

        headerPanel.add(lblWelcome, BorderLayout.CENTER);
        headerPanel.add(lblDateTime, BorderLayout.SOUTH);
        return headerPanel;
    }

    // Membuat panel penjualan dengan card-style
    private JPanel createSalesPanel() {
        JPanel salesPanel = new JPanel(new GridBagLayout());
        salesPanel.setBackground(MIXUE_GRAY);
        salesPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(MIXUE_RED, 2), "Statistik Penjualan",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 16), MIXUE_RED));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Card untuk Penjualan Harian
        lblDailySales = createStatLabel("💰 Penjualan Harian: Rp0");
        gbc.gridx = 0; gbc.gridy = 0;
        salesPanel.add(createCardPanel(lblDailySales), gbc);

        // Card untuk Penjualan Mingguan
        lblWeeklySales = createStatLabel("💰 Penjualan Mingguan: Rp0");
        gbc.gridx = 1; gbc.gridy = 0;
        salesPanel.add(createCardPanel(lblWeeklySales), gbc);

        // Card untuk Penjualan Tahunan
        lblAnnualSales = createStatLabel("💰 Penjualan Tahunan: Rp0");
        gbc.gridx = 0; gbc.gridy = 1;
        salesPanel.add(createCardPanel(lblAnnualSales), gbc);

        // Card untuk Transaksi Harian
        lblDailyTransactions = createStatLabel("🛒 Transaksi Hari Ini: 0");
        gbc.gridx = 1; gbc.gridy = 1;
        salesPanel.add(createCardPanel(lblDailyTransactions), gbc);

        // Card untuk Produk Terlaris
        lblTopProduct = createStatLabel("🍦 Produk Terlaris: -");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        salesPanel.add(createCardPanel(lblTopProduct), gbc);

        // Tombol Refresh
        JButton btnRefresh = createStyledButton("Refresh", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
        btnRefresh.addActionListener(e -> loadDashboardData());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        salesPanel.add(btnRefresh, gbc);

        return salesPanel;
    }

    // Membuat panel card dengan shadow dan rounded border
    private JPanel createCardPanel(JLabel label) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(MIXUE_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MIXUE_GRAY, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setPreferredSize(new Dimension(200, 100));
        card.add(label, BorderLayout.CENTER);
        card.setBorder(new javax.swing.border.LineBorder(MIXUE_GRAY, 1, true));
        card.setUI(new javax.swing.plaf.basic.BasicPanelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(MIXUE_WHITE);
                g2d.fillRoundRect(0, 0, c.getWidth()-1, c.getHeight()-1, 15, 15);
                super.paint(g, c);
            }
        });
        return card;
    }

    // Membuat label statistik
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(MIXUE_BLACK);
        return label;
    }

    // Memuat data dashboard
    private void loadDashboardData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Load Welcome Message
            PreparedStatement userStmt = conn.prepareStatement("SELECT nama FROM users WHERE username = ?");
            userStmt.setString(1, username);
            ResultSet userRs = userStmt.executeQuery();
            if (userRs.next()) {
                lblWelcome.setText("Selamat Datang, " + userRs.getString("nama") + "! 🍦");
            }

            Statement stmt = conn.createStatement();
            ResultSet rs;

            // Daily Sales
            rs = stmt.executeQuery("SELECT SUM(total) AS total FROM transaksi WHERE DATE(tanggal) = CURDATE()");
            double dailySales = rs.next() && rs.getObject("total") != null ? rs.getDouble("total") : 0;
            lblDailySales.setText("💰 Penjualan Harian: " + currencyFormat.format(dailySales));

            // Weekly Sales
            rs = stmt.executeQuery("SELECT SUM(total) AS total FROM transaksi WHERE YEARWEEK(tanggal, 1) = YEARWEEK(CURDATE(), 1)");
            double weeklySales = rs.next() && rs.getObject("total") != null ? rs.getDouble("total") : 0;
            lblWeeklySales.setText("💰 Penjualan Mingguan: " + currencyFormat.format(weeklySales));

            // Annual Sales
            rs = stmt.executeQuery("SELECT SUM(total) AS total FROM transaksi WHERE YEAR(tanggal) = YEAR(CURDATE())");
            double annualSales = rs.next() && rs.getObject("total") != null ? rs.getDouble("total") : 0;
            lblAnnualSales.setText("💰 Penjualan Tahunan: " + currencyFormat.format(annualSales));

            // Daily Transactions
            rs = stmt.executeQuery("SELECT COUNT(id) AS count FROM transaksi WHERE DATE(tanggal) = CURDATE()");
            int dailyTransactions = rs.next() ? rs.getInt("count") : 0;
            lblDailyTransactions.setText("🛒 Transaksi Hari Ini: " + dailyTransactions);

            // Top Product
            rs = stmt.executeQuery(
                "SELECT p.nama_produk, SUM(dt.jumlah) AS qty " +
                "FROM detail_transaksi dt " +
                "JOIN produk p ON dt.id_produk = p.id " +
                "JOIN transaksi t ON dt.id_transaksi = t.id " +
                "WHERE DATE(t.tanggal) = CURDATE() " +
                "GROUP BY p.id ORDER BY qty DESC LIMIT 1"
            );
            if (rs.next()) {
                lblTopProduct.setText("🍦 Produk Terlaris: " + rs.getString("nama_produk") + " (" + rs.getInt("qty") + " unit)");
            } else {
                lblTopProduct.setText("🍦 Produk Terlaris: -");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("SQL Error: " + ex.getMessage());
        }
    }

    // Animasi fade-in untuk selamat datang
    private void animateWelcome() {
        lblWelcome.setForeground(new Color(255, 255, 255, 0));
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int alpha = 0;
            @Override
            public void run() {
                alpha += 10;
                if (alpha >= 255) {
                    alpha = 255;
                    timer.cancel();
                }
                lblWelcome.setForeground(new Color(255, 255, 255, alpha));
            }
        }, 0, 50);
    }

    // Memulai jam real-time
    private void startClock() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm:ss");
                lblDateTime.setText(sdf.format(new java.util.Date()));
            }
        }, 0, 1000);
    }

    // Membuat tombol dengan efek hover dan pressed
    private JButton createStyledButton(String text, Color bg, Color fg, Color hover) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(MIXUE_RED_LIGHT);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(button.getModel().isRollover() ? hover : bg);
            }
        });
        return button;
    }

    // Membuat sidebar dengan navigasi
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