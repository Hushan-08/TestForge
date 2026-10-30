package com.testforge.ui.components;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

/**
 * Modern dark-themed styling configuration for TestForge.
 * Features ultra-sleek obsidian/navy backgrounds, electric-cyan accents, and crisp typography.
 */
public class UITheme {

    // Dark Color Palette (High-contrast, modern obsidian-navy)
    public static final Color BG_DARK = new Color(0x0A, 0x0F, 0x1D);       // Deep Obsidian Navy (#0A0F1D)
    public static final Color BG_CARD = new Color(0x13, 0x1C, 0x31);       // Sleek Navy Card (#131C31)
    public static final Color BG_SURFACE = new Color(0x1C, 0x28, 0x42);    // Elevated Surface (#1C2842)
    public static final Color BG_INPUT = new Color(0x0E, 0x16, 0x26);      // Dark Input Box (#0E1626)

    // Electric Blue & Neon Cyan Accents
    public static final Color PRIMARY_BLUE = new Color(0x25, 0x63, 0xEB);  // Electric Blue (#2563EB)
    public static final Color PRIMARY_HOVER = new Color(0x3B, 0x82, 0xF6); // Lighter Blue Hover (#3B82F6)
    public static final Color ACCENT_CYAN = new Color(0x00, 0xF0, 0xFF);   // Neon Cyan Glow (#00F0FF)
    public static final Color ACCENT_CYAN_MUTED = new Color(0x38, 0xBD, 0xF8); // Soft Cyan (#38BDF8)

    // Semantic Status Colors
    public static final Color SUCCESS = new Color(0x10, 0xB9, 0x81);       // Emerald Green (#10B981)
    public static final Color SUCCESS_HOVER = new Color(0x05, 0x96, 0x69); // Emerald Green Darker
    public static final Color DANGER = new Color(0xF4, 0x3F, 0x5E);        // Rose Red (#F43F5E)
    public static final Color WARNING = new Color(0xFB, 0xBF, 0x24);       // Amber Gold (#FBBF24)
    public static final Color BORDER = new Color(0x26, 0x35, 0x4D);        // Slate Border (#26354D)
    public static final Color BORDER_ACTIVE = new Color(0x00, 0xF0, 0xFF); // Active Cyan Border

    // Typography Colors
    public static final Color TEXT_PRIMARY = new Color(0xF8, 0xFA, 0xFC);  // Crisp Bright White (#F8FAFC)
    public static final Color TEXT_SECONDARY = new Color(0x94, 0xA3, 0xB8);// Slate Secondary (#94A3B8)
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);    // Dim Muted (#64748B)

    // Modern Typography
    public static final Font FONT_BRAND = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_TIMER = new Font("Segoe UI", Font.BOLD, 28);

    /**
     * Initializes FlatLaf Dark theme with customized high-DPI scaling and modern styling defaults.
     */
    public static void setupTheme() {
        try {
            FlatDarkLaf.setup();
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));

            // Custom UI colors override
            UIManager.put("Panel.background", BG_DARK);
            UIManager.put("OptionPane.background", BG_CARD);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
            UIManager.put("Label.foreground", TEXT_PRIMARY);
            UIManager.put("Table.background", BG_CARD);
            UIManager.put("Table.foreground", TEXT_PRIMARY);
            UIManager.put("Table.selectionBackground", PRIMARY_BLUE);
            UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
            UIManager.put("Table.gridColor", BORDER);
            UIManager.put("TableHeader.background", BG_SURFACE);
            UIManager.put("TableHeader.foreground", ACCENT_CYAN);
            UIManager.put("TableHeader.font", FONT_BOLD);

            // TabbedPane Styling
            UIManager.put("TabbedPane.selectedBackground", BG_CARD);
            UIManager.put("TabbedPane.background", BG_DARK);
            UIManager.put("TabbedPane.foreground", TEXT_SECONDARY);
            UIManager.put("TabbedPane.selectedForeground", ACCENT_CYAN);
            UIManager.put("TabbedPane.underlineColor", ACCENT_CYAN);

        } catch (Exception e) {
            System.err.println("Could not initialize FlatLaf theme, falling back to system defaults: " + e.getMessage());
        }
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        );
    }

    public static Border createFocusBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_ACTIVE, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        );
    }
}
