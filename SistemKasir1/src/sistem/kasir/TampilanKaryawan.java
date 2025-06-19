package sistem.kasir;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class TampilanKaryawan extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private String username;
    private int role;
    private JButton btnTambah, btnKembali;

    private static final Color MIXUE_RED = new Color(213, 0, 0);
    private static final Color MIXUE_WHITE = Color.WHITE;
    private static final Color MIXUE_YELLOW = new Color(255, 193, 7);
    private static final Color MIXUE_BLACK = Color.BLACK;
    private static final Color MIXUE_GRAY = new Color(245, 245, 245);

    public TampilanKaryawan(String username, int role) {
        this.username = username;
        this.role = role;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setTitle("Mixue Cashier System - Karyawan");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel sidebar = createSidebar();
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(MIXUE_GRAY);
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(new Object[]{"ID", "Nama", "Username", "Role", "Aksi"}, 0);
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? MIXUE_WHITE : MIXUE_GRAY);
                } else {
                    c.setBackground(MIXUE_YELLOW);
                    c.setForeground(MIXUE_BLACK);
                }
                return c;
            }
        };
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setGridColor(Color.LIGHT_GRAY);
        table.setSelectionBackground(MIXUE_YELLOW);
        table.setSelectionForeground(MIXUE_BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(MIXUE_RED);
        header.setForeground(MIXUE_WHITE);

        TableColumn actionColumn = table.getColumnModel().getColumn(4);
        actionColumn.setCellRenderer(new ButtonRenderer());
        if (role == 1) {
            actionColumn.setCellEditor(new ButtonEditor(new JCheckBox()));
        }

        JScrollPane scrollPane = new JScrollPane(table);
        content.add(scrollPane, BorderLayout.CENTER);

        btnTambah = createStyledButton("Tambah Karyawan", MIXUE_WHITE, MIXUE_RED, MIXUE_YELLOW);
        btnTambah.setEnabled(role == 1);
        btnKembali = createStyledButton("Kembali", MIXUE_WHITE, MIXUE_RED, MIXUE_YELLOW);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(MIXUE_GRAY);
        buttonPanel.add(btnTambah);
        buttonPanel.add(btnKembali);
        content.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);

        btnTambah.addActionListener(e -> {
            FormTambahKaryawan form = new FormTambahKaryawan(username, role);
            form.setVisible(true);
            form.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    loadData();
                }
            });
        });

        btnKembali.addActionListener(e -> {
            new TampilanDashboard(username, role).setVisible(true);
            dispose();
        });
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
                // Sudah di halaman ini
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

    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            while (rs.next()) {
                String roleStr = rs.getString("role");
                String roleDisplay = "admin".equals(roleStr) ? "Admin" : "Kasir";
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("username"),
                    roleDisplay,
                    "Edit/Hapus"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private JButton createStyledButton(String text, Color bg, Color fg, Color hover) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hover);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bg);
            }
        });
        return button;
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(MIXUE_YELLOW);
            setFont(new Font("Arial", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(MIXUE_YELLOW);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int id = (int) table.getValueAt(table.getSelectedRow(), 0);
                if (label.equals("Edit/Hapus")) {
                    FormEditKaryawan form = new FormEditKaryawan(username, role, id);
                    form.setVisible(true);
                    form.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent e) {
                            loadData();
                        }
                    });
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}
