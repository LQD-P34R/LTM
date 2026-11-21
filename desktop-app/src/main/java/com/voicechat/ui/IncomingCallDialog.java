package com.voicechat.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * VoiceLink IncomingCallDialog: Hộp thoại cuộc gọi đến dạng thẻ bo tròn hiện đại,
 * loại bỏ hoàn toàn viền và thanh tiêu đề xám thô cứng của JDialog mặc định.
 */
public final class IncomingCallDialog extends JDialog {

    public interface ResponseListener {
        void onAccepted();
        void onRejected();
    }

    public IncomingCallDialog(Frame owner, String callerName, ResponseListener listener) {
        super(owner, "Cuộc gọi đến", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // Nền trong suốt để viền bo tròn phát huy tác dụng
        setSize(360, 320);
        setLocationRelativeTo(owner);

        // Khung thẻ bo góc hiện đại có đổ bóng nhẹ
        UiUtils.RoundedPanel card = new UiUtils.RoundedPanel(
                UiUtils.ARC_LG, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 28, 28, 28));

        // 1. Avatar lớn phát sáng
        UiUtils.AvatarBadge avatar = new UiUtils.AvatarBadge(callerName, 64, false, true, false);
        avatar.setSpeaking(true);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(avatar);
        card.add(Box.createVerticalStrut(16));

        // 2. Tên người gọi
        JLabel callerLbl = new JLabel(callerName, SwingConstants.CENTER);
        callerLbl.setFont(UiUtils.F_TITLE_LG);
        callerLbl.setForeground(UiUtils.C_TEXT_PRIMARY);
        callerLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(callerLbl);
        card.add(Box.createVerticalStrut(6));

        // 3. Phụ đề
        JLabel subLbl = new JLabel("Cuộc gọi thoại P2P đến...", SwingConstants.CENTER);
        subLbl.setFont(UiUtils.F_BODY);
        subLbl.setForeground(UiUtils.C_TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subLbl);
        card.add(Box.createVerticalStrut(28));

        // 4. Hàng nút Chấp nhận / Từ chối
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 14, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        UiUtils.RoundedButton acceptBtn = new UiUtils.RoundedButton(
                "✓  Chấp nhận", UiUtils.C_ONLINE, Color.WHITE, UiUtils.ARC_MD);
        acceptBtn.addActionListener(e -> {
            dispose();
            if (listener != null) listener.onAccepted();
        });

        UiUtils.RoundedButton rejectBtn = new UiUtils.RoundedButton(
                "✕  Từ chối", UiUtils.C_DANGER, Color.WHITE, UiUtils.ARC_MD);
        rejectBtn.addActionListener(e -> {
            dispose();
            if (listener != null) listener.onRejected();
        });

        btnRow.add(acceptBtn);
        btnRow.add(rejectBtn);
        card.add(btnRow);

        setContentPane(card);
    }
}
