package com.mycompany.finals.ui;

import com.mycompany.finals.data.CoverLoader;
import com.mycompany.finals.model.Game;
import com.mycompany.finals.model.GameStatus;
import com.mycompany.finals.model.Library;
import com.mycompany.finals.model.LibraryEntry;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * One game in My Library: its cover, title and genres, the review as a speech bubble,
 * and controls to change its status or remove it.
 */
public class LibraryRow extends JPanel {

    private final LibraryEntry entry;
    private final JComboBox<GameStatus> statusBox = new JComboBox<>(GameStatus.values());
    private final SpeechBubble bubble = new SpeechBubble();
    private final JButton removeButton = new JButton("Remove");

    public LibraryRow(LibraryEntry entry, Library library, CoverLoader covers) {
        super(new BorderLayout(18, 0));
        this.entry = entry;
        setOpaque(false);
        setAlignmentX(LEFT_ALIGNMENT);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.RULE),
                BorderFactory.createEmptyBorder(16, 0, 16, 0)));
        Game game = entry.getGame();

        CoverView cover = new CoverView(covers, 3);
        cover.setGame(game);
        cover.setPreferredSize(new Dimension(160, 90));
        JPanel coverColumn = new JPanel(new BorderLayout());
        coverColumn.setOpaque(false);
        coverColumn.add(cover, BorderLayout.NORTH);

        JLabel title = Theme.label(game.getName(), Theme.body(Font.BOLD, 16f), Theme.INK);
        JLabel details = Theme.label(detailsText(game), Theme.body(Font.PLAIN, 12f), Theme.TONE);
        title.setMinimumSize(new Dimension(0, title.getPreferredSize().height));
        details.setMinimumSize(new Dimension(0, details.getPreferredSize().height));
        bubble.setReview(entry.getReview());
        bubble.addActionListener(e -> Dialogs.editReview(this, entry)
                .ifPresent(review -> library.setReview(entry, review)));

        JPanel text = new JPanel(new GridBagLayout());
        text.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        text.add(title, c);
        c.insets = new Insets(2, 0, 0, 0);
        text.add(details, c);
        c.insets = new Insets(10, 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        text.add(bubble, c);
        c.insets = new Insets(0, 0, 0, 0);
        c.weighty = 1;
        text.add(Box.createGlue(), c);

        statusBox.setSelectedItem(entry.getStatus());
        statusBox.setRenderer(new StatusRenderer());
        statusBox.getAccessibleContext().setAccessibleName("Status of " + game.getName());
        statusBox.addActionListener(e -> {
            GameStatus chosen = (GameStatus) statusBox.getSelectedItem();
            if (chosen != null && chosen != entry.getStatus()) {
                library.setStatus(entry, chosen);
            }
        });
        Theme.styleQuiet(removeButton);
        removeButton.addActionListener(e -> {
            if (Dialogs.confirmRemove(this, entry)) {
                library.remove(entry);
            }
        });

        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        GridBagConstraints a = new GridBagConstraints();
        a.gridx = 0;
        a.fill = GridBagConstraints.HORIZONTAL;
        actions.add(statusBox, a);
        a.insets = new Insets(6, 0, 0, 0);
        a.fill = GridBagConstraints.NONE;
        a.anchor = GridBagConstraints.EAST;
        actions.add(removeButton, a);
        a.weighty = 1;
        actions.add(Box.createGlue(), a);

        add(coverColumn, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
        add(actions, BorderLayout.EAST);
    }

    public LibraryEntry getEntry() {
        return entry;
    }

    JComboBox<GameStatus> getStatusBox() {
        return statusBox;
    }

    SpeechBubble getBubble() {
        return bubble;
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    private static String detailsText(Game game) {
        String year = game.getYear();
        String genres = game.getGenres();
        if (year.isEmpty()) {
            return genres;
        }
        return genres.isEmpty() ? year : genres + ",  " + year;
    }
}
