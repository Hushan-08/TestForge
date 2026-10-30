package com.testforge.ui.components;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Modern styled text input field with electric-blue focus outline.
 */
public class ModernTextField extends JTextField {

    private boolean isFocused = false;

    public ModernTextField(int columns) {
        super(columns);
        init();
    }

    public ModernTextField(String text) {
        super(text);
        init();
    }

    public ModernTextField() {
        super();
        init();
    }

    private void init() {
        setFont(UITheme.FONT_BODY);
        setForeground(UITheme.TEXT_PRIMARY);
        setBackground(UITheme.BG_INPUT);
        setCaretColor(UITheme.ACCENT_CYAN);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        setPreferredSize(new Dimension(220, 40));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(UITheme.BG_INPUT);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

        // Border outline
        g2.setColor(isFocused ? UITheme.ACCENT_CYAN : UITheme.BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

        g2.dispose();
        super.paintComponent(g);
    }
}
