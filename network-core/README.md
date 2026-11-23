# Module 1: network-core — Thanh vien 1

- `network/`: TCP Socket, UDP Socket, PeerManager, xu ly timeout, ket noi peer.
- `common/api` va `common/model`: giao uoc dung chung **ca nhom** (TV1 luu tru vi de giu 4 module Maven, khong tu y sua).
- Khong duoc import `ui`, `audio`, `chat`, `management` vao module nay.

Build: `mvn -pl network-core -am compile` tu thu muc goc.

## Tich hop PeerManager

Tao mot `PeerManager(localPeerId)` cho moi peer, voi ID duy nhat. Dang ky
`onControlMessage` va `onVoicePacket` truoc khi ket noi. Callback chay tren
luong mang, nen chuyen cong viec cham sang luong cua module tich hop.

- Peer nhan goi `startListening(tcpPort, udpPort)`. Gia tri tra ve la cong
  TCP thuc te. Truyen 0 de he dieu hanh chon cong tu do.
- Peer khoi tao goi `connect(host, tcpPort)`. Hai dau tu trao doi ID va cong
  UDP tren TCP. Neu peer khoi tao chua lang nghe, no tu mo mot cong UDP.
- Dung ID cua peer dich khi goi `sendControl(peerId, message)` va
  `sendVoice(peerId, packet)`. Callback UDP nhan ID nguon va mang byte.
- Goi `disconnect()` khi thoat. Co the goi `startListening` va `connect`
  lai sau do.

## Giao thức trên mạng

- TCP dùng độ dài 4 byte big-endian rồi đến một object JSON UTF-8, tối đa
  524288 byte mỗi frame. Các trường là `type`, `senderId`, `targetId`,
  `roomId`, `payload` và `sentAt`. `sentAt` dùng chuỗi ISO-8601 của `Instant`.
- Frame đầu tiên ở mỗi chiều là `HELLO`. `senderId` là ID peer và
  `payload` là chuỗi JSON, ví dụ `{"udpPort":6101}`. B trả HELLO sau khi
  nhận HELLO hợp lệ của A. HELLO do PeerManager xử lý nội bộ.
- Frame có độ dài 0 là heartbeat, gửi mỗi 2 giây. Timeout đọc 8 giây,
  EOF hoặc lỗi frame làm đóng TCP và xóa peer tương ứng. Timeout kết
  nối và timeout đọc HELLO đều là 3 giây.
- UDP gửi nguyên byte[] tới IP đầu xa của TCP và cổng quảng bá qua HELLO.
  Callback nhận byte[] theo đúng kích thước datagram và ID đã bắt tay.
- Nhóm tích hợp cần chốt ID ổn định, cổng TCP/UDP và cấu hình firewall
  trên LAN. Giao thức hiện chạy trực tiếp, chưa có xác thực, mã hóa hay
  NAT traversal. UDP có thể mất gói hoặc đến sai thứ tự.

## Chạy trong IntelliJ IDEA trên Windows

1. Mở thư mục gốc LTM và chọn JDK 21 trong File > Project Structure >
   Project SDK. Trong Settings > Build Tools > Maven, chọn Maven Bundled
   và chọn JDK 21 ở Maven > Runner > JRE.
2. Mở cửa sổ Maven, chọn Reload All Maven Projects để nạp Jackson Core.
   Chọn Execute Maven Goal và nhập `-pl network-core -am compile`.
3. Để chạy ứng dụng, tạo cấu hình Application dùng class `com.voicechat.Main`,
   classpath của `desktop-app` và JRE 21. Cho phép chạy nhiều phiên nếu thử
   hai người dùng trên cùng máy.
4. Nhập tên, địa chỉ IP và cổng trên màn hình đăng nhập theo bảng cấu hình
   trong [README gốc](../README.md). Khi chạy trên hai máy cùng LAN, dùng IP
   LAN của máy chờ và cho phép các cổng TCP/UDP tương ứng qua firewall.

Chạy `mvn verify` từ thư mục gốc để kiểm tra chat và điều phối cuộc gọi qua
kết nối TCP thật; phần âm thanh trong kiểm thử được giả lập.
