package com.mycompany.finals.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.function.IntUnaryOperator;
import javax.swing.SwingUtilities;

/**
 * Lays cards out in rows that fill the width: as many columns as fit at the minimum
 * card width, with the leftover space shared between them. Card height follows card width.
 */
public class CardGridLayout implements LayoutManager {

    private final int minCardWidth;
    private final int hgap;
    private final int vgap;
    private final IntUnaryOperator heightForWidth;
    private int widthOfLastMeasure = -1;

    public CardGridLayout(int minCardWidth, int hgap, int vgap, IntUnaryOperator heightForWidth) {
        this.minCardWidth = minCardWidth;
        this.hgap = hgap;
        this.vgap = vgap;
        this.heightForWidth = heightForWidth;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            widthOfLastMeasure = parent.getWidth();
            int width = parent.getWidth() > 0
                    ? parent.getWidth()
                    : minCardWidth * 4 + hgap * 3 + insets.left + insets.right;
            Grid grid = grid(width - insets.left - insets.right, parent.getComponentCount());
            int height = grid.rows() == 0 ? 0 : grid.rows() * grid.cellHeight() + (grid.rows() - 1) * vgap;
            return new Dimension(width, height + insets.top + insets.bottom);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Insets insets = parent.getInsets();
        return new Dimension(minCardWidth + insets.left + insets.right, 0);
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            if (parent.getWidth() != widthOfLastMeasure) {
                // The height we asked for depends on the width, so measure again at the new width.
                SwingUtilities.invokeLater(parent::revalidate);
            }
            Insets insets = parent.getInsets();
            Grid grid = grid(parent.getWidth() - insets.left - insets.right, parent.getComponentCount());
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component card = parent.getComponent(i);
                int column = i % grid.columns();
                int row = i / grid.columns();
                card.setBounds(
                        insets.left + column * (grid.cellWidth() + hgap),
                        insets.top + row * (grid.cellHeight() + vgap),
                        grid.cellWidth(),
                        grid.cellHeight());
            }
        }
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    private Grid grid(int width, int count) {
        int columns = Math.max(1, (width + hgap) / (minCardWidth + hgap));
        int cellWidth = Math.max(1, (width - hgap * (columns - 1)) / columns);
        int rows = (count + columns - 1) / columns;
        return new Grid(columns, rows, cellWidth, heightForWidth.applyAsInt(cellWidth));
    }

    private record Grid(int columns, int rows, int cellWidth, int cellHeight) {
    }
}
