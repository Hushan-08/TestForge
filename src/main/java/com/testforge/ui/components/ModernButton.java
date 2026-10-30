package com.testforge.ui.components;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern rounded button with smooth hover effects, accent glow border, and distinct styling.
 */
public class ModernButton extends JButton {

    private Color normalColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color borderColor = null;
    private boolean isHovered = false;
    private int cornerRadius = 10;

    public ModernButton(String text) {
        this(text, UITheme.PRIMARY_BLUE, UITheme.PRIMARY_HOVER);
    }

    public ModernButton(String text, Color baseColor, Color hoverColor) {
        super(text);
        this.normalColor = baseColor;
        this.hoverColor = hoverColor;
        this.pressedColor = baseColor.darker();

        setFont(UITheme.FONT_BOLD);
        setForeground(UITheme.TEXT_PRIMARY);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(140, 40));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public static ModernButton createPrimary(String text) {
        ModernButton btn = new ModernButton(text, UITheme.PRIMARY_BLUE, UITheme.PRIMARY_HOVER);
        btn.borderColor = UITheme.ACCENT_CYAN;
        return btn;
    }

    public static ModernButton createSecondary(String text) {
        ModernButton btn = new ModernButton(text, UITheme.BG_SURFACE, new Color(0x27, 0x37, 0x59));
        btn.setForeground(UITheme.TEXT_PRIMARY);
        btn.borderColor = UITheme.BORDER;
        return btn;
    }

    public static ModernButton createSuccess(String text) {
        ModernButton btn = new ModernButton(text, UITheme.SUCCESS, UITheme.SUCCESS_HOVER);
        btn.borderColor = new Color(0x34, 0xD3, 0x99);
        return btn;
    }

    public static ModernButton createDanger(String text) {
        ModernButton btn = new ModernButton(text, UITheme.DANGER, new Color(0xE1, 0x1D, 0x48));
        btn.borderColor = new Color(0xFB, 0x71, 0x85);
        return btn;
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background color calculation
        if (!isEnabled()) {
            g2.setColor(new Color(0x1F, 0x29, 0x37));
        } else if (getModel().isPressed()) {
            g2.setColor(pressedColor);
        } else if (isHovered) {
            g2.setColor(hoverColor);
        } else {
            g2.setColor(normalColor);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Border glow / outline
        if (borderColor != null && isEnabled()) {
            g2.setColor(isHovered ? UITheme.ACCENT_CYAN : borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
