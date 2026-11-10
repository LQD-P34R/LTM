# Module 4: chat-core — Thanh vien 4 (CHAT)

- `chat/ChatController.java`: dong goi lenh gui chat qua `NetworkGateway`.
- `chat/ChatService.java`: xu ly nghiep vu tin nhan, goi `HistoryStore`, xu ly tin nhan nhan vao (TODO).
- `database/DatabaseManager.java`: ket noi SQLite (can them sqlite-jdbc khi trien khai).
- `database/HistoryRepository.java`: luu/doc lich su chat (TODO).
- Tuyet doi KHONG import Java Swing trong module chat.
- Phan quan ly nguoi dung/phong (`management/`) da chuyen sang phan viec CHUNG, khong thuoc TV4.

Build: `mvn -pl chat-core -am compile` tu thu muc goc.
