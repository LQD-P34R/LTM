package com.voicechat.ui;

import com.voicechat.common.model.PeerInfo;
import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class DirectChatPanelTest {
    @Test public void restoresFailedDraftAndDisablesSendingWhileOfflineOrPending() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicInteger sends = new AtomicInteger();
            DirectChatPanel panel = new DirectChatPanel(new PeerInfo("B", "B", "", 0, 0),
                    "A", new DirectChatPanel.ActionCallback() {
                        public void onSendMessage(PeerInfo p, String text) { sends.incrementAndGet(); }
                        public void onCallRequested(PeerInfo p) { }
                    });
            InputComposer composer = find(panel, InputComposer.class);
            JTextField input = find(composer, JTextField.class);
            composer.setText("Bản nháp");
            input.postActionEvent();
            assertEquals(1, sends.get());
            assertEquals("", composer.getText());
            panel.setSending(true);
            panel.setConnected(false);
            panel.restoreFailedMessage("Bản nháp");
            assertEquals("Bản nháp", composer.getText());
            assertFalse(input.isEnabled());
            input.postActionEvent();
            assertEquals(1, sends.get());
            panel.setConnected(true);
            assertTrue(input.isEnabled());
            panel.setSending(true);
            assertFalse(input.isEnabled());
            input.postActionEvent();
            assertEquals(1, sends.get());
            panel.setSending(false);
            input.postActionEvent();
            assertEquals(2, sends.get());
        });
    }

    private static <T> T find(Container container, Class<T> type) {
        for (Component c : container.getComponents()) {
            if (type.isInstance(c)) return type.cast(c);
            if (c instanceof Container child) {
                T result = find(child, type);
                if (result != null) return result;
            }
        }
        return null;
    }
}
