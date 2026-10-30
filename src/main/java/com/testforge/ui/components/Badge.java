package com.testforge.ui.components;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Modern status pill badge (e.g. Published, Draft, Passed, Failed).
 */
public class Badge extends JLabel {

    private Color bgColor;
    private Color textColor;

    public Badge(String text, Color bgColor, Color textColor) {
        super(text, CENTER);
        this.bgColor = bgColor;
        this.textColor = textColor;

        setFont(UITheme.FONT_SMALL);
        setForeground(textColor);
        setOpaque(false);
        setPreferredSize(new Dimension(85, 24));
    }

    public static Badge success(String text) {
        return new Badge(text, new Color(0x10, 0xB9, 0x81, 40), UITheme.SUCCESS);
    }

    public static Badge warning(String text) {
        return new Badge(text, new Color(0xF5, 0x9E, 0x0B, 40), UITheme.WARNING);
    }

    public static Badge danger(String text) {
        return new Badge(text, new Color(0xEF, 0x44, 0x44, 40), UITheme.DANGER);
    }

    public static Badge info(String text) {
        return new Badge(text, new Color(0x38, 0xBD, 0xF8, 40), UITheme.ACCENT_CYAN);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background pill
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

        // Draw subtle border
        g2.setColor(textColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

        g2.dispose();
        super.paintComponent(g);
    }
}
