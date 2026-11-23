package com.voicechat.integration;

import com.voicechat.common.model.RoomInfo;
import com.voicechat.group.GroupClient;
import com.voicechat.group.GroupProtocol.View;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Timer;
import javax.swing.SwingUtilities;

public final class GroupChatCoordinator implements AutoCloseable {

    public interface Listener {
        void onView(View data, int page);

        void onRoomCreated(RoomInfo room);

        void onSendFailed(String roomId, String text);

        void onError(String message, boolean showDialog);
    }

    private final GroupClient client;
    private final Listener listener;

    private final ExecutorService requests = Executors.newSingleThreadExecutor(
            r -> Thread.ofVirtual().unstarted(r));

    private Timer refresh;
    private RoomInfo activeRoom;
    private boolean viewingHistory;
    private int generation;
    private int pending;
    private int page;
    private boolean closed;

    public GroupChatCoordinator(GroupClient client, Listener listener) {
        this.client = Objects.requireNonNull(client);
        this.listener = Objects.requireNonNull(listener);
    }

    public void start() {
        if (closed || refresh != null) {
            return;
        }

        refresh = new Timer(1800, e -> {
            if (pending == 0 && page == 0) {
                fetch(viewingHistory ? "history" : "view", "", 0);
            }
        });

        refresh.start();
    }

    public void selectRoom(RoomInfo room) {
        if (closed) {
            return;
        }

        resetSelection(Objects.requireNonNull(room), false);
        fetch("view", "", 0);
    }

    public void showDirectChat() {
        if (closed) {
            return;
        }

        resetSelection(null, false);
    }

    public void showHistory() {
        if (closed) {
            return;
        }

        resetSelection(null, true);
        fetch("history", "", 0);
    }

    public void createRoom(String name) {
        fetch("create", name, 0);
    }

    public void sendText(String roomId, String text) {
        if (closed) {
            return;
        }

        if (activeRoom == null || !activeRoom.id().equals(roomId)) {
            listener.onSendFailed(roomId, text);
            listener.onError(
                    "Hãy chọn lại phòng trước khi gửi tin.",
                    true);
            return;
        }

        fetch("send", text, 0);
    }

    public void changeMembership(boolean join) {
        if (closed) {
            return;
        }

        if (activeRoom == null) {
            listener.onError("Hãy chọn một phòng trước.", true);
            return;
        }

        fetch(join ? "join" : "leave", "", 0);
    }

    public void olderPage() {
        navigateHistory(page + 1);
    }

    public void newerPage() {
        navigateHistory(Math.max(0, page - 1));
    }

    private void resetSelection(RoomInfo room, boolean history) {
        generation++;
        activeRoom = room;
        viewingHistory = history;
        page = 0;
    }

    private void navigateHistory(int next) {
        if (!closed && pending == 0 && viewingHistory && next >= 0) {
            fetch("history", "", next);
        }
    }

    private void fetch(String action, String value, int requestedPage) {
        if (closed || requests.isShutdown()) {
            return;
        }

        int current = generation;
        String roomId = activeRoom == null ? null : activeRoom.id();
        pending++;

        requests.execute(() -> {
            try {
                View result = client.request(
                        action, roomId, value, requestedPage);

                SwingUtilities.invokeLater(() -> {
                    pending--;

                    if (closed || current != generation) {
                        return;
                    }

                    if ("create".equals(action)) {
                        result.rooms().stream()
                                .filter(room -> room.id().equals(result.room()))
                                .findFirst()
                                .ifPresent(listener::onRoomCreated);
                        return;
                    }

                    if (viewingHistory && "rename".equals(action)) {
                        fetch("history", "", page);
                        return;
                    }

                    page = requestedPage;
                    listener.onView(result, page);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    pending--;

                    if (closed) {
                        return;
                    }

                    if ("send".equals(action)) {
                        listener.onSendFailed(roomId, value);
                    }

                    if (current != generation) {
                        return;
                    }

                    boolean showDialog = !"view".equals(action)
                            && !"history".equals(action);

                    listener.onError(
                            Objects.toString(
                                    ex.getMessage(),
                                    "Không thể thực hiện yêu cầu."),
                            showDialog);
                });
            }
        });
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;
        generation++;

        if (refresh != null) {
            refresh.stop();
        }

        requests.shutdownNow();
        client.close();
    }
}