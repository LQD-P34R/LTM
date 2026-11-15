package com.voicechat.ui;

import java.awt.*;
import javax.swing.*;

/**
 * Tiện ích UI dùng chung trong package ui (package-private).
 * Cung cấp avatar hình tròn và bảng màu nhất quán.
 */
final class UiUtils {

    private UiUtils() { /* utility class */ }

    private static final Color[] AVATAR_COLORS = {
        new Color(0x3A, 0x7B, 0xD5), new Color(0xE7, 0x4C, 0x3C),
        new Color(0x2E, 0xCC, 0x71), new Color(0xF3, 0x9C, 0x12),
        new Color(0x9B, 0x59, 0xB6), new Color(0x1A, 0xBC, 0x9C),
        new Color(0xE9, 0x1E, 0x63), new Color(0x00, 0x96, 0x88),
    };

    /** Trả về màu avatar ổn định dựa trên hash tên. */
    static Color avatarColorFor(String name) {
        return AVATAR_COLORS[Math.abs(name.hashCode()) % AVATAR_COLORS.length];
    }

    /**
     * Xây dựng JPanel hình tròn hiển thị ký tự đầu hoặc icon nhóm.
     *
     * @param name    tên hiển thị (lấy ký tự đầu làm chữ cái)
     * @param size    đường kính tính bằng pixel
     * @param isGroup nếu true, hiển thị icon 👥 thay vì chữ cái
     */
    static JPanel buildAvatarCircle(String name, int size, boolean isGroup) {
        Color bg = isGroup
                ? new Color(0x2E, 0xCC, 0x71)
                : avatarColorFor(name);

        String letter = isGroup
                ? "\uD83D\uDC65"  // 👥
                : (name.isEmpty() ? "?" : String.valueOf(Character.toUpperCase(name.charAt(0))));

        JPanel avatar = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setOpaque(false);
        Dimension d = new Dimension(size, size);
        avatar.setPreferredSize(d);
        avatar.setMinimumSize(d);
        avatar.setMaximumSize(d);

        int fontSize = isGroup ? Math.max(size / 2, 10) : Math.max((int)(size * 0.42), 10);
        JLabel lbl = new JLabel(letter, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", isGroup ? Font.PLAIN : Font.BOLD, fontSize));
        lbl.setForeground(Color.WHITE);
        avatar.add(lbl);
        return avatar;
    }
}
