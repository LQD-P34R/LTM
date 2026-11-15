package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * TV3 – Bước 3A: Render một ChatMessage thành bubble chat.
 * <p>Tin của mình: căn phải, nền xanh accent.<br>
 * Tin của người khác: căn trái, nền xám nhạt.</p>
 *
 * <p>Dùng trong DirectChatPanel và GroupChatPanel:</p>
 * <pre>
 *   JPanel bubble = new ChatBubbleRenderer(msg, localUserId);
 *   messageContainer.add(bubble);
 * </pre>
 */
public final class ChatBubbleRenderer extends JPanel {

    // ── Màu sắc bubble ────────────────────────────────────────────
    private static final Color COLOR_MINE_BG    = new Color(0x3A, 0x7B, 0xD5);
    private static final Color COLOR_OTHER_BG   = new Color(0xE8, 0xEC, 0xF0);
    private static final Color COLOR_MINE_TEXT  = Color.WHITE;
    private static final Color COLOR_OTHER_TEXT = new Color(0x2C, 0x3E, 0x50);
    private static final Color COLOR_MINE_TIME  = new Color(0xBB, 0xD4, 0xF8);
    private static final Color COLOR_OTHER_TIME = new Color(0x95, 0xA5, 0xA6);
    private static final Color COLOR_SENDER     = new Color(0x3A, 0x7B, 0xD5);
    private static final Color COLOR_ROW_BG     = new Color(0xF5, 0xF7, 0xFA);

    // ── Font ─────────────────────────────────────────────────────
    private static final Font FONT_TEXT   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_TIME   = new Font("SansSerif", Font.PLAIN, 10);
    private static final Font FONT_SENDER = new Font("SansSerif", Font.BOLD,  11);

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    // Góc bo tròn của bubble
    private static final int ARC = 18;

    // Màu nền bubble (cần lưu để paintComponent dùng)
    private final Color bubbleColor;

    // ─────────────────────────────────────────────────────────────

    public ChatBubbleRenderer(ChatMessage msg, String localUserId) {
        boolean isMine = msg.senderId().equals(localUserId);
        this.bubbleColor = isMine ? COLOR_MINE_BG : COLOR_OTHER_BG;

        setLayout(new BorderLayout());
        setOpaque(false);
        // Margin dọc giữa các bubble
        setBorder(new EmptyBorder(4, 12, 4, 12));

        // Row căn phải hoặc trái
        JPanel row = new JPanel(new FlowLayout(
                isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.add(buildBubble(msg, isMine));
        add(row, BorderLayout.CENTER);
    }

    // ── Dựng bubble nội dung ──────────────────────────────────────
    private JPanel buildBubble(ChatMessage msg, boolean isMine) {
        JPanel bubble = new JPanel(new BorderLayout(0, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bubbleColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
                g2.dispose();
                // Không gọi super để tránh nền mặc định đè lên
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(10, 14, 8, 14));

        // ── Tên người gửi (chỉ hiển thị cho tin của người khác) ──
        if (!isMine) {
            JLabel senderLbl = new JLabel(msg.senderId());
            senderLbl.setFont(FONT_SENDER);
            senderLbl.setForeground(COLOR_SENDER);
            bubble.add(senderLbl, BorderLayout.NORTH);
        }

        // ── Nội dung tin nhắn ─────────────────────────────────────
        JTextArea textArea = new JTextArea(msg.text());
        textArea.setFont(FONT_TEXT);
        textArea.setForeground(isMine ? COLOR_MINE_TEXT : COLOR_OTHER_TEXT);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        // Width cứng để lineWrap hoạt động đúng trong BoxLayout
        textArea.setPreferredSize(new Dimension(260, Short.MAX_VALUE));
        bubble.add(textArea, BorderLayout.CENTER);

        // ── Footer: thời gian (+ ✓✓ cho tin của mình) ────────────
        JPanel footer = new JPanel(new FlowLayout(
                isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);

        String timeStr = TIME_FMT.format(msg.sentAt());
        JLabel timeLbl = new JLabel(isMine ? timeStr + "  \u2713\u2713" : timeStr);
        timeLbl.setFont(FONT_TIME);
        timeLbl.setForeground(isMine ? COLOR_MINE_TIME : COLOR_OTHER_TIME);
        footer.add(timeLbl);

        bubble.add(footer, BorderLayout.SOUTH);
        return bubble;
    }
}
