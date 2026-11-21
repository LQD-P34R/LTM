package com.voicechat.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Full-screen call view with a dark canvas, vector controls and call states. */
public final class CallPanel extends JPanel {
    public enum CallState { IDLE, RINGING, OUTGOING, INCOMING, IN_CALL, CONNECTED, MUTED, ENDED }
    public interface CallListener {
        void onCallCancelled(); void onCallEnded(); void onMuteToggled(boolean muted);
        default void onReturnToChat() { onCallCancelled(); }
        default void onCallAccepted() {}
    }
    private static final Color NAVY = UiUtils.C_APP_BG;
    private static final Color BLUE = UiUtils.C_PRIMARY_TEXT;
    private static final Color PURPLE = UiUtils.C_DANGER;
    private final CallListener listener;
    private CallState state = CallState.IDLE;
    private boolean muted, speakerOn = true;
    private int callSecs;
    private String currentPeerName = "";
    private Timer callTimer;
    private final JLabel title = new JLabel("", SwingConstants.CENTER);
    private final JLabel status = new JLabel("", SwingConstants.CENTER);
    private final JLabel timer = new JLabel("00:00", SwingConstants.CENTER);
    private final UiUtils.AvatarBadge avatar = new UiUtils.AvatarBadge("", 104, false, true, false);
    private final JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 32, 0));
    private final JButton mute = control(AppIcons.Kind.MIC, "Tắt mic", UiUtils.C_CALL_CTRL_BG, UiUtils.C_TEXT_PRIMARY);
    private final JButton speaker = control(AppIcons.Kind.SPEAKER, "Tắt loa", UiUtils.C_CALL_CTRL_BG, UiUtils.C_TEXT_PRIMARY);
    private final JButton hangup = control(AppIcons.Kind.HANGUP, "Kết thúc cuộc gọi", UiUtils.C_DANGER, Color.WHITE);
    private final JButton accept = control(AppIcons.Kind.PHONE, "Chấp nhận", UiUtils.C_ONLINE, UiUtils.C_TEXT_PRIMARY);
    private final JButton reject = control(AppIcons.Kind.HANGUP, "Từ chối", UiUtils.C_DANGER, Color.WHITE);
    private final JButton switchChat = control(AppIcons.Kind.CHAT, "Chuyển sang chat", UiUtils.C_CALL_CTRL_BG, UiUtils.C_TEXT_PRIMARY);

    public CallPanel(CallListener listener) {
        this.listener = listener; setLayout(new BorderLayout()); setBackground(NAVY);
        add(buildTopBar(), BorderLayout.NORTH); add(buildCenter(), BorderLayout.CENTER);
        controls.setOpaque(false); controls.setBorder(new EmptyBorder(0, 0, 44, 0)); add(controls, BorderLayout.SOUTH);
        mute.addActionListener(e -> toggleMute());
        speaker.addActionListener(e -> { speakerOn = !speakerOn; speaker.setIcon(new AppIcons(speakerOn ? AppIcons.Kind.SPEAKER : AppIcons.Kind.SPEAKER_OFF, 22, UiUtils.C_TEXT_PRIMARY)); speaker.setToolTipText(speakerOn ? "Tắt loa" : "Bật loa"); });
        hangup.addActionListener(e -> { finish(true); if (listener != null) listener.onReturnToChat(); });
        reject.addActionListener(e -> finish(false));
        accept.addActionListener(e -> { setState(CallState.CONNECTED, currentPeerName); if (listener != null) listener.onCallAccepted(); });
        switchChat.addActionListener(e->{if(listener!=null)listener.onReturnToChat();});
        applyState(CallState.IDLE, "");
    }
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()); bar.setOpaque(false); bar.setPreferredSize(new Dimension(0,64));
        bar.setBorder(new EmptyBorder(0, 22, 0, 28));
        JButton back = new JButton("< Quay lại chat");
        back.setForeground(UiUtils.C_TEXT_MUTED); back.setFont(UiUtils.F_BODY); back.setOpaque(false); back.setContentAreaFilled(false);
        back.setBorderPainted(false); back.setFocusPainted(false); back.addActionListener(e -> { if (listener != null) listener.onReturnToChat(); });
        bar.add(back,BorderLayout.WEST);
        JPanel connection=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); connection.setOpaque(true); connection.setBackground(UiUtils.C_SIDEBAR_LIGHT);
        connection.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiUtils.C_BORDER),new EmptyBorder(5,10,5,10)));
        JPanel dot=new JPanel(); dot.setPreferredSize(new Dimension(8,8)); dot.setBackground(UiUtils.C_ONLINE); dot.setOpaque(true); connection.add(dot);
        JLabel label=new JLabel("P2P  •  Chưa kết nối"); label.setFont(UiTheme.REGULAR_FONT.deriveFont(12f)); label.setForeground(UiUtils.C_TEXT_PRIMARY); connection.add(label);
        connection.setPreferredSize(new Dimension(225,32)); bar.add(connection,BorderLayout.EAST); return bar;
    }
    private JComponent buildCenter() {
        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);
        JPanel stack = new JPanel(); stack.setOpaque(false); stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        JPanel halo = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int d=Math.min(getWidth(),getHeight()); int cx=(getWidth()-d)/2, cy=(getHeight()-d)/2;
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(BLUE.getRed(),BLUE.getGreen(),BLUE.getBlue(),135)); g2.drawOval(cx+4,cy+4,d-8,d-8);
                g2.setColor(new Color(PURPLE.getRed(),PURPLE.getGreen(),PURPLE.getBlue(),115)); g2.drawOval(cx+18,cy+18,d-36,d-36);
                g2.setColor(new Color(BLUE.getRed(),BLUE.getGreen(),BLUE.getBlue(),185)); g2.drawOval(cx+32,cy+32,d-64,d-64); g2.dispose();
            }
        };
        halo.setOpaque(false); halo.setPreferredSize(new Dimension(228,228)); halo.add(avatar);
        title.setFont(UiUtils.F_TITLE_XL); title.setForeground(UiUtils.C_TEXT_PRIMARY); title.setAlignmentX(Component.CENTER_ALIGNMENT);
        status.setFont(UiUtils.F_BODY); status.setForeground(UiUtils.C_TEXT_MUTED); status.setAlignmentX(Component.CENTER_ALIGNMENT);
        timer.setFont(new Font("Segoe UI",Font.BOLD,28)); timer.setForeground(UiUtils.C_TEXT_PRIMARY); timer.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel waveform = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiUtils.C_PRIMARY_TEXT); int[] heights={10,18,27,15,30,22,34,18,28,13,31,20,12,26,16,30,19,11,24,16,10};
                int startX=(getWidth()-143)/2;
                for(int i=0;i<heights.length;i++) g2.fillRoundRect(startX+i*7, (42-heights[i])/2, 3, heights[i], 3, 3); g2.dispose();
            }
        };
        waveform.setOpaque(false); waveform.setPreferredSize(new Dimension(143,42)); waveform.setAlignmentX(Component.CENTER_ALIGNMENT);
        stack.add(halo); stack.add(Box.createVerticalStrut(26)); stack.add(title); stack.add(Box.createVerticalStrut(8));
        stack.add(status); stack.add(Box.createVerticalStrut(8)); stack.add(timer); stack.add(Box.createVerticalStrut(10));
        stack.add(waveform);
        center.add(stack); return center;
    }
    private static JButton control(AppIcons.Kind kind, String tip, Color bg, Color fg) {
        JButton b = new JButton(new AppIcons(kind,22,fg)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); UiUtils.enableAntiAliasing(g2);
                g2.setColor(bg); g2.fillOval(0,0,getWidth()-1,getHeight()-1); g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setToolTipText(tip); b.setBackground(bg); b.setForeground(fg);
        b.setPreferredSize(new Dimension(52,52)); b.setMinimumSize(new Dimension(52,52)); b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        return b;
    }
    private JPanel labeledControl(JButton button,String text) {
        JPanel item=new JPanel(); item.setOpaque(false); item.setLayout(new BoxLayout(item,BoxLayout.Y_AXIS));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel caption=new JLabel(text); caption.setFont(UiUtils.F_SMALL); caption.setForeground(UiUtils.C_TEXT_MUTED);
        caption.setAlignmentX(Component.CENTER_ALIGNMENT); item.add(button); item.add(Box.createVerticalStrut(7)); item.add(caption); return item;
    }
    private void toggleMute() {
        muted = !muted; mute.setIcon(new AppIcons(muted ? AppIcons.Kind.MIC_OFF : AppIcons.Kind.MIC,22,UiUtils.C_TEXT_PRIMARY));
        mute.setToolTipText(muted ? "Bật mic" : "Tắt mic"); if (listener != null) listener.onMuteToggled(muted);
        if (state == CallState.CONNECTED || state == CallState.IN_CALL) { state=muted?CallState.MUTED:CallState.CONNECTED; status.setText(muted?"Đã tắt micro":"Đang kết nối"); }
    }
    public void setState(CallState newState, String name) { state=newState; currentPeerName=name==null?"":name; applyState(newState,currentPeerName); }
    public void setMuteState(boolean value) { muted=value; mute.setIcon(new AppIcons(value?AppIcons.Kind.MIC_OFF:AppIcons.Kind.MIC,22,UiUtils.C_TEXT_PRIMARY)); }
    public CallState getState(){return state;} public String getCurrentPeerName(){return currentPeerName;} public int getCallDurationSeconds(){return callSecs;}
    private void applyState(CallState value, String name) {
        if(callTimer!=null){callTimer.stop();callTimer=null;} callSecs=0;
        avatar.setName(name.isEmpty()?"VoiceLink":name); avatar.setSpeaking(value==CallState.CONNECTED||value==CallState.IN_CALL||value==CallState.MUTED);
        title.setText(name.isEmpty()?"VoiceLink":name); timer.setText("00:00");
        controls.removeAll();
        if(value==CallState.IDLE) title.setText("Cuộc gọi thoại");
        if(value==CallState.ENDED) status.setText("Cuộc gọi đã kết thúc");
        controls.add(labeledControl(mute,"Tắt mic")); controls.add(labeledControl(speaker,"Tắt loa"));
        controls.add(labeledControl(switchChat,"Chuyển sang chat")); controls.add(labeledControl(hangup,"Kết thúc"));
        timer.setVisible(false);
        if(value!=CallState.ENDED) {
            callTimer=new Timer(1000,e->{callSecs++;updateElapsedStatus();}); callTimer.start();
            updateElapsedStatus();
        }
        controls.revalidate(); controls.repaint(); revalidate(); repaint();
    }
    private void updateElapsedStatus() {
        String elapsed=String.format("%02d:%02d",callSecs/60,callSecs%60);
        status.setText("<html><span style='color:#78666A'>Đang gọi &bull; </span><b style='color:#4A1D22'>"+elapsed+"</b></html>");
    }
    private void finish(boolean ended) { setState(ended?CallState.ENDED:CallState.IDLE, currentPeerName); if(listener!=null) listener.onCallEnded(); }
}
