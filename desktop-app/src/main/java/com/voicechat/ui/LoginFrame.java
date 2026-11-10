package com.voicechat.ui;

import javax.swing.*;
import java.awt.*;

/** TV3: khoi tao giao dien; nut vao chi chay DEMO cuc bo. */
public final class LoginFrame extends JFrame {
    public LoginFrame() {
        super("VoiceChat P2P - Demo giao dien");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(430, 270);
        setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridLayout(0, 1, 7, 7));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        JTextField username = new JTextField("User_A");
        JTextField ip = new JTextField("127.0.0.1");
        JTextField port = new JTextField("5000");
        panel.add(new JLabel("Ten nguoi dung:")); panel.add(username);
        panel.add(new JLabel("IP peer (demo, chua ket noi):")); panel.add(ip);
        panel.add(new JLabel("TCP port (demo):")); panel.add(port);
        JButton open = new JButton("Mo giao dien demo (chua ket noi)");
        open.addActionListener(e -> {
            new ChatFrame(username.getText().trim()).setVisible(true);
            dispose();
        });
        JPanel outer = new JPanel(new BorderLayout());
        outer.add(panel, BorderLayout.CENTER);
        outer.add(open, BorderLayout.SOUTH);
        setContentPane(outer);
    }
}
