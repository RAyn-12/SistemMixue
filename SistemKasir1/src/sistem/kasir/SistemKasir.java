package sistem.kasir;

import javax.swing.*;

public class SistemKasir {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FormLogin2().setVisible(true);
        });
    }
}