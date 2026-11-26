package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;
import com.voicechat.database.HistoryRepository.Line;
import com.voicechat.group.GroupProtocol.View;
import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

/** Quản lý các màn hình chat, lịch sử và cuộc gọi; không gửi yêu cầu mạng. */
final class ChatContentPanel extends JPanel {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    private static final String WELCOME = "WELCOME";
    private static final String HISTORY = "HISTORY";
    private static final String CALL = "CALL";
    private final CardLayout cards = new CardLayout();
    private final WelcomePanel welcome;
    private final HistoryPanel history = new HistoryPanel();
    private final CallPanel call;
    private final String localUserId;
    private final Map<String, DirectChatPanel> directChats = new LinkedHashMap<>();
    private final Map<String, GroupChatPanel> groupChats = new LinkedHashMap<>();
    private final List<ChatMessage> chatHistory = new ArrayList<>();
    private final JButton older = smallButton("Cũ hơn");
    private final JButton newer = smallButton("Mới hơn");
    private RoomInfo activeRoom;
    private boolean viewingHistory;
    private List<Line> shownLines;
    private List<PeerInfo> shownMembers;
    private final Set<String> onlinePeers = new HashSet<>();

    // ===== Khởi tạo các màn hình nội dung =====
    // Tạo các thẻ CardLayout cho màn chào, lịch sử, cuộc gọi và nút phân trang.
    ChatContentPanel(String username, String localUserId, boolean groupMode,
            CallPanel.CallListener callListener,
            Runnable olderPage, Runnable newerPage) {
        this.localUserId = localUserId;
        welcome = new WelcomePanel(username);
        call = new CallPanel(callListener);
        setLayout(cards);
        setBackground(UiUtils.C_APP_BG);
        add(welcome, WELCOME);

        if (groupMode) {
            JPanel navigation = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            navigation.setBackground(UiUtils.C_CARD_BG);
            older.setEnabled(false);
            newer.setEnabled(false);
            older.addActionListener(e -> olderPage.run());
            newer.addActionListener(e -> newerPage.run());
            navigation.add(older);
            navigation.add(newer);
            history.add(navigation, BorderLayout.SOUTH);
        }

        add(history, HISTORY);
        add(call, CALL);
        cards.show(this, WELCOME);
    }

    private static JButton smallButton(String text) {
        return new UiUtils.RoundedButton(
                text, UiUtils.C_PRIMARY_LIGHT, UiUtils.C_PRIMARY, UiUtils.ARC_SM);
    }

    // ===== Điều hướng giữa các cuộc trò chuyện =====
    // Tạo khung chat khi cần, đổi thẻ đang hiển thị và đặt lại dữ liệu lựa chọn.
    void showPeer(PeerInfo peer, DirectChatPanel.ActionCallback callback) {
        resetSelection(null, false);
        ensurePeer(peer, callback);
        cards.show(this, "dm_" + peer.id());
    }

    void ensurePeer(PeerInfo peer, DirectChatPanel.ActionCallback callback) {
        String key = "dm_" + peer.id();
        if (!directChats.containsKey(peer.id())) {
            boolean first = directChats.isEmpty();
            DirectChatPanel panel = new DirectChatPanel(peer, localUserId, callback);
            panel.setConnected(onlinePeers.contains(peer.id()));
            panel.loadHistory(chatHistory.stream()
                    .filter(message -> key.equals(message.roomId())).toList());
            directChats.put(peer.id(), panel);
            add(panel, key);
            if (first && activeRoom == null && !viewingHistory) cards.show(this, key);
        }
    }

    void showRoom(RoomInfo room, GroupChatPanel.ActionCallback callback,
            Consumer<Boolean> changeMembership) {
        resetSelection(room, false);
        String key = "room_" + room.id();
        if (!groupChats.containsKey(room.id())) {
            GroupChatPanel panel = new GroupChatPanel(room, localUserId, callback);
            panel.loadHistory(chatHistory.stream()
                    .filter(message -> room.id().equals(message.roomId())).toList());
            if (changeMembership != null) {
                panel.bindMembership(changeMembership);
            }
            groupChats.put(room.id(), panel);
            add(panel, key);
        }
        cards.show(this, key);
    }

    void showHistory(boolean remote) {
        resetSelection(null, true);
        if (remote) {
            history.clearHistory();
        } else {
            history.loadHistory(chatHistory);
        }
        cards.show(this, HISTORY);
    }

    private void resetSelection(RoomInfo room, boolean historySelected) {
        activeRoom = room;
        viewingHistory = historySelected;
        shownLines = null;
        shownMembers = null;
    }

    // ===== Cập nhật dữ liệu chat nhóm =====
    // Đổ thành viên/lịch sử từ View vào phòng đang chọn và khôi phục bản nháp khi gửi lỗi.
    void renderGroupView(View view, int page) {
        welcome.setUsername(view.username());
        if (viewingHistory) {
            if (!view.history().equals(shownLines)) {
                history.loadHistory(view.history().stream()
                        .map(line -> new ChatMessage(
                                line.id(), line.roomName(), line.sender(),
                                line.text(), Instant.parse(line.at())))
                        .toList());
                shownLines = view.history();
            }
            newer.setEnabled(page > 0);
            older.setEnabled(view.hasOlder());
            return;
        }

        if (activeRoom == null || !activeRoom.id().equals(view.room())) {
            return;
        }
        GroupChatPanel panel = groupChats.get(view.room());
        if (panel == null) {
            return;
        }

        panel.showMembership(view.joined());
        if (!view.members().equals(shownMembers)) {
            panel.setMembers(view.members());
            shownMembers = view.members();
        }
        if (!view.history().equals(shownLines)) {
            Map<String, String> names = new HashMap<>();
            for (Line line : view.history()) {
                names.put(line.senderId(), line.sender());
            }
            panel.setDisplayNames(names);
            panel.loadHistory(view.history().stream()
                    .map(line -> new ChatMessage(
                            line.id(), view.room(), line.senderId(),
                            line.text(), Instant.parse(line.at())))
                    .toList());
            shownLines = view.history();
        }
    }

    void restoreDraft(String roomId, String text) {
        GroupChatPanel panel = groupChats.get(roomId);
        if (panel != null) {
            panel.restoreDraft(text);
        }
    }

    // ===== Cập nhật dữ liệu chat trực tiếp =====
    // Nạp lịch sử, phục hồi tin gửi lỗi và bật/tắt gửi theo trạng thái kết nối.
    void loadDirectHistory(String peerId, List<ChatMessage> messages) {
        String key = "dm_" + peerId;

        List<ChatMessage> displayMessages = messages.stream()
                .map(message -> new ChatMessage(
                        message.id(),
                        key,
                        message.senderId(),
                        message.text(),
                        message.sentAt()))
                .toList();

        chatHistory.removeIf(message -> key.equals(message.roomId()));
        chatHistory.addAll(displayMessages);

        DirectChatPanel panel = directChats.get(peerId);
        if (panel != null) {
            panel.loadHistory(displayMessages);
        }

        if (viewingHistory) {
            history.loadHistory(chatHistory);
        }
    }

    void setDirectSending(String peerId, boolean sending) {
        DirectChatPanel panel = directChats.get(peerId);
        if (panel != null) panel.setSending(sending);
    }

    void restoreDirectDraft(String peerId, String text) {
        DirectChatPanel panel = directChats.get(peerId);
        if (panel != null) {
            panel.restoreFailedMessage(text);
        }
    }

    void setConnectedPeers(List<PeerInfo> peers) {
        onlinePeers.clear();
        for (PeerInfo peer : peers) {
            onlinePeers.add(peer.id());
        }
        directChats.forEach((id, panel) -> panel.setConnected(onlinePeers.contains(id)));
    }

    // ===== Phân phối tin nhắn để hiển thị =====
    // Lưu tin vào lịch sử của phiên và thêm vào đúng khung chat trực tiếp hoặc nhóm.
    void appendMessage(ChatMessage message) {
        chatHistory.add(message);
        String roomId = message.roomId();
        if (roomId.startsWith("dm_")) {
            DirectChatPanel panel = directChats.get(roomId.substring(3));
            if (panel != null) {
                panel.appendMessage(message);
            }
        } else {
            GroupChatPanel panel = groupChats.get(roomId);
            if (panel != null) {
                panel.appendMessage(message);
            }
        }
    }

    // ===== Chuyển màn hình cuộc gọi =====
    // Cung cấp CallPanel, mở màn gọi và quay về hội thoại sau cuộc gọi.
    CallPanel callPanel() {
        return call;
    }

    void showCall() {
        cards.show(this, CALL);
    }

    void showAfterCall(PeerInfo peer) {
        if (peer != null && directChats.containsKey(peer.id())) {
            cards.show(this, "dm_" + peer.id());
        } else {
            cards.show(this, WELCOME);
        }
    }
}
