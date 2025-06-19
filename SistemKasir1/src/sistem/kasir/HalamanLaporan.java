package sistem.kasir;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class HalamanLaporan extends JFrame {
    private JComboBox<String> cmbPeriode;
    private JTable tableLaporan;
    private DefaultTableModel tableModel;

    public HalamanLaporan() {
        setTitle("Laporan Penjualan - Sistem Kasir Mixue");
        setSize(800, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // Panel Filter
        JPanel filterPanel = new JPanel(new FlowLayout());
        JLabel lblPeriode = new JLabel("Pilih Periode:");
        cmbPeriode = new JComboBox<>(new String[]{"Harian", "Mingguan", "Bulanan"});
        JButton btnTampilkan = new JButton("Tampilkan");
        filterPanel.add(lblPeriode);
        filterPanel.add(cmbPeriode);
        filterPanel.add(btnTampilkan);
        panel.add(filterPanel, BorderLayout.NORTH);

        // Tabel Laporan
        tableModel = new DefaultTableModel(new String[]{"ID Transaksi", "Tanggal", "Kasir", "Total"}, 0);
        tableLaporan = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableLaporan);
        panel.add(scrollPane, BorderLayout.CENTER);

        btnTampilkan.addActionListener(_ -> loadLaporan());

        add(panel);
    }

    private void loadLaporan() {
        String periode = (String) cmbPeriode.getSelectedItem();
        String query = "";
        if (periode.equals("Harian")) {
            query = "SELECT t.id, t.tanggal, u.nama, t.total FROM transaksi t JOIN users u ON t.id_user = u.id WHERE DATE(t.tanggal) = CURDATE()";
        } else if (periode.equals("Mingguan")) {
            query = "SELECT t.id, t.tanggal, u.nama, t.total FROM transaksi t JOIN users u ON t.id_user = u.id WHERE WEEK(t.tanggal) = WEEK(CURDATE())";
        } else {
            query = "SELECT t.id, t.tanggal, u.nama, t.total FROM transaksi t JOIN users u ON t.id_user = u.id WHERE MONTH(t.tanggal) = MONTH(CURDATE())";
        }

        tableModel.setRowCount(0);
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getTimestamp("tanggal"),
                    rs.getString("nama"),
                    currency.format(rs.getDouble("total"))
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}