
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
      <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/CHAT%20TCP/%E1%BA%A2nh/aiotlab_logo.png?raw=true" alt="AIoTLab Logo" width="170"/>
      <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/CHAT%20TCP/%E1%BA%A2nh/fitdnu_logo.png?raw=true" alt="FITDNU Logo" width="180"/>
      <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/CHAT%20TCP/%E1%BA%A2nh/dnu_logo.png?raw=true" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>



## 📖 1. Giới thiệu hệ thống

-Hệ thống ChatTCP bao gồm hai phần chính: Server và Client.

- Server: Quản lý kết nối từ nhiều client, xử lý đăng ký/đăng nhập, lưu trữ tài khoản (sử dụng hashing SHA-256), quản lý nhóm chat, bạn bè, và lịch sử trò chuyện. Server sử dụng các file văn bản để lưu dữ liệu (accounts.txt, friends.txt, groups.txt, và thư mục history cho lịch sử chat).

- Client: Giao diện đồ họa sử dụng Swing, cho phép người dùng đăng ký, đăng nhập, xem danh sách người dùng trực tuyến, bạn bè, nhóm chat, gửi tin nhắn cá nhân/nhóm, gửi hình ảnh, và xem lịch sử chat. Client kết nối đến server qua socket TCP (mặc định port 5000).

-Các chức năng chính:  

- ✅ Đăng ký và đăng nhập
- ✅ Chat cá nhân và nhóm.
- ✅ Gửi file hình ảnh.
- ✅ Quản lý bạn bè (gửi yêu cầu, chấp nhận/từ chối).
- ✅ Hiển thị danh sách người dùng online
- ✅ Tạo/tham gia/rời nhóm, thêm thành viên vào nhóm.
- ✅ Lưu và tải lịch sử chat.
- ✅ Giao diện thân thiện với emoji và tùy chỉnh UI.
- ✅ GUI Swing hiện đại và dễ sử dụng


## 🔧 2. Công nghệ sử dụng

- Ngôn ngữ lập trình: Java (JDK 8+).
- Giao diện người dùng (Client): Swing (javax.swing) với tùy chỉnh Nimbus Look and Feel.
- Mạng: Socket TCP (java.net.Socket và ServerSocket).
- Xử lý dữ liệu: ObjectInputStream/ObjectOutputStream cho serialize/deserialize các đối tượng Message.
- Bảo mật: Hashing mật khẩu bằng SHA-256 (java.security.MessageDigest).
- Lưu trữ: File hệ thống (NIO - java.nio.file) cho tài khoản, bạn bè, nhóm, và lịch sử.
- Thư viện bổ sung: ImageIO cho xử lý hình ảnh, DateTimeFormatter cho timestamp.
- Cấu trúc: Các gói (packages) riêng biệt cho client (com.chattcp.client), server (com.chattcp.server), và shared (com.chattcp.shared cho Message, MessageType, UserInfo).

## 🚀 3. Hình ảnh các chức năng

<p align="center">
<img src="https://github.com/Tank97king/LapTrinhMang/blob/main/CHAT%20TCP/%E1%BA%A2nh/%C4%90%C4%83ng%20nh%E1%BA%ADp.png?raw=true" alt="Chức năng đăng nhập" width="700"/>
</p>

<p align="center">
  <em>Hình 1: Ảnh Chức năng đăng nhập  </em>
</p>

<p align="center">
<img src="https://github.com/Tank97king/LapTrinhMang/blob/main/CHAT%20TCP/%E1%BA%A2nh/%C4%90%C4%83ng%20K%C3%BD.png?raw=true" alt="Chức năng đăng ký" width="700"/>
</p>
<p align="center">
  <em> Hình 2: Chức năng đăng ký </em>
</p>


<p align="center">
  <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/CHAT%20TCP/%E1%BA%A2nh/Giao%20di%E1%BB%87n.png?raw=true" alt="Hệ thống thông báo tham gia thành công" width="800"/>
</p>
<p align="center">
  <em> Hình 3: Giao diện .</em>
</p>

<p align="center">
  <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/CHAT%20TCP/%E1%BA%A2nh/Ch%E1%BB%A9c%20N%C4%83ng%20Chat.png?raw=true" alt="Giao diện hai người chat với nhau" width="800"/>
</p>
<p align="center">
  <em> Hình 4: Giao diện hai người chat với nhau </em>
</p>

<p align="center">
  <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/CHAT%20TCP/%E1%BA%A2nh/ch%E1%BB%A9c%20n%C4%83ng%20g%E1%BB%ADi%20%E1%BA%A3nh.png?raw=true" alt="Bộ sưu tập 4" width="800"/>
</p>
<p align="center">
  <em> Hình 5: Chức năng gửi ảnh/file.</em>
</p>

<p align="center">
  <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/CHAT%20TCP/%E1%BA%A2nh/Ch%E1%BB%A9c%20n%C4%83ng%20k%E1%BA%BFt%20b%E1%BA%A1n.png?raw=true" alt="Bộ sưu tập trạng thái" width="800"/>
</p>
<p align="center">
  <em> Hình 6: Chức năng kết bạn </em>
</p>

<p align="center">
    <img src="https://github.com/Tank97king/LapTrinhMang/blob/main/CHAT%20TCP/%E1%BA%A2nh/Ch%E1%BB%A9c%20n%C4%83ng%20t%E1%BA%A1o%20nh%C3%B3m.png?raw=true" alt="Bộ sưu tập tin nhắn" width="800"/>
</p>
<p align="center">
  <em> Hình 7: Chức năng tạo nhóm </em>
</p>


## 📝 4. Hướng dẫn cài đặt và sử dụng



## Yêu cầu hệ thống

- JDK 8 hoặc cao hơn (đã cài đặt và cấu hình PATH).
- Máy chủ (server) và máy khách (client) có thể chạy trên cùng máy hoặc mạng LAN.

## Cài đặt và chạy

Lưu ý: hướng dẫn dưới đây áp dụng cho Windows (PowerShell) và cho trường hợp bạn muốn chạy bằng file class đã compile trong thư mục `bin/` hoặc biên dịch từ `src/`.

1) Import vào Eclipse (tùy chọn)
- File -> Import -> Existing Projects into Workspace -> chọn thư mục gốc dự án (folder chứa `src/`).
- Khi import xong, chạy lớp `com.chattcp.server.ChatServer` trước, sau đó chạy `com.chattcp.client.ChatClient` cho từng client.

2) Chạy bằng dòng lệnh (PowerShell)

- (Tùy chọn) Biên dịch từ `src/` sang `bin/`:

```powershell
# tạo thư mục bin nếu chưa có
if (-not (Test-Path -Path .\bin)) { New-Item -ItemType Directory -Path .\bin }

# biên dịch tất cả .java vào bin
javac -d .\bin .\src\com\chattcp\shared\*.java .\src\com\chattcp\server\*.java .\src\com\chattcp\client\*.java
```

- Chạy server (mặc định lắng nghe port 5000):

```powershell
java -cp .\bin com.chattcp.server.ChatServer
```

- Chạy client (mỗi client một cửa sổ):

```powershell
java -cp .\bin com.chattcp.client.ChatClient
```

3) Tệp và thư mục quan trọng
- `src/` — mã nguồn Java
- `bin/` — chứa các .class đã biên dịch (nếu có)
- `accounts.txt` — tệp danh sách tài khoản (đơn giản, username)
- `history/` — chứa lịch sử chat (file text theo cuộc hội thoại)

4) Cấu hình / tham số
- Port mặc định: 5000 (thay đổi trong `ChatServer` nếu muốn)

5) Khắc phục sự cố nhanh
- Nếu gặp lỗi ClassNotFound khi chạy: kiểm tra rằng `-cp` trỏ tới thư mục `bin` chứa cấu trúc package (ví dụ `bin\com\chattcp\...`).
- Nếu cổng 5000 bị chiếm: sửa port trong `ChatServer.java` và recompile.
- Nếu client không kết nối: đảm bảo firewall cho phép ứng dụng Java hoặc tắt tạm firewall để kiểm tra.

## Gợi ý phát triển tiếp

- Lưu tài khoản kèm mật khẩu (băm) vào file hoặc SQLite
- Chuyển đổi giao thức sang JSON để dễ mở rộng và an toàn hơn
- Thêm TLS (SSL) cho kết nối để mã hóa dữ liệu
- Thêm chat nhóm và danh sách bạn bè có persist

---



## Thông tin liên hệ  
Họ tên: Đinh Thế Thành.  
Lớp: CNTT 16-01.  
Email: dinhthethanh73@gmail.com.

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.

---
