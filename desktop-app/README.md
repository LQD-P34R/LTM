# desktop-app — Giao diện và tích hợp VoiceLink

Module chạy ứng dụng, phụ thuộc `network-core`, `voice-core` và `chat-core`.
Thành viên phụ trách UI chỉ phụ trách package `ui/`; phần tích hợp,
quản lý và entry point là công việc chung của nhóm.

## Phân chia thành phần

### Giao diện Swing: ui/

| Class | Vai trò |
|---|---|
| `LoginFrame` | Nhập hồ sơ, chọn P2P hoặc chat nhóm, mở phiên tương ứng |
| `ChatFrame` | Cửa sổ chính, chuyển thao tác và nhận callback từ coordinator |
| `ChatContentPanel` | Các màn hình chat/lịch sử/cuộc gọi, cache hiển thị và phân trang |
| `WelcomePanel`, `ChatStatusBar` | Màn hình chào và thanh trạng thái |
| `CreateGroupDialog` | Nhận tên nhóm từ người dùng |
| `DirectChatPanel`, `GroupChatPanel` | Hiển thị cuộc trò chuyện và ô soạn thảo |
| `HistoryPanel` | Bảng lịch sử |
| `CallPanel`, `IncomingCallDialog` | Trạng thái cuộc gọi và phản hồi lời mời |

Các component giao diện được tách khỏi `ChatFrame` để cửa sổ chính tập trung
vào ghép sự kiện với coordinator.

### Phần chung của nhóm

- `Main.java`: mở `LoginFrame`.
- `integration/DesktopSession`: tạo phiên P2P, kết nối mạng, voice và chat
  1-1; quản lý vòng đời khi mở/đóng cửa sổ.
- `integration/DirectChatCoordinator`: gửi, nhận và đọc lịch sử ngoài EDT;
  callback hiển thị chạy trên EDT.
- `integration/CallCoordinator`: điều phối cuộc gọi, trạng thái peer và
  chuyển tin `CHAT` sang handler chat trực tiếp.
- `integration/GroupChatCoordinator`: yêu cầu nhóm, cập nhật định kỳ,
  phân trang và đóng client nhóm.
- `management/UserManager`, `management/RoomManager`: quản lý người dùng,
  phòng và thành viên phía máy chủ nhóm.
- `group/GroupHost`: entry point máy chủ nhóm.
- `src/test/java/com/voicechat/group/GroupSmoke.java`: kiểm thử tích hợp nhóm.

## Luồng chạy

```text
Main -> LoginFrame
  ├── P2P -> DesktopSession -> CallCoordinator + DirectChatCoordinator
  │                            -> ChatFrame
  └── Nhóm -> GroupClient đăng nhập -> ChatFrame + GroupChatCoordinator
```

Các coordinator chuyển kết quả về EDT để cập nhật Swing.
Chat 1-1 dùng kết nối TCP của phiên P2P; client nhóm dùng kết nối riêng
đến `GroupHost`.

## Build và chạy

Chạy các lệnh từ thư mục gốc repository:

```powershell
mvn -pl desktop-app -am package
java -jar desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar
```

Xem [README chính](../README.md) để cấu hình hai peer, chạy máy chủ nhóm
và thực hiện kiểm thử. Vị trí database và giao thức nằm trong
[README chat-core](../chat-core/README.md).
