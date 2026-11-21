package com.mycompany.finals.ui;

import com.mycompany.finals.data.CoverLoader;
import com.mycompany.finals.model.GameStatus;
import com.mycompany.finals.model.Library;
import com.mycompany.finals.model.LibraryEntry;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

/**
 * My Library page: every game the user added, filtered by status, with its review and controls.
 */
public class LibraryPanel extends JPanel {

    private static final String LIST = "list";
    private static final String EMPTY = "empty";

    private final Library library;
    private final CoverLoader covers;
    private final Runnable browseCatalog;

    private final JToggleButton allTab = new JToggleButton();
    private final Map<GameStatus, JToggleButton> statusTabs = new EnumMap<>(GameStatus.class);
    private final ScrollablePanel list = new ScrollablePanel();
    private final EmptyState empty = new EmptyState();
    private final CardLayout bodyLayout = new CardLayout();
    private final JPanel body = new JPanel(bodyLayout);
    private final List<LibraryRow> rows = new ArrayList<>();
    private GameStatus filter; // null shows every game

    public LibraryPanel(Library library, CoverLoader covers, Runnable browseCatalog) {
        super(new BorderLayout(0, 18));
        this.library = library;
        this.covers = covers;
        this.browseCatalog = browseCatalog;
        setBackground(Theme.PAPER);
        setBorder(BorderFactory.createEmptyBorder(28, Theme.GUTTER, 0, Theme.GUTTER));

        JPanel tabs = new JPanel();
        tabs.setLayout(new BoxLayout(tabs, BoxLayout.X_AXIS));
        tabs.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        addTab(tabs, group, allTab, null);
        for (GameStatus status : GameStatus.values()) {
            JToggleButton tab = new JToggleButton();
            tab.setIcon(new StatusIcon(status, 11, Theme.INK));
            tab.setSelectedIcon(new StatusIcon(status, 11, Theme.PAPER)); // white on the black selected tab
            tab.setIconTextGap(7);
            statusTabs.put(status, tab);
            addTab(tabs, group, tab, status);
        }
        allTab.setSelected(true);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(Theme.label("My Library", Theme.display(36f), Theme.INK), BorderLayout.NORTH);
        top.add(tabs, BorderLayout.SOUTH);

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));
        body.setOpaque(false);
        body.add(Theme.scroll(list), LIST);
        body.add(empty, EMPTY);

        add(top, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        library.addChangeListener(() -> SwingUtilities.invokeLater(this::refresh));
        refresh();
    }

    private void addTab(JPanel tabs, ButtonGroup group, JToggleButton tab, GameStatus status) {
        Theme.styleTab(tab);
        tab.setFont(Theme.body(Font.BOLD, 13f));
        tab.addActionListener(e -> {
            filter = status;
            refresh();
        });
        if (tabs.getComponentCount() > 0) {
            tabs.add(Box.createHorizontalStrut(4));
        }
        group.add(tab);
        tabs.add(tab);
    }

    /** Rebuilds the rows and the counts on the filter tabs. */
    void refresh() {
        allTab.setText("All  " + library.getEntries().size());
        statusTabs.forEach((status, tab) ->
                tab.setText(status + "  " + library.withStatus(EnumSet.of(status)).size()));

        List<LibraryEntry> shown = filter == null ? library.getEntries() : library.withStatus(EnumSet.of(filter));
        list.removeAll();
        rows.clear();
        for (LibraryEntry entry : shown) {
            LibraryRow row = new LibraryRow(entry, library, covers);
            rows.add(row);
            list.add(row);
        }
        list.revalidate();
        list.repaint();

        if (library.getEntries().isEmpty()) {
            empty.setMessage("Your library is empty",
                    "Find games in the Catalog and press Add to start tracking them.",
                    "Browse the catalog", browseCatalog);
            bodyLayout.show(body, EMPTY);
        } else if (shown.isEmpty()) {
            empty.setMessage("Nothing marked " + filter,
                    "Games you mark as " + filter + " will show up here.",
                    "Show all games", allTab::doClick);
            bodyLayout.show(body, EMPTY);
        } else {
            bodyLayout.show(body, LIST);
        }
    }

    List<LibraryRow> getRows() {
        return List.copyOf(rows);
    }

    JToggleButton getTab(GameStatus status) {
        return status == null ? allTab : statusTabs.get(status);
    }

    EmptyState getEmptyState() {
        return empty;
    }
}
