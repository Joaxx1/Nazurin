package com.mycompany.finals.ui;

import com.mycompany.finals.model.GameStatus;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/**
 * A small ink glyph for each status, borrowed from media controls so it reads without colour:
 * ○ plan to play, ▶ playing, ✓ played, ‖ paused, ✕ dropped.
 */
public class StatusIcon implements Icon {

    private final GameStatus status;
    private final int size;
    private final Color color;

    /** Pass null as the colour to use the text colour of whatever shows the icon. */
    public StatusIcon(GameStatus status, int size, Color color) {
        this.status = status;
        this.size = size;
        this.color = color;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(color != null ? color : c.getForeground());
            float s = size;
            g2.setStroke(new BasicStroke(Math.max(1.4f, s / 7f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (status) {
                case PLAN_TO_PLAY -> g2.draw(new Ellipse2D.Float(s * 0.12f, s * 0.12f, s * 0.76f, s * 0.76f));
                case PLAYING -> {
                    Path2D play = new Path2D.Float();
                    play.moveTo(s * 0.2f, s * 0.08f);
                    play.lineTo(s * 0.92f, s * 0.5f);
                    play.lineTo(s * 0.2f, s * 0.92f);
                    play.closePath();
                    g2.fill(play);
                }
                case PLAYED -> {
                    Path2D check = new Path2D.Float();
                    check.moveTo(s * 0.1f, s * 0.55f);
                    check.lineTo(s * 0.4f, s * 0.84f);
                    check.lineTo(s * 0.92f, s * 0.18f);
                    g2.draw(check);
                }
                case PAUSED -> {
                    float barWidth = s * 0.24f;
                    g2.fill(new RoundRectangle2D.Float(s * 0.16f, s * 0.1f, barWidth, s * 0.8f, 2, 2));
                    g2.fill(new RoundRectangle2D.Float(s * 0.6f, s * 0.1f, barWidth, s * 0.8f, 2, 2));
                }
                case DROPPED -> {
                    Path2D cross = new Path2D.Float();
                    cross.moveTo(s * 0.16f, s * 0.16f);
                    cross.lineTo(s * 0.84f, s * 0.84f);
                    cross.moveTo(s * 0.84f, s * 0.16f);
                    cross.lineTo(s * 0.16f, s * 0.84f);
                    g2.draw(cross);
                }
            }
        } finally {
            g2.dispose();
        }
    }
}
