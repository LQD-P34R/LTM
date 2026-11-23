package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * VoiceLink ChatBubbleRenderer: Render bong bóng tin nhắn bo tròn 2 chiều mượt mà.
 * <p>Tin nhắn của mình: căn phải, màu chủ đạo C_PRIMARY, chữ trắng.<br>
 * Tin nhắn người khác: căn trái, thẻ trắng tinh C_CARD_BG có viền mảnh 1px C_BORDER, kèm avatar nhỏ.</p>
 */
public final class ChatBubbleRenderer extends JPanel {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    private static final int BUBBLE_ARC = 16;

    public ChatBubbleRenderer(ChatMessage msg, String localUserId) {
        boolean isMine = msg.senderId().equals(localUserId);

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(4, 16, 4, 16));

        JPanel row = new JPanel(new FlowLayout(
                isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        // Nếu là tin nhắn của người khác, hiển thị thêm avatar nhỏ bên trái
        if (!isMine) {
            UiUtils.AvatarBadge avatar = new UiUtils.AvatarBadge(msg.senderId(), 28, false, false, false);
            row.add(avatar);
        }

        row.add(buildBubble(msg, isMine));
        add(row, BorderLayout.CENTER);
    }

    private JPanel buildBubble(ChatMessage msg, boolean isMine) {
        JPanel bubble = new JPanel(new BorderLayout(0, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.enableAntiAliasing(g2);

                int w = getWidth();
                int h = getHeight();

                if (isMine) {
                    // Nền màu chủ đạo xanh
                    g2.setColor(UiUtils.C_PRIMARY);
                    g2.fillRoundRect(0, 0, w, h, BUBBLE_ARC, BUBBLE_ARC);
                } else {
                    // Nền trắng tinh + viền mảnh tinh tế 1px
                    g2.setColor(UiUtils.C_CARD_BG);
                    g2.fillRoundRect(0, 0, w, h, BUBBLE_ARC, BUBBLE_ARC);
                    g2.setColor(UiUtils.C_BORDER);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, BUBBLE_ARC, BUBBLE_ARC);
                }
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(8, 14, 8, 14));

        // Tên người gửi (cho tin nhắn của người khác)
        if (!isMine) {
            JLabel senderLbl = new JLabel(msg.senderId());
            senderLbl.setFont(UiUtils.F_SMALL_BOLD);
            senderLbl.setForeground(UiUtils.avatarColorFor(msg.senderId()));
            bubble.add(senderLbl, BorderLayout.NORTH);
        }

        // Nội dung tin nhắn
        JTextArea textArea = new JTextArea(msg.text());
        textArea.setFont(UiUtils.F_BODY);
        textArea.setForeground(isMine ? UiUtils.C_TEXT_LIGHT : UiUtils.C_TEXT_PRIMARY);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMaximumSize(new Dimension(380, Integer.MAX_VALUE));
        bubble.setMaximumSize(new Dimension(420, Integer.MAX_VALUE));
        bubble.add(textArea, BorderLayout.CENTER);

        // Footer: Thời gian
        JPanel footer = new JPanel(new FlowLayout(
                isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);

        String timeStr = TIME_FMT.format(msg.sentAt());
        JLabel timeLbl = new JLabel(timeStr);
        timeLbl.setFont(UiUtils.F_SMALL);
        timeLbl.setForeground(isMine ? new Color(255, 255, 255, 190) : UiUtils.C_TEXT_MUTED);
        footer.add(timeLbl);

        bubble.add(footer, BorderLayout.SOUTH);
        return bubble;
    }
}
