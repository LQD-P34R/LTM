package com.voicechat.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * TV3 – Bước 7: Panel quản lý cuộc gọi voice với 3 trạng thái.
 * <ul>
 *   <li>{@link CallState#IDLE} — Chưa có cuộc gọi nào.</li>
 *   <li>{@link CallState#RINGING} — Đang gọi ra, chờ đối phương trả lời.</li>
 *   <li>{@link CallState#IN_CALL} — Đang trong cuộc gọi, hiển thị timer.</li>
 * </ul>
 *
 * <p>Cuộc gọi đến được xử lý bởi {@link IncomingCallDialog}.
 * Khi người dùng chấp nhận, ChatFrame gọi {@code setState(IN_CALL, callerName)}.</p>
 */
public final class CallPanel extends JPanel {

    // ── Trạng thái ────────────────────────────────────────────────
    public enum CallState { IDLE, RINGING, IN_CALL }

    // ── Callback ──────────────────────────────────────────────────
    public interface CallListener {
        /** Người dùng huỷ cuộc gọi đang đổ chuông (RINGING → IDLE). */
        void onCallCancelled();
        /** Người dùng kết thúc cuộc gọi (IN_CALL → IDLE). */
        void onCallEnded();
        /** Người dùng toggle mute. */
        void onMuteToggled(boolean muted);
    }

    // ── Màu sắc ───────────────────────────────────────────────────
    private static final Color COLOR_BG        = new Color(0xF5, 0xF7, 0xFA);
    private static final Color COLOR_CARD_BG   = Color.WHITE;
    private static final Color COLOR_BORDER    = new Color(0xD0, 0xD8, 0xE4);
    private static final Color COLOR_TEXT_PRI  = new Color(0x2C, 0x3E, 0x50);
    private static final Color COLOR_TEXT_SEC  = new Color(0x7F, 0x8C, 0x8D);
    private static final Color COLOR_BTN_END   = new Color(0xE7, 0x4C, 0x3C);
    private static final Color COLOR_BTN_MUTE  = new Color(0xF3, 0x9C, 0x12);
    private static final Color COLOR_BTN_MUTED = new Color(0x7F, 0x8C, 0x8D);
    private static final Color COLOR_BTN_CANCEL= new Color(0x95, 0xA5, 0xA6);
    private static final Color COLOR_TIMER     = new Color(0x27, 0xAE, 0x60);
    private static final Color COLOR_RINGING   = new Color(0xF3, 0x9C, 0x12);
    private static final Color COLOR_IN_CALL   = new Color(0x27, 0xAE, 0x60);

    // ── Font ─────────────────────────────────────────────────────
    private static final Font FONT_ICON   = new Font("SansSerif", Font.PLAIN, 56);
    private static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD,  20);
    private static final Font FONT_SUB    = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_TIMER  = new Font("SansSerif", Font.BOLD,  36);
    private static final Font FONT_BTN    = new Font("SansSerif", Font.BOLD,  13);

    // ── Widgets ───────────────────────────────────────────────────
    private final CallListener listener;
    private CallState  state    = CallState.IDLE;
    private boolean    isMuted  = false;
    private int        callSecs = 0;
    private String     currentPeerName = "";
    private javax.swing.Timer callTimer;

    private final JLabel iconLbl   = new JLabel("", SwingConstants.CENTER);
    private final JLabel titleLbl  = new JLabel("", SwingConstants.CENTER);
    private final JLabel subLbl    = new JLabel("", SwingConstants.CENTER);
    private final JLabel timerLbl  = new JLabel("00:00", SwingConstants.CENTER);
    private final JButton btnMute  = new JButton();
    private final JButton btnEnd   = new JButton();
    private final JButton btnCancel= new JButton();
    private final JPanel btnPanel  = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));

    // ─────────────────────────────────────────────────────────────

    public CallPanel(CallListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        buildButtons();
        buildUI();
        applyState(CallState.IDLE, "");
    }

    // ── Khởi tạo nút (một lần) ───────────────────────────────────
    private void buildButtons() {
        styleBtn(btnMute,   "\uD83C\uDFA4 Mute",         COLOR_BTN_MUTE,   110, 44);
        styleBtn(btnEnd,    "\uD83D\uDCF5 K\u1EBFt th\u00FAc", COLOR_BTN_END, 130, 44);  // 📵 Kết thúc
        styleBtn(btnCancel, "\uD83D\uDEAB Hu\u1EF7 g\u1ECDi",  COLOR_BTN_CANCEL, 120, 44); // 🚫 Huỷ gọi

        btnMute.addActionListener(e -> {
            isMuted = !isMuted;
            setMuteState(isMuted);
            if (listener != null) listener.onMuteToggled(isMuted);
        });
        btnEnd.addActionListener(e -> {
            setState(CallState.IDLE, "");
            if (listener != null) listener.onCallEnded();
        });
        btnCancel.addActionListener(e -> {
            setState(CallState.IDLE, "");
            if (listener != null) listener.onCallCancelled();
        });
    }

    private void styleBtn(JButton btn, String text, Color bg, int w, int h) {
        btn.setText(text);
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setPreferredSize(new Dimension(w, h));
        final Color normalBg = bg;
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(normalBg.darker());
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    // Giữ màu MUTED nếu đang muted
                    btn.setBackground(btn == btnMute && isMuted ? COLOR_BTN_MUTED : normalBg);
                }
            }
        });
    }

    // ── Dựng layout card ─────────────────────────────────────────
    private void buildUI() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new EmptyBorder(60, 60, 60, 60));

        iconLbl.setFont(FONT_ICON);
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(COLOR_TEXT_PRI);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        subLbl.setFont(FONT_SUB);
        subLbl.setForeground(COLOR_TEXT_SEC);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        timerLbl.setFont(FONT_TIMER);
        timerLbl.setForeground(COLOR_TIMER);
        timerLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnPanel.setBackground(COLOR_CARD_BG);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLbl);
        card.add(Box.createVerticalStrut(20));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(subLbl);
        card.add(Box.createVerticalStrut(14));
        card.add(timerLbl);
        card.add(Box.createVerticalStrut(32));
        card.add(btnPanel);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(COLOR_BG);
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }

    // ── Áp dụng trạng thái ───────────────────────────────────────
    private void applyState(CallState newState, String peerName) {
        this.state = newState;
        this.currentPeerName = peerName;
        btnPanel.removeAll();
        stopTimer();
        timerLbl.setVisible(false);

        switch (newState) {
            case IDLE -> {
                iconLbl.setText("\uD83D\uDCDE"); // 📞
                iconLbl.setForeground(new Color(0xCC, 0xD5, 0xE0));
                titleLbl.setText("Ch\u01B0a c\u00F3 cu\u1ED9c g\u1ECDi n\u00E0o"); // Chưa có cuộc gọi nào
                subLbl.setText("Ch\u1ECDn m\u1ED9t li\u00EAn h\u1EC7 v\u00E0 nh\u1EA5n n\u00FAt G\u1ECDi \u0111\u1EC3 b\u1EAFt \u0111\u1EA7u");
            }
            case RINGING -> {
                iconLbl.setText("\uD83D\uDCF2"); // 📲
                iconLbl.setForeground(COLOR_RINGING);
                titleLbl.setText("\u0110ang g\u1ECDi \u0111\u1EBFn " + peerName); // Đang gọi đến
                subLbl.setText("\u0110ang ch\u1EDD \u0111\u1ED1i ph\u01B0\u01A1ng tr\u1EA3 l\u1EDDi..."); // Đang chờ đối phương trả lời...
                btnPanel.add(btnCancel);
            }
            case IN_CALL -> {
                iconLbl.setText("\uD83D\uDCDE"); // 📞
                iconLbl.setForeground(COLOR_IN_CALL);
                titleLbl.setText("\u0110ang g\u1ECDi v\u1EDBi " + peerName); // Đang gọi với
                subLbl.setText("Cu\u1ED9c g\u1ECDi \u0111ang di\u1EC5n ra"); // Cuộc gọi đang diễn ra
                timerLbl.setText("00:00");
                timerLbl.setVisible(true);
                callSecs = 0;
                isMuted  = false;
                setMuteState(false);
                btnPanel.add(btnMute);
                btnPanel.add(btnEnd);
                startTimer();
            }
        }

        btnPanel.revalidate();
        btnPanel.repaint();
        revalidate();
        repaint();
    }

    // ── Public API ────────────────────────────────────────────────

    /**
     * Chuyển trạng thái giao diện.
     * Phải gọi trên EDT.
     */
    public void setState(CallState newState, String peerName) {
        applyState(newState, peerName);
    }

    /**
     * Cập nhật trạng thái nút Mute/Unmute.
     * Phải gọi trên EDT.
     */
    public void setMuteState(boolean muted) {
        this.isMuted = muted;
        if (muted) {
            btnMute.setText("\uD83D\uDD07 Unmute"); // 🔇
            btnMute.setBackground(COLOR_BTN_MUTED);
        } else {
            btnMute.setText("\uD83C\uDFA4 Mute"); // 🎤
            btnMute.setBackground(COLOR_BTN_MUTE);
        }
    }

    // ── Timer ─────────────────────────────────────────────────────
    private void startTimer() {
        callTimer = new javax.swing.Timer(1000, e -> {
            callSecs++;
            int m = callSecs / 60;
            int s = callSecs % 60;
            timerLbl.setText(String.format("%02d:%02d", m, s));
        });
        callTimer.start();
    }

    private void stopTimer() {
        if (callTimer != null) {
            callTimer.stop();
            callTimer = null;
        }
        callSecs = 0;
    }
}
