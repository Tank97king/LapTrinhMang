
<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   XÂY DỰNG ỨNG DỤNG CHAT CLIENT-SERVER SỬ DỤNG GIAO THỨC TCP
</h2>
<div align="center">
    <p align="center">
      <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/X%C3%82Y%20D%E1%BB%B0NG%20%E1%BB%A8NG%20D%E1%BB%A4NG%20CHAT%20CLIENT-SERVER%20S%E1%BB%AC%20D%E1%BB%A4NG%20GIAO%20TH%E1%BB%A8C%20TCP/%E1%BA%A2nh/aiotlab_logo.png?raw=true" alt="AIoTLab Logo" width="170"/>
      <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/X%C3%82Y%20D%E1%BB%B0NG%20%E1%BB%A8NG%20D%E1%BB%A4NG%20CHAT%20CLIENT-SERVER%20S%E1%BB%AC%20D%E1%BB%A4NG%20GIAO%20TH%E1%BB%A8C%20TCP/%E1%BA%A2nh/fitdnu_logo.png?raw=true" alt="FITDNU Logo" width="180"/>
      <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/X%C3%82Y%20D%E1%BB%B0NG%20%E1%BB%A8NG%20D%E1%BB%A4NG%20CHAT%20CLIENT-SERVER%20S%E1%BB%AC%20D%E1%BB%A4NG%20GIAO%20TH%E1%BB%A8C%20TCP/%E1%BA%A2nh/dnu_logo.png?raw=true" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>
## 📖 1. Giới thiệu hệ thống

Ứng dụng chat Client-Server sử dụng giao thức TCP cho phép nhiều người dùng giao tiếp thời gian thực qua mạng.  
**Server**: đóng vai trò trung tâm, quản lý kết nối và chuyển tiếp tin nhắn.  
**Client**: cung cấp giao diện để gửi/nhận tin nhắn.  
**Lưu trữ dữ liệu**: lịch sử chat được lưu vào file văn bản thay vì cơ sở dữ liệu, giúp triển khai đơn giản.  

Các chức năng chính:  

- ✅ Kết nối Client-Server qua Socket
- ✅ Đăng ký/Đăng nhập với password hashing
- ✅ Chat real-time với Unicode support
- ✅ Quản lý phòng chat riêng tư
- ✅ Hiển thị danh sách người dùng online
- ✅ Lưu trữ lịch sử chat trong MongoDB
- ✅ Quản lý trạng thái người dùng (online/offline/away)
- ✅ Auto-reconnect khi mất kết nối
- ✅ GUI Swing hiện đại và dễ sử dụng

**🖥️ Chức năng của Server**:  
1. Kết nối & Quản lý Client: Lắng nghe các yêu cầu kết nối, tạo luồng riêng cho từng Client, quản lý danh sách Client đang hoạt động.  
2. Trung gian phân phối tin nhắn:

    Client gửi tin nhắn → Server nhận.
    Server chuyển tiếp tin nhắn đến tất cả Client khác.
    Các Client không giao tiếp trực tiếp mà thông qua Server.  
    
3. Quản lý lịch sử chat: Lưu tin nhắn (có timestamp) vào file văn bản.  
4. Xóa lịch sử: Cung cấp chức năng xóa toàn bộ file lưu trữ khi cần.  
5. Xử lý lỗi & đóng kết nối: Khi Client ngắt kết nối hoặc lỗi I/O, Server loại bỏ Client khỏi danh sách và tiếp tục phục vụ các Client khác.

**💻 Chức năng của Client**:  
1. Kết nối Server: Tạo socket đến Server theo IP + port.  
2. Gửi tin nhắn: Người dùng nhập nội dung → Client gửi lên Server.  
3. Nhận tin nhắn: Client lắng nghe phản hồi từ Server và hiển thị trong giao diện.  
4. Giao diện người dùng (GUI): Cửa sổ chat có vùng hiển thị tin nhắn, ô nhập văn bản, nút gửi.  
5. Quản lý trạng thái: Hiển thị thông báo khi mất kết nối, xử lý lỗi gửi/nhận.

**🌐 Chức năng hệ thống**:  
1. Giao thức TCP: Dùng ServerSocket và Socket, hỗ trợ nhiều Client đồng thời nhờ đa luồng.  
2. Trung gian quản lý tin nhắn: Server giữ vai trò trung tâm, tất cả trao đổi giữa Client đều đi qua Server.  
3. Lưu trữ dữ liệu: File I/O (append mode), ghi kèm thời gian (LocalDateTime).  
4. Xử lý lỗi: Hiển thị lỗi trong GUI (Client), ghi log/debug ở Server.

## 🔧 2. Công nghệ sử dụng
Các công nghệ được sử dụng để xây dựng ứng dụng chat Client-Server sử dụng TCP với Java Swing  
**Java Core và Multithreading**  
**Java Swing**  
**Java Sockets**  
**File I/O**  
**MongoDB**  
**Hỗ trợ**: 

    java.util.Date hoặc java.time.LocalDateTime: Tạo timestamp cho mỗi tin nhắn để ghi vào file và hiển thị trên giao diện, giúp người dùng theo dõi thời gian gửi.
    ArrayList: Quản lý danh sách các client đang kết nối trên server (lưu trữ PrintWriter hoặc DataOutputStream của từng client) để broadcast tin nhắn. Có thể mở rộng để lưu danh sách tên người dùng và trạng thái online/offline.
Không sử dụng thư viện bên ngoài, đảm bảo ứng dụng nhẹ và dễ triển khai trên mọi môi trường Java.

## 🚀 3. Hình ảnh các chức năng

<p align="center">
<img src="https://github.com/Tank97king/LapTrinhMang/blob/main/X%C3%82Y%20D%E1%BB%B0NG%20%E1%BB%A8NG%20D%E1%BB%A4NG%20CHAT%20CLIENT-SERVER%20S%E1%BB%AC%20D%E1%BB%A4NG%20GIAO%20TH%E1%BB%A8C%20TCP/%E1%BA%A2nh/Ch%E1%BB%A9c%20n%C4%83ng%20%C4%91%C4%83ng%20nh%E1%BA%ADp.png?raw=true" alt="Chức năng đăng nhập" width="700"/>
</p>

<p align="center">
  <em>Hình 1: Ảnh Chức năng đăng nhập  </em>
</p>

<p align="center">
<img src="https://github.com/Tank97king/LapTrinhMang/blob/main/X%C3%82Y%20D%E1%BB%B0NG%20%E1%BB%A8NG%20D%E1%BB%A4NG%20CHAT%20CLIENT-SERVER%20S%E1%BB%AC%20D%E1%BB%A4NG%20GIAO%20TH%E1%BB%A8C%20TCP/%E1%BA%A2nh/Ch%E1%BB%A9c%20n%C4%83ng%20%C4%91%C4%83ng%20k%C3%BD.png?raw=true" alt="Chức năng đăng ký" width="700"/>
</p>
<p align="center">
  <em> Hình 2: Chức năng đăng ký </em>
</p>


<p align="center">
  <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/X%C3%82Y%20D%E1%BB%B0NG%20%E1%BB%A8NG%20D%E1%BB%A4NG%20CHAT%20CLIENT-SERVER%20S%E1%BB%AC%20D%E1%BB%A4NG%20GIAO%20TH%E1%BB%A8C%20TCP/%E1%BA%A2nh/H%E1%BB%87%20th%E1%BB%91ng%20th%C3%B4ng%20bao%20tham%20gia%20th%C3%A0nh%20c%C3%B4ng.png?raw=true" alt="Hệ thống thông báo tham gia thành công" width="800"/>
</p>
<p align="center">
  <em> Hình 3: Hai Client chat với nhau.</em>
</p>

<p align="center">
  <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/X%C3%82Y%20D%E1%BB%B0NG%20%E1%BB%A8NG%20D%E1%BB%A4NG%20CHAT%20CLIENT-SERVER%20S%E1%BB%AC%20D%E1%BB%A4NG%20GIAO%20TH%E1%BB%A8C%20TCP/%E1%BA%A2nh/Giao%20di%E1%BB%87n%202%20ng%C6%B0%E1%BB%9Di%20chat%20v%E1%BB%9Bi%20nhau.png?raw=true" alt="Giao diện hai người chat với nhau" width="800"/>
</p>
<p align="center">
  <em> Hình 4: Giao diện hai người chat với nhau </em>
</p>

<p align="center">
  <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/X%C3%82Y%20D%E1%BB%B0NG%20%E1%BB%A8NG%20D%E1%BB%A4NG%20CHAT%20CLIENT-SERVER%20S%E1%BB%AC%20D%E1%BB%A4NG%20GIAO%20TH%E1%BB%A8C%20TCP/%E1%BA%A2nh/4%20Collection.png?raw=true" alt="Bộ sưu tập 4" width="800"/>
</p>
<p align="center">
  <em> Hình 5: 4 Collection.</em>
</p>

<p align="center">
  <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/X%C3%82Y%20D%E1%BB%B0NG%20%E1%BB%A8NG%20D%E1%BB%A4NG%20CHAT%20CLIENT-SERVER%20S%E1%BB%AC%20D%E1%BB%A4NG%20GIAO%20TH%E1%BB%A8C%20TCP/%E1%BA%A2nh/Collection%20tr%E1%BA%A1ng%20th%C3%A1i.png?raw=true" alt="Bộ sưu tập trạng thái" width="800"/>
</p>
<p align="center">
  <em> Hình 6: Ảnh trạng thái </em>
</p>

<p align="center">
    <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/X%C3%82Y%20D%E1%BB%B0NG%20%E1%BB%A8NG%20D%E1%BB%A4NG%20CHAT%20CLIENT-SERVER%20S%E1%BB%AC%20D%E1%BB%A4NG%20GIAO%20TH%E1%BB%A8C%20TCP/%E1%BA%A2nh/Collection%20tin%20nh%E1%BA%AFn.png?raw=true" alt="Bộ sưu tập tin nhắn" width="800"/>
</p>
<p align="center">
  <em> Hình 7: Ảnh lưu tập tin nhắn</em>
</p>


## 📝 4. Hướng dẫn cài đặt và sử dụng

### 🔧 Yêu cầu hệ thống

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


## Thông tin liên hệ  
Họ tên: Đinh Thế Thành.  
Lớp: CNTT 16-01.  
Email: dinhthethanh73@gmail.com.

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.

---
