package com.voicechat.integration;

import com.voicechat.chat.ChatService;
import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.api.VoiceGateway;
import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.ControlMessage;
import java.io.IOException;
import java.util.Objects;
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
    }

    private final CallCoordinator calls;
    private final ChatService chat;
    private final View view;
    private final String localId;
    private final AtomicBoolean closing = new AtomicBoolean();
    private final ExecutorService chatLoop = Executors.newSingleThreadExecutor(
            Thread.ofPlatform().name("chat-control").factory());

    public AppCoordinator(NetworkGateway network, VoiceGateway voice, String localId, View view) {
        this.localId = Objects.requireNonNull(localId);
        this.view = Objects.requireNonNull(view);
        chat = new ChatService(network);
        calls = new CallCoordinator(network, voice, localId, view);
        // NetworkGateway has one control callback: register the shared router last.
        network.onControlMessage(message -> {
            if (message != null && message.type() == ControlMessage.Type.CHAT) {
                submit(() -> {
                    ChatMessage received = chat.receiveDirectText(message, localId);
                    if (received != null) notifyUi(() -> view.onMessageReceived(received));
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
                ChatMessage sent = chat.sendDirectText(peerId, localId, text);
                notifyUi(() -> view.onMessageSent(sent));
            } catch (IOException | RuntimeException e) {
                notifyUi(() -> view.onMessageSendFailed(peerId, text, e.getMessage()));
            }
        });
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
