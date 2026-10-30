package com.testforge.ui.components;

import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern rounded pill input field with embedded vector icon,
 * in-field placeholder text comfortably spaced after the icon,
 * and optional trailing password show/hide eye toggle.
 */
public class PillInputField extends JPanel {

    public enum IconType {
        EMAIL,
        LOCK,
        USER
    }

    private final IconType iconType;
    private final boolean isPassword;
    private final String placeholder;

    private final JTextField innerTextField;
    private boolean isFocused = false;
    private boolean isPasswordVisible = false;
    private boolean isEyeHovered = false;

    private static final int HEIGHT = 46;
    private static final int CORNER_RADIUS = 14;
    private static final int TEXT_START_X = 48; // Generous space: Icon ends at ~32px, text starts at 48px

    public PillInputField(IconType iconType, String placeholder, boolean isPassword) {
        this.iconType = iconType;
        this.placeholder = placeholder;
        this.isPassword = isPassword;

        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(360, HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));

        int rightPad = isPassword ? 46 : 16;

        if (isPassword) {
            JPasswordField pwd = new JPasswordField() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Draw placeholder when field is empty
                    if (getPassword().length == 0) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        g2.setColor(hasFocus() ? new Color(0x64, 0x74, 0x8B) : new Color(0x94, 0xA3, 0xB8));
                        g2.setFont(getFont());
                        int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
                        // Position placeholder text strictly at TEXT_START_X (after icon)
                        g2.drawString(placeholder, TEXT_START_X, y);
                        g2.dispose();
                    }
                }
            };
            pwd.setEchoChar('•');
            this.innerTextField = pwd;
        } else {
            JTextField txt = new JTextField() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Draw placeholder when field is empty
                    if (getText().isEmpty()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        g2.setColor(hasFocus() ? new Color(0x64, 0x74, 0x8B) : new Color(0x94, 0xA3, 0xB8));
                        g2.setFont(getFont());
                        int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
                        // Position placeholder text strictly at TEXT_START_X (after icon)
                        g2.drawString(placeholder, TEXT_START_X, y);
                        g2.dispose();
                    }
                }
            };
            this.innerTextField = txt;
        }

        // Configure inner text field
        innerTextField.setFont(UITheme.FONT_BODY);
        innerTextField.setForeground(UITheme.TEXT_PRIMARY);
        innerTextField.setCaretColor(UITheme.ACCENT_CYAN);
        innerTextField.setOpaque(false);
        // Set padding so typed text and caret also start strictly at TEXT_START_X
        innerTextField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, TEXT_START_X, 0, rightPad));

        // Focus handling
        innerTextField.addFocusListener(new FocusAdapter() {
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

        // Click on panel focuses text field or toggles eye icon
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isPassword && isOverEyeIcon(e.getX(), e.getY())) {
                    togglePasswordVisibility();
                } else {
                    innerTextField.requestFocusInWindow();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (isPassword) {
                    boolean over = isOverEyeIcon(e.getX(), e.getY());
                    if (over != isEyeHovered) {
                        isEyeHovered = over;
                        setCursor(over ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.TEXT_CURSOR));
                        repaint();
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEyeHovered) {
                    isEyeHovered = false;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (isPassword) {
                    boolean over = isOverEyeIcon(e.getX(), e.getY());
                    if (over != isEyeHovered) {
                        isEyeHovered = over;
                        setCursor(over ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.TEXT_CURSOR));
                        repaint();
                    }
                }
            }
        });

        add(innerTextField, BorderLayout.CENTER);
    }

    private boolean isOverEyeIcon(int x, int y) {
        int w = getWidth();
        int eyeX = w - 34;
        return x >= eyeX - 6 && x <= eyeX + 22 && y >= 10 && y <= HEIGHT - 10;
    }

    private void togglePasswordVisibility() {
        if (!isPassword) return;
        isPasswordVisible = !isPasswordVisible;
        JPasswordField pwd = (JPasswordField) innerTextField;
        if (isPasswordVisible) {
            pwd.setEchoChar((char) 0);
        } else {
            pwd.setEchoChar('•');
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Background Pill Fill (Slightly elevated dark slate/navy)
        Color bg = isFocused ? new Color(0x15, 0x21, 0x38) : new Color(0x11, 0x1A, 0x2C);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w - 1, h - 1, CORNER_RADIUS, CORNER_RADIUS);

        // 2. Border Stroke (Accent Cyan when focused, subtle slate otherwise)
        if (isFocused) {
            g2.setColor(UITheme.ACCENT_CYAN);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, CORNER_RADIUS, CORNER_RADIUS);
        } else {
            g2.setColor(new Color(0x2A, 0x3A, 0x54));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, CORNER_RADIUS, CORNER_RADIUS);
        }

        // 3. Draw Leading Vector Icon (at x = 16, width <= 18)
        drawLeadingIcon(g2, h);

        // 4. Draw Trailing Eye Icon (if password)
        if (isPassword) {
            drawEyeIcon(g2, w, h);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    /**
     * Vector drawing for Email, Lock, or User icons with crisp anti-aliased geometry.
     */
    private void drawLeadingIcon(Graphics2D g2, int h) {
        Color iconColor = isFocused ? UITheme.ACCENT_CYAN : new Color(0x94, 0xA3, 0xB8);
        g2.setColor(iconColor);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (iconType) {
            case EMAIL -> {
                // Envelope icon (18 x 13, located at x = 15, ending at x = 33)
                int x = 15;
                int y = (h - 13) / 2;
                g2.drawRoundRect(x, y, 18, 13, 3, 3);
                g2.drawLine(x, y, x + 9, y + 6);
                g2.drawLine(x + 18, y, x + 9, y + 6);
            }
            case LOCK -> {
                // Padlock icon (16 x 17, located at x = 16, ending at x = 32)
                int x = 16;
                int y = (h - 17) / 2;
                // Shackle loop
                g2.drawArc(x + 3, y, 10, 10, 0, 180);
                // Body
                g2.fillRoundRect(x, y + 6, 16, 11, 3, 3);
                // Keyhole
                g2.setColor(UITheme.BG_DARK);
                g2.fillOval(x + 7, y + 9, 3, 3);
                g2.fillRect(x + 7, y + 10, 3, 3);
            }
            case USER -> {
                // User head & shoulders (16 x 17, located at x = 16, ending at x = 32)
                int x = 16;
                int y = (h - 17) / 2;
                g2.drawOval(x + 4, y, 8, 8);
                g2.drawArc(x + 1, y + 10, 14, 8, 0, 180);
            }
        }
    }

    /**
     * Vector drawing for Password Show/Hide Eye toggle.
     */
    private void drawEyeIcon(Graphics2D g2, int w, int h) {
        Color eyeColor = isEyeHovered ? UITheme.ACCENT_CYAN : new Color(0x94, 0xA3, 0xB8);
        g2.setColor(eyeColor);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int x = w - 30;
        int y = (h - 12) / 2;

        // Eye contour (top and bottom curves)
        g2.drawArc(x - 2, y - 2, 18, 15, 25, 130);
        g2.drawArc(x - 2, y - 1, 18, 15, 205, 130);

        // Pupil
        g2.fillOval(x + 5, y + 3, 5, 5);

        // If password is NOT visible (masked), draw diagonal slash across eye
        if (!isPasswordVisible) {
            g2.setColor(isEyeHovered ? UITheme.ACCENT_CYAN : new Color(0x64, 0x74, 0x8B));
            g2.drawLine(x - 1, y + 12, x + 15, y - 1);
        }
    }

    public String getText() {
        return innerTextField.getText();
    }

    public String getPassword() {
        if (isPassword && innerTextField instanceof JPasswordField) {
            return new String(((JPasswordField) innerTextField).getPassword());
        }
        return innerTextField.getText();
    }

    public void setText(String text) {
        innerTextField.setText(text);
        repaint();
    }

    public void addActionListener(ActionListener l) {
        innerTextField.addActionListener(l);
    }

    @Override
    public boolean requestFocusInWindow() {
        return innerTextField.requestFocusInWindow();
    }

    public JTextField getInnerTextField() {
        return innerTextField;
    }
}
