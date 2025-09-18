# BTL Chat Application

Ứng dụng chat real-time với Java Socket, MongoDB và Swing GUI.

## Tính năng chính

- ✅ Kết nối Client-Server qua Socket
- ✅ Đăng ký/Đăng nhập với password hashing
- ✅ Chat real-time với Unicode support
- ✅ Quản lý phòng chat riêng tư
- ✅ Hiển thị danh sách người dùng online
- ✅ Lưu trữ lịch sử chat trong MongoDB
- ✅ Quản lý trạng thái người dùng (online/offline/away)
- ✅ Auto-reconnect khi mất kết nối
- ✅ GUI Swing hiện đại và dễ sử dụng

## Yêu cầu hệ thống

- Java 8 trở lên
- MongoDB 4.0 trở lên
- Ant (để build project)

## Cài đặt và chạy

### 1. Cài đặt MongoDB

Tải và cài đặt MongoDB từ: https://www.mongodb.com/try/download/community

Khởi động MongoDB:
```bash
mongod
```

### 2. Download dependencies

```bash
ant download-deps
```

### 3. Build project

```bash
ant compile
```

### 4. Chạy Server

```bash
ant run-server
```

### 5. Chạy Client

Mở terminal mới và chạy:
```bash
ant run-client
```

Hoặc chạy nhiều client để test:
```bash
ant run-client
ant run-client
ant run-client
```

## Cấu trúc project

```
btl/
├── src/btl/
│   ├── Server.java          # Server chính với MongoDB integration
│   └── Client.java          # Client với Swing GUI
├── build.xml               # Ant build script
├── lib/                    # Dependencies (MongoDB driver)
└── README.md              # Hướng dẫn này
```

## Sử dụng

### Đăng ký/Đăng nhập
- Mở client, nhập tên đăng nhập và mật khẩu
- Chọn "Đăng ký" để tạo tài khoản mới
- Chọn "Đăng nhập" để đăng nhập với tài khoản có sẵn

### Chat
- Gõ tin nhắn và nhấn Enter hoặc nút "Gửi"
- Tin nhắn sẽ được gửi đến tất cả người dùng trong phòng hiện tại
- Lịch sử chat được tự động load khi vào phòng

### Quản lý phòng
- Sử dụng dropdown "Phòng" để chuyển phòng
- Các lệnh chat:
  - `/join <tên_phòng>` - Chuyển vào phòng
  - `/create <tên_phòng>` - Tạo phòng mới
  - `/rooms` - Xem danh sách phòng
  - `/users` - Xem danh sách người dùng online
  - `/status <trạng_thái>` - Đổi trạng thái

### Danh sách người dùng
- Panel bên phải hiển thị danh sách người dùng đang online
- Tự động cập nhật khi có người tham gia/rời khỏi

## Cấu hình

### Thay đổi port server
Sửa trong `Server.java`:
```java
private static final int PORT = 8888; // Đổi port ở đây
```

### Thay đổi MongoDB URI
Sửa trong `Server.java`:
```java
private static final String MONGODB_URI = "mongodb://localhost:27017";
```

### Thay đổi server host
Sửa trong `Client.java`:
```java
private static final String SERVER_HOST = "localhost";
```

## Troubleshooting

### Lỗi kết nối MongoDB
- Đảm bảo MongoDB đang chạy
- Kiểm tra URI MongoDB trong code
- Kiểm tra firewall/antivirus

### Lỗi kết nối Server
- Đảm bảo Server đã khởi động
- Kiểm tra port có bị chiếm không
- Kiểm tra firewall

### Lỗi build
- Đảm bảo đã download dependencies: `ant download-deps`
- Kiểm tra Java version: `java -version`
- Kiểm tra Ant: `ant -version`

## Tính năng nâng cao

### Auto-reconnect
Client tự động thử kết nối lại mỗi 5 giây khi mất kết nối.

### Password Security
Mật khẩu được hash bằng SHA-256 trước khi lưu vào MongoDB.

### Unicode Support
Hỗ trợ đầy đủ tiếng Việt và các ký tự Unicode.

### Room Management
- Tạo phòng chat riêng tư
- Lưu lịch sử chat theo phòng
- Chuyển phòng dễ dàng

### Status Management
- Theo dõi trạng thái online/offline
- Lưu trạng thái trong MongoDB
- Hiển thị thời gian hoạt động cuối

## License

MIT License - Sử dụng tự do cho mục đích học tập và thương mại.
