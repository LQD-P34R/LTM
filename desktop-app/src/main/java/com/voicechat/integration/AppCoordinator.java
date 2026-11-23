package com.voicechat.integration;

import com.voicechat.chat.ChatService;
import com.voicechat.common.api.HistoryStore;
import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.api.VoiceGateway;
import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.ControlMessage;
import java.io.IOException;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;

/** Owns the single TCP callback and routes chat separately from call signalling. */
public final class AppCoordinator implements AutoCloseable {
    public interface View extends CallCoordinator.View {
        void onMessageReceived(ChatMessage message);
        void onMessageSent(ChatMessage message);
        void onMessageSendFailed(String peerId, String text, String reason);
        default void onChatHistory(String peerId, List<ChatMessage> messages) { }
    }

    private final CallCoordinator calls;
    private final ChatService chat;
    private final View view;
    private final String localId;
    private final Set<String> publishedIds = new HashSet<>();
    private final AtomicBoolean closing = new AtomicBoolean();
    private final ExecutorService chatLoop = Executors.newSingleThreadExecutor(
            Thread.ofPlatform().name("chat-control").factory());

    public AppCoordinator(NetworkGateway network, VoiceGateway voice, String localId, View view) {
        this(network, voice, localId, view, new HistoryStore() {
            private final Map<String, ChatMessage> messages = new LinkedHashMap<>();
            public void save(ChatMessage message) { messages.putIfAbsent(message.id(), message); }
            public List<ChatMessage> findByRoom(String roomId) {
                return messages.values().stream().filter(m -> roomId.equals(m.roomId())).toList();
            }
        });
    }

    public AppCoordinator(NetworkGateway network, VoiceGateway voice, String localId,
                          View view, HistoryStore history) {
        this.localId = Objects.requireNonNull(localId);
        this.view = Objects.requireNonNull(view);
        chat = new ChatService(network, history);
        calls = new CallCoordinator(network, voice, localId, view);
        // NetworkGateway has one control callback: register the shared router last.
        network.onControlMessage(message -> {
            if (message != null && message.type() == ControlMessage.Type.CHAT) {
                submit(() -> {
                    try {
                        ChatMessage received = display(chat.receiveDirect(localId, message), message.senderId());
                        if (publishedIds.add(received.id())) {
                            notifyUi(() -> view.onMessageReceived(received));
                        }
                    } catch (IOException | IllegalArgumentException ignored) {
                        // Invalid direct-chat frames must not interrupt calls or other chats.
                    } catch (RuntimeException e) {
                        notifyUi(() -> view.error("Không thể lưu tin nhắn đến: " + e.getMessage()));
                    }
                });
            } else {
                calls.receiveControlMessage(message);
            }
        });
    }

    public CallCoordinator calls() {
        return calls;
    }

    public void sendText(String peerId, String text) {
        submit(() -> {
            try {
                ChatMessage sent = display(chat.sendDirect(localId, peerId, text), peerId);
                publishedIds.add(sent.id());
                notifyUi(() -> view.onMessageSent(sent));
            } catch (IOException | RuntimeException e) {
                notifyUi(() -> view.onMessageSendFailed(peerId, text, e.getMessage()));
            }
        });
    }

    public void loadHistory(String peerId) {
        submit(() -> {
            try {
                List<ChatMessage> messages = chat.directHistory(localId, peerId).stream()
                        .map(message -> display(message, peerId)).toList();
                messages.forEach(message -> publishedIds.add(message.id()));
                notifyUi(() -> view.onChatHistory(peerId, messages));
            } catch (RuntimeException e) {
                notifyUi(() -> view.error("Không thể đọc lịch sử: " + e.getMessage()));
            }
        });
    }

    private static ChatMessage display(ChatMessage message, String peerId) {
        return new ChatMessage(message.id(), "dm_" + peerId, message.senderId(),
                message.text(), message.sentAt());
    }

    private void submit(Runnable action) {
        if (closing.get()) return;
        try {
            chatLoop.execute(() -> {
                if (!closing.get()) action.run();
            });
        } catch (RejectedExecutionException ignored) {
            // Session has closed; no further UI work is needed.
        }
    }

    private void notifyUi(Runnable action) {
        SwingUtilities.invokeLater(() -> {
            if (!closing.get()) action.run();
        });
    }

    public CompletableFuture<Void> closeAsync() {
        closing.set(true);
        // Closing sockets also releases a chat send blocked in network I/O.
        CompletableFuture<Void> closed = calls.closeAsync();
        chatLoop.shutdownNow();
        return closed;
    }

    @Override public void close() {
        closeAsync();
    }
}
