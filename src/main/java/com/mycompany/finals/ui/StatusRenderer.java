package com.mycompany.finals.ui;

import com.mycompany.finals.model.GameStatus;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Shows each status in a drop-down with its glyph in front of the name.
 */
class StatusRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean selected, boolean focused) {
        super.getListCellRendererComponent(list, value, index, selected, focused);
        if (value instanceof GameStatus status) {
            setIcon(new StatusIcon(status, 11, null));
            setIconTextGap(8);
        }
        return this;
    }
}
