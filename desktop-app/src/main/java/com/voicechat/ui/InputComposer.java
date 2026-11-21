package com.voicechat.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * VoiceLink InputComposer: Thanh nhập tin nhắn nổi ở đáy khung chat,
 * gồm nút emoji, ô nhập bo tròn dạng viên thuốc (pill-shape) và nút Gửi hình tròn.
 */
public final class InputComposer extends JPanel {

    @FunctionalInterface
    public interface SendListener {
        void onSend(String text);
    }

    private final UiUtils.RoundedTextField inputField = new UiUtils.RoundedTextField("Nhập tin nhắn...", UiUtils.ARC_XL);
    private final JButton btnEmoji = new JButton(UiIcon.SMILE); // 🙂
    private final SendListener sendListener;

    public InputComposer(SendListener listener) {
        this.sendListener = listener;

        setLayout(new BorderLayout(10, 0));
        setBackground(UiUtils.C_CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiUtils.C_BORDER),
                new EmptyBorder(12, 16, 12, 16)
        ));

        add(buildEmojiButton(), BorderLayout.WEST);
        add(buildInputField(),  BorderLayout.CENTER);
        add(buildSendButton(),  BorderLayout.EAST);
    }

    // ── Emoji button ──────────────────────────────────────────────
    private JButton buildEmojiButton() {
        btnEmoji.setMargin(new Insets(0,0,0,0));
        btnEmoji.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        btnEmoji.setForeground(UiUtils.C_TEXT_MUTED);
        btnEmoji.setBorderPainted(false);
        btnEmoji.setFocusPainted(false);
        btnEmoji.setOpaque(false);
        btnEmoji.setContentAreaFilled(false);
        btnEmoji.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEmoji.setPreferredSize(new Dimension(38, 38));
        btnEmoji.setToolTipText("Chọn biểu tượng cảm xúc");

        JPopupMenu emojiMenu = new JPopupMenu();
        emojiMenu.setBorder(BorderFactory.createLineBorder(UiUtils.C_BORDER, 1));
        emojiMenu.setBackground(Color.WHITE);

        String[] emojis = {
            "\uD83D\uDE0A", "\uD83D\uDE02", "\u2764\uFE0F", "\uD83D\uDC4D", "\uD83C\uDF89",
            "\uD83D\uDD25", "\u2705",        "\uD83D\uDE4F", "\uD83D\uDE22", "\uD83D\uDE2E",
            "\uD83D\uDC4F", "\uD83D\uDCAA", "\uD83E\uDD14", "\uD83D\uDE0E", "\uD83E\uDD73"
        };

        JPanel grid = new JPanel(new GridLayout(3, 5, 4, 4));
        grid.setBorder(new EmptyBorder(8, 8, 8, 8));
        grid.setBackground(Color.WHITE);

        for (String em : emojis) {
            JButton eb = new JButton(em);
            eb.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            eb.setMargin(new Insets(3,3,3,3));
            if (eb.getFont().canDisplayUpTo(em.replace("\uFE0F","")) >= 0) continue;
            eb.setBorderPainted(false);
            eb.setFocusPainted(false);
            eb.setBackground(Color.WHITE);
            eb.setOpaque(true);
            eb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eb.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { eb.setBackground(UiUtils.C_HOVER_BG); }
                @Override public void mouseExited(MouseEvent e)  { eb.setBackground(Color.WHITE); }
            });
            eb.addActionListener(e -> {
                insertAtCaret(em);
                emojiMenu.setVisible(false);
                inputField.requestFocusInWindow();
            });
            grid.add(eb);
        }
        emojiMenu.add(grid);

        btnEmoji.addActionListener(e ->
                emojiMenu.show(btnEmoji, 0, -emojiMenu.getPreferredSize().height - 4));

        return btnEmoji;
    }

    // ── Pill-shape Input field ────────────────────────────────────
    private JTextField buildInputField() {
        inputField.setFont(UiUtils.F_BODY);
        inputField.setBgColor(UiUtils.C_SIDEBAR_LIGHT);
        inputField.setPreferredSize(new Dimension(300, 42));
        inputField.addActionListener(e -> handleSend());
        return inputField;
    }

    // ── Circular Send button (Nút gửi hình tròn) ──────────────────
    private JButton buildSendButton() {
        JButton btnSend = new JButton() {
            private boolean isHover = false;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.enableAntiAliasing(g2);
                g2.setColor(isHover ? UiUtils.C_PRIMARY_HOVER : UiUtils.C_PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());

                // Vẽ mũi tên gửi giấy (Paper plane hoặc mũi tên)
                g2.setColor(Color.WHITE);
                UiIcon.SEND.draw(g2,(getWidth()-20)/2,(getHeight()-20)/2,20,Color.WHITE);
                g2.dispose();
            }
        };

        btnSend.setPreferredSize(new Dimension(42, 42));
        btnSend.setOpaque(false);
        btnSend.setContentAreaFilled(false);
        btnSend.setBorderPainted(false);
        btnSend.setFocusPainted(false);
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.setToolTipText("Gửi tin nhắn (Enter)");

        btnSend.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnSend.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btnSend.repaint(); }
        });

        btnSend.addActionListener(e -> handleSend());
        return btnSend;
    }

    private void handleSend() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        if (sendListener != null) {
            sendListener.onSend(text);
        }
        inputField.setText("");
        inputField.requestFocusInWindow();
    }

    private void insertAtCaret(String str) {
        int pos = inputField.getCaretPosition();
        String current = inputField.getText();
        String updated = current.substring(0, pos) + str + current.substring(pos);
        inputField.setText(updated);
        inputField.setCaretPosition(pos + str.length());
    }

    public void requestInputFocus() {
        inputField.requestFocusInWindow();
    }

    public void clear() {
        inputField.setText("");
    }

    public String getText() {
        return inputField.getText().trim();
    }

    public void setText(String text) {
        inputField.setText(text != null ? text : "");
    }
}
