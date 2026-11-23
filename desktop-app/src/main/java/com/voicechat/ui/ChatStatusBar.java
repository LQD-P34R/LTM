package com.voicechat.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/** Thanh trạng thái và nút chuyển sang màn hình cuộc gọi. */
final class ChatStatusBar extends JPanel {
    private final JLabel status = new JLabel();

    ChatStatusBar(String username, Runnable showCall) {
        super(new BorderLayout());
        setBackground(UiUtils.C_CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, UiUtils.C_BORDER),
                new EmptyBorder(6, 16, 6, 16)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dot.setForeground(UiUtils.C_ONLINE);
        status.setText(username + "  •  Sẵn sàng kết nối P2P");
        status.setFont(UiUtils.F_SMALL);
        status.setForeground(UiUtils.C_TEXT_MUTED);
        left.add(dot);
        left.add(status);

        UiUtils.RoundedButton callButton = new UiUtils.RoundedButton(
                "Cuộc gọi", UiUtils.C_PRIMARY_LIGHT, UiUtils.C_PRIMARY, UiUtils.ARC_SM);
        callButton.addActionListener(e -> showCall.run());
        left.add(callButton);
        add(left, BorderLayout.WEST);

        JLabel version = new JLabel("VoiceLink P2P v1.0");
        version.setFont(UiUtils.F_SMALL);
        version.setForeground(UiUtils.C_TEXT_HINT);
        add(version, BorderLayout.EAST);
    }

    void setStatus(String text) {
        status.setText(text);
    }
}
