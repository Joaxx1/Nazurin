package com.mycompany.finals.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.finals.data.CoverLoader;
import com.mycompany.finals.model.Game;
import com.mycompany.finals.model.GameStatus;
import com.mycompany.finals.model.Library;
import com.mycompany.finals.model.LibraryEntry;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * One game in the catalog grid: its framed cover, title, year and rating, and a button that
 * adds it to the library or, once added, shows and changes its status.
 */
public class GameCard extends JPanel {

    private static final int TEXT_GAP = 10;
    private static final int TITLE_HEIGHT = 20;
    private static final int ROW_GAP = 4;
    private static final int ROW_HEIGHT = 28;
    private static final int INFO_HEIGHT = TEXT_GAP + TITLE_HEIGHT + ROW_GAP + ROW_HEIGHT;

    private final Game game;
    private final Library library;
    private final CoverView cover;
    private final JLabel title;
    private final JLabel meta;
    private final JButton statusButton = new JButton();

    public GameCard(Game game, Library library, CoverLoader covers) {
        super(null); // laid out by hand in doLayout
        this.game = game;
        this.library = library;
        setOpaque(false);

        cover = new CoverView(covers, 3);
        cover.setGame(game);
        cover.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cover.setToolTipText(details(game));
        cover.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showStatusMenu(cover, e.getX(), e.getY());
            }
        });

        title = Theme.label(game.getName(), Theme.body(Font.BOLD, 14f), Theme.INK);
        title.setToolTipText(game.getName());
        meta = Theme.label(metaText(game), Theme.body(Font.PLAIN, 12f), Theme.TONE);

        statusButton.putClientProperty(FlatClientProperties.MINIMUM_HEIGHT, ROW_HEIGHT);
        statusButton.putClientProperty("JComponent.sizeVariant", "small");
        statusButton.addActionListener(e -> showStatusMenu(statusButton, 0, statusButton.getHeight()));

        add(cover);
        add(title);
        add(meta);
        add(statusButton);
        refresh();
    }

    /** Card height for a given width: a 16:9 cover plus the text below it. */
    public static int heightForWidth(int width) {
        return Math.round(width * 9 / 16f) + INFO_HEIGHT;
    }

    public Game getGame() {
        return game;
    }

    /** Updates the button after the library changes. */
    public void refresh() {
        Optional<LibraryEntry> entry = library.find(game);
        if (entry.isPresent()) {
            GameStatus status = entry.get().getStatus();
            statusButton.setText(status.toString());
            statusButton.setIcon(new StatusIcon(status, 10, null));
            Theme.stylePrimary(statusButton);
            statusButton.setToolTipText("In your library. Click to change its status.");
        } else {
            statusButton.setText("Add");
            statusButton.setIcon(null);
            Theme.styleOutline(statusButton);
            statusButton.setToolTipText("Add " + game.getName() + " to your library");
        }
        doLayout();
        repaint();
    }

    String getButtonText() {
        return statusButton.getText();
    }

    @Override
    public void doLayout() {
        int w = getWidth();
        int coverHeight = Math.round(w * 9 / 16f);
        cover.setBounds(0, 0, w, coverHeight);
        int y = coverHeight + TEXT_GAP;
        title.setBounds(0, y, w, TITLE_HEIGHT);
        y += TITLE_HEIGHT + ROW_GAP;
        Dimension button = statusButton.getPreferredSize();
        int buttonWidth = Math.min(button.width, w);
        statusButton.setBounds(w - buttonWidth, y, buttonWidth, ROW_HEIGHT);
        meta.setBounds(0, y, Math.max(0, w - buttonWidth - 8), ROW_HEIGHT);
    }

    /** The status menu. Package-private so tests can use it without showing it on screen. */
    JPopupMenu statusMenu() {
        JPopupMenu menu = new JPopupMenu();
        Optional<LibraryEntry> entry = library.find(game);
        JLabel heading = Theme.label(entry.isPresent() ? "Change status" : "Add to your library as",
                Theme.body(Font.PLAIN, 12f), Theme.TONE);
        heading.setBorder(BorderFactory.createEmptyBorder(6, 12, 4, 12));
        menu.add(heading);
        for (GameStatus status : GameStatus.values()) {
            JMenuItem item = new JMenuItem(status.toString(), new StatusIcon(status, 11, null));
            if (entry.isPresent() && entry.get().getStatus() == status) {
                item.setFont(item.getFont().deriveFont(Font.BOLD));
            }
            item.addActionListener(e -> choose(status));
            menu.add(item);
        }
        entry.ifPresent(existing -> {
            menu.addSeparator();
            JMenuItem remove = new JMenuItem("Remove from library");
            remove.addActionListener(e -> {
                if (existing.getReview().isBlank() || Dialogs.confirmRemove(this, existing)) {
                    library.remove(existing);
                }
            });
            menu.add(remove);
        });
        return menu;
    }

    private void showStatusMenu(Component invoker, int x, int y) {
        statusMenu().show(invoker, x, y);
    }

    private void choose(GameStatus status) {
        library.find(game).ifPresentOrElse(
                entry -> library.setStatus(entry, status),
                () -> library.add(game, status));
    }

    private static String metaText(Game game) {
        String year = game.getYear().isEmpty() ? "TBA" : game.getYear();
        return game.getRating() > 0
                ? year + "     ★ " + String.format(Locale.ROOT, "%.1f", game.getRating())
                : year;
    }

    private static String details(Game game) {
        StringBuilder text = new StringBuilder("<html><div style='width:260px'><b>")
                .append(Theme.html(game.getName())).append("</b>");
        if (!game.getGenres().isEmpty()) {
            text.append("<br>").append(Theme.html(game.getGenres()));
        }
        if (!game.getPlatforms().isEmpty()) {
            text.append("<br>").append(Theme.html(game.getPlatforms()));
        }
        return text.append("</div></html>").toString();
    }
}
