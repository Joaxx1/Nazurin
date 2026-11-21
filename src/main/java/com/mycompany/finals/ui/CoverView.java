package com.mycompany.finals.ui;

import com.mycompany.finals.data.CoverLoader;
import com.mycompany.finals.model.Game;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * A game's cover in a black frame, like a manga panel. Games without a picture get a
 * screentone placeholder with their initials.
 *
 * The picture is requested the first time the cover is drawn, and the view keeps no copy of
 * its own: it asks the loader's cache each time. So a long grid only uses memory for the covers
 * that were recently on screen.
 */
public class CoverView extends JComponent {

    private static final int DOT_SPACING = 7;

    private final CoverLoader covers;
    private final int arc;
    private Game game;
    private String url = "";
    private boolean requested; // a load is on its way
    private boolean failed;    // there is no usable picture, so show the placeholder

    public CoverView(CoverLoader covers, int arc) {
        this.covers = covers;
        this.arc = arc;
        setOpaque(false);
    }

    /** Shows this game's cover. Null shows a question mark, meaning "not picked yet". */
    public void setGame(Game game) {
        this.game = game;
        this.url = game == null ? "" : game.getImageUrl();
        requested = false;
        failed = false;
        repaint();
    }

    public Game getGame() {
        return game;
    }

    /** True when the real picture (not a placeholder) is ready to draw. */
    public boolean hasPicture() {
        return covers.cached(url) != null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth();
            int h = getHeight();
            int frame = Theme.FRAME;

            g2.setColor(Theme.INK);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc * 2f, arc * 2f));

            int innerW = w - 2 * frame;
            int innerH = h - 2 * frame;
            float innerArc = Math.max(0, arc - frame) * 2f;
            g2.clip(new RoundRectangle2D.Float(frame, frame, innerW, innerH, innerArc, innerArc));
            BufferedImage image = covers.cached(url);
            if (image != null) {
                drawPicture(g2, image, frame, frame, innerW, innerH);
            } else if (!url.isEmpty() && !failed) {
                g2.setColor(Theme.WASH);
                g2.fillRect(frame, frame, innerW, innerH);
                requestPicture();
            } else {
                drawPlaceholder(g2, frame, frame, innerW, innerH);
            }
        } finally {
            g2.dispose();
        }
    }

    private void requestPicture() {
        if (requested) {
            return;
        }
        requested = true;
        String wanted = url;
        covers.load(wanted).thenAcceptAsync(loaded -> {
            if (!wanted.equals(url)) {
                return; // this view shows another game now
            }
            requested = false;
            failed = loaded == null;
            repaint();
        }, SwingUtilities::invokeLater);
    }

    /** Scales the picture to fill the frame, cropping whatever sticks out. */
    private static void drawPicture(Graphics2D g2, BufferedImage image, int x, int y, int w, int h) {
        double scale = Math.max(w / (double) image.getWidth(), h / (double) image.getHeight());
        int drawW = (int) Math.ceil(image.getWidth() * scale);
        int drawH = (int) Math.ceil(image.getHeight() * scale);
        g2.drawImage(image, x + (w - drawW) / 2, y + (h - drawH) / 2, drawW, drawH, null);
    }

    /** Screentone dots that grow toward the bottom-right corner, with the game's initials on top. */
    private void drawPlaceholder(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(Theme.PAPER);
        g2.fillRect(x, y, w, h);
        g2.setColor(Theme.INK);
        float maxRadius = DOT_SPACING * 0.48f;
        for (int dy = DOT_SPACING / 2; dy < h; dy += DOT_SPACING) {
            for (int dx = DOT_SPACING / 2; dx < w; dx += DOT_SPACING) {
                double progress = (dx / (double) w + dy / (double) h) / 2;
                float radius = (float) (maxRadius * Math.pow(progress, 1.6));
                if (radius >= 0.35f) {
                    g2.fill(new Ellipse2D.Float(x + dx - radius, y + dy - radius, radius * 2, radius * 2));
                }
            }
        }

        String text = game == null ? "?" : initials(game.getName());
        if (text.isEmpty()) {
            return;
        }
        Font font = Theme.display(Math.max(14f, Math.min(h * 0.42f, w * 0.3f)));
        TextLayout layout = new TextLayout(text, font, g2.getFontRenderContext());
        Rectangle2D bounds = layout.getBounds();
        float pad = Math.max(8f, h * 0.09f);
        layout.draw(g2, (float) (x + pad - bounds.getX()), (float) (y + pad - bounds.getY()));
    }

    /** First letter of the first two words, e.g. "The Witcher 3" becomes "TW". */
    static String initials(String name) {
        StringBuilder out = new StringBuilder();
        for (String word : name.split("[^\\p{L}\\p{N}]+")) {
            if (!word.isEmpty()) {
                out.appendCodePoint(word.codePointAt(0));
            }
            if (out.codePointCount(0, out.length()) == 2) {
                break;
            }
        }
        return out.toString().toUpperCase(Locale.ROOT);
    }
}
