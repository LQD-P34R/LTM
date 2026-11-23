package com.voicechat.integration;

import com.voicechat.chat.ChatService;
import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.ControlMessage;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

public final class DirectChatCoordinator implements AutoCloseable {

    public interface Listener {
        void onDirectHistory(String peerId, List<ChatMessage> messages);

        void onDirectSendFailed(String peerId, String text);

        void onDirectError(String message);
    }

    @FunctionalInterface
    private interface Work {
        void run() throws Exception;
    }

    private final String localId;
    private final ChatService chat;
    private final Listener listener;

    private final ExecutorService worker = Executors.newSingleThreadExecutor(
            r -> Thread.ofVirtual().unstarted(r));

    private final AtomicBoolean closed = new AtomicBoolean();

    public DirectChatCoordinator(
            String localId,
            ChatService chat,
            Listener listener) {

        this.localId = Objects.requireNonNull(localId);
        this.chat = Objects.requireNonNull(chat);
        this.listener = Objects.requireNonNull(listener);

        if (localId.isBlank()) {
            throw new IllegalArgumentException("ID peer không được rỗng.");
        }
    }

    public void loadHistory(String peerId) {
        submit(() -> publishHistory(peerId));
    }

    public void sendText(String peerId, String text) {
        submit(() -> {
            try {
                chat.sendDirect(localId, peerId, text);
            } catch (Exception ex) {
                notifyUi(() -> {
                    listener.onDirectSendFailed(peerId, text);
                    listener.onDirectError(
                            "Không thể hoàn tất gửi/lưu tin nhắn: "
                                    + errorMessage(ex));
                });
                return;
            }

            publishHistory(peerId);
        });
    }

    public void receive(ControlMessage control) {
        if (control == null
                || control.type() != ControlMessage.Type.CHAT) {
            return;
        }

        submit(() -> {
            chat.receiveDirect(localId, control);
            publishHistory(control.senderId());
        });
    }

    private void publishHistory(String peerId) {
        List<ChatMessage> messages = List.copyOf(chat.directHistory(localId, peerId));

        notifyUi(() -> listener.onDirectHistory(peerId, messages));
    }

    private void submit(Work work) {
        if (closed.get()) {
            return;
        }

        try {
            worker.execute(() -> {
                if (closed.get()) {
                    return;
                }

                try {
                    work.run();
                } catch (Exception ex) {
                    notifyUi(() -> listener.onDirectError(errorMessage(ex)));
                }
            });
        } catch (RejectedExecutionException ignored) {
            // Coordinator vừa đóng.
        }
    }

    private void notifyUi(Runnable action) {
        SwingUtilities.invokeLater(() -> {
            if (!closed.get()) {
                action.run();
            }
        });
    }

    private static String errorMessage(Exception ex) {
        return Objects.toString(
                ex.getMessage(), "Thao tác chat không thành công.");
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            worker.shutdownNow();
        }
    }
}