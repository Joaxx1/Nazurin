package com.mycompany.finals.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A centred message for screens with nothing to show, saying what to do next.
 */
public class EmptyState extends JPanel {

    private final JLabel title = Theme.label(" ", Theme.display(30f), Theme.INK);
    private final JLabel message = Theme.label(" ", Theme.body(Font.PLAIN, 14f), Theme.TONE);
    private final JButton action = new JButton();
    private Runnable onAction = () -> { };

    public EmptyState() {
        super(new GridBagLayout());
        setOpaque(false);
        Theme.styleOutline(action);
        action.addActionListener(e -> onAction.run());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        add(title, c);
        c.insets = new Insets(8, 0, 0, 0);
        add(message, c);
        c.insets = new Insets(20, 0, 0, 0);
        add(action, c);
    }

    /** Shows a title and message. Pass null as the action text to hide the button. */
    public void setMessage(String titleText, String messageText, String actionText, Runnable onAction) {
        title.setText(titleText);
        message.setText("<html><div style='text-align:center;width:360px'>"
                + Theme.html(messageText) + "</div></html>");
        action.setVisible(actionText != null);
        if (actionText != null) {
            action.setText(actionText);
            this.onAction = onAction;
        }
    }

    String getTitle() {
        return title.getText();
    }
}
