# Module 3: desktop-app — Thanh vien 3 (CHI UI)

**TV3 chi phu trach** `src/main/java/com/voicechat/ui/`:
- `LoginFrame.java`: ten, IP, Port.
- `ChatFrame.java`: khung chat, hien thi tin nhan, nut gui.
- `CallPanel.java`: nut goi, mute, ket thuc va trang thai.
- Ban dau dung mock data; khong viet Socket hay nghiep vu chat vao UI.

**CA NHOM phu trach** phan khac trong module nay:
- `src/main/java/com/voicechat/Main.java`: entry point.
- `src/main/java/com/voicechat/management/`: UserManager, RoomManager.
- `src/main/java/com/voicechat/integration/`: AppCoordinator, ghep API backend vao UI.

Ly do: giu dung 4 Maven module, nen code tich hop/chung nam o module chay ung dung,
nhung *khong duoc giao rieng cho TV3*.

Build: `mvn -pl desktop-app -am package` tu thu muc goc.
