package sistem.kasir;

import java.sql.*;

public class lblPenjualanBulanIni {
    public static String getPenjualanBulanIni() {
        double total = 0.0;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(total) as total FROM transaksi WHERE MONTH(tanggal) = MONTH(CURDATE())")) {
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return currencyFormat.format(total);
    }
}