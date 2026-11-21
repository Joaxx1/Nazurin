package com.mycompany.finals.ui;

import com.mycompany.finals.data.CoverLoader;
import com.mycompany.finals.model.Game;
import com.mycompany.finals.model.GameStatus;
import com.mycompany.finals.model.Library;
import com.mycompany.finals.model.LibraryEntry;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Game Roulette page: one button that picks a random game from the backlog, meaning the games
 * marked Plan to Play or Paused. Covers flicker past, slowing down, until the pick lands.
 */
public class RoulettePanel extends JPanel {

    /** Games not started yet, or put on hold. */
    private static final Set<GameStatus> BACKLOG = EnumSet.of(GameStatus.PLAN_TO_PLAY, GameStatus.PAUSED);
    private static final int SPIN_STEPS = 22;

    private final Library library;
    private final Random random = new Random();

    private final JPanel stage = new JPanel(new GridBagLayout());
    private final CoverView cover;
    private final JLabel resultTitle = Theme.label(" ", Theme.display(38f), Theme.INK);
    private final JLabel resultDetail = Theme.label(" ", Theme.body(Font.PLAIN, 14f), Theme.TONE);
    private final JButton spinButton = new JButton("Spin");
    private final JButton startButton = new JButton("Start playing");
    private final JButton browseButton = new JButton("Browse the catalog");

    private LibraryEntry winner;
    private boolean spinning;

    public RoulettePanel(Library library, CoverLoader covers, Runnable browseCatalog) {
        super(new BorderLayout(0, 18));
        this.library = library;
        setBackground(Theme.PAPER);
        setBorder(BorderFactory.createEmptyBorder(28, Theme.GUTTER, 24, Theme.GUTTER));

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints t = new GridBagConstraints();
        t.gridx = 0;
        t.weightx = 1;
        t.anchor = GridBagConstraints.WEST;
        top.add(Theme.label("Game Roulette", Theme.display(36f), Theme.INK), t);
        top.add(Theme.label("Picks a random game from your Plan to Play and Paused games.",
                Theme.body(Font.PLAIN, 14f), Theme.TONE), t);

        // The picked game, centred on the page.
        cover = new CoverView(covers, 4);
        cover.setPreferredSize(new Dimension(400, 225));
        resultTitle.setHorizontalAlignment(SwingConstants.CENTER);
        resultDetail.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel result = new JPanel(new GridBagLayout());
        result.setOpaque(false);
        GridBagConstraints r = new GridBagConstraints();
        r.gridx = 0;
        result.add(cover, r);
        r.insets = new Insets(16, 0, 0, 0);
        result.add(resultTitle, r);
        r.insets = new Insets(4, 0, 0, 0);
        result.add(resultDetail, r);
        stage.setOpaque(false);
        stage.add(result);

        Theme.stylePrimary(spinButton);
        spinButton.setFont(Theme.display(22f));
        spinButton.setMargin(new Insets(6, 48, 6, 48));
        for (JButton secondary : new JButton[] {startButton, browseButton}) {
            Theme.styleOutline(secondary);
            secondary.setFont(Theme.body(Font.BOLD, 14f));
            secondary.setMargin(new Insets(10, 20, 10, 20));
        }
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);
        buttons.add(spinButton);
        buttons.add(startButton);
        buttons.add(browseButton);

        add(top, BorderLayout.NORTH);
        add(stage, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        spinButton.addActionListener(e -> spin());
        startButton.addActionListener(e -> {
            if (winner != null) {
                library.setStatus(winner, GameStatus.PLAYING);
            }
        });
        browseButton.addActionListener(e -> browseCatalog.run());
        library.addChangeListener(() -> SwingUtilities.invokeLater(this::onLibraryChanged));

        refresh();
    }

    private List<LibraryEntry> backlog() {
        return library.withStatus(BACKLOG);
    }

    /** Enables Spin when there is something to pick, and shows the waiting message until a pick is made. */
    private void refresh() {
        if (spinning) {
            return;
        }
        spinButton.setEnabled(!backlog().isEmpty());
        if (winner == null) {
            showWaiting();
        }
    }

    private void showWaiting() {
        cover.setGame(null);
        resultDetail.setIcon(null);
        startButton.setVisible(false);
        int size = backlog().size();
        browseButton.setVisible(size == 0);
        if (library.getEntries().isEmpty()) {
            resultTitle.setText("Your library is empty");
            resultDetail.setText("Add games from the Catalog, then come back to spin.");
        } else if (size == 0) {
            resultTitle.setText("Nothing to pick from");
            resultDetail.setText("Mark games as Plan to Play or Paused and the roulette will pick from them.");
        } else {
            resultTitle.setText("Ready to spin");
            resultDetail.setText(size == 1 ? "1 game in your backlog." : size + " games in your backlog.");
        }
    }

    /** Flashes random games, slowing down, then lands on the winner. */
    void spin() {
        List<LibraryEntry> pool = backlog();
        if (pool.isEmpty() || spinning) {
            return;
        }
        spinning = true;
        winner = pool.get(random.nextInt(pool.size()));
        spinButton.setEnabled(false);
        startButton.setVisible(false);
        browseButton.setVisible(false);
        resultDetail.setIcon(null);
        resultDetail.setText(" ");

        int[] step = {0};
        LibraryEntry[] lastShown = {null};
        Timer timer = new Timer(0, null);
        timer.setRepeats(false);
        timer.addActionListener(e -> {
            step[0]++;
            if (step[0] >= SPIN_STEPS) {
                reveal();
                return;
            }
            LibraryEntry next = pool.get(random.nextInt(pool.size()));
            while (pool.size() > 1 && next == lastShown[0]) {
                next = pool.get(random.nextInt(pool.size()));
            }
            lastShown[0] = next;
            showGame(next.getGame());
            double progress = step[0] / (double) SPIN_STEPS;
            timer.setInitialDelay((int) (45 + 330 * progress * progress * progress));
            timer.start();
        });
        timer.start();
    }

    private void reveal() {
        spinning = false;
        showGame(winner.getGame());
        showWinnerDetails();
        refresh();
    }

    private void showGame(Game game) {
        cover.setGame(game);
        resultTitle.setText(game.getName());
    }

    private void showWinnerDetails() {
        GameStatus status = winner.getStatus();
        String genres = winner.getGame().getGenres();
        resultDetail.setIcon(new StatusIcon(status, 12, Theme.TONE));
        resultDetail.setIconTextGap(7);
        resultDetail.setText(genres.isEmpty() ? status.toString() : status + "      " + genres);
        startButton.setVisible(status != GameStatus.PLAYING);
    }

    private void onLibraryChanged() {
        if (winner != null && !spinning) {
            if (library.getEntries().contains(winner)) {
                showWinnerDetails();
            } else {
                winner = null; // the picked game was removed
            }
        }
        refresh();
    }

    LibraryEntry getWinner() {
        return winner;
    }

    boolean isSpinning() {
        return spinning;
    }

    JButton getSpinButton() {
        return spinButton;
    }

    JButton getStartButton() {
        return startButton;
    }

    String getResultTitle() {
        return resultTitle.getText();
    }
}
