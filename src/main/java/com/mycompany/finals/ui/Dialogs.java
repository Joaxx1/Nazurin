package com.mycompany.finals.ui;

import com.mycompany.finals.model.LibraryEntry;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.HierarchyEvent;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * The two dialogs the app asks: writing a review and confirming a removal.
 */
final class Dialogs {

    private Dialogs() {
    }

    /** Asks for a review. Empty when the user cancels. */
    static Optional<String> editReview(Component parent, LibraryEntry entry) {
        JTextArea area = new JTextArea(entry.getReview(), 5, 36);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Theme.body(Font.PLAIN, 14f));
        area.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && area.isShowing()) {
                SwingUtilities.invokeLater(area::requestFocusInWindow);
            }
        });

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(Theme.label("What did you think of " + entry.getGame().getName() + "?",
                Theme.body(Font.BOLD, 13f), Theme.INK), BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);

        String[] options = {"Save review", "Cancel"};
        int choice = JOptionPane.showOptionDialog(parent, panel, "Review",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        return choice == 0 ? Optional.of(area.getText()) : Optional.empty();
    }

    /** Asks before removing a game, since its status and review go with it. */
    static boolean confirmRemove(Component parent, LibraryEntry entry) {
        String message = "<html><div style='width:300px'>Remove <b>" + Theme.html(entry.getGame().getName())
                + "</b> from your library? Its status and review will be deleted.</div></html>";
        String[] options = {"Remove", "Cancel"};
        int choice = JOptionPane.showOptionDialog(parent, message, "Remove game",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
        return choice == 0;
    }
}
