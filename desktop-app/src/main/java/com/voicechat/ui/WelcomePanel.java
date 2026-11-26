package com.voicechat.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Màn hình chào mừng trong vùng nội dung của VoiceLink. */
final class WelcomePanel extends JPanel {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    private final JLabel welcomeLabel = new JLabel("", SwingConstants.CENTER);

    // ===== Dựng màn hình chào =====
    // Tạo phần chào mừng hiển thị khi chưa chọn cuộc trò chuyện.
    WelcomePanel(String username) {
        super(new GridBagLayout());
        setBackground(UiUtils.C_APP_BG);

        UiUtils.RoundedPanel card = new UiUtils.RoundedPanel(
                UiUtils.ARC_LG, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(460, 320));

        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.enableAntiAliasing(g2);
                g2.setColor(UiUtils.C_PRIMARY_LIGHT);
                g2.fillOval((getWidth() - 64) / 2, 0, 64, 64);
                g2.dispose();
            }
        };
        iconPanel.setLayout(new GridBagLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(460, 66));
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel icon = new JLabel("\uD83D\uDCAC");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        icon.setForeground(UiUtils.C_PRIMARY);
        iconPanel.add(icon);
        card.add(iconPanel);
        card.add(Box.createVerticalStrut(16));

        welcomeLabel.setFont(UiUtils.F_TITLE_LG);
        welcomeLabel.setForeground(UiUtils.C_TEXT_PRIMARY);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        setUsername(username);
        card.add(welcomeLabel);
        card.add(Box.createVerticalStrut(8));

        JLabel hint = new JLabel(
                "Chọn một liên hệ từ danh sách bên trái để bắt đầu nhắn tin hoặc gọi thoại.",
                SwingConstants.CENTER);
        hint.setFont(UiUtils.F_BODY);
        hint.setForeground(UiUtils.C_TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(hint);
        add(card);
    }

    // ===== Cập nhật tên người dùng =====
    // Thay tên hiển thị khi thông tin đăng nhập thay đổi.
    void setUsername(String username) {
        welcomeLabel.setText("Chào mừng, " + username + "!");
    }
}
