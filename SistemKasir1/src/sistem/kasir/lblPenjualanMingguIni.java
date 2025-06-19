package sistem.kasir;

import java.sql.*;

public class lblPenjualanMingguIni {
    public static String getPenjualanMingguIni() {
        double total = 0.0;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(total) as total FROM transaksi WHERE WEEK(tanggal) = WEEK(CURDATE())")) {
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return currencyFormat.format(total);
    }
}