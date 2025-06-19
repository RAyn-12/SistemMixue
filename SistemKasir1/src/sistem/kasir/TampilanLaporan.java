package sistem.kasir;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TampilanLaporan extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnFilter, btnKembali;
    private JFormattedTextField txtStartDate, txtEndDate;
    private String username;
    private int role;
    private static final Color MIXUE_RED = new Color(213, 0, 0);
    private static final Color MIXUE_WHITE = Color.WHITE;
    private static final Color MIXUE_YELLOW = new Color(255, 193, 7);
    private static final Color MIXUE_BLACK = Color.BLACK;
    private static final Color MIXUE_GRAY = new Color(245, 245, 245);

    public TampilanLaporan(String username, int role) {
        this.username = username;
        this.role = role;
        initComponents();
    }

    private void initComponents() {
        setTitle("Mixue Cashier System - Laporan");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel sidebar = createSidebar();
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(MIXUE_WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(MIXUE_WHITE);
        filterPanel.add(new JLabel("Tanggal Mulai (yyyy-MM-dd):"));
        txtStartDate = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        txtStartDate.setColumns(10);
        txtStartDate.setFont(new Font("Arial", Font.PLAIN, 14));
        txtStartDate.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        filterPanel.add(txtStartDate);
        filterPanel.add(new JLabel("Tanggal Selesai (yyyy-MM-dd):"));
        txtEndDate = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        txtEndDate.setColumns(10);
        txtEndDate.setFont(new Font("Arial", Font.PLAIN, 14));
        txtEndDate.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        filterPanel.add(txtEndDate);
        btnFilter = createStyledButton("Filter", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
        filterPanel.add(btnFilter);

        model = new DefaultTableModel(new Object[]{"ID", "Tanggal", "Total", "Kasir"}, 0);
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
        btnKembali = createStyledButton("Kembali", MIXUE_GRAY, MIXUE_BLACK, MIXUE_YELLOW);
        buttonPanel.add(btnKembali);

        content.add(filterPanel, BorderLayout.NORTH);
        content.add(tableScroll, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);

        loadTransactions();

        btnFilter.addActionListener(e -> filterTransactions());
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

    private void loadTransactions() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.id, t.tanggal, t.total, u.username " +
                 "FROM transaksi t JOIN users u ON t.id_user = u.id")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getTimestamp("tanggal"),
                    rs.getDouble("total"),
                    rs.getString("username")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("SQL Error: " + ex.getMessage());
        }
    }

    private void filterTransactions() {
        String startDate = txtStartDate.getText();
        String endDate = txtEndDate.getText();
        if (startDate.isEmpty() || endDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi tanggal mulai dan selesai!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.id, t.tanggal, t.total, u.username " +
                 "FROM transaksi t JOIN users u ON t.id_user = u.id " +
                 "WHERE t.tanggal BETWEEN ? AND ?")) {
            stmt.setString(1, startDate + " 00:00:00");
            stmt.setString(2, endDate + " 23:59:59");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getTimestamp("tanggal"),
                    rs.getDouble("total"),
                    rs.getString("username")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("SQL Error: " + ex.getMessage());
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
            btnLaporan.addActionListener(e -> System.out.println("Laporan clicked: " + e.getActionCommand()));
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