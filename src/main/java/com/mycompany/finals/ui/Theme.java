package com.mycompany.finals.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

/**
 * The look of the app, taken from the mascot logo: black ink on white paper,
 * with grey only for secondary text and screentone. Every cover sits in a black
 * frame like a manga panel.
 */
public final class Theme {

    public static final Color PAPER = Color.WHITE;
    public static final Color INK = Color.BLACK;
    /** Secondary text. */
    public static final Color TONE = new Color(0x5F5F5F);
    /** Dividers between rows. */
    public static final Color RULE = new Color(0xDADADA);
    /** Covers that are still loading, and hovered controls. */
    public static final Color WASH = new Color(0xF0F0F0);

    /** Thickness of the black frame around every cover. */
    public static final int FRAME = 2;
    /** Space on the left and right of every page. */
    public static final int GUTTER = 32;

    private static final String PRIMARY_STYLE =
            "background: #000000; foreground: #ffffff; hoverBackground: #333333; pressedBackground: #4d4d4d;"
            + " borderColor: #000000; hoverBorderColor: #000000; focusedBorderColor: #000000;";
    private static final String OUTLINE_STYLE =
            "background: #ffffff; foreground: #000000; hoverBackground: #f0f0f0; pressedBackground: #e2e2e2;"
            + " borderColor: #000000; hoverBorderColor: #000000; focusedBorderColor: #000000;";
    private static final String TAB_STYLE =
            "arc: 4; borderWidth: 0; focusWidth: 0; margin: 5,12,5,12;"
            + " background: #ffffff; hoverBackground: #f0f0f0; pressedBackground: #e2e2e2;"
            + " selectedBackground: #000000; selectedForeground: #ffffff;";

    private static String displayFace;

    private Theme() {
    }

    /** Sets up FlatLaf with the ink-on-paper colours. Call once, before any window is created. */
    public static void install() {
        FlatLaf.setGlobalExtraDefaults(Map.of(
                "@accentColor", "#000000",
                "@background", "#ffffff"));
        FlatLightLaf.setup();
        UIManager.put("Component.arc", 6);
        UIManager.put("Button.arc", 6);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("Component.borderColor", INK);
        UIManager.put("Component.focusedBorderColor", INK);
        UIManager.put("Button.borderColor", INK);
        UIManager.put("Button.hoverBorderColor", INK);
        UIManager.put("Button.focusedBorderColor", INK);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollPane.smoothScrolling", true);
        UIManager.put("ToolTip.background", INK);
        UIManager.put("ToolTip.foreground", PAPER);
        UIManager.put("PopupMenu.borderColor", INK);
    }

    /** The heavy condensed face used for titles, like a manga cover. */
    public static Font display(float size) {
        String face = displayFace();
        Font base = face != null ? new Font(face, Font.PLAIN, 12) : new Font(Font.SANS_SERIF, Font.BOLD, 12);
        return base.deriveFont(size);
    }

    /** The system font, used for everything that is read rather than glanced at. */
    public static Font body(int style, float size) {
        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        }
        return base.deriveFont(style, size);
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /** Solid black button, for the main action on a screen. */
    public static void stylePrimary(AbstractButton button) {
        button.putClientProperty(FlatClientProperties.STYLE, PRIMARY_STYLE);
    }

    /** Black outline on white, for other actions. */
    public static void styleOutline(AbstractButton button) {
        button.putClientProperty(FlatClientProperties.STYLE, OUTLINE_STYLE);
    }

    /** Grey text with no outline, for quiet actions such as Remove. */
    public static void styleQuiet(AbstractButton button) {
        button.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        button.setForeground(TONE);
    }

    /** A tab that reads as plain text and turns into a black block when selected. */
    public static void styleTab(AbstractButton button) {
        button.putClientProperty(FlatClientProperties.STYLE, TAB_STYLE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** A vertical scroll pane with no border and a sensible scroll speed. */
    public static JScrollPane scroll(JComponent view) {
        JScrollPane scroll = new JScrollPane(view,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(PAPER);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        return scroll;
    }

    /** Escapes text for use inside Swing's HTML labels. */
    static String html(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Futura Condensed ExtraBold on a Mac, Impact on Windows, otherwise bold sans-serif. */
    private static synchronized String displayFace() {
        if (displayFace == null) {
            Set<String> installed = Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
                    .map(Font::getFontName)
                    .collect(Collectors.toSet());
            displayFace = List.of("Futura-CondensedExtraBold", "Impact").stream()
                    .filter(installed::contains)
                    .findFirst()
                    .orElse("");
        }
        return displayFace.isEmpty() ? null : displayFace;
    }
}
