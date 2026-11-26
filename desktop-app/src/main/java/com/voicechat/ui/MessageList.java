package com.voicechat.ui;

import javax.swing.*;
import java.awt.*;

/** A vertical transcript whose rows wrap to the actual viewport, including after resize. */
final class MessageList extends JPanel implements Scrollable {

    // ===== Khởi tạo danh sách tin =====
    // Dùng layout tự tính để từng bong bóng bám theo chiều rộng viewport.
    MessageList() { super(null); }

    // ===== Đo và bố trí các dòng tin =====
    // Tính chiều rộng khả dụng, yêu cầu bong bóng xuống dòng và đặt vị trí từng dòng.
    private int transcriptWidth() {
        if (getParent() instanceof JViewport viewport && viewport.getExtentSize().width > 0)
            return viewport.getExtentSize().width;
        return getWidth() > 0 ? getWidth() : 480;
    }

    private int arrange(int width, boolean position) {
        Insets insets = getInsets();
        int available = Math.max(1,width-insets.left-insets.right);
        int y = insets.top;
        for (Component component : getComponents()) {
            ChatBubbleRenderer row = (ChatBubbleRenderer) component;
            row.fitTo(available);
            int height = row.getPreferredSize().height;
            if (position) row.setBounds(insets.left,y,available,height);
            y += height;
        }
        return y+insets.bottom;
    }

    @Override public Dimension getPreferredSize() {
        int width = transcriptWidth();
        return new Dimension(width,arrange(width,false));
    }

    @Override public void doLayout() { arrange(getWidth(),true); }

    // ===== Hợp đồng cuộn với JScrollPane =====
    // Bám chiều rộng viewport, cho phép cuộn dọc và quy định bước cuộn.
    @Override public boolean getScrollableTracksViewportWidth() { return true; }

    @Override public boolean getScrollableTracksViewportHeight() { return false; }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }

    @Override public int getScrollableUnitIncrement(Rectangle r,int orientation,int direction) { return 16; }

    @Override public int getScrollableBlockIncrement(Rectangle r,int orientation,int direction) { return Math.max(16,r.height-16); }
}
