package com.voicechat.integration;

import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.api.VoiceGateway;
import com.voicechat.common.model.ControlMessage;
import com.voicechat.common.model.PeerInfo;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

/**
 * Dieu phoi mot cuoc goi. Tat ca trang thai chi doi tren luong call-control.
 */
public final class CallCoordinator implements AutoCloseable {
    public interface View {
        void outgoing(PeerInfo peer);

        void incoming(PeerInfo peer, String callId);

        void connected(PeerInfo peer);

        void idle(String message);

        void muted(boolean muted);

        void error(String message);

        void setPeers(List<PeerInfo> peers);
    }

    @FunctionalInterface
    public interface ConnectionAction {
        void run() throws IOException;
    }

    private enum Phase {
        OUTGOING, INCOMING, ACTIVE
    }

    private static final class Call {
        final String id;
        final PeerInfo peer;
        Phase phase;
        ScheduledFuture<?> timeout;

        Call(String id, PeerInfo peer, Phase phase) {
            this.id = id;
            this.peer = peer;
            this.phase = phase;
        }
    }

    private final NetworkGateway network;
    private final VoiceGateway voice;
    private final String localId;
    private final View view;
    private final Executor ui;
    private final Duration ringTimeout;
    private final ScheduledExecutorService loop = Executors.newSingleThreadScheduledExecutor(
            Thread.ofPlatform().name("call-control").factory());
    private final AtomicBoolean closing = new AtomicBoolean();
    private final CompletableFuture<Void> closed = new CompletableFuture<>();
    private Call call;
    private volatile Consumer<ControlMessage> chatHandler = message -> {
    };

    public CallCoordinator(NetworkGateway network, VoiceGateway voice,
            String localId, View view) {
        this(network, voice, localId, view,
                SwingUtilities::invokeLater, Duration.ofSeconds(30));
    }

    // Overload de test khong can cua so Swing hoac cho 30 giay.
    public CallCoordinator(NetworkGateway network, VoiceGateway voice,
            String localId, View view, Executor ui,
            Duration ringTimeout) {
        this.network = Objects.requireNonNull(network);
        this.voice = Objects.requireNonNull(voice);
        this.localId = Objects.requireNonNull(localId);
        this.view = Objects.requireNonNull(view);
        this.ui = Objects.requireNonNull(ui);
        this.ringTimeout = Objects.requireNonNull(ringTimeout);
        if (localId.isBlank() || ringTimeout.isZero() || ringTimeout.isNegative()) {
            throw new IllegalArgumentException("ID hoac timeout khong hop le");
        }
        network.onControlMessage(message -> submit(() -> receive(message)));
        network.onPeerConnected(id -> submit(this::refreshPeers));
        network.onPeerDisconnected(id -> submit(() -> {
            if (call != null && call.peer.id().equals(id)) {
                finish("Đã mất kết nối với " + id);
            }
            refreshPeers();
        }));
    }

    public void open(ConnectionAction connect) {
        open(connect, () -> {
        }, error -> view.error(
                "Không thể mở kết nối: " + error.getMessage()));
    }

    /** Ca hai callback chay tren EDT voi constructor mac dinh. */
    public void open(ConnectionAction connect, Runnable onReady,
            Consumer<Exception> onFailure) {
        Objects.requireNonNull(connect);
        Objects.requireNonNull(onReady);
        Objects.requireNonNull(onFailure);
        submit(() -> {
            try {
                connect.run();
                refreshPeers();
                // Mo ChatFrame truoc cac su kien goi den dang cho.
                notifyUi(onReady);
            } catch (IOException | RuntimeException e) {
                network.disconnect();
                finish("Không thể mở kết nối.");
                notifyUi(() -> onFailure.accept(e));
            }
        });
    }

    public void requestCall(PeerInfo peer) {
        submit(() -> {
            if (call != null) {
                notifyUi(() -> view.error("Bạn đang có cuộc gọi khác."));
                return;
            }
            if (!network.connectedPeerIds().contains(peer.id())) {
                notifyUi(() -> view.error("Peer chưa kết nối: " + peer.id()));
                return;
            }
            Call outgoing = new Call(UUID.randomUUID().toString(), peer, Phase.OUTGOING);
            call = outgoing;
            try {
                send(outgoing, ControlMessage.Type.CALL_REQUEST);
                armTimeout(outgoing);
                notifyUi(() -> view.outgoing(peer));
            } catch (IOException e) {
                fail("Không thể gửi yêu cầu gọi", e);
            }
        });
    }

    public void accept(String callId) {
        submit(() -> {
            Call incoming = call;
            if (incoming == null || incoming.phase != Phase.INCOMING
                    || !incoming.id.equals(callId))
                return;
            try {
                // Mo thiet bi thanh cong roi moi bao chap nhan cho ben kia.
                voice.startCall(incoming.peer.id());
                send(incoming, ControlMessage.Type.CALL_ACCEPT);
                activate(incoming);
            } catch (IOException | RuntimeException e) {
                fail("Không thể bắt đầu cuộc gọi", e);
            }
        });
    }

    public void reject(String callId) {
        submit(() -> {
            if (call != null && call.phase == Phase.INCOMING && call.id.equals(callId)) {
                sendQuietly(call, ControlMessage.Type.CALL_REJECT);
                finish("Đã từ chối cuộc gọi.");
            }
        });
    }

    public void endCall() {
        submit(() -> {
            if (call != null) {
                sendQuietly(call, ControlMessage.Type.CALL_END);
                finish("Cuộc gọi đã kết thúc.");
            }
        });
    }

    public void setMuted(boolean value) {
        submit(() -> {
            if (call != null && call.phase == Phase.ACTIVE) {
                voice.setMuted(value);
                boolean actual = voice.isMuted();
                notifyUi(() -> view.muted(actual));
            }
        });
    }

    private void receive(ControlMessage message) {
        if (message == null || message.type() == null
                || !localId.equals(message.targetId())
                || !network.connectedPeerIds().contains(message.senderId()))
            return;
        switch (message.type()) {
            case CHAT -> {
                chatHandler.accept(message);
                return;
            }
            case CALL_REQUEST, CALL_ACCEPT, CALL_REJECT, CALL_END -> {
            }
            default -> {
                return;
            }
        }
        try {
            UUID.fromString(message.payload());
        } catch (IllegalArgumentException | NullPointerException e) {
            return;
        }

        if (message.type() == ControlMessage.Type.CALL_REQUEST) {
            if (call != null) {
                // Goi trung cua cung phien khong tao them dialog.
                if (!matches(message)) {
                    sendQuietly(new Call(message.payload(), peer(message.senderId()),
                            Phase.INCOMING), ControlMessage.Type.CALL_REJECT);
                }
                return;
            }
            Call incoming = new Call(message.payload(), peer(message.senderId()), Phase.INCOMING);
            call = incoming;
            armTimeout(incoming);
            notifyUi(() -> view.incoming(incoming.peer, incoming.id));
            return;
        }
        if (!matches(message))
            return; // Bo ACCEPT/END cua cuoc goi cu.
        switch (message.type()) {
            case CALL_ACCEPT -> {
                if (call.phase == Phase.OUTGOING) {
                    try {
                        voice.startCall(call.peer.id());
                        activate(call);
                    } catch (RuntimeException e) {
                        fail("Không thể mở microphone hoặc loa", e);
                    }
                }
            }
            case CALL_REJECT -> {
                if (call.phase == Phase.OUTGOING)
                    finish("Đối phương từ chối hoặc đang bận.");
            }
            case CALL_END -> finish("Đối phương đã kết thúc cuộc gọi.");
            default -> {
            }
        }
    }

    private boolean matches(ControlMessage message) {
        return call != null && call.id.equals(message.payload())
                && call.peer.id().equals(message.senderId());
    }

    private void activate(Call active) {
        active.phase = Phase.ACTIVE;
        if (active.timeout != null)
            active.timeout.cancel(false);
        notifyUi(() -> view.connected(active.peer));
    }

    private void armTimeout(Call pending) {
        pending.timeout = loop.schedule(() -> {
            if (call == pending && pending.phase != Phase.ACTIVE) {
                sendQuietly(pending, ControlMessage.Type.CALL_END);
                finish("Cuộc gọi hết thời gian chờ.");
            }
        }, ringTimeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void finish(String message) {
        Call previous = call;
        call = null;
        if (previous != null && previous.timeout != null)
            previous.timeout.cancel(false);
        try {
            voice.stopCall();
        } finally {
            notifyUi(() -> view.idle(message));
        }
    }

    private void fail(String message, Exception error) {
        if (call != null)
            sendQuietly(call, ControlMessage.Type.CALL_END);
        finish(message);
        notifyUi(() -> view.error(message + ": " + error.getMessage()));
    }

    private void send(Call target, ControlMessage.Type type) throws IOException {
        network.sendControl(target.peer.id(), new ControlMessage(type, localId,
                target.peer.id(), null, target.id, Instant.now()));
    }

    private void sendQuietly(Call target, ControlMessage.Type type) {
        try {
            send(target, type);
        } catch (IOException | RuntimeException e) {
            System.err.println("Call control: " + e);
        }
    }

    private static PeerInfo peer(String id) {
        return new PeerInfo(id, id, "", 0, 0);
    }

    private void refreshPeers() {
        List<PeerInfo> peers = network.connectedPeerIds().stream()
                .sorted().map(CallCoordinator::peer).toList();
        notifyUi(() -> view.setPeers(peers));
    }

    private void notifyUi(Runnable action) {
        ui.execute(() -> {
            if (!closing.get())
                action.run();
        });
    }

    private void submit(Runnable action) {
        if (closing.get())
            return;
        try {
            loop.execute(() -> {
                if (closing.get())
                    return;
                try {
                    action.run();
                } catch (RuntimeException e) {
                    fail("Lỗi điều phối cuộc gọi", e);
                }
            });
        } catch (RejectedExecutionException ignored) {
            // Cua so vua dong.
        }
    }

    public CompletableFuture<Void> closeAsync() {
        if (closing.compareAndSet(false, true)) {
            loop.execute(() -> {
                try {
                    if (call != null)
                        sendQuietly(call, ControlMessage.Type.CALL_END);
                    finish("");
                } finally {
                    network.disconnect();
                    loop.shutdownNow();
                    closed.complete(null);
                }
            });
        }
        return closed;
    }

    public void onChatMessage(Consumer<ControlMessage> handler) {
        chatHandler = Objects.requireNonNull(handler);
    }

    @Override
    public void close() {
        closeAsync();
    }
}
