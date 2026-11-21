package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Responsive message row pinned directly to EAST/WEST so it stays close to the right edge. */
public final class ChatBubbleRenderer extends JPanel {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
    private static final int ARC = 20;
    private final boolean mine;
    private final JPanel bubble;
    private final JTextArea content;
    private final JLabel sender;
    private final JLabel timestamp;
    private final int padWidth = 32;

    public ChatBubbleRenderer(ChatMessage msg, String localUserId) {
        this.mine = msg.senderId() != null && msg.senderId().equalsIgnoreCase(localUserId);
        setLayout(new BorderLayout());
        setOpaque(false);
        // Cách lề phải đúng 12px (rất gần đường kẻ cột phải), cách lề trái 16px
        setBorder(new EmptyBorder(4, 12, 4, 2));

        bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.enableAntiAliasing(g2);
                g2.setColor(mine ? UiUtils.C_BUBBLE_OUTGOING : UiUtils.C_BUBBLE_INCOMING);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(10, 16, 10, 16));

        sender = new JLabel(msg.senderId());
        sender.setFont(UiUtils.F_SMALL_BOLD);
        sender.setForeground(UiUtils.C_TEXT_PRIMARY);
        if (!mine) {
            sender.setBorder(new EmptyBorder(0, 0, 4, 0));
            bubble.add(sender, BorderLayout.NORTH);
        }

        content = new JTextArea(msg.text() == null ? "" : msg.text().trim());
        content.setFont(UiUtils.F_BODY);
        content.setForeground(mine ? Color.WHITE : UiUtils.C_TEXT_PRIMARY);
        content.setOpaque(false);
        content.setEditable(false);
        content.setFocusable(false);
        content.setMargin(new Insets(0, 0, 0, 0));
        content.setBorder(new EmptyBorder(0, 0, 4, 0));
        content.setLineWrap(true);
        content.setWrapStyleWord(true);

        timestamp = new JLabel(TIME_FMT.format(msg.sentAt()), SwingConstants.LEFT);
        timestamp.setFont(UiTheme.REGULAR_FONT.deriveFont(11f));
        timestamp.setForeground(mine ? new Color(255,251,242,220) : UiUtils.C_TEXT_MUTED);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(content, BorderLayout.CENTER);
        body.add(timestamp, BorderLayout.SOUTH);
        bubble.add(body, BorderLayout.CENTER);

        if (mine) {
            // Đẩy trực tiếp sát sang mép phải
            add(bubble, BorderLayout.EAST);
        } else {
            JPanel leftGroup = new JPanel(new BorderLayout(8, 0));
            leftGroup.setOpaque(false);
            JPanel avatarWrap = new JPanel(new BorderLayout());
            avatarWrap.setOpaque(false);
            avatarWrap.add(new UiUtils.AvatarBadge(msg.senderId(), 28, false, false, false), BorderLayout.NORTH);
            leftGroup.add(avatarWrap, BorderLayout.WEST);
            leftGroup.add(bubble, BorderLayout.CENTER);
            add(leftGroup, BorderLayout.WEST);
        }

        updateForAvailableWidth(650);
    }

    private int availableWidth() {
        Container parent = getParent();
        while (parent != null) {
            if (parent instanceof JViewport viewport && viewport.getWidth() > 0) {
                return viewport.getWidth();
            }
            parent = parent.getParent();
        }
        return 650;
    }

    private void updateForAvailableWidth(int viewportWidth) {
        int maxBubbleWidth = Math.max(140, (int) (viewportWidth * 0.72));
        FontMetrics metrics = content.getFontMetrics(content.getFont());
        FontMetrics timeMetrics = timestamp.getFontMetrics(timestamp.getFont());

        int maxTextWidth = Math.max(60, maxBubbleWidth - padWidth - (!mine ? 36 : 0));
        String[] lines = content.getText().split("\\R", -1);
        int widestLine = timeMetrics.stringWidth(timestamp.getText()) + 8;
        for (String line : lines) {
            widestLine = Math.max(widestLine, metrics.stringWidth(line) + 8);
        }

        int textWidth = Math.min(maxTextWidth, widestLine);
        content.setPreferredSize(null);
        content.setSize(new Dimension(textWidth, Short.MAX_VALUE));
        Dimension pref = content.getPreferredSize();
        int textHeight = Math.max(metrics.getHeight(), pref.height) + 4;
        content.setPreferredSize(new Dimension(textWidth, textHeight));

        int bubbleWidth = textWidth + padWidth;
        int senderHeight = mine ? 0 : sender.getPreferredSize().height + 4;
        int timeHeight = timeMetrics.getHeight() + 2;
        int bubbleHeight = textHeight + senderHeight + timeHeight + 20;

        Dimension bubbleDim = new Dimension(bubbleWidth, bubbleHeight);
        bubble.setPreferredSize(bubbleDim);
        bubble.setMinimumSize(bubbleDim);
        bubble.setMaximumSize(bubbleDim);
        revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        updateForAvailableWidth(availableWidth());
        return super.getPreferredSize();
    }

    private void refreshHeightConstraint(int width) {
        updateForAvailableWidth(width);
        Dimension preferred = super.getPreferredSize();
        setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(() -> {
            Container p = getParent();
            while (p != null && !(p instanceof JViewport)) {
                p = p.getParent();
            }
            if (p instanceof JViewport viewport) {
                viewport.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        refreshHeightConstraint(viewport.getWidth());
                    }
                });
                refreshHeightConstraint(viewport.getWidth());
            }
        });
    }
}
