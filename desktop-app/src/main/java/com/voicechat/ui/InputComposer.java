package com.voicechat.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/** Message composer matching the compact desktop chat footer. */
public final class InputComposer extends JPanel {
    @FunctionalInterface public interface SendListener { void onSend(String text); }
    private final JTextField inputField = new JTextField();
    private final SendListener sendListener;

    public InputComposer(SendListener listener) {
        sendListener=listener; setLayout(new BorderLayout(10,0)); setBackground(UiUtils.C_CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(new javax.swing.border.MatteBorder(1,0,0,0,UiUtils.C_BORDER),
                new EmptyBorder(8,14,8,14)));
        JButton attach=iconButton(AppIcons.Kind.ATTACH,"Đính kèm",UiUtils.C_TEXT_MUTED,32);
        add(attach,BorderLayout.WEST);
        JPanel shell=new JPanel(new BorderLayout()); shell.setOpaque(false);
        shell.setBorder(new EmptyBorder(0,2,0,2));
        JPanel fieldSurface=new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); UiUtils.enableAntiAliasing(g2);
                g2.setColor(UiUtils.C_BEIGE_FIELD); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(UiUtils.C_BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12); g2.dispose();
                super.paintComponent(g);
            }
        };
        fieldSurface.setOpaque(false); fieldSurface.setBorder(new EmptyBorder(0,4,0,4));
        inputField.setFont(UiUtils.F_BODY); inputField.setForeground(UiUtils.C_TEXT_PRIMARY);
        inputField.setOpaque(false); inputField.setBorder(new EmptyBorder(8,12,8,4));
        inputField.setToolTipText("Nhập tin nhắn"); inputField.addActionListener(e->handleSend());
        JButton smile=iconButton(AppIcons.Kind.SMILE,"Biểu tượng cảm xúc",UiUtils.C_TEXT_MUTED,30);
        fieldSurface.add(inputField,BorderLayout.CENTER); fieldSurface.add(smile,BorderLayout.EAST);
        shell.add(fieldSurface,BorderLayout.CENTER); add(shell,BorderLayout.CENTER);
        JButton send=new JButton(new AppIcons(AppIcons.Kind.SEND,17,UiUtils.C_TEXT_PRIMARY)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); UiUtils.enableAntiAliasing(g2);
                g2.setColor(UiUtils.C_PRIMARY); g2.fillOval(0,0,getWidth(),getHeight()); g2.dispose(); super.paintComponent(g);
            }
        };
        send.setPreferredSize(new Dimension(38,38)); send.setMinimumSize(new Dimension(38,38));
        send.setOpaque(false); send.setContentAreaFilled(false); send.setBorderPainted(false); send.setFocusPainted(false);
        send.setToolTipText("Gửi tin nhắn"); send.addActionListener(e->handleSend()); add(send,BorderLayout.EAST);
        setPreferredSize(new Dimension(0,64));
    }
    private JButton iconButton(AppIcons.Kind kind,String tooltip,Color color,int size) {
        JButton button=new JButton(new AppIcons(kind,16,color)); button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(size,size)); button.setMinimumSize(new Dimension(size,size));
        button.setOpaque(false); button.setContentAreaFilled(false); button.setBorderPainted(false); button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return button;
    }
    private void handleSend() {
        String text=inputField.getText().trim(); if(text.isEmpty()) return;
        if(sendListener!=null) sendListener.onSend(text);
        inputField.setText(""); inputField.requestFocusInWindow();
    }
    public void requestInputFocus(){inputField.requestFocusInWindow();}
    public void clear(){inputField.setText("");}
    public String getText(){return inputField.getText().trim();}
    public void setText(String text){inputField.setText(text==null?"":text);}
}
