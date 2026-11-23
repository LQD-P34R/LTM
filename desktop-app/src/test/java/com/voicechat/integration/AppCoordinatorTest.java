package com.voicechat.integration;

import com.voicechat.chat.ChatService;
import com.voicechat.common.api.VoiceGateway;
import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.ControlMessage;
import com.voicechat.common.model.PeerInfo;
import com.voicechat.network.PeerManager;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javax.swing.SwingUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exercises the same shared TCP router used by DesktopSession, with real sockets. */
public class AppCoordinatorTest {
    private final PeerManager aNet = new PeerManager("An");
    private final PeerManager bNet = new PeerManager("Bình");
    private final TestView aView = new TestView();
    private final TestView bView = new TestView();
    private final TestVoice aVoice = new TestVoice();
    private final TestVoice bVoice = new TestVoice();
    private AppCoordinator a;
    private AppCoordinator b;

    @Before public void connect() throws Exception {
        a = new AppCoordinator(aNet, aVoice, "An", aView);
        b = new AppCoordinator(bNet, bVoice, "Bình", bView);
        int port = bNet.startListening(0, 0);
        aNet.startListening(0, 0);
        aNet.connect("127.0.0.1", port);
        await(() -> aView.peers.size() == 1 && bView.peers.size() == 1);
    }

    @After public void close() throws Exception {
        if (a != null) a.closeAsync().get(4, TimeUnit.SECONDS);
        if (b != null) b.closeAsync().get(4, TimeUnit.SECONDS);
        SwingUtilities.invokeAndWait(() -> { });
        assertFalse("All view callbacks must use EDT", aView.wrongThread || bView.wrongThread);
        assertTrue(aView.errors.toString(), aView.errors.isEmpty());
        assertTrue(bView.errors.toString(), bView.errors.isEmpty());
    }

    @Test public void exchangesUnicodeAndKeepsOrderWithoutEchoes() throws Exception {
        String first = "Chào Bình! Tiếng Việt, emoji 😊, <html>, dấu nháy \" và \\";
        a.sendText("Bình", first);
        b.sendText("An", "Chào An 👋\nDòng thứ hai");
        for (int i = 0; i < 30; i++) {
            a.sendText("Bình", "A-" + i);
            b.sendText("An", "B-" + i);
        }
        await(() -> aView.sent.size() == 31 && bView.received.size() == 31
                && bView.sent.size() == 31 && aView.received.size() == 31);
        assertEquals(first, bView.received.get(0).text());
        assertEquals("dm_An", bView.received.get(0).roomId());
        assertEquals("dm_Bình", aView.sent.get(0).roomId());
        assertEquals("An", bView.received.get(0).senderId());
        assertEquals(aView.sent.get(0).sentAt(), bView.received.get(0).sentAt());
        for (int i = 0; i < 30; i++) {
            assertEquals("A-" + i, bView.received.get(i + 1).text());
            assertEquals("B-" + i, aView.received.get(i + 1).text());
        }
        assertTrue(aView.failures.isEmpty());
        assertTrue(bView.failures.isEmpty());
    }

    @Test public void chatAndCallSignallingShareOneConnection() throws Exception {
        a.calls().requestCall(new PeerInfo("Bình", "Bình", "", 0, 0));
        await(() -> bView.callId != null);
        a.sendText("Bình", "Đang đổ chuông");
        b.calls().accept(bView.callId);
        await(() -> aVoice.active && bVoice.active && bView.received.size() == 1);
        b.sendText("An", "Vừa gọi vừa chat");
        await(() -> aView.received.size() == 1);
        assertTrue(aVoice.active && bVoice.active);
        a.calls().endCall();
        await(() -> !aVoice.active && !bVoice.active);
        a.sendText("Bình", "Chat sau khi kết thúc cuộc gọi");
        await(() -> bView.received.size() == 2);
        assertTrue(aNet.connectedPeerIds().contains("Bình"));
    }

    @Test public void reportsFailureWithoutAppendingUnsentText() throws Exception {
        bNet.disconnect();
        await(() -> aView.peers.isEmpty());
        String draft = "Nội dung cần giữ lại khi mất kết nối";
        a.sendText("Bình", draft);
        await(() -> aView.failures.size() == 1);
        assertEquals("Bình", aView.failures.get(0).peerId());
        assertEquals(draft, aView.failures.get(0).text());
        assertTrue(aView.sent.isEmpty());
        int newPort = bNet.startListening(0, 0);
        aNet.connect("127.0.0.1", newPort);
        await(() -> aView.peers.size() == 1 && bView.peers.size() == 1);
        a.sendText("Bình", draft);
        await(() -> bView.received.size() == 1 && aView.sent.size() == 1);
        assertEquals(draft, bView.received.get(0).text());
    }

    @Test public void rejectsInvalidOutgoingTextBeforeSending() throws Exception {
        a.sendText("Bình", "   ");
        a.sendText("Bình", "x".repeat(ChatService.MAX_TEXT_LENGTH + 1));
        a.sendText("Bình", "Sau lỗi vẫn gửi được");
        await(() -> aView.failures.size() == 2 && bView.received.size() == 1
                && aView.sent.size() == 1);
        assertEquals(1, aView.sent.size());
        assertEquals("Sau lỗi vẫn gửi được", bView.received.get(0).text());
    }

    @Test public void ignoresInvalidAndGroupFramesWithoutBreakingChat() throws Exception {
        sendRaw("Người khác", null, "Sai người nhận", Instant.now());
        sendRaw("Bình", "room_group", "Tin nhóm", Instant.now());
        sendRaw("Bình", null, "   ", Instant.now());
        sendRaw("Bình", null, null, Instant.now());
        sendRaw("Bình", null, "Thiếu thời gian", null);
        sendRaw("Bình", null, "x".repeat(ChatService.MAX_TEXT_LENGTH + 1), Instant.now());
        a.sendText("Bình", "Tin hợp lệ cuối cùng");
        await(() -> bView.received.size() >= 1);
        assertEquals(1, bView.received.size());
        assertEquals("Tin hợp lệ cuối cùng", bView.received.get(0).text());
    }

    private void sendRaw(String target, String room, String text, Instant time) throws Exception {
        aNet.sendControl("Bình", new ControlMessage(ControlMessage.Type.CHAT,
                "An", target, room, text, time));
    }

    private static void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() >= deadline) fail("Timed out waiting for peer events");
            Thread.sleep(10);
        }
    }

    private record Failure(String peerId, String text, String reason) { }

    private static final class TestView implements AppCoordinator.View {
        final List<ChatMessage> received = new CopyOnWriteArrayList<>();
        final List<ChatMessage> sent = new CopyOnWriteArrayList<>();
        final List<Failure> failures = new CopyOnWriteArrayList<>();
        final List<String> errors = new CopyOnWriteArrayList<>();
        volatile List<PeerInfo> peers = List.of();
        volatile String callId;
        volatile boolean wrongThread;
        private void checkThread() { wrongThread |= !SwingUtilities.isEventDispatchThread(); }
        public void onMessageReceived(ChatMessage m) { checkThread(); received.add(m); }
        public void onMessageSent(ChatMessage m) { checkThread(); sent.add(m); }
        public void onMessageSendFailed(String id, String text, String reason) {
            checkThread(); failures.add(new Failure(id, text, reason));
        }
        public void outgoing(PeerInfo p) { checkThread(); }
        public void incoming(PeerInfo p, String id) { checkThread(); callId = id; }
        public void connected(PeerInfo p) { checkThread(); }
        public void idle(String m) { checkThread(); }
        public void muted(boolean m) { checkThread(); }
        public void error(String m) { checkThread(); errors.add(m); }
        public void setPeers(List<PeerInfo> p) { checkThread(); peers = p; }
    }

    private static final class TestVoice implements VoiceGateway {
        volatile boolean active;
        boolean muted;
        public void startCall(String id) { active = true; }
        public void stopCall() { active = false; }
        public void setMuted(boolean value) { muted = value; }
        public boolean isMuted() { return muted; }
    }
}
