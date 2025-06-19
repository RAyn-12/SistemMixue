package sistem.kasir;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

// Separate public class to fix visibility and renderer issues
public class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        return this;
    }
}