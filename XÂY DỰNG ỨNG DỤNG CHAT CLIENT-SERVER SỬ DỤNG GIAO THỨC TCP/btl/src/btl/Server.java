package btl;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Chat Server với MongoDB integration
 * Hỗ trợ đa client, phòng chat, quản lý user và trạng thái
 */
public class Server {
    private static final int PORT = 8888;
    private static final String MONGODB_URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "btl_chat";
    
    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;
    private Map<String, ClientHandler> connectedClients;
    private Map<String, Set<String>> chatRooms;
    private MongoClient mongoClient;
    private MongoDatabase database;
    
    // Inner classes for modularity
    private MongoDBHandler mongoHandler;
    private UserManager userManager;
    private RoomManager roomManager;
    private MessageHandler messageHandler;
    private StatusManager statusManager;
    
    public Server() {
        this.clientThreadPool = Executors.newCachedThreadPool();
        this.connectedClients = new ConcurrentHashMap<>();
        this.chatRooms = new ConcurrentHashMap<>();
        this.chatRooms.put("general", ConcurrentHashMap.newKeySet());
        
        initializeMongoDB();
        initializeManagers();
        
        // Load rooms từ database
        roomManager.loadRoomsFromDatabase();
    }
    
    private void initializeMongoDB() {
        try {
            mongoClient = MongoClients.create(MONGODB_URI);
            database = mongoClient.getDatabase(DATABASE_NAME);
            mongoHandler = new MongoDBHandler();
            System.out.println("✓ Kết nối MongoDB thành công");
        } catch (Exception e) {
            System.err.println("✗ Lỗi kết nối MongoDB: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private void initializeManagers() {
        this.userManager = new UserManager();
        this.roomManager = new RoomManager();
        this.messageHandler = new MessageHandler();
        this.statusManager = new StatusManager();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("🚀 Server đang chạy trên port " + PORT);
            System.out.println("📱 Chờ client kết nối...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientThreadPool.submit(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("✗ Lỗi server: " + e.getMessage());
        }
    }
    
    /**
     * Inner class xử lý từng client
     */
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private String currentRoom = "general";
        private long lastActivity;
        
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.lastActivity = System.currentTimeMillis();
        }
        
        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                
                // Xử lý authentication
                if (!handleAuthentication()) {
                    return;
                }
                
                // Thêm client vào danh sách
                connectedClients.put(username, this);
                roomManager.joinRoom(username, currentRoom);
                statusManager.setUserStatus(username, "online");
                
                // Gửi lịch sử chat
                sendChatHistory();
                broadcastUserList();
                broadcastMessage("SYSTEM", username + " đã tham gia chat", currentRoom);
                
                // Xử lý tin nhắn từ client
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    lastActivity = System.currentTimeMillis();
                    handleClientMessage(inputLine);
                }
                
            } catch (IOException e) {
                System.err.println("✗ Lỗi xử lý client " + username + ": " + e.getMessage());
            } finally {
                cleanup();
            }
        }
        
        private boolean handleAuthentication() throws IOException {
            String authType = in.readLine();
            
            if ("LOGIN".equals(authType)) {
                String username = in.readLine();
                String password = in.readLine();
                
                // Tạm thời chấp nhận mọi đăng nhập để test
                this.username = username;
                out.println("AUTH_SUCCESS");
                return true;
            } else if ("REGISTER".equals(authType)) {
                String username = in.readLine();
                String password = in.readLine();
                String email = in.readLine();
                String fullName = in.readLine();
                
                // Đăng ký user mới
                if (!userManager.registerUser(username, password, email, fullName)) {
                    out.println("AUTH_FAILED:Username đã tồn tại!");
                    return false;
                }
                
                this.username = username;
                out.println("AUTH_SUCCESS");
                System.out.println("✓ Đăng ký thành công user: " + username);
                return true;
            }
            
            return false;
        }
        
        private void handleClientMessage(String message) {
            if (message.startsWith("/")) {
                handleCommand(message);
            } else {
                // Tin nhắn thường
                messageHandler.broadcastMessage(username, message, currentRoom);
                mongoHandler.saveMessage(username, message, currentRoom);
            }
        }
        
        private void handleCommand(String command) {
            String[] parts = command.split(" ", 3);
            String cmd = parts[0].substring(1);
            
            switch (cmd) {
                case "join":
                    if (parts.length > 1) {
                        String roomName = parts[1];
                        roomManager.joinRoom(username, roomName);
                        currentRoom = roomName;
                        out.println("SYSTEM: Đã chuyển vào phòng " + roomName);
                    }
                    break;
                case "create":
                    if (parts.length > 1) {
                        String roomName = parts[1];
                        roomManager.createRoom(roomName, username);
                        out.println("SYSTEM: Đã tạo phòng " + roomName);
                    }
                    break;
                case "rooms":
                    out.println("SYSTEM: Danh sách phòng: " + String.join(", ", roomManager.getRoomList()));
                    break;
                case "users":
                    out.println("SYSTEM: Người dùng online: " + String.join(", ", connectedClients.keySet()));
                    break;
                case "status":
                    if (parts.length > 1) {
                        String status = parts[1];
                        statusManager.setUserStatus(username, status);
                        out.println("SYSTEM: Đã đổi trạng thái thành " + status);
                    }
                    break;
            }
        }
        
        private void sendChatHistory() {
            try {
                List<Document> messages = mongoHandler.getChatHistory(currentRoom, 50);
                for (Document msg : messages) {
                    out.println("HISTORY:" + msg.getString("username") + ":" + 
                              msg.getString("message") + ":" + 
                              msg.getDate("timestamp"));
                }
            } catch (Exception e) {
                System.err.println("Lỗi load lịch sử chat: " + e.getMessage());
            }
        }
        
        private void cleanup() {
            if (username != null) {
                connectedClients.remove(username);
                roomManager.leaveRoom(username, currentRoom);
                statusManager.setUserStatus(username, "offline");
                broadcastUserList();
                broadcastMessage("SYSTEM", username + " đã rời khỏi chat", currentRoom);
            }
            
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Lỗi cleanup client: " + e.getMessage());
            }
        }
        
        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }
    
    /**
     * MongoDB Handler - Quản lý dữ liệu MongoDB
     */
    private class MongoDBHandler {
        private MongoCollection<Document> users;
        private MongoCollection<Document> messages;
        private MongoCollection<Document> rooms;
        private MongoCollection<Document> userStatus;
        
        public MongoDBHandler() {
            this.users = database.getCollection("users");
            this.messages = database.getCollection("messages");
            this.rooms = database.getCollection("rooms");
            this.userStatus = database.getCollection("user_status");
            
            // Tạo indexes
            users.createIndex(Indexes.ascending("username"), new IndexOptions().unique(true));
            messages.createIndex(Indexes.ascending("room"));
            messages.createIndex(Indexes.descending("timestamp"));
        }
        
        public void saveMessage(String username, String message, String room) {
            Document msgDoc = new Document()
                .append("username", username)
                .append("message", message)
                .append("room", room)
                .append("timestamp", new Date());
            
            messages.insertOne(msgDoc);
        }
        
        public List<Document> getChatHistory(String room, int limit) {
            return messages.find(Filters.eq("room", room))
                          .sort(Sorts.descending("timestamp"))
                          .limit(limit)
                          .into(new ArrayList<>());
        }
        
        public void saveUser(String username, String hashedPassword, String email, String fullName) {
            Document userDoc = new Document()
                .append("username", username)
                .append("password", hashedPassword)
                .append("email", email)
                .append("fullName", fullName)
                .append("createdAt", new Date())
                .append("lastLogin", null)
                .append("isActive", true);
            
            users.replaceOne(Filters.eq("username", username), userDoc, 
                           new ReplaceOptions().upsert(true));
        }
        
        public Document getUser(String username) {
            return users.find(Filters.eq("username", username)).first();
        }
        
        public void saveUserStatus(String username, String status) {
            Document statusDoc = new Document()
                .append("username", username)
                .append("status", status)
                .append("lastSeen", new Date());
            
            userStatus.replaceOne(Filters.eq("username", username), statusDoc,
                                new ReplaceOptions().upsert(true));
        }
        
        public Document getUserStatus(String username) {
            return userStatus.find(Filters.eq("username", username)).first();
        }
        
        public void saveRoom(String roomName, String createdBy) {
            Document roomDoc = new Document()
                .append("roomName", roomName)
                .append("createdBy", createdBy)
                .append("createdAt", new Date())
                .append("memberCount", 0);
            
            rooms.replaceOne(Filters.eq("roomName", roomName), roomDoc, 
                           new ReplaceOptions().upsert(true));
        }
        
        public Document getRoom(String roomName) {
            return rooms.find(Filters.eq("roomName", roomName)).first();
        }
        
        public List<Document> getAllRooms() {
            return rooms.find().into(new ArrayList<>());
        }
    }
    
    /**
     * User Manager - Quản lý người dùng và authentication
     */
    private class UserManager {
        public boolean registerUser(String username, String password, String email, String fullName) {
            if (mongoHandler.getUser(username) != null) {
                return false; // User đã tồn tại
            }
            
            String hashedPassword = hashPassword(password);
            mongoHandler.saveUser(username, hashedPassword, email, fullName);
            System.out.println("✓ Đăng ký user mới: " + username + " (" + fullName + ")");
            return true;
        }
        
        public boolean authenticateUser(String username, String password) {
            Document user = mongoHandler.getUser(username);
            if (user == null) {
                return false;
            }
            
            String hashedPassword = hashPassword(password);
            return hashedPassword.equals(user.getString("password"));
        }
        
        private String hashPassword(String password) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(password.getBytes("UTF-8"));
                StringBuilder hexString = new StringBuilder();
                
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                
                return hexString.toString();
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                throw new RuntimeException("Lỗi hash password", e);
            }
        }
    }
    
    /**
     * Room Manager - Quản lý phòng chat
     */
    private class RoomManager {
        public void createRoom(String roomName, String createdBy) {
            chatRooms.putIfAbsent(roomName, ConcurrentHashMap.newKeySet());
            // Lưu phòng vào database
            mongoHandler.saveRoom(roomName, createdBy);
            System.out.println("✓ Tạo phòng mới: " + roomName + " bởi " + createdBy);
        }
        
        public void joinRoom(String username, String roomName) {
            createRoom(roomName, username);
            chatRooms.get(roomName).add(username);
        }
        
        public void leaveRoom(String username, String roomName) {
            Set<String> room = chatRooms.get(roomName);
            if (room != null) {
                room.remove(username);
            }
        }
        
        public Set<String> getRoomMembers(String roomName) {
            return chatRooms.getOrDefault(roomName, Collections.emptySet());
        }
        
        public Set<String> getRoomList() {
            return chatRooms.keySet();
        }
        
        public void loadRoomsFromDatabase() {
            List<Document> rooms = mongoHandler.getAllRooms();
            for (Document room : rooms) {
                String roomName = room.getString("roomName");
                chatRooms.putIfAbsent(roomName, ConcurrentHashMap.newKeySet());
            }
            System.out.println("✓ Loaded " + rooms.size() + " rooms from database");
        }
    }
    
    /**
     * Message Handler - Xử lý tin nhắn
     */
    private class MessageHandler {
        public void broadcastMessage(String sender, String message, String room) {
            Set<String> roomMembers = roomManager.getRoomMembers(room);
            String formattedMessage = sender + ":" + message;
            
            for (String username : roomMembers) {
                ClientHandler client = connectedClients.get(username);
                if (client != null) {
                    client.sendMessage(formattedMessage);
                }
            }
        }
    }
    
    /**
     * Status Manager - Quản lý trạng thái người dùng
     */
    private class StatusManager {
        public void setUserStatus(String username, String status) {
            mongoHandler.saveUserStatus(username, status);
        }
        
        public String getUserStatus(String username) {
            Document status = mongoHandler.getUserStatus(username);
            return status != null ? status.getString("status") : "offline";
        }
    }
    
    private void broadcastUserList() {
        String userList = "USERLIST:" + String.join(",", connectedClients.keySet());
        for (ClientHandler client : connectedClients.values()) {
            client.sendMessage(userList);
        }
    }
    
    private void broadcastMessage(String sender, String message, String room) {
        messageHandler.broadcastMessage(sender, message, room);
    }
    
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
