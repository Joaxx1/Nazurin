package com.mycompany.finals.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.mycompany.finals.data.CoverLoader;
import com.mycompany.finals.data.GameCatalog;
import com.mycompany.finals.data.GamePage;
import com.mycompany.finals.model.Game;
import com.mycompany.finals.model.Library;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Catalog page: search RAWG and browse the results as a grid of covers. More games load
 * as the user scrolls toward the bottom. Each card can add its game to the library.
 */
public class CatalogPanel extends JPanel {

    private static final String GRID = "grid";
    private static final String MESSAGE = "message";
    /** Start loading the next page when the bottom of the grid is this close (in pixels). */
    private static final int LOAD_AHEAD = 600;

    private final GameCatalog catalog;
    private final Library library;
    private final CoverLoader covers;

    private final JTextField searchField = new JTextField();
    private final JLabel heading = Theme.label("Popular right now", Theme.display(32f), Theme.INK);
    private final JLabel statusLabel = Theme.label(" ", Theme.body(Font.PLAIN, 13f), Theme.TONE);
    private final JPanel grid = new JPanel(new CardGridLayout(210, 22, 28, GameCard::heightForWidth));
    private final JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    private final JLabel footerLabel = Theme.label(" ", Theme.body(Font.PLAIN, 13f), Theme.TONE);
    private final JButton moreButton = new JButton("Show more games");
    private final JScrollPane gridScroll;
    private final EmptyState message = new EmptyState();
    private final CardLayout bodyLayout = new CardLayout();
    private final JPanel body = new JPanel(bodyLayout);
    private final List<GameCard> cards = new ArrayList<>();
    private final Set<Integer> shownIds = new HashSet<>();
    /** Searches shortly after the user stops typing, instead of on every key. */
    private final Timer typingPause = new Timer(450, e -> runSearch());

    private int latestSearch;     // results from older searches that finish late are ignored
    private boolean searching;    // a new search is waiting for its first page
    private boolean loadingMore;  // a further page is on its way
    private String currentQuery = "";
    private int nextPage = 1;
    private int total;
    private boolean hasMore;

    public CatalogPanel(GameCatalog catalog, Library library, CoverLoader covers) {
        super(new BorderLayout(0, 18));
        this.catalog = catalog;
        this.library = library;
        this.covers = covers;
        setBackground(Theme.PAPER);
        setBorder(BorderFactory.createEmptyBorder(24, Theme.GUTTER, 0, Theme.GUTTER));

        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search games");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchField.putClientProperty(FlatClientProperties.STYLE,
                "borderWidth: 2; focusWidth: 0; innerFocusWidth: 0; arc: 4; margin: 7,10,7,10;");
        searchField.setFont(Theme.body(Font.PLAIN, 15f));
        searchField.getAccessibleContext().setAccessibleName("Search games");

        JPanel headingRow = new JPanel(new BorderLayout());
        headingRow.setOpaque(false);
        headingRow.add(heading, BorderLayout.WEST);
        headingRow.add(statusLabel, BorderLayout.EAST);
        JPanel top = new JPanel(new BorderLayout(0, 22));
        top.setOpaque(false);
        top.add(searchField, BorderLayout.NORTH);
        top.add(headingRow, BorderLayout.SOUTH);

        // The grid, with a footer below it for "Show more games" and loading messages.
        grid.setOpaque(false);
        Theme.styleOutline(moreButton);
        moreButton.addActionListener(e -> loadMore());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(28, 0, 0, 0));
        footer.add(footerLabel);
        footer.add(moreButton);
        ScrollablePanel content = new ScrollablePanel();
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(2, 0, 28, 0));
        content.add(grid, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        gridScroll = Theme.scroll(content);
        gridScroll.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (nearBottom()) {
                loadMore();
            }
        });

        body.setOpaque(false);
        body.add(gridScroll, GRID);
        body.add(message, MESSAGE);

        add(top, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        typingPause.setRepeats(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                typingPause.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                typingPause.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                typingPause.restart();
            }
        });
        searchField.addActionListener(e -> {
            typingPause.stop();
            runSearch();
        });
        library.addChangeListener(() -> cards.forEach(GameCard::refresh));

        updateFooter(false);
        runSearch(); // show popular games on start
    }

    public void focusSearch() {
        searchField.requestFocusInWindow();
        searchField.selectAll();
    }

    /** Starts a new search and shows its first page. */
    void runSearch() {
        String query = searchField.getText().strip();
        latestSearch++;
        searching = true;
        loadingMore = false;
        statusLabel.setText("Searching…");
        fetch(query, 1, latestSearch);
    }

    /** Loads the next page of the current search, if there is one and nothing is already loading. */
    void loadMore() {
        if (!hasMore || loadingMore || searching || cards.isEmpty()) {
            return;
        }
        loadingMore = true;
        updateFooter(false);
        fetch(currentQuery, nextPage, latestSearch);
    }

    private void fetch(String query, int page, int searchNumber) {
        new SwingWorker<GamePage, Void>() {
            @Override
            protected GamePage doInBackground() throws Exception {
                return catalog.search(query, page);
            }

            @Override
            protected void done() {
                if (searchNumber != latestSearch) {
                    return; // a newer search replaced this one
                }
                try {
                    if (page == 1) {
                        showFirstPage(query, get());
                    } else {
                        appendPage(get());
                    }
                } catch (ExecutionException e) {
                    if (page == 1) {
                        showError(e.getCause().getMessage());
                    } else {
                        loadingMore = false;
                        updateFooter(true);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void showFirstPage(String query, GamePage page) {
        searching = false;
        currentQuery = query;
        heading.setText(query.isEmpty() ? "Popular right now" : "Results for “" + query + "”");
        grid.removeAll();
        cards.clear();
        shownIds.clear();
        nextPage = 2;
        total = page.total();
        hasMore = page.hasMore() && !page.games().isEmpty();

        if (page.games().isEmpty()) {
            statusLabel.setText("0 games from " + catalog.getName());
            message.setMessage("No games found",
                    "Nothing in the catalog matches “" + query + "”. Check the spelling or try a shorter name.",
                    null, null);
            bodyLayout.show(body, MESSAGE);
            return;
        }
        addCards(page.games());
        gridScroll.getVerticalScrollBar().setValue(0);
        bodyLayout.show(body, GRID);
        updateStatus();
        updateFooter(false);
    }

    private void appendPage(GamePage page) {
        loadingMore = false;
        nextPage++;
        total = page.total();
        hasMore = page.hasMore() && !page.games().isEmpty();
        addCards(page.games());
        updateStatus();
        updateFooter(false);
    }

    /** Adds a card per game, skipping games already shown (RAWG's order can shift between pages). */
    private void addCards(List<Game> games) {
        for (Game game : games) {
            if (shownIds.add(game.getId())) {
                GameCard card = new GameCard(game, library, covers);
                cards.add(card);
                grid.add(card);
            }
        }
        grid.revalidate();
        grid.repaint();
    }

    private void showError(String reason) {
        searching = false;
        statusLabel.setText(" ");
        message.setMessage("Couldn't load games",
                reason + " Check your internet connection, then try again.",
                "Try again", this::runSearch);
        bodyLayout.show(body, MESSAGE);
    }

    private void updateStatus() {
        NumberFormat number = NumberFormat.getIntegerInstance();
        String count = hasMore
                ? number.format(cards.size()) + " of " + number.format(total) + " games"
                : (cards.size() == 1 ? "1 game" : number.format(cards.size()) + " games");
        statusLabel.setText(count + " from " + catalog.getName());
    }

    private void updateFooter(boolean failed) {
        if (failed) {
            footerLabel.setText("Couldn't load more games.");
            moreButton.setText("Try again");
        } else if (loadingMore) {
            footerLabel.setText("Loading more games…");
        } else {
            moreButton.setText("Show more games");
        }
        footerLabel.setVisible(failed || loadingMore);
        moreButton.setVisible(failed || (hasMore && !loadingMore));
        footer.setVisible(failed || loadingMore || hasMore);
        footer.revalidate();
    }

    private boolean nearBottom() {
        JScrollBar bar = gridScroll.getVerticalScrollBar();
        return bar.getValue() + bar.getVisibleAmount() >= bar.getMaximum() - LOAD_AHEAD;
    }

    List<GameCard> getCards() {
        return List.copyOf(cards);
    }

    JTextField getSearchField() {
        return searchField;
    }

    EmptyState getMessage() {
        return message;
    }

    JButton getMoreButton() {
        return moreButton;
    }

    String getStatusText() {
        return statusLabel.getText();
    }

    boolean isBusy() {
        return searching || loadingMore;
    }

    JScrollPane getScroll() {
        return gridScroll;
    }
}
