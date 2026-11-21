package com.mycompany.finals.ui;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * The user's review, drawn as a manga speech bubble pointing at the cover.
 * With no review yet it shows a dashed bubble asking for one. Clicking it opens the editor.
 */
public class SpeechBubble extends JButton {

    private static final int TAIL = 10;
    private static final int MAX_TEXT_WIDTH = 340;
    private static final int SHOWN_CHARACTERS = 180;

    private boolean empty = true;

    public SpeechBubble() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(BorderFactory.createEmptyBorder(8, TAIL + 12, 8, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setReview("");
    }

    public void setReview(String review) {
        empty = review.isBlank();
        String shown = empty ? "Write a review" : shorten(review.strip());
        setFont(Theme.body(empty ? Font.ITALIC : Font.PLAIN, 13f));
        setForeground(empty ? Theme.TONE : Theme.INK);

        String body = Theme.html(shown).replace("\n", "<br>");
        FontMetrics metrics = getFontMetrics(getFont());
        boolean wraps = shown.contains("\n") || metrics.stringWidth(shown) > MAX_TEXT_WIDTH;
        setText(wraps
                ? "<html><div style='width:" + MAX_TEXT_WIDTH + "px'>" + body + "</div></html>"
                : "<html>" + body + "</html>");
        setToolTipText(empty ? "Write a short review" : "Edit your review");
        getAccessibleContext().setAccessibleName(empty ? "Write a review" : "Your review: " + review);
    }

    public boolean isEmptyReview() {
        return empty;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            float w = getWidth();
            float h = getHeight();
            Area bubble = new Area(new RoundRectangle2D.Float(TAIL, 1, w - TAIL - 1.5f, h - 2, 20, 20));
            Path2D tail = new Path2D.Float();
            tail.moveTo(TAIL + 2, 13);
            tail.lineTo(1, 25);
            tail.lineTo(TAIL + 2, 21);
            tail.closePath();
            bubble.add(new Area(tail));

            g2.setColor(Theme.PAPER);
            g2.fill(bubble);
            boolean active = getModel().isRollover() || isFocusOwner();
            g2.setColor(empty && !active ? Theme.TONE : Theme.INK);
            g2.setStroke(empty
                    ? new BasicStroke(active ? 1.6f : 1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                            10f, new float[] {4f, 3f}, 0f)
                    : new BasicStroke(active ? 2.2f : 1.4f));
            g2.draw(bubble);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    private static String shorten(String text) {
        return text.length() <= SHOWN_CHARACTERS ? text : text.substring(0, SHOWN_CHARACTERS).strip() + "…";
    }
}
