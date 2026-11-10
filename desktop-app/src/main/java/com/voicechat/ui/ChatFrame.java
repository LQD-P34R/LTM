package com.voicechat.ui;

import javax.swing.*;
import java.awt.*;

/** TV3: giao dien demo. Gui tin nhan o day CHI la gia lap local. */
public final class ChatFrame extends JFrame {
    public ChatFrame(String username) {
        super("VoiceChat P2P - " + username + " (DEMO LOCAL)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 480);
        setLocationRelativeTo(null);
        JTextArea history = new JTextArea("DEMO GIAO DIEN - Chua co TCP/UDP/Voice.\n");
        history.setEditable(false);
        history.setLineWrap(true);
        JTextField input = new JTextField();
        JButton send = new JButton("Gui (demo local)");
        send.addActionListener(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty()) { history.append(username + ": " + text + "\n"); input.setText(""); }
        });
        JPanel composer = new JPanel(new BorderLayout(7, 7));
        composer.add(input, BorderLayout.CENTER); composer.add(send, BorderLayout.EAST);
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.add(new JLabel("Chat P2P - Prototype UI, chua truyen mang"), BorderLayout.NORTH);
        root.add(new JScrollPane(history), BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout(6, 6));
        south.add(new CallPanel(), BorderLayout.NORTH);
        south.add(composer, BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
    }
}
