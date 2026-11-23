# chat-core — Chat 1-1, chat nhóm và lịch sử SQLite

Phần của Tùng gồm nghiệp vụ chat và lưu/đọc tin nhắn.
Quản lý người dùng/phòng và ghép backend với UI là phần chung của nhóm,
đặt trong `desktop-app`.

## Thành phần

| Thành phần | Vai trò |
|---|---|
| `chat/DirectChatProtocol` | Payload JSON có phiên bản, ID tin nhắn và nội dung; tạo mã cuộc trò chuyện chung cho hai peer |
| `chat/ChatService` | Gửi/nhận tin trực tiếp qua `NetworkGateway`, lưu và đọc qua `HistoryStore` |
| `database/DatabaseManager` | Kết nối, transaction và schema SQLite |
| `database/HistoryRepository` | Lưu tin theo ID duy nhất, lấy lịch sử cuộc trò chuyện và trang lịch sử nhóm |
| `group/GroupClient`, `group/GroupProtocol` | Gửi yêu cầu và nhận phản hồi từ máy chủ nhóm qua TCP |
| `desktop-app/.../group/GroupHost` | Xử lý yêu cầu nhóm, kiểm tra thành viên và truy cập SQLite máy chủ |
| `desktop-app/.../integration/DirectChatCoordinator` | Chạy chat trực tiếp ngoài EDT và trả kết quả về UI |
| `desktop-app/.../integration/GroupChatCoordinator` | Điều phối yêu cầu nhóm, timer cập nhật và phân trang |

`ChatService` và `GroupClient` là hai đường xử lý riêng.
`DesktopSession` ghép chat trực tiếp với gọi thoại; `CallCoordinator` chuyển
tin `CHAT` sang `DirectChatCoordinator` qua `onChatMessage()`.
Callback TCP của `PeerManager` chỉ giữ một handler, vì vậy phần chat trực tiếp
nhận tin qua điểm tích hợp này.

## Chạy và kiểm tra chat 1-1

Các lệnh dùng PowerShell trên Windows, chạy từ thư mục gốc repository:

```powershell
mvn clean verify
java -jar desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar
```

Mở hai client và bỏ chọn **Chat nhóm qua máy chủ**.
Dùng bảng cấu hình P2P trong [README chính](../README.md#chat-và-gọi-thoại-1-1):
B nghe TCP/UDP `5100/6100`; A nghe `5101/6101` và kết nối đến B qua TCP `5100`.
Tên hai peer phải khác nhau.

Kiểm tra:

1. B chờ kết nối, sau đó A kết nối đến B.
2. Chọn liên hệ, gửi tin từ A sang B và ngược lại, gồm tiếng Việt và emoji.
3. Đóng hai client, mở lại với đúng tên cũ, kết nối và chọn liên hệ để tải lịch sử.
4. Ngắt kết nối rồi thử gửi từ cuộc trò chuyện đang mở; giao diện phải báo lỗi
   và khôi phục nội dung nếu ô soạn thảo đang trống.
5. Gọi thoại giữa hai peer, thử nhắn tin khi đang gọi và sau khi kết thúc.

### Dữ liệu và giao thức trực tiếp

- Tin nhắn dùng `ControlMessage.Type.CHAT`. Header có người gửi, người nhận,
  mã cuộc trò chuyện và thời gian; payload JSON chứa `version`, `id`, `text`.
- Mã cuộc trò chuyện được tạo từ hai ID theo thứ tự ổn định, nên hai phía dùng
  cùng mã. ID tin nhắn cũng được giữ nguyên ở hai phía.
- Nội dung được bỏ khoảng trắng đầu/cuối, không được rỗng, tối đa 1.000 đơn vị
  UTF-16 (`String.length()` trong Java).
- Mỗi peer lưu bản tin tại máy mình. Database mặc định:
  `<user.home>/.voicechat/direct/<UUID>.db`; UUID được tạo từ tên đăng nhập.
  Trên Windows thường nằm dưới `%USERPROFILE%\.voicechat\direct\`.
- Để mở lại lịch sử của cùng hồ sơ, sử dụng cùng tên và cùng thư mục
  `user.home`. Khi chọn liên hệ, dữ liệu được đọc lại từ SQLite.
- Tin nhận sai người nhận, sai mã cuộc trò chuyện hoặc payload không hợp lệ
  bị từ chối. SQLite dùng ID duy nhất để tránh ghi lại cùng tin.
- Lịch sử P2P trên giao diện tổng hợp các cuộc trò chuyện đã được nạp trong
  phiên hiện tại.

## Chạy máy chủ và client nhóm

Từ thư mục gốc, sau khi build:

```powershell
java -cp desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar com.voicechat.group.GroupHost 5000
```

Mở client bằng JAR chính:

```powershell
java -jar desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar
```

Chọn **Chat nhóm qua máy chủ**, nhập tên hồ sơ riêng, IP máy chủ và cổng
`5000`. Khi chạy trên cùng máy, IP là `127.0.0.1`.
Mỗi client nhóm có kết nối TCP riêng; không cần cấu hình cổng TCP/UDP P2P
trong chế độ này.

Một người tạo nhóm bằng nút **+**. Những người còn lại chọn phòng,
bấm **Tham gia**, rồi nhắn tin. Sau khi **Rời phòng**, người dùng không thể
gửi tin hoặc đọc lịch sử phòng đó cho đến khi tham gia lại.

Máy chủ giữ các bảng `users`, `rooms`, `members`, `messages` trong
`voicechat.db` ở thư mục chạy. Có thể chọn file khác bằng thuộc tính JVM:

```powershell
java "-Dvoicechat.db=group-history.db" -cp desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar com.voicechat.group.GroupHost 5000
```

ID hồ sơ nhóm được lưu bằng Java Preferences trên máy client. Khi đăng nhập
lại, dùng đúng tên hồ sơ ban đầu trên cùng tài khoản hệ điều hành.
Client lấy cập nhật khoảng 1,8 giây một lần ở trang mới nhất.
Mục **Lịch sử** lấy tin từ các phòng người dùng hiện đang tham gia,
mỗi trang tối đa 30 tin; dùng **Cũ hơn / Mới hơn** để chuyển trang.

## Kiểm thử có sẵn

```powershell
mvn clean package
java -cp "desktop-app/target/test-classes;desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar" com.voicechat.group.GroupSmoke
```

`GroupSmoke` dùng máy chủ thật, ba client TCP và SQLite tạm để kiểm tra
đăng nhập, tên trùng, tham gia/rời phòng, quyền đọc/gửi, cách ly phòng,
phân trang và dữ liệu sau khi mở lại máy chủ.

`CallCoordinatorTest` dùng TCP thật và âm thanh giả lập để kiểm tra
điều phối cuộc gọi; tự chạy trong Maven. `GroupSmoke` cần chạy riêng.
Kiểm tra chat 1-1 qua giao diện theo quy trình ở trên.

Build riêng module: `mvn -pl chat-core -am compile`.
