package com.voicechat.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * VoiceLink CallPanel: Giao diện cuộc gọi voice chuẩn VoiceLink (Page 3 trong
 * PDF).
 * <p>
 * Hiển thị thẻ thành viên với Avatar lớn ở trung tâm (có hiệu ứng vòng hào
 * quang phát sáng khi đang nói),
 * tên to đậm bên dưới, đồng hồ đếm thời gian và thanh điều khiển nổi (Floating
 * Control Bar) bo tròn ở cạnh dưới.
 * </p>
 */
public final class CallPanel extends JPanel {
    // ===== Dữ liệu và các kiểu hỗ trợ =====
    // Các trường lưu cấu hình/trạng thái; các kiểu lồng nhau mô tả dữ liệu và hợp đồng dùng trong lớp.
    public enum CallState {
        IDLE, RINGING, IN_CALL
    }
    public interface CallListener {

        // ===== Sự kiện nút điều khiển gọi =====
        // Giao diện báo thao tác hủy, kết thúc hoặc đổi mute để lớp điều phối xử lý.
        void onCallCancelled();

        void onCallEnded();

        void onMuteToggled(boolean muted);
    }
    private final CallListener listener;
    private CallState state = CallState.IDLE;
    private boolean isMuted = false;
    private int callSecs = 0;
    private String currentPeerName = "";
    private javax.swing.Timer callTimer;
    // Center elements
    private final UiUtils.AvatarBadge avatarBadge = new UiUtils.AvatarBadge("", 96, false, true, false);
    private final JLabel titleLbl = new JLabel("", SwingConstants.CENTER);
    private final JLabel subLbl = new JLabel("", SwingConstants.CENTER);
    private final JLabel timerLbl = new JLabel("00:00", SwingConstants.CENTER);
    // Floating Control buttons
    private final JButton btnMute = createCircularBtn("\uD83C\uDFA4", "Tắt/Bật mic", UiUtils.C_SIDEBAR_LIGHT,
            UiUtils.C_TEXT_PRIMARY); // 🎤
    private final JButton btnSpeaker = createCircularBtn("\uD83D\uDD0A", "Loa ngoài", UiUtils.C_SIDEBAR_LIGHT,
            UiUtils.C_TEXT_PRIMARY); // 🔊
    private final JButton btnEnd = createCircularBtn("\uD83D\uDCF5", "Kết thúc cuộc gọi", UiUtils.C_DANGER,
            Color.WHITE); // 📵
    private final JButton btnCancel = createCircularBtn("\u2715", "Huỷ gọi", UiUtils.C_DANGER, Color.WHITE); // ✕
    private final JPanel floatingBar = new UiUtils.RoundedPanel(
            UiUtils.ARC_XL, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);

    // ===== Khởi tạo và dựng giao diện gọi =====
    // Tạo thẻ thông tin người gọi, các nút điều khiển và trạng thái ban đầu.
    public CallPanel(CallListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(UiUtils.C_APP_BG);

        buildUI();
        initActions();
        applyState(CallState.IDLE, "");
    }

    private void buildUI() {
        // Trung tâm: Thẻ thành viên
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        UiUtils.RoundedPanel memberCard = new UiUtils.RoundedPanel(
                UiUtils.ARC_LG, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
        memberCard.setLayout(new BoxLayout(memberCard, BoxLayout.Y_AXIS));
        memberCard.setBorder(new EmptyBorder(40, 50, 40, 50));
        memberCard.setPreferredSize(new Dimension(460, 380));

        avatarBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        titleLbl.setFont(UiUtils.F_TITLE_LG);
        titleLbl.setForeground(UiUtils.C_TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        subLbl.setFont(UiUtils.F_BODY);
        subLbl.setForeground(UiUtils.C_TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        timerLbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        timerLbl.setForeground(UiUtils.C_ONLINE);
        timerLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        memberCard.add(avatarBadge);
        memberCard.add(Box.createVerticalStrut(20));
        memberCard.add(titleLbl);
        memberCard.add(Box.createVerticalStrut(8));
        memberCard.add(subLbl);
        memberCard.add(Box.createVerticalStrut(14));
        memberCard.add(timerLbl);

        centerWrapper.add(memberCard);
        add(centerWrapper, BorderLayout.CENTER);

        // Đáy: Thanh điều khiển nổi (Floating Control Bar)
        JPanel bottomWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
        bottomWrap.setOpaque(false);

        floatingBar.setLayout(new FlowLayout(FlowLayout.CENTER, 18, 10));
        floatingBar.setBorder(new EmptyBorder(4, 16, 4, 16));

        floatingBar.add(btnMute);
        floatingBar.add(btnSpeaker);
        floatingBar.add(btnEnd);
        floatingBar.add(btnCancel);

        bottomWrap.add(floatingBar);
        add(bottomWrap, BorderLayout.SOUTH);
    }

    private static JButton createCircularBtn(String icon, String tooltip, Color bg, Color fg) {
        JButton btn = new JButton(icon) {
            private boolean isHover = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.enableAntiAliasing(g2);
                Color c = getBackground();
                if (isHover)
                    c = c.darker();
                g2.setColor(c);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setPreferredSize(new Dimension(50, 50));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.repaint();
            }
        });
        return btn;
    }

    // ===== Gắn sự kiện thao tác cuộc gọi =====
    // Chuyển thao tác mute, kết thúc hoặc hủy gọi cho CallListener.
    private void initActions() {
        btnMute.addActionListener(e -> {
            isMuted = !isMuted;
            setMuteState(isMuted);
            if (listener != null)
                listener.onMuteToggled(isMuted);
        });

        btnEnd.addActionListener(e -> {
            setState(CallState.IDLE, "");
            if (listener != null)
                listener.onCallEnded();
        });

        btnCancel.addActionListener(e -> {
            setState(CallState.IDLE, "");
            if (listener != null)
                listener.onCallCancelled();
        });
    }

    // ===== Cập nhật và đọc trạng thái cuộc gọi =====
    // Đổi nội dung/nút theo IDLE, RINGING, IN_CALL và đồng bộ trạng thái microphone.
    public void setState(CallState newState, String peerName) {
        if (newState != this.state)
            setMuteState(false);
        this.state = newState;
        this.currentPeerName = peerName != null ? peerName : "";
        applyState(newState, currentPeerName);
    }

    private void applyState(CallState s, String name) {
        stopTimer();
        switch (s) {
            case IDLE -> {
                avatarBadge.setName("VoiceLink");
                avatarBadge.setSpeaking(false);
                titleLbl.setText("Chưa có cuộc gọi nào");
                subLbl.setText("Chọn một liên hệ từ danh bạ bên trái để bắt đầu gọi");
                timerLbl.setVisible(false);

                btnMute.setVisible(false);
                btnSpeaker.setVisible(false);
                btnEnd.setVisible(false);
                btnCancel.setVisible(false);
                floatingBar.setVisible(false);
            }
            case RINGING -> {
                avatarBadge.setName(name);
                avatarBadge.setSpeaking(true);
                titleLbl.setText("Đang gọi đến " + name);
                subLbl.setText("Đang chờ đối phương trả lời...");
                timerLbl.setVisible(false);

                btnMute.setVisible(false);
                btnSpeaker.setVisible(false);
                btnEnd.setVisible(false);
                btnCancel.setVisible(true);
                floatingBar.setVisible(true);
            }
            case IN_CALL -> {
                avatarBadge.setName(name);
                avatarBadge.setSpeaking(true);
                titleLbl.setText(name);
                subLbl.setText("Cuộc gọi thoại đang diễn ra • Kết nối P2P trực tiếp");
                timerLbl.setText("00:00");
                timerLbl.setVisible(true);

                btnMute.setVisible(true);
                btnSpeaker.setVisible(true);
                btnEnd.setVisible(true);
                btnCancel.setVisible(false);
                floatingBar.setVisible(true);
                startTimer();
            }
        }
        revalidate();
        repaint();
    }

    public void setMuteState(boolean muted) {
        this.isMuted = muted;
        if (muted) {
            btnMute.setBackground(UiUtils.C_WARNING);
            btnMute.setForeground(Color.WHITE);
            btnMute.setToolTipText("Bật mic (đang tắt)");
        } else {
            btnMute.setBackground(UiUtils.C_SIDEBAR_LIGHT);
            btnMute.setForeground(UiUtils.C_TEXT_PRIMARY);
            btnMute.setToolTipText("Tắt mic");
        }
        btnMute.repaint();
    }

    public CallState getState() {
        return state;
    }

    public String getCurrentPeerName() {
        return currentPeerName;
    }

    public int getCallDurationSeconds() {
        return callSecs;
    }

    // ===== Đồng hồ cuộc gọi =====
    // Bắt đầu đếm thời gian khi đang gọi và dừng/reset khi chuyển trạng thái.
    private void startTimer() {
        callSecs = 0;
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
