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
    // ===== Dữ liệu và các kiểu hỗ trợ =====
    // Các trường lưu cấu hình/trạng thái; các kiểu lồng nhau mô tả dữ liệu và hợp đồng dùng trong lớp.
    public interface Listener {

        // ===== Lịch sử chat trực tiếp =====
        // Nhận toàn bộ lịch sử của peer sau khi đọc hoặc cập nhật tin nhắn.
        void onDirectHistory(String peerId, List<ChatMessage> messages);

        // ===== Lỗi chat và khôi phục bản nháp =====
        // Nhận nội dung gửi thất bại và thông báo lỗi để giao diện phục hồi ô soạn.
        void onDirectSendFailed(String peerId, String text);

        void onDirectError(String message);
    }
    @FunctionalInterface
    private interface Work {

        // ===== Tác vụ chat chạy nền =====
        // Đóng gói thao tác có thể phát sinh lỗi để worker thực thi và báo về giao diện.
        void run() throws Exception;
    }
    private final String localId;
    private final ChatService chat;
    private final Listener listener;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(
            r -> Thread.ofVirtual().unstarted(r));
    private final AtomicBoolean closed = new AtomicBoolean();

    // ===== Khởi tạo điều phối chat trực tiếp =====
    // Nhận ID cục bộ, nghiệp vụ chat và listener cập nhật giao diện.
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

    // ===== Gửi và nhận chat =====
    // Thực hiện nghiệp vụ chat trên worker; gửi lỗi hoặc nạp lại lịch sử sau mỗi thay đổi.
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

    // ===== Nạp và công bố lịch sử =====
    // Đọc lịch sử hội thoại và gửi bản sao danh sách về listener.
    public void loadHistory(String peerId) {
        submit(() -> publishHistory(peerId));
    }

    private void publishHistory(String peerId) {
        List<ChatMessage> messages = List.copyOf(chat.directHistory(localId, peerId));

        notifyUi(() -> listener.onDirectHistory(peerId, messages));
    }

    // ===== Điều phối nền và xử lý lỗi =====
    // Xếp việc tuần tự, đưa thông báo về EDT và chuẩn hóa thông điệp lỗi.
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

    // ===== Đóng worker chat =====
    // Đánh dấu đã đóng và ngừng nhận/xử lý thêm tác vụ.
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            worker.shutdownNow();
        }
    }
}