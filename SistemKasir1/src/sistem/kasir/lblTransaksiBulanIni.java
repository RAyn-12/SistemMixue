package sistem.kasir;

import java.sql.*;

public class lblTransaksiBulanIni {
    public static String getTransaksiBulanIni() {
        int count = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM transaksi WHERE MONTH(tanggal) = MONTH(CURDATE())")) {
            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return String.valueOf(count);
    }
}