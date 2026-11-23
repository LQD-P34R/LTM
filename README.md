# VoiceChatP2P - 4 Maven Modules (phan cong moi)

> **Trạng thái hiện tại:** Giao diện đăng nhập đã kết nối mạng thật; chat văn bản 1-1 qua TCP và điều khiển gọi thoại UDP đã được tích hợp. Phòng nhóm và lưu lịch sử SQLite chưa hoàn thiện; lịch sử hiện chỉ giữ trong phiên chạy.

## Phan cong

| Thanh vien | Module | Lam gi  
| Son | `network-core` | TCP/UDP, ket noi P2P, giao thuc mang |
| Duy | `voice-core` | Microphone, PCM, voice UDP, mute |
| Nga | `desktop-app` -> **chi `ui/`** | Giao dien Swing, cac nut va hien thi |
| Tung | `chat-core` | Chat 1-1/nhom, luu va lay lich su SQLite |
| **Ca nhom** | `common/`, `management/`, `integration/`, `docs/`, `Main.java` | Hop dong API, quan ly nguoi dung/phong, ket noi module, test | Khong giao rieng ai |

## Cay thu muc (rut gon)

```text
VoiceChatP2P_4_Modules_Reassigned/
├── pom.xml                         # Parent Maven
├── network-core/                   # Son
│   ├── pom.xml
│   └── src/main/java/com/voicechat/{network,common}/
├── voice-core/                     # Duy
│   ├── pom.xml
│   └── src/main/java/com/voicechat/audio/
├── chat-core/                      # Tung
│   ├── pom.xml
│   └── src/main/java/com/voicechat/{chat,database}/
├── desktop-app/                    # Nga
│   ├── pom.xml
│   └── src/main/java/com/voicechat/
│       ├── Main.java               # CHUNG
│       ├── ui/                     # Nga
│       ├── management/             # CHUNG (RoomManager, UserManager)
│       └── integration/            # CHUNG (AppCoordinator)
├── docs/                           # CHUNG
└── assets/
```

**Huong phu thuoc:** `network-core` -> `voice-core` va `chat-core` -> `desktop-app`. Khong import nguoc vao module thap hon.
`network-core/common/` chua api, model boi vi chi co 4 Maven module; moi thay doi phai duoc 4 nguoi phe duyet.

## Chay bang Java 21 + Maven

Mo thu muc goc bang VS Code/IntelliJ, sau do:

```bash
mvn clean package
java -jar desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar
```

## Thử chat văn bản giữa hai người

Chạy ứng dụng trên cả hai máy bằng lệnh `java -jar` ở trên. Dùng tên khác nhau cho hai người.

| Thiết lập | Máy B (chờ kết nối) | Máy A (kết nối tới B) |
|---|---|---|
| Tên người dùng | `User_B` | `User_A` |
| TCP máy này | `5100` | `5101` |
| UDP máy này | `6100` | `6101` |
| Chờ peer kết nối | Bật | Tắt |
| Địa chỉ IP peer | Không cần nhập | IP LAN của B, hoặc `127.0.0.1` nếu chạy hai phiên trên một máy |
| Cổng TCP của peer | Không cần nhập | `5100` |

1. Bấm kết nối trên B trước, sau đó trên A. Cho phép các cổng TCP/UDP tương ứng qua firewall khi thử trong LAN.
2. Chọn người còn lại ở danh sách bên trái, nhập nội dung rồi nhấn **Enter** hoặc nút **Gửi**. Tin nhắn đến cũng được giữ và tạo khung chat ngay cả khi chưa chọn người gửi.
3. Gửi thử tiếng Việt, emoji và nhiều tin liên tiếp theo cả hai chiều. Có thể chat trong khi gọi thoại và sau khi kết thúc cuộc gọi.
4. Khi peer mất kết nối, ô gửi bị vô hiệu hóa. Nếu một lần gửi lỗi, ứng dụng báo lỗi và khôi phục nội dung vào ô soạn thảo. Mỗi tin tối đa 16.000 ký tự; tin trống không được gửi.

Tin gửi chỉ được thêm vào hội thoại sau khi ghi thành công vào kết nối TCP. Ứng dụng chưa có xác nhận đã đọc/đã nhận ở cấp ứng dụng; lịch sử sẽ mất khi thoát.

Kiểm tra tự động: `mvn verify` (TCP hai chiều, Unicode, thứ tự tin, phân luồng chat/cuộc gọi, mất kết nối, phục hồi gửi và thao tác Swing). Kiểm thử cửa sổ Swing được bỏ qua khi môi trường chạy ở chế độ headless.

Build rieng tung thanh vien:

```bash
mvn -pl network-core -am compile
mvn -pl voice-core -am compile
mvn -pl desktop-app -am compile
mvn -pl chat-core -am compile
```
