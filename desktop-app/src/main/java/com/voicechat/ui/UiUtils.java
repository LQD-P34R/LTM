package com.voicechat.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;

/**
 * VoiceLink Design System & Custom UI Components.
 * Cung cấp Design Tokens (màu sắc, typography, kích thước bo góc)
 * và các Swing Components tự vẽ bằng Graphics2D có khử răng cưa (Anti-Aliasing).
 */
public final class UiUtils {

    private UiUtils() { /* utility class */ }

    // =========================================================================
    // 1. DESIGN TOKENS (BẢNG MÀU & THIẾT KẾ VOICELINK)
    // =========================================================================

    // --- Bảng màu nền (Background Colors) ---
    /** Nền kem ấm của vùng làm việc. */
    public static final Color C_APP_BG          = new Color(0xFB, 0xF4, 0xE3);
    /** Nền thứ cấp cho Dock / Tabs rail / Sidebar */
    public static final Color C_SIDEBAR_LIGHT   = new Color(0xFF, 0xFB, 0xF2);
    /** Nền kem sáng cho thẻ và bề mặt. */
    public static final Color C_CARD_BG         = new Color(0xFF, 0xFB, 0xF2);
    /** Nền be đậm hơn dành riêng cho ô nhập và hành động gọi nhóm. */
    public static final Color C_BEIGE_FIELD     = new Color(0xF1, 0xE4, 0xC4);
    /** Nền khi di chuột (Hover nhẹ) */
    public static final Color C_HOVER_BG        = new Color(0xF3, 0xE8, 0xD2);
    /** Nền mục đang được chọn (Selected pastel blue) */
    public static final Color C_SELECTED_BG     = new Color(0xDC, 0xE8, 0xF1);

    // --- Bảng màu viền (Borders) ---
    /** Viền mảnh tinh tế 1px */
    public static final Color C_BORDER          = new Color(0xC8, 0xBE, 0xAA);
    /** Viền khi ô nhập liệu được Active/Focus */
    public static final Color C_BORDER_FOCUS    = new Color(0x70, 0x9B, 0xBA);

    // --- Bảng màu nhấn chủ đạo (Primary Accent) ---
    /** Màu xanh chủ đạo VoiceLink (Primary Blue) */
    public static final Color C_PRIMARY         = new Color(0x8D, 0xB4, 0xD2);
    /** Màu xanh khi Hover */
    public static final Color C_PRIMARY_HOVER   = new Color(0x70, 0x9B, 0xBA);
    /** Sắc xanh đậm dành cho chữ/icon trên nền xanh nhạt và trạng thái đang chọn. */
    public static final Color C_PRIMARY_TEXT    = new Color(0x49, 0x6D, 0x86);
    /** Màu xanh pastel cho badge/pill phụ */
    public static final Color C_PRIMARY_LIGHT   = new Color(0xDC, 0xE8, 0xF1);
    /** Màu nâu rượu đậm cho điểm nhấn thương hiệu. */
    public static final Color C_PRIMARY_HERO    = new Color(0x4A, 0x1D, 0x22);

    // --- Bảng màu trạng thái (Status Colors) ---
    /** Xanh bột cho chấm trạng thái và điểm nhấn nhẹ. */
    public static final Color C_ONLINE          = new Color(0x8D, 0xB4, 0xD2);
    /** Xanh lá đậm khi Hover nút gọi */
    public static final Color C_ONLINE_HOVER    = new Color(0x75, 0x9F, 0xBF);
    /** Đỏ: Kết thúc cuộc gọi / Hủy / Nguy hiểm */
    public static final Color C_DANGER          = new Color(0xA8, 0x2A, 0x35);
    /** Đỏ khi Hover */
    public static final Color C_DANGER_HOVER    = new Color(0x8F, 0x20, 0x2B);
    /** Vàng cam: Cảnh báo / Tắt mic (Mute) */
    public static final Color C_WARNING         = new Color(0xE4, 0xC8, 0x8B);

    // --- Bảng màu chữ (Typography Colors) ---
    /** Chữ chính màu nâu rượu đậm, tương phản cao. */
    public static final Color C_TEXT_PRIMARY    = new Color(0x3B, 0x16, 0x1B);
    /** Chữ phụ màu nâu xám đậm cho mô tả và thời gian. */
    public static final Color C_TEXT_MUTED      = new Color(0x65, 0x50, 0x55);
    /** Chữ gợi ý (Placeholder) / Chấm trạng thái Offline (Slate-400) */
    public static final Color C_TEXT_HINT       = new Color(0x78, 0x64, 0x68);
    /** Màu chữ tương phản trên nền xanh bột. */
    public static final Color C_TEXT_LIGHT      = C_TEXT_PRIMARY;

    // --- Bong bóng tin nhắn (Chat Bubbles) ---
    public static final Color C_BUBBLE_INCOMING = new Color(0xDC, 0xE8, 0xF1);
    public static final Color C_BUBBLE_OUTGOING = new Color(0xA8, 0x2A, 0x35);

    // --- Màu Call Dark Mode (Cuộc gọi toàn cảnh) ---
    public static final Color C_CALL_DARK_BG    = new Color(0xFB, 0xF4, 0xE3);
    public static final Color C_CALL_CTRL_BG    = new Color(0xFF, 0xFB, 0xF2);
    public static final Color C_CALL_CTRL_HOV   = new Color(0xDC, 0xE8, 0xF1);

    // --- Bảng màu Avatar Pastel rực rỡ ---
    public static final Color[] AVATAR_PALETTE = {
        new Color(0xE8, 0xD9, 0xB9), new Color(0xD6, 0xE4, 0xED),
        new Color(0xE8, 0xC9, 0xCE), new Color(0xD7, 0xB9, 0xA2),
        new Color(0xDC, 0xE8, 0xF1), new Color(0xF0, 0xDE, 0xB8),
        new Color(0xE7, 0xD2, 0xC8), new Color(0xE6, 0xC9, 0xD0)
    };

    // --- Độ bo góc chuẩn (Arc Radii) ---
    public static final int ARC_SM = 10; // Nút bấm phẳng, pill nhỏ
    public static final int ARC_MD = 14; // Input textfield, thẻ danh sách
    public static final int ARC_LG = 18; // Khung Card chính, container
    public static final int ARC_XL = 24; // Floating dock, pill search

    static { installGlobalTypography(); }

    /** Apply Segoe UI to Swing delegates before component instances are created. */
    public static void installGlobalTypography() {
        UIDefaults defaults = UIManager.getDefaults();
        java.util.Enumeration<Object> keys = defaults.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = defaults.get(key);
            if (value instanceof Font font) {
                Font base = font.isBold() ? UiTheme.SEMIBOLD_FONT : UiTheme.REGULAR_FONT;
                defaults.put(key, new FontUIResource(base.deriveFont(font.getStyle(), (float) font.getSize())));
            }
        }
        FontUIResource base = new FontUIResource(UiTheme.REGULAR_FONT.deriveFont(13f));
        for (String key : new String[]{"defaultFont", "Label.font", "Button.font", "ToggleButton.font",
                "TextField.font", "TextArea.font", "TextPane.font", "EditorPane.font", "ComboBox.font",
                "List.font", "Table.font", "TableHeader.font", "Menu.font", "MenuItem.font",
                "CheckBox.font", "RadioButton.font", "TabbedPane.font", "ToolTip.font", "OptionPane.font"}) {
            UIManager.put(key, base);
        }
    }

    // --- Phông chữ chuẩn hiện đại ---
    public static final Font F_TITLE_XL  = UiTheme.SEMIBOLD_FONT.deriveFont(20f);
    public static final Font F_TITLE_LG  = UiTheme.SEMIBOLD_FONT.deriveFont(18f);
    public static final Font F_HEADING   = UiTheme.SEMIBOLD_FONT.deriveFont(15f);
    public static final Font F_BODY      = UiTheme.REGULAR_FONT.deriveFont(13.5f);
    public static final Font F_BODY_BOLD = UiTheme.SEMIBOLD_FONT.deriveFont(13.5f);
    public static final Font F_SMALL     = UiTheme.REGULAR_FONT.deriveFont(11.5f);
    public static final Font F_SMALL_BOLD= UiTheme.SEMIBOLD_FONT.deriveFont(11.5f);
    public static final Font F_BTN       = UiTheme.SEMIBOLD_FONT.deriveFont(13f);

    // =========================================================================
    // 2. HELPER METHODS
    // =========================================================================

    /** Trả về màu avatar ổn định dựa trên mã băm của tên. */
    public static Color avatarColorFor(String name) {
        if (name == null || name.isEmpty()) return AVATAR_PALETTE[0];
        return AVATAR_PALETTE[Math.abs(name.hashCode()) % AVATAR_PALETTE.length];
    }

    /**
     * Trích xuất chữ cái viết tắt (Initials) thông minh giống VoiceLink:
     * "Minh Anh" -> "MA", "Quốc Bảo" -> "QB", "Bạn" -> "BN", "User_A" -> "UA".
     */
    public static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String clean = name.trim();
        if (clean.equalsIgnoreCase("Bạn") || clean.startsWith("Bạn -")) return "BN";
        String[] parts = clean.split("[^\\p{L}\\p{N}]+");
        if (parts.length == 0) return "?";
        if (parts.length == 1) {
            String w = parts[0];
            return w.length() >= 2 ? w.substring(0, 2).toUpperCase() : w.toUpperCase();
        }
        String first = parts[0];
        String second = parts[1];
        char c1 = first.isEmpty() ? ' ' : first.charAt(0);
        char c2 = second.isEmpty() ? ' ' : second.charAt(0);
        return ("" + c1 + c2).trim().toUpperCase();
    }

    /**
     * Bật Anti-Aliasing mượt mà cho Graphics2D.
     */
    public static void enableAntiAliasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /**
     * Tương thích ngược với các file hiện tại đang gọi buildAvatarCircle.
     */
    public static JPanel buildAvatarCircle(String name, int size, boolean isGroup) {
        return new AvatarBadge(name, size, isGroup, false, false);
    }

    // =========================================================================
    // 3. CUSTOM SWING COMPONENTS (VẼ BẰNG GRAPHICS2D)
    // =========================================================================

    /**
     * Khung bo góc mềm mại loại bỏ hoàn toàn viền Swing mặc định,
     * hỗ trợ nền phẳng, viền mảnh 1px tinh tế và hiệu ứng đổ bóng nhẹ (drop shadow).
     */
    public static class RoundedPanel extends JPanel {
        private int arc = ARC_LG;
        private Color backgroundColor = C_CARD_BG;
        private Color borderColor = C_BORDER;
        private float borderWidth = 1f;
        private boolean drawShadow = false;

        public RoundedPanel() {
            this(ARC_LG, C_CARD_BG, C_BORDER, false);
        }

        public RoundedPanel(int arc) {
            this(arc, C_CARD_BG, C_BORDER, false);
        }

        public RoundedPanel(int arc, Color bg) {
            this(arc, bg, null, false);
        }

        public RoundedPanel(int arc, Color bg, Color borderColor) {
            this(arc, bg, borderColor, false);
        }

        public RoundedPanel(int arc, Color bg, Color borderColor, boolean drawShadow) {
            super();
            init(arc, bg, borderColor, drawShadow);
        }

        public RoundedPanel(LayoutManager layout, int arc, Color bg, Color borderColor, boolean drawShadow) {
            super(layout);
            init(arc, bg, borderColor, drawShadow);
        }

        private void init(int arc, Color bg, Color border, boolean shadow) {
            this.arc = arc;
            this.backgroundColor = bg;
            this.borderColor = border;
            this.drawShadow = shadow;
            setOpaque(false);
            setBorder(null);
        }

        public void setBackgroundColor(Color bg) {
            this.backgroundColor = bg;
            repaint();
        }

        public void setBorderColor(Color border) {
            this.borderColor = border;
            repaint();
        }

        public void setArc(int arc) {
            this.arc = arc;
            repaint();
        }

        public void setDrawShadow(boolean shadow) {
            this.drawShadow = shadow;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            int width = getWidth();
            int height = getHeight();

            if (drawShadow) {
                // Đổ bóng mềm nhiều lớp (subtle soft shadow)
                int shadowSize = 3;
                for (int i = shadowSize; i >= 1; i--) {
                    g2.setColor(new Color(74, 29, 34, 3 * i));
                    g2.fillRoundRect(i - 1, i, width - (i - 1) * 2, height - i * 2, arc + i, arc + i);
                }
            }

            int pad = drawShadow ? 2 : 0;
            int drawW = width - pad * 2;
            int drawH = height - pad * 2;

            if (backgroundColor != null) {
                g2.setColor(backgroundColor);
                g2.fillRoundRect(pad, pad, drawW, drawH, arc, arc);
            }

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(borderWidth));
                g2.drawRoundRect(pad, pad, drawW - 1, drawH - 1, arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Nút phẳng hiện đại: bo góc, tắt viền focus, tự đổi màu mượt mà khi hover và nhấn.
     */
    public static class RoundedButton extends JButton {
        private int arc = ARC_SM;
        private Color normalBg = C_PRIMARY;
        private Color hoverBg = C_PRIMARY_HOVER;
        private Color pressedBg = C_PRIMARY_HOVER.darker();
        private Color normalFg = C_TEXT_LIGHT;
        private Color hoverFg = C_TEXT_LIGHT;
        private Color borderColor = null;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public RoundedButton(String text) {
            this(text, C_PRIMARY, C_TEXT_LIGHT, ARC_SM);
        }

        public RoundedButton(String text, Color bg, Color fg) {
            this(text, bg, fg, ARC_SM);
        }

        public RoundedButton(String text, Color bg, Color fg, int arc) {
            this(text, bg, fg, null, arc);
        }

        public RoundedButton(String text, Color bg, Color fg, Color borderColor, int arc) {
            super(text);
            this.normalBg = bg;
            this.hoverBg = bg != null ? calculateHoverColor(bg) : null;
            this.pressedBg = hoverBg != null ? hoverBg.darker() : null;
            this.normalFg = fg;
            this.hoverFg = fg;
            this.borderColor = borderColor;
            this.arc = arc;

            setFont(F_BTN);
            setForeground(normalFg);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        public void setCustomColors(Color bg, Color hover, Color fg) {
            this.normalBg = bg;
            this.hoverBg = hover;
            this.pressedBg = hover != null ? hover.darker() : null;
            this.normalFg = fg;
            this.hoverFg = fg;
            setForeground(fg);
            repaint();
        }

        public void setBorderColor(Color border) {
            this.borderColor = border;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension preferred = super.getPreferredSize();
            FontMetrics metrics = getFontMetrics(getFont());
            int textWidth = getText() == null ? 0 : metrics.stringWidth(getText());
            int textHeight = getText() == null || getText().isEmpty() ? 0 : metrics.getHeight();
            Icon icon = getIcon();
            int iconWidth = icon == null ? 0 : icon.getIconWidth();
            int iconHeight = icon == null ? 0 : icon.getIconHeight();
            int gap = iconWidth > 0 && textWidth > 0 ? getIconTextGap() : 0;
            Insets insets = getInsets();
            int neededWidth = textWidth + iconWidth + gap + insets.left + insets.right;
            int neededHeight = Math.max(textHeight, iconHeight) + insets.top + insets.bottom;
            return new Dimension(Math.max(preferred.width, neededWidth), Math.max(preferred.height, neededHeight));
        }

        private static Color calculateHoverColor(Color base) {
            // Nếu màu trắng hoặc rất sáng thì tối đi một chút
            if (base.getRed() > 230 && base.getGreen() > 230 && base.getBlue() > 230) {
            return new Color(0xF3, 0xE8, 0xD2);
            }
            // Màu thông thường: làm sáng nhẹ hoặc tối nhẹ
            return new Color(
                Math.max(0, base.getRed() - 15),
                Math.max(0, base.getGreen() - 15),
                Math.max(0, base.getBlue() - 15)
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            Color currentBg;
            if (!isEnabled()) {
                currentBg = new Color(0xE3, 0xD9, 0xC8);
            } else if (isPressed && pressedBg != null) {
                currentBg = pressedBg;
            } else if (isHovered && hoverBg != null) {
                currentBg = hoverBg;
            } else {
                currentBg = normalBg;
            }

            if (currentBg != null) {
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            }

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }

            g2.dispose();

            // Cập nhật màu chữ theo trạng thái
            if (!isEnabled()) {
                setForeground(C_TEXT_HINT);
            } else {
                setForeground(isHovered && hoverFg != null ? hoverFg : normalFg);
            }

            super.paintComponent(g);
        }
    }

    /**
     * Ô nhập liệu bo góc mềm mại, có padding bên trong rộng rãi,
     * tự hiển thị placeholder màu nhạt và tự đổi màu viền sang xanh chủ đạo khi focus.
     */
    public static class RoundedTextField extends JTextField {
        private int arc = ARC_MD;
        private Color normalBorder = C_BORDER;
        private Color focusBorder = C_BORDER_FOCUS;
        private Color bgColor = C_CARD_BG;
        private Color placeholderColor = C_TEXT_HINT;
        private String placeholder = "";
        private boolean isFocused = false;

        public RoundedTextField(String placeholder) {
            this(placeholder, ARC_MD, C_CARD_BG);
        }

        public RoundedTextField(String placeholder, int arc) {
            this(placeholder, arc, C_CARD_BG);
        }

        public RoundedTextField(String placeholder, int arc, Color bg) {
            super();
            this.placeholder = placeholder;
            this.arc = arc;
            this.bgColor = bg;

            setFont(F_BODY);
            setForeground(C_TEXT_PRIMARY);
            setCaretColor(C_PRIMARY);
            setOpaque(false);
            setBorder(new EmptyBorder(8, 16, 8, 16));
            focusBorder = normalBorder;

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

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        public void setNormalBorder(Color border) {
            this.normalBorder = border;
            repaint();
        }

        public void setBgColor(Color bg) {
            this.bgColor = bg;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            int w = getWidth();
            int h = getHeight();

            // Tô nền
            g2.setColor(bgColor != null ? bgColor : Color.WHITE);
            g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);

            // Vẽ viền
            g2.setColor(normalBorder != null ? normalBorder : C_BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            g2.dispose();

            super.paintComponent(g);

            // Vẽ placeholder nếu đang trống và chưa có focus
            if (getText().isEmpty() && !isFocusOwner() && placeholder != null && !placeholder.isEmpty()) {
                Graphics2D gp = (Graphics2D) g.create();
                enableAntiAliasing(gp);
                gp.setColor(placeholderColor);
                gp.setFont(getFont());
                FontMetrics fm = gp.getFontMetrics();
                Insets insets = getInsets();
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                gp.drawString(placeholder, insets.left, textY);
                gp.dispose();
            }
        }
    }

    /**
     * Ô nhập mật khẩu bo góc mềm mại, kế thừa từ JPasswordField.
     */
    public static class RoundedPasswordField extends JPasswordField {
        private int arc = ARC_MD;
        private Color normalBorder = C_BORDER;
        private Color focusBorder = C_BORDER_FOCUS;
        private Color bgColor = C_CARD_BG;
        private Color placeholderColor = C_TEXT_HINT;
        private String placeholder = "";
        private boolean isFocused = false;

        public RoundedPasswordField(String placeholder) {
            this(placeholder, ARC_MD, C_CARD_BG);
        }

        public RoundedPasswordField(String placeholder, int arc, Color bg) {
            super();
            this.placeholder = placeholder;
            this.arc = arc;
            this.bgColor = bg;

            setFont(F_BODY);
            setForeground(C_TEXT_PRIMARY);
            setCaretColor(C_PRIMARY);
            setOpaque(false);
            setBorder(new EmptyBorder(10, 14, 10, 14));

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

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(bgColor != null ? bgColor : Color.WHITE);
            g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);

            if (isFocused) {
                g2.setColor(focusBorder);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            } else {
                g2.setColor(normalBorder != null ? normalBorder : C_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            }

            g2.dispose();

            super.paintComponent(g);

            if (getPassword().length == 0 && !isFocusOwner() && placeholder != null && !placeholder.isEmpty()) {
                Graphics2D gp = (Graphics2D) g.create();
                enableAntiAliasing(gp);
                gp.setColor(placeholderColor);
                gp.setFont(getFont());
                FontMetrics fm = gp.getFontMetrics();
                Insets insets = getInsets();
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                gp.drawString(placeholder, insets.left, textY);
                gp.dispose();
            }
        }
    }

    /**
     * AvatarBadge: Hiển thị hình tròn với chữ cái viết tắt (Initials) hoặc icon nhóm
     * trên nền màu pastel rực rỡ, kèm theo chấm tròn trạng thái (Online/Offline) ở góc dưới phải.
     * Hỗ trợ hiệu ứng phát sáng gợn sóng khi thành viên đang nói (isSpeaking).
     */
    public static class AvatarBadge extends JPanel {
        private String name = "";
        private int size = 40;
        private boolean isGroup = false;
        private boolean isOnline = false;
        private boolean showStatusBadge = true;
        private boolean isSpeaking = false;

        public AvatarBadge(String name, int size) {
            this(name, size, false, false, false);
        }

        public AvatarBadge(String name, int size, boolean isGroup) {
            this(name, size, isGroup, false, false);
        }

        public AvatarBadge(String name, int size, boolean isGroup, boolean isOnline, boolean showStatusBadge) {
            this.name = name != null ? name : "";
            this.size = size;
            this.isGroup = isGroup;
            this.isOnline = isOnline;
            this.showStatusBadge = showStatusBadge;

            setOpaque(false);
            updateDimension();
        }

        private void updateDimension() {
            int totalDim = size + (isSpeaking ? 8 : 0);
            Dimension d = new Dimension(totalDim, totalDim);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
            revalidate();
        }

        public void setOnline(boolean online) {
            this.isOnline = online;
            repaint();
        }

        public void setSpeaking(boolean speaking) {
            if (this.isSpeaking != speaking) {
                this.isSpeaking = speaking;
                updateDimension();
                repaint();
            }
        }

        public void setName(String name) {
            this.name = name != null ? name : "";
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            int circleOffset = isSpeaking ? 4 : 0;
            int circleX = circleOffset;
            int circleY = circleOffset;

            // 1. Nếu đang nói (Speaking): vẽ vòng hào quang phát sáng
            if (isSpeaking) {
                g2.setColor(new Color(C_PRIMARY.getRed(), C_PRIMARY.getGreen(), C_PRIMARY.getBlue(), 60));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(C_PRIMARY.getRed(), C_PRIMARY.getGreen(), C_PRIMARY.getBlue(), 140));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
            }

            // 2. Vẽ hình tròn Avatar
            Color bg = isGroup ? new Color(0xD6, 0xE4, 0xED) : avatarColorFor(name);
            g2.setColor(bg);
            g2.fillOval(circleX, circleY, size, size);

            // 3. Vẽ chữ viết tắt (Initials) hoặc Icon
            g2.setColor(C_TEXT_PRIMARY);
            if (isGroup) {
                int iconSize = Math.min(26, size - 12);
                new AppIcons(AppIcons.Kind.GROUP, iconSize, C_TEXT_PRIMARY).paintIcon(this, g2,
                        circleX + (size - iconSize) / 2, circleY + (size - iconSize) / 2);
            } else {
                String initials = getInitials(name);
                int fontSize = initials.length() > 1
                        ? Math.max((int) (size * 0.38), 10)
                        : Math.max((int) (size * 0.44), 11);
                g2.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
                FontMetrics fm = g2.getFontMetrics();
                int tx = circleX + (size - fm.stringWidth(initials)) / 2;
                int ty = circleY + (size - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initials, tx, ty);
            }

            // 4. Vẽ chấm tròn trạng thái (Status Badge)
            if (showStatusBadge) {
                int badgeSize = Math.max(9, (int) (size * 0.28));
                int bx = circleX + size - badgeSize;
                int by = circleY + size - badgeSize;

                // Vành trắng ngăn cách
                g2.setColor(C_SIDEBAR_LIGHT);
                g2.fillOval(bx - 2, by - 2, badgeSize + 4, badgeSize + 4);

                // Chấm màu
                g2.setColor(isOnline ? C_ONLINE : C_TEXT_HINT);
                g2.fillOval(bx, by, badgeSize, badgeSize);
            }

            g2.dispose();
        }
    }
}
