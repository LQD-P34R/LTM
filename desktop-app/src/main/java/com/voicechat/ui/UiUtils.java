package com.voicechat.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * VoiceLink Design System & Custom UI Components.
 * Cung cấp Design Tokens (màu sắc, typography, kích thước bo góc)
 * và các Swing Components tự vẽ bằng Graphics2D có khử răng cưa (Anti-Aliasing).
 */
public final class UiUtils {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    // --- Bảng màu nền (Background Colors) ---
    /** Nền ứng dụng dịu mắt, phong cách hiện đại (Slate-50) */
    public static final Color C_APP_BG          = new Color(0xF8, 0xFA, 0xFC);
    /** Nền thứ cấp cho Dock / Tabs rail / Sidebar */
    public static final Color C_SIDEBAR_LIGHT   = new Color(0xF1, 0xF5, 0xF9);
    /** Nền thẻ card trắng tinh khiết */
    public static final Color C_CARD_BG         = Color.WHITE;
    /** Nền khi di chuột (Hover nhẹ) */
    public static final Color C_HOVER_BG        = new Color(0xF1, 0xF5, 0xF9);
    /** Nền mục đang được chọn (Selected pastel blue) */
    public static final Color C_SELECTED_BG     = new Color(0xEB, 0xF3, 0xFF);
    // --- Bảng màu viền (Borders) ---
    /** Viền mảnh tinh tế 1px (Slate-200) */
    public static final Color C_BORDER          = new Color(0xE2, 0xE8, 0xF0);
    /** Viền khi ô nhập liệu được Active/Focus */
    public static final Color C_BORDER_FOCUS    = new Color(0x0B, 0x63, 0xE5);
    // --- Bảng màu nhấn chủ đạo (Primary Accent) ---
    /** Màu xanh chủ đạo VoiceLink (Primary Blue) */
    public static final Color C_PRIMARY         = new Color(0x0B, 0x63, 0xE5);
    /** Màu xanh khi Hover */
    public static final Color C_PRIMARY_HOVER   = new Color(0x09, 0x52, 0xC4);
    /** Màu xanh pastel cho badge/pill phụ */
    public static final Color C_PRIMARY_LIGHT   = new Color(0xEB, 0xF3, 0xFF);
    /** Màu xanh đậm cho banner chào mừng / Login Hero */
    public static final Color C_PRIMARY_HERO    = new Color(0x13, 0x51, 0xB4);
    // --- Bảng màu trạng thái (Status Colors) ---
    /** Xanh lá: Online / Đang nói / Gọi thoại */
    public static final Color C_ONLINE          = new Color(0x22, 0xC5, 0x5E);
    /** Xanh lá đậm khi Hover nút gọi */
    public static final Color C_ONLINE_HOVER    = new Color(0x16, 0xA3, 0x4A);
    /** Đỏ: Kết thúc cuộc gọi / Hủy / Nguy hiểm */
    public static final Color C_DANGER          = new Color(0xEF, 0x44, 0x44);
    /** Đỏ khi Hover */
    public static final Color C_DANGER_HOVER    = new Color(0xDC, 0x26, 0x26);
    /** Vàng cam: Cảnh báo / Tắt mic (Mute) */
    public static final Color C_WARNING         = new Color(0xF5, 0x9E, 0x0B);
    // --- Bảng màu chữ (Typography Colors) ---
    /** Chữ chính: Đậm, rõ ràng, tương phản cao (Slate-900) */
    public static final Color C_TEXT_PRIMARY    = new Color(0x0F, 0x17, 0x2A);
    /** Chữ phụ: Mô tả, tin nhắn phụ, thời gian (Slate-500) */
    public static final Color C_TEXT_MUTED      = new Color(0x64, 0x74, 0x8B);
    /** Chữ gợi ý (Placeholder) / Chấm trạng thái Offline (Slate-400) */
    public static final Color C_TEXT_HINT       = new Color(0x94, 0xA3, 0xB8);
    /** Chữ trắng trên nền tối / nút nhấn */
    public static final Color C_TEXT_LIGHT      = Color.WHITE;
    // --- Bong bóng tin nhắn (Chat Bubbles) ---
    public static final Color C_BUBBLE_INCOMING = new Color(0xF1, 0xF5, 0xF9);
    public static final Color C_BUBBLE_OUTGOING = new Color(0x0B, 0x63, 0xE5);
    // --- Màu Call Dark Mode (Cuộc gọi toàn cảnh) ---
    public static final Color C_CALL_DARK_BG    = new Color(0x0F, 0x17, 0x2A);
    public static final Color C_CALL_CTRL_BG    = new Color(0x1E, 0x29, 0x3B);
    public static final Color C_CALL_CTRL_HOV   = new Color(0x33, 0x41, 0x55);
    // --- Bảng màu Avatar Pastel rực rỡ ---
    public static final Color[] AVATAR_PALETTE = {
        new Color(0x63, 0x66, 0xF1), // Tím Indigo (Minh Anh)
        new Color(0x05, 0x96, 0x69), // Xanh Ngọc Emerald (Đồ án Mạng)
        new Color(0xDB, 0x27, 0x77), // Hồng Rose (Thu Hà)
        new Color(0xEA, 0x58, 0x0C), // Cam Amber (Quốc Bảo)
        new Color(0x25, 0x63, 0xEB), // Xanh Royal (Team Java)
        new Color(0x65, 0xA3, 0x0D), // Xanh Lá Olive (Hoàng Nam)
        new Color(0x0D, 0x94, 0x88), // Xanh Teal (Đức Anh)
        new Color(0x7C, 0x3A, 0xED), // Tím Violet (Lan Phương)
    };
    // --- Độ bo góc chuẩn (Arc Radii) ---
    public static final int ARC_SM = 10; // Nút bấm phẳng, pill nhỏ
    public static final int ARC_MD = 14; // Input textfield, thẻ danh sách
    public static final int ARC_LG = 18; // Khung Card chính, container
    public static final int ARC_XL = 24; // Floating dock, pill search
    // --- Phông chữ chuẩn hiện đại (Segoe UI fallback SansSerif) ---
    public static final Font F_TITLE_XL  = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font F_TITLE_LG  = new Font("Segoe UI", Font.BOLD,  18);
    public static final Font F_HEADING   = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font F_BODY      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_BODY_BOLD = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font F_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font F_SMALL_BOLD= new Font("Segoe UI", Font.BOLD,  11);
    public static final Font F_BTN       = new Font("Segoe UI", Font.BOLD,  13);

    // ===== Khởi tạo lớp tiện ích giao diện =====
    // Constructor riêng vì các hàm/hằng số dùng chung đều là static.
    private UiUtils() { /* utility class */ }

    // ===== Tạo thông tin avatar =====
    // Chọn màu ổn định từ tên, lấy chữ viết tắt và tạo avatar tương thích với nơi gọi cũ.
    /** Trả về màu avatar ổn định dựa trên mã băm của tên. */
    public static Color avatarColorFor(String name) {
        if (name == null || name.isEmpty()) return AVATAR_PALETTE[0];
        return AVATAR_PALETTE[Math.abs(name.hashCode()) % AVATAR_PALETTE.length];
    }

    /**
     * Trích xuất chữ cái viết tắt (Initials) thông minh giống VoiceLink:
     * "Minh Anh" -> "MA", "Quốc Bảo" -> "QB", "Bạn" -> "B", "User_A" -> "UA".
     */
    public static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String clean = name.trim();
        String[] parts = clean.split("[\\s_]+");
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
     * Tương thích ngược với các file hiện tại đang gọi buildAvatarCircle.
     */
    public static JPanel buildAvatarCircle(String name, int size, boolean isGroup) {
        return new AvatarBadge(name, size, isGroup, false, false);
    }

    // ===== Thiết lập chất lượng vẽ =====
    // Bật khử răng cưa cho các thành phần dùng Graphics2D.
    /**
     * Bật Anti-Aliasing mượt mà cho Graphics2D.
     */
    public static void enableAntiAliasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // ===== Các thành phần Swing dùng chung =====
    // Các lớp bên dưới tái sử dụng bảng màu và hàm vẽ để tạo panel, nút, ô nhập và avatar.
    /**
     * Khung bo góc mềm mại loại bỏ hoàn toàn viền Swing mặc định,
     * hỗ trợ nền phẳng, viền mảnh 1px tinh tế và hiệu ứng đổ bóng nhẹ (drop shadow).
     */
    public static class RoundedPanel extends JPanel {
        // ===== Cấu hình và trạng thái =====
        // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
        private int arc = ARC_LG;
        private Color backgroundColor = C_CARD_BG;
        private Color borderColor = C_BORDER;
        private float borderWidth = 1f;
        private boolean drawShadow = false;

        // ===== Khởi tạo thành phần =====
        // Các constructor dùng chung cấu hình để thiết lập kích thước, màu sắc và sự kiện.
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

        // ===== Cập nhật thuộc tính hiển thị =====
        // Đổi thuộc tính của thành phần và yêu cầu Swing vẽ lại khi cần.
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

        // ===== Vẽ thành phần tùy biến =====
        // Dùng Graphics2D để vẽ hình dạng và màu sắc theo trạng thái hiện tại.
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
                    g2.setColor(new Color(15, 23, 42, 3 * i));
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
        // ===== Cấu hình và trạng thái =====
        // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
        private int arc = ARC_SM;
        private Color normalBg = C_PRIMARY;
        private Color hoverBg = C_PRIMARY_HOVER;
        private Color pressedBg = C_PRIMARY_HOVER.darker();
        private Color normalFg = C_TEXT_LIGHT;
        private Color hoverFg = C_TEXT_LIGHT;
        private Color borderColor = null;
        private boolean isHovered = false;
        private boolean isPressed = false;

        // ===== Khởi tạo thành phần =====
        // Các constructor dùng chung cấu hình để thiết lập kích thước, màu sắc và sự kiện.
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

        // ===== Cập nhật thuộc tính hiển thị =====
        // Đổi thuộc tính của thành phần và yêu cầu Swing vẽ lại khi cần.
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

        // ===== Vẽ thành phần tùy biến =====
        // Dùng Graphics2D để vẽ hình dạng và màu sắc theo trạng thái hiện tại.
        private static Color calculateHoverColor(Color base) {
            // Nếu màu trắng hoặc rất sáng thì tối đi một chút
            if (base.getRed() > 230 && base.getGreen() > 230 && base.getBlue() > 230) {
                return new Color(0xEA, 0xEE, 0xF4);
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
                currentBg = new Color(0xE2, 0xE8, 0xF0);
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
        // ===== Cấu hình và trạng thái =====
        // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
        private int arc = ARC_MD;
        private Color normalBorder = C_BORDER;
        private Color focusBorder = C_BORDER_FOCUS;
        private Color bgColor = C_CARD_BG;
        private Color placeholderColor = C_TEXT_HINT;
        private String placeholder = "";
        private boolean isFocused = false;

        // ===== Khởi tạo thành phần =====
        // Các constructor dùng chung cấu hình để thiết lập kích thước, màu sắc và sự kiện.
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

        // ===== Cập nhật thuộc tính hiển thị =====
        // Đổi thuộc tính của thành phần và yêu cầu Swing vẽ lại khi cần.
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

        // ===== Vẽ thành phần tùy biến =====
        // Dùng Graphics2D để vẽ hình dạng và màu sắc theo trạng thái hiện tại.
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
        // ===== Cấu hình và trạng thái =====
        // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
        private int arc = ARC_MD;
        private Color normalBorder = C_BORDER;
        private Color focusBorder = C_BORDER_FOCUS;
        private Color bgColor = C_CARD_BG;
        private Color placeholderColor = C_TEXT_HINT;
        private String placeholder = "";
        private boolean isFocused = false;

        // ===== Khởi tạo thành phần =====
        // Các constructor dùng chung cấu hình để thiết lập kích thước, màu sắc và sự kiện.
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

        // ===== Cập nhật thuộc tính hiển thị =====
        // Đổi thuộc tính của thành phần và yêu cầu Swing vẽ lại khi cần.
        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        // ===== Vẽ thành phần tùy biến =====
        // Dùng Graphics2D để vẽ hình dạng và màu sắc theo trạng thái hiện tại.
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
        // ===== Cấu hình và trạng thái =====
        // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
        private String name = "";
        private int size = 40;
        private boolean isGroup = false;
        private boolean isOnline = false;
        private boolean showStatusBadge = true;
        private boolean isSpeaking = false;

        // ===== Khởi tạo thành phần =====
        // Các constructor dùng chung cấu hình để thiết lập kích thước, màu sắc và sự kiện.
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

        // ===== Cập nhật thuộc tính hiển thị =====
        // Đổi thuộc tính của thành phần và yêu cầu Swing vẽ lại khi cần.
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

        // ===== Vẽ thành phần tùy biến =====
        // Dùng Graphics2D để vẽ hình dạng và màu sắc theo trạng thái hiện tại.
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
                g2.setColor(new Color(34, 197, 94, 60));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(34, 197, 94, 140));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
            }

            // 2. Vẽ hình tròn Avatar
            Color bg = isGroup ? new Color(0x05, 0x96, 0x69) : avatarColorFor(name);
            g2.setColor(bg);
            g2.fillOval(circleX, circleY, size, size);

            // 3. Vẽ chữ viết tắt (Initials) hoặc Icon
            g2.setColor(Color.WHITE);
            if (isGroup) {
                int iconSize = Math.max(12, size / 2);
                UiIcon.GROUP.draw(g2,circleX+(size-iconSize)/2,circleY+(size-iconSize)/2,iconSize,Color.WHITE);
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
                g2.setColor(Color.WHITE);
                g2.fillOval(bx - 2, by - 2, badgeSize + 4, badgeSize + 4);

                // Chấm màu
                g2.setColor(isOnline ? C_ONLINE : C_TEXT_HINT);
                g2.fillOval(bx, by, badgeSize, badgeSize);
            }

            g2.dispose();
        }
    }
}
