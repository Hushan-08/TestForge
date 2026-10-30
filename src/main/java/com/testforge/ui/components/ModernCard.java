package com.testforge.ui.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/**
 * Reusable rounded dark navy card container with subtle border and top accent line support.
 */
public class ModernCard extends JPanel {

    private Color cardBackground = UITheme.BG_CARD;
    private Color borderColor = UITheme.BORDER;
    private Color topAccentColor = null;
    private int cornerRadius = 14;

    public ModernCard() {
        super();
        init();
    }

    public ModernCard(LayoutManager layout) {
        super(layout);
        init();
    }

    public ModernCard(Color background, Color border) {
        super();
        this.cardBackground = background;
        this.borderColor = border;
        init();
    }

    private void init() {
        setOpaque(false);
        setBorder(UITheme.createCardBorder());
    }

    public void setCardBackground(Color color) {
        this.cardBackground = color;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    public void setTopAccentColor(Color color) {
        this.topAccentColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background
        g2.setColor(cardBackground);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // Draw top accent strip if configured
        if (topAccentColor != null) {
            g2.setColor(topAccentColor);
            g2.fillRoundRect(1, 1, getWidth() - 2, 4, 4, 4);
        }

        // Draw subtle border
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
