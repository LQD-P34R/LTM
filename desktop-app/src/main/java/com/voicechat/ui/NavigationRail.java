package com.voicechat.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

/** Compact primary navigation rail shared by chat and history workspaces. */
public final class NavigationRail extends JPanel {
    public enum Destination { CHAT, CALLS, GROUPS, HISTORY }
    public interface Listener {
        void onNavigate(Destination destination);
        default void onProfileRequested(String username) { }
    }

    private final Listener listener;
    private final Map<Destination, NavButton> buttons = new EnumMap<>(Destination.class);
    private Destination selected = Destination.CHAT;

    public NavigationRail(String username, Listener listener) {
        this.listener = listener;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UiUtils.C_CARD_BG);
        setBorder(new MatteBorder(0, 0, 0, 1, UiUtils.C_BORDER));
        setPreferredSize(new Dimension(64, 0));
        setMinimumSize(new Dimension(64, 0));
        setMaximumSize(new Dimension(64, Integer.MAX_VALUE));

        JPanel logo = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); UiUtils.enableAntiAliasing(g2);
                g2.setColor(UiUtils.C_DANGER); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12); g2.dispose();
                super.paintComponent(g);
            }
        };
        logo.setOpaque(false); logo.setPreferredSize(new Dimension(38,38)); logo.setMaximumSize(new Dimension(38,38));
        logo.add(new JLabel(new AppIcons(AppIcons.Kind.LOGO, 22, Color.WHITE)));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(14)); add(logo); add(Box.createVerticalStrut(20));

        addDestination(Destination.CHAT, AppIcons.Kind.CHAT, "Tin nhắn");
        addDestination(Destination.CALLS, AppIcons.Kind.PHONE, "Cuộc gọi");
        addDestination(Destination.GROUPS, AppIcons.Kind.GROUP, "Nhóm");
        addDestination(Destination.HISTORY, AppIcons.Kind.HISTORY, "Lịch sử");
        add(Box.createVerticalGlue());

        NavButton settings = new NavButton(AppIcons.Kind.SETTINGS, "Cài đặt");
        settings.setAlignmentX(Component.CENTER_ALIGNMENT); add(settings);
        add(Box.createVerticalStrut(14));
        JPanel account = new JPanel(); account.setOpaque(false); account.setLayout(new BoxLayout(account,BoxLayout.Y_AXIS));
        UiUtils.AvatarBadge avatar = new UiUtils.AvatarBadge(username, 32, false, true, true);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT); account.add(avatar);
        JLabel accountName = new JLabel(username); accountName.setFont(UiUtils.F_SMALL); accountName.setForeground(UiUtils.C_TEXT_MUTED); accountName.setAlignmentX(Component.CENTER_ALIGNMENT); account.add(accountName);
        account.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        account.setToolTipText("Thông tin tài khoản");
        account.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if(listener!=null) listener.onProfileRequested(username); }
        });
        java.awt.event.MouseAdapter profileClick=new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if(listener!=null) listener.onProfileRequested(username); }
        };
        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); avatar.addMouseListener(profileClick);
        accountName.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); accountName.addMouseListener(profileClick);
        add(account); add(Box.createVerticalStrut(12));
        select(Destination.CHAT);
    }

    private void addDestination(Destination destination, AppIcons.Kind icon, String tooltip) {
        NavButton button = new NavButton(icon, tooltip);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(e -> {
            select(destination);
            if (listener != null) listener.onNavigate(destination);
        });
        buttons.put(destination, button);
        add(button); add(Box.createVerticalStrut(8));
    }

    public void select(Destination destination) {
        selected = destination;
        for (Map.Entry<Destination, NavButton> entry : buttons.entrySet())
            entry.getValue().setSelected(entry.getKey() == destination);
    }

    public Destination getSelectedDestination() { return selected; }

    private static final class NavButton extends JButton {
        private boolean selected;
        private final AppIcons.Kind kind;
        NavButton(AppIcons.Kind icon, String tip) {
            super(new AppIcons(icon, 19, UiUtils.C_TEXT_PRIMARY));
            this.kind = icon;
            setToolTipText(tip); setPreferredSize(new Dimension(40,40));
            setMinimumSize(new Dimension(40,40)); setMaximumSize(new Dimension(40,40));
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setBorder(new EmptyBorder(0,0,0,0)); setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        public void setSelected(boolean value) { selected=value; setIcon(new AppIcons(kind,19,
                value ? UiUtils.C_PRIMARY_TEXT : UiUtils.C_TEXT_PRIMARY)); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create(); UiUtils.enableAntiAliasing(g2);
            if (selected) { g2.setColor(UiUtils.C_SELECTED_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10); }
            g2.dispose(); super.paintComponent(g);
        }
    }
}
