# VoiceChatP2P — VoiceLink

Ứng dụng Java Swing gồm bốn Maven module, hỗ trợ chat 1-1 qua TCP, gọi thoại
1-1 qua UDP và chat nhóm qua máy chủ TCP. Tin nhắn được lưu bằng SQLite.

Có hai chế độ đăng nhập: **P2P để chat/gọi 1-1** và **Chat nhóm qua máy chủ**.
Xem [chi tiết chat và lưu trữ](chat-core/README.md) hoặc
[cấu trúc giao diện và tích hợp](desktop-app/README.md).

## Phân công

| Thành viên | Module/phần phụ trách | Công việc |
|---|---|---|
| Sơn | `network-core` | TCP/UDP, kết nối P2P, giao thức mạng |
| Duy | `voice-core` | Microphone, PCM, voice UDP, mute |
| Nga | `desktop-app/src/main/java/com/voicechat/ui/` | Giao diện Swing, các nút và hiển thị |
| Tùng | `chat-core` | Chat 1-1/nhóm, lưu và lấy lịch sử SQLite |
| Cả nhóm | `common/`, `management/`, `integration/`, `docs/`, `Main.java` | Hợp đồng API, quản lý người dùng/phòng, tích hợp và kiểm thử |

`desktop-app` chứa cả UI và phần tích hợp chung; module này không được giao
toàn bộ cho thành viên phụ trách UI. API/model chung nằm trong
`network-core/src/main/java/com/voicechat/common/`; mọi thay đổi tại đây cần
cả bốn thành viên phê duyệt.

Cấu trúc chính:

```text
VoiceChatP2P/
├── pom.xml                 # Parent Maven
├── network-core/           # network/, common/api/, common/model/
├── voice-core/             # audio/
├── chat-core/              # chat/, database/, group/
└── desktop-app/            # Main.java, ui/, integration/, management/, group/
```

Hướng phụ thuộc (`A -> B` nghĩa là A sử dụng B):

```text
desktop-app -> voice-core -> network-core
desktop-app -> chat-core  -> network-core
desktop-app -> network-core
```

## Build và mở ứng dụng

Cần **JDK 21** và **Maven** trong PATH. Các lệnh dưới chạy từ thư mục gốc
của repository bằng PowerShell trên Windows.

```powershell
mvn clean package
java -jar desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar
```

Các ví dụ sử dụng JAR vừa build trong `desktop-app/target/`.

## Chat và gọi thoại 1-1

Mở hai terminal, mỗi terminal chạy lệnh mở ứng dụng ở trên.
Bỏ chọn **Chat nhóm qua máy chủ** trên cả hai client.

| Cấu hình | Client B | Client A |
|---|---|---|
| Tên người dùng | `User_B` | `User_A` |
| TCP máy này | `5100` | `5101` |
| UDP máy này | `6100` | `6101` |
| Chờ peer kết nối đến | Chọn | Bỏ chọn |
| Địa chỉ IP máy chủ / peer | Không cần nhập khi đang chờ | `127.0.0.1` |
| Cổng TCP máy chủ / peer | Không cần nhập khi đang chờ | `5100` |

1. Bấm **Kết nối / Đăng nhập** ở B trước, rồi ở A.
2. Chọn peer trong sidebar và nhắn tin hai chiều.
3. Bấm **Gọi thoại** trong cuộc trò chuyện; bên nhận chấp nhận hoặc từ chối.
   Khi đang gọi có thể mute, kết thúc cuộc gọi và tiếp tục chat.
4. Để kiểm tra lưu trữ, đóng hai client, mở lại bằng đúng tên cũ, kết nối lại
   và chọn liên hệ. Lịch sử sẽ được nạp từ SQLite.

Mỗi tên người dùng có một database tại
`<user.home>/.voicechat/direct/<UUID>.db`, trong đó UUID được tạo từ tên.
Trên Windows, thư mục mặc định thường là `%USERPROFILE%\.voicechat\direct\`.
Mục **Lịch sử** ở chế độ P2P hiển thị các cuộc trò chuyện đã được nạp trong phiên.

## Chat nhóm qua máy chủ

Sau khi build, mở một terminal riêng để chạy máy chủ:

```powershell
java -cp desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar com.voicechat.group.GroupHost 5000
```

Giữ terminal máy chủ mở. Mở ứng dụng cho từng người dùng:

```powershell
java -jar desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar
```

1. Chọn **Chat nhóm qua máy chủ**.
2. Nhập tên hồ sơ riêng, IP máy chủ (`127.0.0.1` khi chạy cùng máy) và TCP
   `5000`. Các ô cổng P2P của máy này sẽ bị vô hiệu hóa.
3. Một người tạo nhóm bằng nút **+** trong sidebar. Người khác chọn nhóm
   và bấm **Tham gia** trước khi gửi tin.
4. Dùng **Rời phòng** để rời nhóm. Mục **Lịch sử** chỉ lấy tin từ các phòng
   người dùng hiện còn tham gia; dùng **Cũ hơn / Mới hơn** để chuyển trang.

Máy chủ lưu `voicechat.db` trong thư mục chạy. Client lấy cập nhật khoảng
1,8 giây một lần ở trang mới nhất. Khi đăng nhập lại, dùng đúng tên hồ sơ ban đầu
trên cùng tài khoản hệ điều hành để giữ ID nhóm đã lưu.
Gọi thoại thực hiện ở chế độ P2P.

Khi chạy trên LAN, thay `127.0.0.1` bằng IP LAN của máy B hoặc máy chủ nhóm.
Cho phép TCP/UDP trên các cổng P2P đã chọn và TCP trên cổng máy chủ nhóm.

## Kiểm thử

Sau `mvn clean package`, chạy riêng từng chương trình kiểm thử:

```powershell
java -cp "desktop-app/target/test-classes;desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar" com.voicechat.group.GroupSmoke
java -cp desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar com.voicechat.integration.CallCoordinatorDemo
```

- `GroupSmoke`: ba client TCP, SQLite tạm, thành viên, lịch sử, phân trang
  và dữ liệu sau khi khởi động lại máy chủ.
- `CallCoordinatorDemo`: TCP thật, kiểm tra request/accept/reject, mute,
  kết thúc, timeout và đóng phiên; âm thanh được giả lập.
- Chat 1-1 trên giao diện: kiểm tra gửi hai chiều, lịch sử sau mở lại và chat
  trong lúc gọi theo hướng dẫn P2P ở trên. Gọi bằng microphone/loa thật cần
  chạy hai client và chấp nhận cuộc gọi.

Các smoke test là chương trình `main`, không tự chạy chỉ bằng `mvn test`.

Build riêng từng module cùng các phụ thuộc:

```powershell
mvn -pl network-core -am compile
mvn -pl voice-core -am compile
mvn -pl chat-core -am compile
mvn -pl desktop-app -am package
```
