package com.mycompany.finals.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * The banner across the top of the window: the mascot standing on a thick ink rule,
 * the Nazurin wordmark, and the page navigation.
 */
public class HeaderPanel extends JPanel {

    /** Stored at twice its on-screen size so it stays sharp on Retina screens. */
    private static final String LOGO = "/nazurin-logo.png";

    private final ButtonGroup navGroup = new ButtonGroup();
    private final JPanel nav = new JPanel();

    public HeaderPanel() {
        super(new BorderLayout(16, 0));
        setBackground(Theme.PAPER);
        // No padding on the left or bottom: the mascot is cropped there, so she stands in the corner.
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.INK),
                BorderFactory.createEmptyBorder(8, 0, 0, Theme.GUTTER)));

        Icon logo = loadLogo();
        if (logo != null) {
            add(new JLabel(logo), BorderLayout.WEST);
        }

        JPanel brand = new JPanel(new GridBagLayout());
        brand.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        brand.add(Theme.label("NAZURIN", Theme.display(40f), Theme.INK), c);
        brand.add(Theme.label("Track your backlog and pick what to play next.",
                Theme.body(Font.PLAIN, 13f), Theme.TONE), c);
        add(brand, BorderLayout.CENTER);

        nav.setLayout(new BoxLayout(nav, BoxLayout.X_AXIS));
        nav.setOpaque(false);
        JPanel navColumn = new JPanel(new GridBagLayout()); // centres the nav vertically
        navColumn.setOpaque(false);
        navColumn.add(nav);
        add(navColumn, BorderLayout.EAST);
    }

    /** Adds a page to the navigation. The first one added starts selected. */
    public JToggleButton addNavButton(String text, Runnable onSelect) {
        JToggleButton button = new JToggleButton(text);
        Theme.styleTab(button);
        button.setFont(Theme.body(Font.BOLD, 14f));
        button.addActionListener(e -> onSelect.run());
        if (nav.getComponentCount() > 0) {
            nav.add(Box.createHorizontalStrut(4));
        }
        navGroup.add(button);
        nav.add(button);
        if (navGroup.getButtonCount() == 1) {
            button.setSelected(true);
        }
        return button;
    }

    /** Loads the logo plus a half-size copy for normal screens. Returns null if the image is missing. */
    private static Icon loadLogo() {
        URL url = HeaderPanel.class.getResource(LOGO);
        if (url == null) {
            return null;
        }
        try {
            BufferedImage retina = ImageIO.read(url);
            if (retina == null) {
                return null;
            }
            BufferedImage normal = new BufferedImage(retina.getWidth() / 2, retina.getHeight() / 2,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = normal.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(retina, 0, 0, normal.getWidth(), normal.getHeight(), null);
            g.dispose();
            // Swing picks whichever copy matches the screen's pixel density.
            return new ImageIcon(new BaseMultiResolutionImage(normal, retina));
        } catch (IOException e) {
            System.err.println("Could not load the logo: " + e.getMessage());
            return null;
        }
    }
}
