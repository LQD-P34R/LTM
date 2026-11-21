# Phần Minh Tùng – Người dùng, phòng nhóm và lịch sử

Đăng nhập tại LoginFrame của VoiceLink. ChatFrame dùng SidebarPanel để chọn phòng,
GroupChatPanel để hiển thị thành viên/tin nhắn và HistoryPanel để xem lịch sử.
Các thao tác nhóm sử dụng NetworkGateway của Sơn. GroupHost giữ một file SQLite
dùng chung cho các client. Mỗi máy khách lấy cập nhật khoảng 1,8 giây một lần.

- DatabaseManager tạo users, rooms, members và messages.
- UserManager quản lý tên hiển thị theo ID ổn định. RoomManager tạo phòng và quản lý thành viên.
- GroupClient gửi yêu cầu qua TCP. GroupHost kiểm tra thành viên trước khi ghi hay đọc.
- HistoryRepository truy vấn trang 30 tin. Mục Lịch sử gồm các phòng người dùng hiện đang tham gia.
- GroupSmoke dùng máy chủ thật, ba client và SQLite tạm để kiểm tra luồng qua TCP.

Từ thư mục gốc của repo, dùng JDK 21 và Maven:

```powershell
mvn clean package
java -cp "desktop-app/target/test-classes;desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar" com.voicechat.group.GroupSmoke
java -cp desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar com.voicechat.group.GroupHost 5000
```

Giữ máy chủ mở. Mở một Terminal mới cho mỗi người dùng:

```powershell
java -jar desktop-app/target/desktop-app-1.0-SNAPSHOT-all.jar
```

Nhập IP máy chủ (127.0.0.1 khi chạy cùng laptop), cổng 5000 và tên hồ sơ riêng.
Tạo nhóm ở sidebar. Người thứ hai chọn nhóm, nhấn Tham gia rồi nhắn tin.
Bấm Lịch sử để đọc các trang cũ. Đổi tên thay tên hiển thị. Khi đăng nhập lại
dùng tên hồ sơ ban đầu trên cùng tài khoản hệ điều hành. Máy chủ dùng voicechat.db
trong thư mục chạy, có thể chọn file bằng thuộc tính JVM voicechat.db.
