package com.mycompany.finals.ui;

import com.mycompany.finals.data.CoverLoader;
import com.mycompany.finals.data.GameCatalog;
import com.mycompany.finals.data.LibraryStore;
import com.mycompany.finals.model.Library;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

/**
 * The main window: the header with navigation, and one page per feature below it.
 * Saves the library whenever it changes.
 */
public class MainFrame extends JFrame {

    static final String CATALOG = "catalog";
    static final String LIBRARY = "library";
    static final String ROULETTE = "roulette";

    private final Library library;
    private final LibraryStore store;
    private final CardLayout pages = new CardLayout();
    private final JPanel pageHost = new JPanel(pages);
    private final Map<String, JToggleButton> navButtons = new HashMap<>();
    private final Map<String, JComponent> pagePanels = new HashMap<>();

    public MainFrame(GameCatalog catalog, Library library, LibraryStore store, CoverLoader covers) {
        super("Nazurin");
        this.library = library;
        this.store = store;

        CatalogPanel catalogPanel = new CatalogPanel(catalog, library, covers);
        addPage(CATALOG, catalogPanel);
        addPage(LIBRARY, new LibraryPanel(library, covers, () -> showPage(CATALOG)));
        addPage(ROULETTE, new RoulettePanel(library, covers, () -> showPage(CATALOG)));

        HeaderPanel header = new HeaderPanel();
        navButtons.put(CATALOG, header.addNavButton("Catalog", () -> showPage(CATALOG)));
        navButtons.put(LIBRARY, header.addNavButton(libraryLabel(), () -> showPage(LIBRARY)));
        navButtons.put(ROULETTE, header.addNavButton("Game Roulette", () -> showPage(ROULETTE)));

        JPanel content = new JPanel(new BorderLayout());
        content.add(header, BorderLayout.NORTH);
        content.add(pageHost, BorderLayout.CENTER);
        setContentPane(content);

        library.addChangeListener(() -> {
            save();
            navButtons.get(LIBRARY).setText(libraryLabel());
        });

        // Cmd+1, 2, 3 (Ctrl on Windows) switch pages; Cmd+F jumps to the search box.
        int menuKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_1, menuKey), () -> showPage(CATALOG));
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_2, menuKey), () -> showPage(LIBRARY));
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_3, menuKey), () -> showPage(ROULETTE));
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_F, menuKey), () -> {
            showPage(CATALOG);
            catalogPanel.focusSearch();
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 660));
        setSize(1120, 780);
        setLocationRelativeTo(null);
    }

    void showPage(String name) {
        navButtons.get(name).setSelected(true);
        pages.show(pageHost, name);
    }

    JComponent getPage(String name) {
        return pagePanels.get(name);
    }

    JToggleButton getNavButton(String name) {
        return navButtons.get(name);
    }

    private void addPage(String name, JComponent page) {
        pagePanels.put(name, page);
        pageHost.add(page, name);
    }

    private String libraryLabel() {
        return "My Library  " + library.getEntries().size();
    }

    private void bindKey(KeyStroke key, Runnable action) {
        String id = "nazurin." + key;
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, id);
        getRootPane().getActionMap().put(id, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void save() {
        try {
            store.save(library.getEntries());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save your library:\n" + e.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
