# VoiceChatP2P - 4 Maven Modules (phan cong moi)

> **Starter project, chua hoan chinh:** UI dang demo local, khong gui tin qua TCP, goi UDP chua duoc trien khai, SQLite chua luu du lieu.

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

Build rieng tung thanh vien:

```bash
mvn -pl network-core -am compile
mvn -pl voice-core -am compile
mvn -pl desktop-app -am compile
mvn -pl chat-core -am compile
```
