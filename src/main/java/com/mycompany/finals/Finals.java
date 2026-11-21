package com.mycompany.finals;

import com.mycompany.finals.data.AppConfig;
import com.mycompany.finals.data.CoverLoader;
import com.mycompany.finals.data.LibraryStore;
import com.mycompany.finals.model.Library;
import com.mycompany.finals.model.LibraryEntry;
import com.mycompany.finals.ui.MainFrame;
import com.mycompany.finals.ui.Theme;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Nazurin — a game backlog tracker. Starts the Swing window.
 *
 * @author Team Nazurin (Calaoagan, Cordero, Juanico)
 */
public class Finals {

    public static void main(String[] args) {
        // macOS: show "Nazurin" in the menu bar and keep the light title bar even in dark mode.
        System.setProperty("apple.awt.application.name", "Nazurin");
        System.setProperty("apple.awt.application.appearance", "NSAppearanceNameAqua");
        SwingUtilities.invokeLater(Finals::start);
    }

    private static void start() {
        Theme.install();

        LibraryStore store = new LibraryStore(AppConfig.APP_DIR.resolve("library.json"));
        List<LibraryEntry> saved;
        try {
            saved = store.load();
        } catch (IOException e) {
            // Stop instead of starting empty, so the next save cannot overwrite the user's data.
            JOptionPane.showMessageDialog(null,
                    "Could not load your library:\n" + e.getMessage()
                    + "\n\nFix or move the file, then start Nazurin again.",
                    "Nazurin", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        CoverLoader covers = new CoverLoader(AppConfig.APP_DIR.resolve("covers"));
        new MainFrame(AppConfig.createCatalog(), new Library(saved), store, covers).setVisible(true);
    }
}
