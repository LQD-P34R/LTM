package com.voicechat.integration;

import com.voicechat.common.api.VoiceGateway;
import com.voicechat.common.model.ControlMessage;
import com.voicechat.common.model.PeerInfo;
import com.voicechat.network.PeerManager;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javax.swing.SwingUtilities;

/** Test dieu khien goi qua TCP localhost that, voice gia lap; khong mo mic/loa. */
public final class CallCoordinatorDemo {
    private static final PeerInfo B = new PeerInfo("B", "B", "127.0.0.1", 0, 0);

    public static void main(String[] args) throws Exception {
        try (Fixture f = new Fixture(Duration.ofSeconds(3))) {
            f.a.requestCall(B);
            await(() -> f.bView.phase.equals("INCOMING"), "incoming request");
            String firstId = f.bView.callId;
            require(!f.aVoice.active && !f.bVoice.active, "No audio before acceptance");
            f.b.accept(firstId);
            await(() -> f.aView.phase.equals("ACTIVE") && f.bView.phase.equals("ACTIVE"), "accept");
            require(f.aVoice.starts == 1 && f.bVoice.starts == 1, "Start audio once per end");
            require(f.aView.lastOnEdt && f.bView.lastOnEdt, "UI callbacks run on EDT");
            f.a.setMuted(true);
            await(() -> f.aVoice.muted && f.aView.muted, "mute");
            require(!f.bVoice.muted, "Mute remains local");
            f.a.setMuted(false);
            await(() -> !f.aVoice.muted, "unmute");
            f.a.endCall();
            await(() -> f.aView.phase.equals("IDLE") && f.bView.phase.equals("IDLE"), "end");
            require(!f.aVoice.active && !f.bVoice.active, "Stop audio at both ends");
            require(f.aNet.connectedPeerIds().contains("B"), "Keep TCP connection after ending");

            f.a.requestCall(B);
            await(() -> f.bView.phase.equals("INCOMING"), "second request");
            String secondId = f.bView.callId;
            require(!secondId.equals(firstId), "Fresh call ID");
            f.bNet.sendControl("A", new ControlMessage(ControlMessage.Type.CALL_ACCEPT,
                    "B", "A", null, firstId, Instant.now()));
            Thread.sleep(150);
            require(!f.aVoice.active && f.aView.phase.equals("OUTGOING"), "Ignore stale ACCEPT");
            f.b.reject(secondId);
            await(() -> f.aView.phase.equals("IDLE") && f.bView.phase.equals("IDLE"), "reject");
            require(f.aVoice.starts == 1 && f.bVoice.starts == 1, "Reject does not open devices");

            f.a.requestCall(B);
            await(() -> f.bView.phase.equals("INCOMING"), "cancel request");
            String cancelledId = f.bView.callId;
            f.a.endCall();
            await(() -> f.aView.phase.equals("IDLE") && f.bView.phase.equals("IDLE"), "cancel");
            f.b.accept(cancelledId);
            Thread.sleep(150);
            require(!f.bVoice.active, "Ignore accept from a disposed dialog");

            f.a.requestCall(B);
            await(() -> f.bView.phase.equals("INCOMING"), "request before device failure");
            f.aVoice.failNextStart = true;
            f.b.accept(f.bView.callId);
            await(() -> f.aView.lastError != null
                    && f.aView.phase.equals("IDLE") && f.bView.phase.equals("IDLE"), "device failure cleanup");
            require(!f.aVoice.active && !f.bVoice.active, "Clean audio after startup failure");

            f.a.requestCall(B);
            await(() -> f.bView.phase.equals("INCOMING"), "request after recovery");
            f.b.accept(f.bView.callId);
            await(() -> f.aVoice.active && f.bVoice.active, "restart after failure");
            f.bNet.disconnect();
            await(() -> f.aView.phase.equals("IDLE") && f.bView.phase.equals("IDLE"), "disconnect");
            require(!f.aVoice.active && !f.bVoice.active, "Disconnect stops devices");
        }
        try (Fixture f = new Fixture(Duration.ofMillis(500))) {
            f.a.requestCall(B);
            await(() -> f.bView.phase.equals("INCOMING"), "timeout request");
            await(() -> f.aView.phase.equals("IDLE") && f.bView.phase.equals("IDLE"), "ring timeout");
            require(f.aVoice.starts == 0 && f.bVoice.starts == 0, "Timeout does not open devices");
        }
        try (Fixture f = new Fixture(Duration.ofSeconds(3))) {
            f.a.requestCall(B);
            await(() -> f.bView.phase.equals("INCOMING"), "request before close");
            f.a.closeAsync().get(3, TimeUnit.SECONDS);
            await(() -> f.bView.phase.equals("IDLE"), "window close ends remote call");
            require(f.aNet.connectedPeerIds().isEmpty(), "Window close releases network");
        }
        System.out.println("PASS: request/accept, EDT, mute/unmute, end, reject,");
        System.out.println("      cancel, stale ACCEPT, device failure, restart,");
        System.out.println("      disconnect, ring timeout and window cleanup.");
    }

    private static final class Fixture implements AutoCloseable {
        final PeerManager aNet = new PeerManager("A");
        final PeerManager bNet = new PeerManager("B");
        final FakeVoice aVoice = new FakeVoice();
        final FakeVoice bVoice = new FakeVoice();
        final FakeView aView = new FakeView();
        final FakeView bView = new FakeView();
        final CallCoordinator a;
        final CallCoordinator b;

        Fixture(Duration timeout) throws Exception {
            a = new CallCoordinator(aNet, aVoice, "A", aView, SwingUtilities::invokeLater, timeout);
            b = new CallCoordinator(bNet, bVoice, "B", bView, SwingUtilities::invokeLater, timeout);
            int port = bNet.startListening(0, 0);
            aNet.startListening(0, 0);
            aNet.connect("127.0.0.1", port);
            await(() -> aView.peers.size() == 1 && bView.peers.size() == 1, "connect");
        }

        @Override public void close() throws Exception {
            a.closeAsync().get(3, TimeUnit.SECONDS);
            b.closeAsync().get(3, TimeUnit.SECONDS);
        }
    }

    private static final class FakeVoice implements VoiceGateway {
        volatile boolean active;
        volatile boolean muted;
        volatile boolean failNextStart;
        volatile int starts;

        @Override public void startCall(String id) {
            if (failNextStart) {
                failNextStart = false;
                throw new IllegalStateException("Simulated microphone failure");
            }
            starts++;
            muted = false;
            active = true;
        }
        @Override public void stopCall() { active = false; muted = false; }
        @Override public void setMuted(boolean value) { muted = value; }
        @Override public boolean isMuted() { return muted; }
    }

    private static final class FakeView implements CallCoordinator.View {
        volatile String phase = "IDLE";
        volatile String callId;
        volatile String lastError;
        volatile boolean muted;
        volatile boolean lastOnEdt;
        volatile List<PeerInfo> peers = List.of();
        @Override public void outgoing(PeerInfo peer) { onEdt(); phase = "OUTGOING"; }
        @Override public void incoming(PeerInfo peer, String id) { onEdt(); callId = id; phase = "INCOMING"; }
        @Override public void connected(PeerInfo peer) { onEdt(); phase = "ACTIVE"; }
        @Override public void idle(String message) { onEdt(); phase = "IDLE"; }
        @Override public void muted(boolean value) { onEdt(); muted = value; }
        @Override public void error(String message) { onEdt(); lastError = message; }
        @Override public void setPeers(List<PeerInfo> value) { onEdt(); peers = value; }
        private void onEdt() { lastOnEdt = SwingUtilities.isEventDispatchThread(); }
    }

    private static void await(BooleanSupplier condition, String label) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(4);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() >= deadline) throw new AssertionError("Timeout: " + label);
            Thread.sleep(10);
        }
    }
    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
