package com.voicechat.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

/**
 * TV3 – Bước 3B: Vùng nhập tin nhắn gồm nút emoji, text field và nút Gửi.
 * <p>Giao tiếp với panel cha qua {@link SendListener}.</p>
 *
 * <p>Dùng trong DirectChatPanel, GroupChatPanel:</p>
 * <pre>
 *   InputComposer composer = new InputComposer(text -> chatService.sendText(...));
 * </pre>
 */
public final class InputComposer extends JPanel {

    // ── Callback ──────────────────────────────────────────────────
    @FunctionalInterface
    public interface SendListener {
        /**
         * Được gọi khi người dùng nhấn Gửi hoặc Enter.
         * {@code text} đã trim, không rỗng, không phải placeholder.
         */
        void onSend(String text);
    }

    // ── Màu sắc ───────────────────────────────────────────────────
    private static final Color COLOR_BG           = Color.WHITE;
    private static final Color COLOR_BORDER_TOP   = new Color(0xD0, 0xD8, 0xE4);
    private static final Color COLOR_FIELD_BG     = new Color(0xF5, 0xF7, 0xFA);
    private static final Color COLOR_FIELD_BORDER = new Color(0xCC, 0xD5, 0xE0);
    private static final Color COLOR_FIELD_FOCUS  = new Color(0x3A, 0x7B, 0xD5);
    private static final Color COLOR_TEXT         = new Color(0x2C, 0x3E, 0x50);
    private static final Color COLOR_PLACEHOLDER  = new Color(0xA0, 0xAE, 0xBE);
    private static final Color COLOR_BTN          = new Color(0x3A, 0x7B, 0xD5);
    private static final Color COLOR_BTN_HOVER    = new Color(0x2D, 0x64, 0xB8);
    private static final Color COLOR_BTN_DISABLED = new Color(0xBD, 0xC3, 0xC7);

    // ── Font ─────────────────────────────────────────────────────
    private static final Font FONT_FIELD = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_BTN   = new Font("SansSerif", Font.BOLD,  13);
    private static final Font FONT_EMOJI = new Font("SansSerif", Font.PLAIN, 18);

    private static final String PLACEHOLDER = "Nhập tin nhắn...";

    // ── Widgets ───────────────────────────────────────────────────
    private final JTextField  inputField = new JTextField();
    private final JButton     btnSend    = new JButton("G\u1EEDi \u2192");   // "Gửi →"
    private final JButton     btnEmoji   = new JButton("\uD83D\uDE42");       // 🙂

    private final SendListener sendListener;

    // ─────────────────────────────────────────────────────────────

    public InputComposer(SendListener listener) {
        this.sendListener = listener;

        setLayout(new BorderLayout(8, 0));
        setBackground(COLOR_BG);
        setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, COLOR_BORDER_TOP),
                new EmptyBorder(10, 12, 10, 12)
        ));

        add(buildEmojiButton(), BorderLayout.WEST);
        add(buildInputField(),  BorderLayout.CENTER);
        add(buildSendButton(),  BorderLayout.EAST);
    }

    // ── Emoji button ──────────────────────────────────────────────
    private JButton buildEmojiButton() {
        btnEmoji.setFont(FONT_EMOJI);
        btnEmoji.setBackground(COLOR_BG);
        btnEmoji.setForeground(COLOR_TEXT);
        btnEmoji.setBorderPainted(false);
        btnEmoji.setFocusPainted(false);
        btnEmoji.setOpaque(false);
        btnEmoji.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEmoji.setPreferredSize(new Dimension(38, 36));
        btnEmoji.setToolTipText("Ch\u1ECBn emoji");

        // Popup menu 15 emoji phổ biến
        JPopupMenu emojiMenu = new JPopupMenu();
        emojiMenu.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_TOP, 1));

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
            eb.setFont(new Font("SansSerif", Font.PLAIN, 20));
            eb.setBorderPainted(false);
            eb.setFocusPainted(false);
            eb.setBackground(Color.WHITE);
            eb.setOpaque(true);
            eb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eb.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    eb.setBackground(new Color(0xEE, 0xF2, 0xF7));
                }
                @Override public void mouseExited(MouseEvent e) {
                    eb.setBackground(Color.WHITE);
                }
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

    // ── Input field ───────────────────────────────────────────────
    private JTextField buildInputField() {
        inputField.setText(PLACEHOLDER);
        inputField.setForeground(COLOR_PLACEHOLDER);
        inputField.setFont(FONT_FIELD);
        inputField.setBackground(COLOR_FIELD_BG);
        inputField.setCaretColor(COLOR_FIELD_FOCUS);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_FIELD_BORDER, 1, true),
                new EmptyBorder(7, 12, 7, 12)
        ));

        // Placeholder focus handler
        inputField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (inputField.getText().equals(PLACEHOLDER)) {
                    inputField.setText("");
                    inputField.setForeground(COLOR_TEXT);
                }
                inputField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_FIELD_FOCUS, 2, true),
                        new EmptyBorder(6, 11, 6, 11)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                if (inputField.getText().isBlank()) {
                    inputField.setText(PLACEHOLDER);
                    inputField.setForeground(COLOR_PLACEHOLDER);
                }
                inputField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_FIELD_BORDER, 1, true),
                        new EmptyBorder(7, 12, 7, 12)
                ));
            }
        });

        // Enter → gửi
        inputField.addActionListener(e -> doSend());

        // Sync trạng thái nút Gửi theo nội dung
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            private void sync() {
                boolean hasText = !inputField.getText().isBlank()
                        && !inputField.getText().equals(PLACEHOLDER);
                btnSend.setEnabled(hasText);
                btnSend.setBackground(hasText ? COLOR_BTN : COLOR_BTN_DISABLED);
            }
            @Override public void insertUpdate(DocumentEvent e)  { sync(); }
            @Override public void removeUpdate(DocumentEvent e)  { sync(); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); }
        });

        return inputField;
    }

    // ── Send button ───────────────────────────────────────────────
    private JButton buildSendButton() {
        btnSend.setFont(FONT_BTN);
        btnSend.setForeground(Color.WHITE);
        btnSend.setBackground(COLOR_BTN_DISABLED);
        btnSend.setEnabled(false);
        btnSend.setBorderPainted(false);
        btnSend.setFocusPainted(false);
        btnSend.setOpaque(true);
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnSend.setPreferredSize(new Dimension(80, 36));

        btnSend.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btnSend.isEnabled()) btnSend.setBackground(COLOR_BTN_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btnSend.isEnabled()) btnSend.setBackground(COLOR_BTN);
            }
        });
        btnSend.addActionListener(e -> doSend());

        return btnSend;
    }

    // ── Logic ─────────────────────────────────────────────────────

    private void doSend() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || text.equals(PLACEHOLDER)) return;
        clear();
        if (sendListener != null) sendListener.onSend(text);
    }

    /** Chèn ký tự emoji tại vị trí con trỏ trong input field. */
    private void insertAtCaret(String text) {
        if (inputField.getText().equals(PLACEHOLDER)) {
            inputField.setText("");
            inputField.setForeground(COLOR_TEXT);
        }
        int pos = inputField.getCaretPosition();
        try {
            inputField.getDocument().insertString(pos, text, null);
        } catch (javax.swing.text.BadLocationException ignored) {
            inputField.setText(inputField.getText() + text);
        }
    }

    // ── Public API ────────────────────────────────────────────────

    /**
     * Xoá nội dung field và reset về trạng thái ban đầu.
     * Phải gọi trên EDT.
     */
    public void clear() {
        inputField.setText("");
        btnSend.setEnabled(false);
        btnSend.setBackground(COLOR_BTN_DISABLED);
        inputField.requestFocusInWindow();
    }

    /**
     * Bật/tắt toàn bộ composer (ví dụ khi chưa có kết nối mạng).
     * Phải gọi trên EDT.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        inputField.setEnabled(enabled);
        btnEmoji.setEnabled(enabled);
        boolean canSend = enabled
                && !inputField.getText().isBlank()
                && !inputField.getText().equals(PLACEHOLDER);
        btnSend.setEnabled(canSend);
        btnSend.setBackground(canSend ? COLOR_BTN : COLOR_BTN_DISABLED);
    }

    /**
     * Đặt focus vào input field.
     * Tiện gọi từ ChatFrame sau khi chuyển CardLayout panel.
     */
    public void focusInput() {
        inputField.requestFocusInWindow();
    }
}
