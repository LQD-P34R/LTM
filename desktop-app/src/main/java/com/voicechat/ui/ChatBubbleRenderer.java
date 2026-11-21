package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * VoiceLink ChatBubbleRenderer: Render bong bóng tin nhắn bo tròn 2 chiều mượt mà.
 * <p>Tin nhắn của mình: căn phải, màu chủ đạo C_PRIMARY, chữ trắng, dấu kiểm nhận ✓✓.<br>
 * Tin nhắn người khác: căn trái, thẻ trắng tinh C_CARD_BG có viền mảnh 1px C_BORDER, kèm avatar nhỏ.</p>
 */
public final class ChatBubbleRenderer extends JPanel {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    private static final int BUBBLE_ARC = 16;

    private final JTextArea body = new JTextArea();
    private final boolean outgoing;
    private int naturalWidth;
    private int metadataWidth;
    private int measuredWidth = -1;

    public ChatBubbleRenderer(ChatMessage msg, String localUserId) {
        this(msg, localUserId, msg.senderId());
    }

    public ChatBubbleRenderer(ChatMessage msg, String localUserId, String displayName) {
        boolean isMine = msg.senderId().equals(localUserId);

        outgoing = isMine;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(4, 16, 4, 16));

        JPanel row = new JPanel(new FlowLayout(
                isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        // Nếu là tin nhắn của người khác, hiển thị thêm avatar nhỏ bên trái
        if (!isMine) {
            UiUtils.AvatarBadge avatar = new UiUtils.AvatarBadge(displayName, 28, false, false, false);
            row.add(avatar);
        }

        row.add(buildBubble(msg, isMine, displayName));
        add(row, BorderLayout.CENTER);
    }

    @Override public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE,getPreferredSize().height);
    }

    void fitTo(int rowWidth) {
        // Reserve row insets, FlowLayout gaps and the optional incoming avatar.
        int available = Math.max(1,rowWidth-48-(outgoing ? 0 : 36));
        int cap = Math.max(1,Math.min(available,Math.round(rowWidth*0.74f))-28);
        int width = Math.min(cap,Math.max(naturalWidth,metadataWidth));
        if (width == measuredWidth) return;
        measuredWidth = width;
        body.setPreferredSize(null);
        body.setSize(width,Short.MAX_VALUE);
        body.setPreferredSize(new Dimension(width,body.getPreferredSize().height));
        invalidate();
    }

    private JPanel buildBubble(ChatMessage msg, boolean isMine, String displayName) {
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
            JLabel senderLbl = new JLabel(displayName);
            senderLbl.setFont(UiUtils.F_SMALL_BOLD);
            senderLbl.setForeground(UiUtils.avatarColorFor(displayName));
            metadataWidth = senderLbl.getPreferredSize().width;
            bubble.add(senderLbl, BorderLayout.NORTH);
        }

        // Nội dung tin nhắn
        body.setFont(UiUtils.F_BODY);
        body.setText(msg.text());
        body.setOpaque(false);
        body.setEditable(false);
        body.setFocusable(false);
        body.setBorder(null);
        body.setMargin(new Insets(0,0,0,0));
        body.setForeground(isMine ? UiUtils.C_TEXT_LIGHT : UiUtils.C_TEXT_PRIMARY);
        // Measure with the actual Swing text UI before enabling wrapping.
        naturalWidth = body.getPreferredSize().width + 6;
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        bubble.add(body,BorderLayout.CENTER);

        // Footer: Thời gian
        JPanel footer = new JPanel(new FlowLayout(
                isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);

        String timeStr = TIME_FMT.format(msg.sentAt());
        JLabel timeLbl = new JLabel(timeStr);
        timeLbl.setFont(UiUtils.F_SMALL);
        timeLbl.setForeground(isMine ? new Color(255, 255, 255, 190) : UiUtils.C_TEXT_MUTED);
        metadataWidth = Math.max(metadataWidth,timeLbl.getPreferredSize().width);
        footer.add(timeLbl);

        bubble.add(footer, BorderLayout.SOUTH);
        return bubble;
    }
}
