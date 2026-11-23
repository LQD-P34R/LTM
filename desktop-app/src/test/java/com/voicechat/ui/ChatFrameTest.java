package com.voicechat.ui;

import com.voicechat.common.api.VoiceGateway;
import com.voicechat.common.model.PeerInfo;
import com.voicechat.integration.AppCoordinator;
import com.voicechat.network.PeerManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

public class ChatFrameTest {
    /** Uses real Swing frames without showing windows, plus real TCP connections. */
    @Test public void enterAndSendButtonReachOtherFrameBeforePeerSelection() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless());
        PeerManager aNet = new PeerManager("A");
        PeerManager bNet = new PeerManager("B");
        ChatFrame aFrame = edt(() -> new ChatFrame("A"));
        ChatFrame bFrame = edt(() -> new ChatFrame("B"));
        AppCoordinator a = new AppCoordinator(aNet, silentVoice(), "A", aFrame);
        AppCoordinator b = new AppCoordinator(bNet, silentVoice(), "B", bFrame);
        try {
            edt(() -> { aFrame.bindApp(a); bFrame.bindApp(b); return null; });
            int port = bNet.startListening(0, 0);
            aNet.startListening(0, 0);
            aNet.connect("127.0.0.1", port);
            // Flush the call-control peer updates before selecting the contact.
            a.calls().open(() -> { });
            b.calls().open(() -> { });
            long readyDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!edt(() -> {
                aFrame.onPeerSelected(new PeerInfo("B", "B", "", 0, 0));
                DirectChatPanel panel = components(aFrame, DirectChatPanel.class).get(0);
                InputComposer composer = components(panel, InputComposer.class).get(0);
                return components(composer, JTextField.class).get(0).isEnabled();
            })) {
                if (System.nanoTime() > readyDeadline) fail("Chat input did not become ready");
                Thread.sleep(10);
            }
            edt(() -> {
                InputComposer composer = components(aFrame, InputComposer.class).get(0);
                composer.setText("Xin chào từ nút Enter 😊");
                components(composer, JTextField.class).get(0).postActionEvent();
                return null;
            });
            awaitBubbles(aFrame, 1);
            awaitBubbles(bFrame, 1);
            edt(() -> {
                // B has not selected A: the incoming message must create its chat panel.
                DirectChatPanel panel = components(bFrame, DirectChatPanel.class).get(0);
                assertTrue(panel.isVisible());
                assertEquals("Xin chào từ nút Enter 😊", components(panel, JTextArea.class).get(0).getText());
                InputComposer composer = components(panel, InputComposer.class).get(0);
                composer.setText("Phản hồi bằng nút Gửi");
                components(composer, JButton.class).stream()
                        .filter(button -> "Gửi tin nhắn (Enter)".equals(button.getToolTipText()))
                        .findFirst().orElseThrow().doClick();
                return null;
            });
            awaitBubbles(aFrame, 2);
            awaitBubbles(bFrame, 2);
            edt(() -> {
                aFrame.onPeerSelected(new PeerInfo("B", "B", "", 0, 0));
                assertEquals(2, components(aFrame, ChatBubbleRenderer.class).size());
                assertEquals("Phản hồi bằng nút Gửi", components(aFrame, JTextArea.class).get(1).getText());
                return null;
            });
        } finally {
            a.closeAsync().get(4, TimeUnit.SECONDS);
            b.closeAsync().get(4, TimeUnit.SECONDS);
            edt(() -> { aFrame.dispose(); bFrame.dispose(); return null; });
        }
    }

    private static void awaitBubbles(ChatFrame frame, int count) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!edt(() -> components(frame, ChatBubbleRenderer.class).size() == count)) {
            if (System.nanoTime() >= deadline) fail("Expected " + count + " chat bubbles");
            Thread.sleep(10);
        }
    }

    private static VoiceGateway silentVoice() {
        return new VoiceGateway() {
            public void startCall(String id) { }
            public void stopCall() { }
            public void setMuted(boolean m) { }
            public boolean isMuted() { return false; }
        };
    }

    private static <T> T edt(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeAndWait(task);
        return task.get(5, TimeUnit.SECONDS);
    }

    private static <T> List<T> components(Container parent, Class<T> type) {
        List<T> found = new ArrayList<>();
        for (Component c : parent.getComponents()) {
            if (type.isInstance(c)) found.add(type.cast(c));
            if (c instanceof Container child) found.addAll(components(child, type));
        }
        return found;
    }
}
