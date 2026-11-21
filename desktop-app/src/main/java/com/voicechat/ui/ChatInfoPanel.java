package com.voicechat.ui;

import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

/** Persistent fourth column for peer profile and group members. */
public final class ChatInfoPanel extends JPanel {
    public interface Actions {
        void onCallPeer(PeerInfo peer);
        void onCallGroup(RoomInfo room);
        void onShowHistory(PeerInfo peer);
        default void onAddMember(RoomInfo room) { }
        default void onLeaveGroup(RoomInfo room) { }
    }
    private final Actions actions;
    public ChatInfoPanel(Actions actions) {
        this.actions=actions; setPreferredSize(new Dimension(280,0)); setMinimumSize(new Dimension(280,0));
        setBackground(UiUtils.C_CARD_BG); setOpaque(true); setBorder(new MatteBorder(0,1,0,0,UiUtils.C_BORDER));
        showEmpty();
    }
    public void showEmpty() {
        removeAll(); setLayout(new GridBagLayout()); setBackground(UiUtils.C_CARD_BG);
        JLabel hint=new JLabel("Chọn một cuộc trò chuyện",SwingConstants.CENTER);
        hint.setFont(UiUtils.F_SMALL); hint.setForeground(UiUtils.C_TEXT_MUTED); add(hint);
        revalidate(); repaint();
    }
    public void showPeer(PeerInfo peer) {
        removeAll(); setLayout(new BorderLayout()); setBackground(UiUtils.C_CARD_BG);
        JPanel body=new JPanel(); body.setOpaque(false); body.setBorder(new EmptyBorder(28,14,14,14));
        body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));
        UiUtils.AvatarBadge avatar=new UiUtils.AvatarBadge(peer.username(),72,false,false,false);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel name=label(peer.username(),UiTheme.SEMIBOLD_FONT.deriveFont(16f),UiUtils.C_TEXT_PRIMARY);
        JLabel handle=label("@"+peer.username(),UiTheme.REGULAR_FONT.deriveFont(12.5f),UiUtils.C_TEXT_MUTED);
        name.setAlignmentX(Component.CENTER_ALIGNMENT); handle.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(avatar); body.add(Box.createVerticalStrut(10)); body.add(name); body.add(Box.createVerticalStrut(3)); body.add(handle);
        body.add(Box.createVerticalStrut(28));
        JLabel recent=label("Gần đây với "+peer.username(),UiUtils.F_BODY_BOLD,UiUtils.C_TEXT_PRIMARY);
        recent.setAlignmentX(Component.CENTER_ALIGNMENT); recent.setHorizontalAlignment(SwingConstants.CENTER); body.add(recent); body.add(Box.createVerticalStrut(10));
        body.add(recentItem("Cuộc gọi đi", "Hôm nay, 09:32", AppIcons.Kind.ARROW_OUT,UiUtils.C_PRIMARY));
        body.add(recentItem("Cuộc gọi nhỡ", "Hôm qua, 20:15", AppIcons.Kind.ARROW_MISSED,UiUtils.C_DANGER));
        JPanel lower=new JPanel(new BorderLayout()); lower.setOpaque(false); lower.setBorder(new EmptyBorder(10,14,14,14));
        UiUtils.RoundedButton history=new UiUtils.RoundedButton("Xem toàn bộ lịch sử",Color.WHITE,UiUtils.C_TEXT_PRIMARY,12);
        history.setPreferredSize(new Dimension(240,36)); history.setMinimumSize(new Dimension(240,36)); history.setMaximumSize(new Dimension(240,36));
        history.setBorderColor(UiUtils.C_BORDER); history.addActionListener(e->{if(actions!=null)actions.onShowHistory(peer);});
        history.setAlignmentX(Component.CENTER_ALIGNMENT); lower.add(history);
        JScrollPane scroll=new JScrollPane(body);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null); scroll.setBackground(UiUtils.C_CARD_BG); scroll.setOpaque(true);
        scroll.getViewport().setBackground(UiUtils.C_CARD_BG); scroll.getViewport().setOpaque(true);
        add(scroll,BorderLayout.CENTER); add(lower,BorderLayout.SOUTH);
        revalidate(); repaint();
    }
    public void showRoom(RoomInfo room) {
        removeAll(); setLayout(new BorderLayout()); setBackground(UiUtils.C_CARD_BG);
        JPanel top=new JPanel(); top.setOpaque(false); top.setBorder(new EmptyBorder(24,16,16,16)); top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
        JPanel groupAvatar=new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) { Graphics2D g2=(Graphics2D)g.create(); UiUtils.enableAntiAliasing(g2); g2.setColor(UiUtils.C_PRIMARY_LIGHT); g2.fillRoundRect(0,0,64,64,18,18); g2.dispose(); super.paintComponent(g); }
        };
        groupAvatar.setOpaque(false); groupAvatar.setPreferredSize(new Dimension(64,64)); groupAvatar.setMaximumSize(new Dimension(64,64)); groupAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        String initials=UiUtils.getInitials(room.name()); JLabel initialsLabel=label(initials,UiTheme.SEMIBOLD_FONT.deriveFont(20f),UiUtils.C_TEXT_PRIMARY); groupAvatar.add(initialsLabel); top.add(groupAvatar); top.add(Box.createVerticalStrut(12));
        JLabel groupName=label(room.name(),UiTheme.SEMIBOLD_FONT.deriveFont(16f),UiUtils.C_TEXT_PRIMARY); groupName.setAlignmentX(Component.CENTER_ALIGNMENT); top.add(groupName); top.add(Box.createVerticalStrut(4));
        JLabel count=label("5 thành viên",UiTheme.REGULAR_FONT.deriveFont(12.5f),UiUtils.C_TEXT_MUTED); count.setAlignmentX(Component.CENTER_ALIGNMENT); top.add(count); top.add(Box.createVerticalStrut(16));
        UiUtils.RoundedButton call=new UiUtils.RoundedButton("Gọi nhóm",UiUtils.C_BEIGE_FIELD,UiUtils.C_TEXT_PRIMARY,12); call.setIcon(new AppIcons(AppIcons.Kind.PHONE,15,UiUtils.C_TEXT_PRIMARY)); call.setPreferredSize(new Dimension(248,38)); call.setMaximumSize(new Dimension(248,38)); call.setAlignmentX(Component.CENTER_ALIGNMENT); call.addActionListener(e->{if(actions!=null)actions.onCallGroup(room);}); top.add(call); top.add(Box.createVerticalStrut(8));
        JLabel note=label("Chưa khả dụng: backend chưa hỗ trợ gọi nhóm.",UiUtils.F_SMALL,UiUtils.C_TEXT_MUTED); note.setAlignmentX(Component.CENTER_ALIGNMENT); note.setHorizontalAlignment(SwingConstants.CENTER); top.add(note); add(top,BorderLayout.NORTH);
        JPanel body=new JPanel(); body.setOpaque(false); body.setBorder(new EmptyBorder(8,16,8,16)); body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));
        JPanel headingWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        headingWrap.setOpaque(false);
        headingWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        headingWrap.add(label("THÀNH VIÊN", UiUtils.F_SMALL_BOLD, UiUtils.C_TEXT_MUTED));
        body.add(headingWrap);
        body.add(Box.createVerticalStrut(10));
        addMember(body,"Bạn - Quản trị viên","Online",true); addMember(body,"Quốc Bảo","Online",true); addMember(body,"Thu Hà","Online",true); addMember(body,"Lan Phương","Ngoại tuyến",false); addMember(body,"Đức Anh","Ngoại tuyến",false);
        JPanel lower=new JPanel(); lower.setOpaque(false); lower.setLayout(new BoxLayout(lower,BoxLayout.Y_AXIS));
        lower.setBorder(new EmptyBorder(12,16,20,16));
        UiUtils.RoundedButton add=new UiUtils.RoundedButton("+ Thêm thành viên",Color.WHITE,UiUtils.C_TEXT_PRIMARY,12);
        add.setBorderColor(UiUtils.C_BORDER); add.setPreferredSize(new Dimension(240,36)); add.setMinimumSize(new Dimension(240,36)); add.setMaximumSize(new Dimension(240,36));
        add.addActionListener(e->{if(actions!=null)actions.onAddMember(room);});
        JButton leave=new JButton("Rời nhóm"); leave.setFont(UiTheme.SEMIBOLD_FONT.deriveFont(13f)); leave.setForeground(UiUtils.C_DANGER);
        leave.setBorderPainted(false); leave.setContentAreaFilled(false); leave.setFocusPainted(false); leave.setAlignmentX(Component.CENTER_ALIGNMENT); leave.setHorizontalAlignment(SwingConstants.CENTER);
        leave.addActionListener(e->{if(actions!=null)actions.onLeaveGroup(room);});
        add.setAlignmentX(Component.CENTER_ALIGNMENT);
        lower.add(add); lower.add(Box.createVerticalStrut(14)); lower.add(leave);
        JScrollPane scroll=new JScrollPane(body);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null); scroll.setBackground(UiUtils.C_CARD_BG); scroll.setOpaque(true);
        scroll.getViewport().setBackground(UiUtils.C_CARD_BG); scroll.getViewport().setOpaque(true);
        add(scroll,BorderLayout.CENTER); add(lower,BorderLayout.SOUTH);
        revalidate(); repaint();
    }
    private JPanel callCard(String title,String subtitle,String buttonText,Color buttonColor,Runnable callback) {
        UiUtils.RoundedPanel card=new UiUtils.RoundedPanel(16,UiUtils.C_SIDEBAR_LIGHT,null,false);
        card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(13,13,13,13));
        JLabel heading=label(title,UiUtils.F_BODY_BOLD,UiUtils.C_TEXT_PRIMARY); heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel state=label(subtitle,UiUtils.F_SMALL,UiUtils.C_TEXT_MUTED); state.setAlignmentX(Component.LEFT_ALIGNMENT);
        UiUtils.RoundedButton button=new UiUtils.RoundedButton(buttonText,buttonColor,Color.WHITE,12);
        button.setIcon(new AppIcons(AppIcons.Kind.HEADSET,15,Color.WHITE)); button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setPreferredSize(new Dimension(216,36)); button.setMinimumSize(new Dimension(216,36)); button.setMaximumSize(new Dimension(216,36)); button.addActionListener(e->callback.run());
        card.add(heading); card.add(Box.createVerticalStrut(4)); card.add(state); card.add(Box.createVerticalStrut(10)); card.add(button);
        card.setPreferredSize(new Dimension(248,124)); card.setMaximumSize(new Dimension(248,124)); card.setAlignmentX(Component.CENTER_ALIGNMENT); return card;
    }
    private JPanel recentItem(String title,String detail,AppIcons.Kind icon,Color color) {
        JPanel row=new JPanel(new BorderLayout(10,0)); row.setOpaque(false); row.setBorder(new EmptyBorder(6,0,6,0));
        row.setPreferredSize(new Dimension(248,44)); row.setMinimumSize(new Dimension(0,44)); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));
        UiUtils.RoundedPanel iconBox=new UiUtils.RoundedPanel(8,color.equals(UiUtils.C_DANGER)?new Color(0xF3,0xE2,0xE3):UiUtils.C_PRIMARY_LIGHT,null,false);
        iconBox.setPreferredSize(new Dimension(32,32)); iconBox.setMinimumSize(new Dimension(32,32)); iconBox.setMaximumSize(new Dimension(32,32)); iconBox.setLayout(new GridBagLayout()); iconBox.add(new JLabel(new AppIcons(icon,16,color))); row.add(iconBox,BorderLayout.WEST);
        JPanel text=new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text,BoxLayout.Y_AXIS));
        JLabel heading=label(title,UiTheme.SEMIBOLD_FONT.deriveFont(13f),UiUtils.C_TEXT_PRIMARY); JLabel when=label(detail,UiUtils.F_SMALL,UiUtils.C_TEXT_MUTED);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT); when.setAlignmentX(Component.LEFT_ALIGNMENT); text.add(heading); text.add(when); row.add(text,BorderLayout.CENTER); return row;
    }
    private void addMember(JPanel list,String name,String state,boolean online) {
        JPanel row=new JPanel(new BorderLayout(9,0)); row.setOpaque(false); row.setBorder(new EmptyBorder(6,0,6,0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));
        row.add(new UiUtils.AvatarBadge(name,32,false,online,true),BorderLayout.WEST);
        JPanel text=new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text,BoxLayout.Y_AXIS));
        JLabel who=label(name,UiUtils.F_SMALL_BOLD,UiUtils.C_TEXT_PRIMARY); JLabel status=label(state,UiUtils.F_SMALL,online?UiUtils.C_ONLINE:UiUtils.C_TEXT_MUTED);
        text.add(who); text.add(status); row.add(text,BorderLayout.CENTER); list.add(row);
    }
    private JLabel label(String text,Font font,Color color) { JLabel l=new JLabel(text); l.setFont(font); l.setForeground(color); return l; }
}
