package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;
import com.voicechat.group.GroupClient;
import com.voicechat.group.GroupProtocol.View;
import com.voicechat.integration.CallCoordinator;
import com.voicechat.integration.AppCoordinator;
import com.voicechat.integration.GroupChatCoordinator;
import com.voicechat.integration.DirectChatCoordinator;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.swing.*;

/** Cửa sổ chính: ghép các thành phần giao diện với coordinator. */
public final class ChatFrame extends JFrame implements
        SidebarPanel.SelectionListener,
        AppCoordinator.View,
        GroupChatCoordinator.Listener,
        DirectChatCoordinator.Listener {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    private final SidebarPanel sidebar;
    private final ChatContentPanel content;
    private final ChatStatusBar status;
    private final String localUserId;
    private final GroupChatCoordinator groups;
    private final List<RoomInfo> localRooms = new ArrayList<>();
    private List<RoomInfo> knownRooms = List.of();
    private CallCoordinator calls;
    private DirectChatCoordinator direct;
    private AppCoordinator app;
    private PeerInfo currentCallPeer;
    private IncomingCallDialog incomingDialog;

    // ===== Khởi tạo và gắn các bộ điều phối =====
    // Ghép sidebar, vùng nội dung, thanh trạng thái và các dịch vụ chat/gọi.
    public ChatFrame(String username) {
        this(username, null, null);
    }

    public ChatFrame(String username, GroupClient client, View initial) {
        super("VoiceLink P2P — " + username);
        localUserId = client == null ? username : client.localId();
        groups = client == null ? null : new GroupChatCoordinator(client, this);
        sidebar = new SidebarPanel(this);
        content = new ChatContentPanel(username, localUserId, groups != null,
                buildCallListener(), () -> groups.olderPage(), () -> groups.newerPage());
        status = new ChatStatusBar(username, content::showCall);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 700);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiUtils.C_APP_BG);
        root.add(sidebar, BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);
        setContentPane(root);

        if (groups != null) {
            sidebar.setPeers(List.of());
            onView(initial, 0);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent event) {
                    groups.close();
                }
            });
            groups.start();
        } else {
            sidebar.setPeers(List.of());
            sidebar.setRooms(List.of());
            status.setStatus("Đang khởi tạo kết nối...");
        }
    }

    public void bindApp(AppCoordinator app) {
        this.app = Objects.requireNonNull(app);
        bindCalls(app.calls());
    }

    public void bindDirectChat(DirectChatCoordinator direct) {
        this.direct = Objects.requireNonNull(direct);
    }

    public void bindCalls(CallCoordinator calls) {
        this.calls = Objects.requireNonNull(calls);
    }

    // ===== Điều hướng từ thanh bên =====
    // Mở chat với peer, phòng nhóm, lịch sử hoặc hộp thoại tạo nhóm.
    @Override
    public void onPeerSelected(PeerInfo peer) {
        if (groups != null) {
            groups.showDirectChat();
        }
        content.showPeer(peer, buildDmCallback());
        if (direct != null) {
            direct.loadHistory(peer.id());
        } else if (app != null) {
            app.loadHistory(peer.id());
        }
    }

    @Override
    public void onRoomSelected(RoomInfo room) {
        sidebar.selectRoom(room);
        content.showRoom(room, buildGroupCallback(),
                groups == null ? null : groups::changeMembership);
        if (groups != null) {
            groups.selectRoom(room);
        }
    }

    @Override
    public void onHistoryRequested() {
        content.showHistory(groups != null);
        if (groups != null) {
            groups.showHistory();
        }
    }

    @Override
    public void onCreateGroupRequested() {
        String name = CreateGroupDialog.showDialog(this);
        if (name == null) {
            return;
        }
        if (groups != null) {
            groups.createRoom(name);
            return;
        }
        RoomInfo room = new RoomInfo(
                "room_" + System.currentTimeMillis(), name, localUserId);
        localRooms.add(room);
        sidebar.setRooms(new ArrayList<>(localRooms));
        onRoomSelected(room);
    }

    // ===== Tạo callback cho thao tác giao diện =====
    // Nối nút gửi tin, gọi, kết thúc và mute với bộ điều phối tương ứng.
    private DirectChatPanel.ActionCallback buildDmCallback() {
        return new DirectChatPanel.ActionCallback() {
            @Override
            public void onSendMessage(PeerInfo peer, String text) {
                if (app != null) {
                    content.setDirectSending(peer.id(), true);
                    app.sendText(peer.id(), text);
                    return;
                }
                if (direct != null) {
                    direct.sendText(peer.id(), text);
                    return;
                }

                onMessageSendFailed(peer.id(), text, "Chưa kết nối dịch vụ chat.");
            }

            @Override
            public void onCallRequested(PeerInfo peer) {
                if (calls != null) {
                    calls.requestCall(peer);
                } else {
                    error("Chưa kết nối dịch vụ gọi thoại.");
                }
            }
        };
    }

    private GroupChatPanel.ActionCallback buildGroupCallback() {
        return (room, text) -> {
            if (groups != null) {
                groups.sendText(room.id(), text);
            } else {
                onMessageReceived(new ChatMessage(UUID.randomUUID().toString(),
                        room.id(), localUserId, text, Instant.now()));
            }
        };
    }

    private CallPanel.CallListener buildCallListener() {
        return new CallPanel.CallListener() {
            @Override
            public void onCallCancelled() {
                if (calls != null) calls.endCall();
                else ChatFrame.this.onCallEnded();
            }

            @Override
            public void onCallEnded() {
                if (calls != null) {
                    calls.endCall();
                } else {
                    ChatFrame.this.onCallEnded();
                }
            }

            @Override
            public void onMuteToggled(boolean muted) {
                if (calls != null) {
                    calls.setMuted(muted);
                }
            }
        };
    }

    // ===== Hiển thị dữ liệu và lỗi chat nhóm =====
    // Nhận View từ máy chủ, cập nhật phòng/nội dung và khôi phục bản nháp khi gửi lỗi.
    @Override
    public void onView(View data, int page) {
        setTitle("VoiceLink P2P — " + data.username());
        status.setStatus(data.username() + " • Đã kết nối máy chủ nhóm");
        if (!knownRooms.equals(data.rooms())) {
            knownRooms = data.rooms();
            sidebar.setRooms(knownRooms);
        }
        content.renderGroupView(data, page);
    }

    @Override
    public void onRoomCreated(RoomInfo room) {
        onRoomSelected(room);
    }

    @Override
    public void onSendFailed(String roomId, String text) {
        content.restoreDraft(roomId, text);
    }

    @Override
    public void onError(String message, boolean showDialog) {
        status.setStatus("Lỗi: " + message);
        if (showDialog) {
            JOptionPane.showMessageDialog(
                    this, message, "VoiceLink", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ===== Hiển thị tin nhắn và lịch sử trực tiếp =====
    // Nhận kết quả chat từ coordinator, cập nhật khung hội thoại và phục hồi nội dung gửi thất bại.
    public void onMessageReceived(ChatMessage message) {
        if (message.roomId().startsWith("dm_")) {
            String peerId = message.roomId().substring(3);
            content.ensurePeer(new PeerInfo(peerId, peerId, "", 0, 0), buildDmCallback());
        }
        content.appendMessage(message);
    }

    @Override
    public void onMessageSent(ChatMessage message) {
        content.setDirectSending(message.roomId().substring(3), false);
        onMessageReceived(message);
    }

    @Override
    public void onMessageSendFailed(String peerId, String text, String reason) {
        content.restoreDirectDraft(peerId, text);
        onError("Không thể gửi tin nhắn: " + reason, true);
    }

    @Override
    public void onChatHistory(String peerId, List<ChatMessage> messages) {
        content.loadDirectHistory(peerId, messages);
    }

    @Override
    public void onDirectHistory(String peerId, List<ChatMessage> messages) {
        content.loadDirectHistory(peerId, messages);
    }

    @Override
    public void onDirectSendFailed(String peerId, String text) {
        content.restoreDirectDraft(peerId, text);
    }

    @Override
    public void onDirectError(String message) {
        onError(message, true);
    }

    // ===== Cập nhật danh bạ và phòng =====
    // Chuyển cập nhật danh sách peer/phòng sang luồng giao diện Swing.
    @Override
    public void setPeers(List<PeerInfo> peers) {
        Runnable update = () -> {
            sidebar.setPeers(peers);
            content.setConnectedPeers(peers);
            status.setStatus("Đang kết nối với " + peers.size() + " peer.");
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    public void setRooms(List<RoomInfo> rooms) {
        SwingUtilities.invokeLater(() -> sidebar.setRooms(rooms));
    }

    // ===== Hiển thị trạng thái cuộc gọi =====
    // Mở/đóng hộp thoại gọi đến, chuyển màn hình cuộc gọi và cập nhật trạng thái mute/lỗi.
    @Override
    public void outgoing(PeerInfo peer) {
        currentCallPeer = peer;
        content.callPanel().setState(CallPanel.CallState.RINGING, peer.username());
        content.showCall();
        status.setStatus("Đang gọi " + peer.username());
    }

    @Override
    public void incoming(PeerInfo peer, String callId) {
        closeIncomingDialog();
        currentCallPeer = peer;
        incomingDialog = new IncomingCallDialog(
                this, peer.username(), new IncomingCallDialog.ResponseListener() {
                    @Override
                    public void onAccepted() {
                        calls.accept(callId);
                    }

                    @Override
                    public void onRejected() {
                        calls.reject(callId);
                    }
                });
        incomingDialog.setVisible(true);
    }

    @Override
    public void connected(PeerInfo peer) {
        closeIncomingDialog();
        currentCallPeer = peer;
        content.callPanel().setMuteState(false);
        content.callPanel().setState(CallPanel.CallState.IN_CALL, peer.username());
        content.showCall();
        status.setStatus("Đang gọi với " + peer.username());
    }

    @Override
    public void idle(String message) {
        closeIncomingDialog();
        onCallEnded();
        status.setStatus(message);
    }

    @Override
    public void muted(boolean value) {
        content.callPanel().setMuteState(value);
    }

    @Override
    public void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Cuộc gọi", JOptionPane.ERROR_MESSAGE);
    }

    public void showIncomingCall(String callerName) {
        SwingUtilities.invokeLater(() -> new IncomingCallDialog(
                this, callerName, new IncomingCallDialog.ResponseListener() {
                    @Override
                    public void onAccepted() {
                        content.callPanel().setState(CallPanel.CallState.IN_CALL, callerName);
                        content.showCall();
                    }

                    @Override
                    public void onRejected() {
                        // No call session is associated with this legacy notification.
                    }
                }).setVisible(true));
    }

    public void onCallEnded() {
        content.callPanel().setState(CallPanel.CallState.IDLE, "");
        content.showAfterCall(currentCallPeer);
        currentCallPeer = null;
    }

    private void closeIncomingDialog() {
        if (incomingDialog != null) {
            incomingDialog.dispose();
            incomingDialog = null;
        }
    }
}
