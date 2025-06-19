package sistem.kasir;

import javax.swing.*;
import java.io.File;

public class fileChooser {
    public static File chooseFile(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih Lokasi File");
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
}