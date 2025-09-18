package btl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Chat Client với Swing GUI
 * Hỗ trợ kết nối server, authentication, chat real-time
 */
public class Client extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    // GUI Components
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JComboBox<String> roomComboBox;
    private JLabel statusLabel;
    private JButton loginButton;
    private JButton registerButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    // Network
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private String currentRoom = "general";
    private boolean isConnected = false;
    private ScheduledExecutorService reconnectExecutor;
    
    // Inner classes for modularity
    private AuthenticationDialog authDialog;
    private MessageHandler messageHandler;
    private ConnectionManager connectionManager;
    private UIManager uiManager;
    
    public Client() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        initializeManagers();
        
        setTitle("BTL Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        showAuthenticationDialog();
    }
    
    private void initializeComponents() {
        // Chat area
        chatArea = new JTextArea(20, 50);
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        chatArea.setBackground(new Color(248, 248, 248));
        chatArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Message input
        messageField = new JTextField(40);
        messageField.setFont(new Font("Arial", Font.PLAIN, 12));
        sendButton = new JButton("Gửi");
        sendButton.setPreferredSize(new Dimension(80, 30));
        
        // User list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setFont(new Font("Arial", Font.PLAIN, 11));
        userList.setBackground(new Color(240, 240, 240));
        
        // Room selection
        roomComboBox = new JComboBox<>(new String[]{"general"});
        roomComboBox.setFont(new Font("Arial", Font.PLAIN, 11));
        
        // Status
        statusLabel = new JLabel("Chưa kết nối");
        statusLabel.setForeground(Color.RED);
        
        // Authentication fields
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Đăng nhập");
        registerButton = new JButton("Đăng ký");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main chat panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Chat area with scroll
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);
        
        // Message input panel
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        mainPanel.add(messagePanel, BorderLayout.SOUTH);
        
        // Right panel with user list and controls
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(200, 0));
        rightPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Room selection
        JPanel roomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        roomPanel.add(new JLabel("Phòng:"));
        roomPanel.add(roomComboBox);
        rightPanel.add(roomPanel, BorderLayout.NORTH);
        
        // User list
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.add(new JLabel("Người dùng online:"), BorderLayout.NORTH);
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        rightPanel.add(userPanel, BorderLayout.CENTER);
        
        // Status
        rightPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }
    
    private void setupEventHandlers() {
        // Send message
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        
        // Room selection
        roomComboBox.addActionListener(e -> {
            String selectedRoom = (String) roomComboBox.getSelectedItem();
            if (selectedRoom != null && !selectedRoom.equals(currentRoom)) {
                switchRoom(selectedRoom);
            }
        });
        
        // Window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
                System.exit(0);
            }
        });
    }
    
    private void initializeManagers() {
        this.authDialog = new AuthenticationDialog();
        this.messageHandler = new MessageHandler();
        this.connectionManager = new ConnectionManager();
        this.uiManager = new UIManager();
    }
    
    private void showAuthenticationDialog() {
        // Hiển thị trang đăng nhập
        showLoginDialog();
    }
    
    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Đăng nhập", true);
        setupLoginDialog(loginDialog);
        loginDialog.setVisible(true);
    }
    
    private void showRegisterDialog() {
        JDialog registerDialog = new JDialog(this, "Đăng ký tài khoản", true);
        setupRegisterDialog(registerDialog);
        registerDialog.setVisible(true);
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && isConnected) {
            out.println(message);
            messageField.setText("");
        }
    }
    
    private void switchRoom(String roomName) {
        if (isConnected) {
            out.println("/join " + roomName);
            currentRoom = roomName;
            chatArea.setText(""); // Clear chat for new room
            loadRoomHistory();
        }
    }
    
    private void loadRoomHistory() {
        // Room history sẽ được load từ server
        out.println("/history " + currentRoom);
    }
    
    private void connect() {
        connectionManager.connect();
    }
    
    private void disconnect() {
        connectionManager.disconnect();
    }
    
    private void setupLoginDialog(JDialog dialog) {
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 100, 200));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(titleLabel, gbc);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(5, 0, 5, 10);
        mainPanel.add(new JLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.insets = new Insets(5, 0, 5, 0);
        JTextField loginUsernameField = new JTextField(20);
        mainPanel.add(loginUsernameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.insets = new Insets(5, 0, 5, 10);
        mainPanel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.insets = new Insets(5, 0, 5, 0);
        JPasswordField loginPasswordField = new JPasswordField(20);
        mainPanel.add(loginPasswordField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginBtn = new JButton("Đăng nhập");
        JButton registerBtn = new JButton("Chưa có tài khoản? Đăng ký");
        registerBtn.setForeground(new Color(0, 100, 200));
        registerBtn.setBorderPainted(false);
        registerBtn.setContentAreaFilled(false);
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        
        // Event handlers
        loginBtn.addActionListener(e -> {
            String username = loginUsernameField.getText().trim();
            String password = new String(loginPasswordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            this.username = username;
            dialog.dispose();
            connect();
        });
        
        registerBtn.addActionListener(e -> {
            dialog.dispose();
            showRegisterDialog();
        });
        
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupRegisterDialog(JDialog dialog) {
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("ĐĂNG KÝ TÀI KHOẢN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 150, 0));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(titleLabel, gbc);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(5, 0, 5, 10);
        mainPanel.add(new JLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.insets = new Insets(5, 0, 5, 0);
        JTextField regUsernameField = new JTextField(20);
        mainPanel.add(regUsernameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.insets = new Insets(5, 0, 5, 10);
        mainPanel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.insets = new Insets(5, 0, 5, 0);
        JPasswordField regPasswordField = new JPasswordField(20);
        mainPanel.add(regPasswordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.insets = new Insets(5, 0, 5, 10);
        mainPanel.add(new JLabel("Xác nhận mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.insets = new Insets(5, 0, 5, 0);
        JPasswordField regConfirmPasswordField = new JPasswordField(20);
        mainPanel.add(regConfirmPasswordField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.insets = new Insets(5, 0, 5, 10);
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.insets = new Insets(5, 0, 5, 0);
        JTextField regEmailField = new JTextField(20);
        mainPanel.add(regEmailField, gbc);
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.insets = new Insets(5, 0, 5, 10);
        mainPanel.add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.insets = new Insets(5, 0, 5, 0);
        JTextField regFullNameField = new JTextField(20);
        mainPanel.add(regFullNameField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton registerBtn = new JButton("Đăng ký");
        JButton backBtn = new JButton("Quay lại đăng nhập");
        backBtn.setForeground(new Color(100, 100, 100));
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        
        buttonPanel.add(registerBtn);
        buttonPanel.add(backBtn);
        
        // Event handlers
        registerBtn.addActionListener(e -> {
            String username = regUsernameField.getText().trim();
            String password = new String(regPasswordField.getPassword());
            String confirmPassword = new String(regConfirmPasswordField.getPassword());
            String email = regEmailField.getText().trim();
            String fullName = regFullNameField.getText().trim();
            
            // Validation
            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu phải có ít nhất 6 ký tự!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JOptionPane.showMessageDialog(dialog, "Đăng ký thành công! Vui lòng đăng nhập.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            showLoginDialog();
        });
        
        backBtn.addActionListener(e -> {
            dialog.dispose();
            showLoginDialog();
        });
        
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Authentication Dialog
     */
    private class AuthenticationDialog extends JDialog {
        public AuthenticationDialog() {
            super(Client.this, "Đăng nhập / Đăng ký", true);
            setupAuthDialog();
        }
        
        private void setupAuthDialog() {
            setLayout(new BorderLayout());
            setSize(350, 200);
            setLocationRelativeTo(Client.this);
            
            JPanel mainPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            // Username
            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
            mainPanel.add(new JLabel("Tên đăng nhập:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            mainPanel.add(usernameField, gbc);
            
            // Password
            gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            mainPanel.add(new JLabel("Mật khẩu:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            mainPanel.add(passwordField, gbc);
            
            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(loginButton);
            buttonPanel.add(registerButton);
            
            loginButton.addActionListener(e -> handleLogin());
            registerButton.addActionListener(e -> handleRegister());
            
            add(mainPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void handleLogin() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Client.this.username = username;
            setVisible(false);
            connectToServer();
        }
        
        private void handleRegister() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Hỏi thêm thông tin cho đăng ký
            String email = JOptionPane.showInputDialog(this, "Nhập email:", "Đăng ký", JOptionPane.QUESTION_MESSAGE);
            if (email == null || email.trim().isEmpty()) {
                email = username + "@example.com"; // Default email nếu không nhập
            }
            
            String fullName = JOptionPane.showInputDialog(this, "Nhập họ tên:", "Đăng ký", JOptionPane.QUESTION_MESSAGE);
            if (fullName == null || fullName.trim().isEmpty()) {
                fullName = username; // Default fullname nếu không nhập
            }
            
            if (attemptRegister(username, password, email.trim(), fullName.trim())) {
                Client.this.username = username;
                setVisible(false);
                connectToServer();
            } else {
                JOptionPane.showMessageDialog(this, "Đăng ký thất bại! Username có thể đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
        
    private boolean attemptLogin(String username, String password) {
        try {
            Socket tempSocket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter tempOut = new PrintWriter(tempSocket.getOutputStream(), true);
            BufferedReader tempIn = new BufferedReader(new InputStreamReader(tempSocket.getInputStream(), "UTF-8"));
            
            tempOut.println("LOGIN");
            tempOut.println(username);
            tempOut.println(password);
            
            String response = tempIn.readLine();
            tempSocket.close();
            
            return "AUTH_SUCCESS".equals(response);
        } catch (IOException e) {
            return false;
        }
    }
    
    private boolean attemptRegister(String username, String password, String email, String fullName) {
        try {
            Socket tempSocket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter tempOut = new PrintWriter(tempSocket.getOutputStream(), true);
            BufferedReader tempIn = new BufferedReader(new InputStreamReader(tempSocket.getInputStream(), "UTF-8"));
            
            tempOut.println("REGISTER");
            tempOut.println(username);
            tempOut.println(password);
            tempOut.println(email);
            tempOut.println(fullName);
            
            String response = tempIn.readLine();
            tempSocket.close();
            
            if ("AUTH_SUCCESS".equals(response)) {
                return true;
            } else if (response != null && response.startsWith("AUTH_FAILED:")) {
                // Hiển thị lỗi cụ thể từ server
                String errorMsg = response.substring("AUTH_FAILED:".length());
                JOptionPane.showMessageDialog(this, errorMsg, "Lỗi đăng ký", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server!", "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void connectToServer() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            
            // Authenticate với server - chỉ gửi username để maintain connection
            out.println("LOGIN");
            out.println(username);
            out.println("temp_password"); // Temporary password for connection
            
            String response = in.readLine();
            if ("AUTH_SUCCESS".equals(response)) {
                isConnected = true;
                uiManager.updateConnectionStatus("Đã kết nối", Color.GREEN);
                
                // Start message listener thread
                Thread messageListener = new Thread(() -> {
                    try {
                        String message;
                        while ((message = in.readLine()) != null && isConnected) {
                            final String finalMessage = message;
                            SwingUtilities.invokeLater(() -> messageHandler.processMessage(finalMessage));
                        }
                    } catch (IOException e) {
                        if (isConnected) {
                            uiManager.updateConnectionStatus("Mất kết nối", Color.RED);
                            isConnected = false;
                        }
                    }
                });
                messageListener.start();
                
            } else {
                uiManager.updateConnectionStatus("Kết nối thất bại", Color.RED);
            }
        } catch (IOException e) {
            uiManager.updateConnectionStatus("Lỗi kết nối", Color.RED);
        }
    }
    }
    
    /**
     * Connection Manager - Quản lý kết nối và auto-reconnect
     */
    private class ConnectionManager {
        public void connect() {
            try {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                
                // Send authentication
                out.println("LOGIN");
                out.println(username);
                out.println("dummy_password");
                
                String response = in.readLine();
                if ("AUTH_SUCCESS".equals(response)) {
                    isConnected = true;
                    uiManager.updateConnectionStatus("Đã kết nối", Color.GREEN);
                    
                    // Start message listener
                    new Thread(() -> messageHandler.listenForMessages()).start();
                    
                    // Start auto-reconnect
                    startAutoReconnect();
                } else {
                    uiManager.updateConnectionStatus("Đăng nhập thất bại", Color.RED);
                }
                
            } catch (IOException e) {
                uiManager.updateConnectionStatus("Lỗi kết nối: " + e.getMessage(), Color.RED);
                scheduleReconnect();
            }
        }
        
        public void disconnect() {
            isConnected = false;
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Lỗi đóng kết nối: " + e.getMessage());
            }
        }
        
        private void startAutoReconnect() {
            reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
            reconnectExecutor.scheduleWithFixedDelay(() -> {
                if (!isConnected) {
                    SwingUtilities.invokeLater(() -> {
                        uiManager.updateConnectionStatus("Đang thử kết nối lại...", Color.ORANGE);
                        connect();
                    });
                }
            }, 5, 5, TimeUnit.SECONDS);
        }
        
        private void scheduleReconnect() {
            if (reconnectExecutor != null) {
                reconnectExecutor.shutdown();
            }
            startAutoReconnect();
        }
    }
    
    /**
     * Message Handler - Xử lý tin nhắn từ server
     */
    private class MessageHandler {
        public void listenForMessages() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    final String finalMessage = message;
                    SwingUtilities.invokeLater(() -> processMessage(finalMessage));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    isConnected = false;
                    uiManager.updateConnectionStatus("Mất kết nối", Color.RED);
                });
            }
        }
        
        private void processMessage(String message) {
            if (message.startsWith("HISTORY:")) {
                // Lịch sử chat
                String[] parts = message.substring(8).split(":", 3);
                if (parts.length >= 3) {
                    String username = parts[0];
                    String msg = parts[1];
                    String timestamp = parts[2];
                    uiManager.addChatMessage(username, msg, timestamp);
                }
            } else if (message.startsWith("USERLIST:")) {
                // Danh sách user
                String users = message.substring(9);
                uiManager.updateUserList(users.split(","));
            } else if (message.contains(":")) {
                // Tin nhắn thường
                String[] parts = message.split(":", 2);
                if (parts.length >= 2) {
                    String sender = parts[0];
                    String msg = parts[1];
                    uiManager.addChatMessage(sender, msg, null);
                }
            } else {
                // Tin nhắn hệ thống
                uiManager.addSystemMessage(message);
            }
        }
    }
    
    /**
     * UI Manager - Quản lý giao diện
     */
    private class UIManager {
        public void updateConnectionStatus(String status, Color color) {
            statusLabel.setText(status);
            statusLabel.setForeground(color);
        }
        
        public void addChatMessage(String sender, String message, String timestamp) {
            SwingUtilities.invokeLater(() -> {
                String timeStr = timestamp != null ? " [" + timestamp + "]" : "";
                String formattedMessage = String.format("[%s]%s %s: %s\n", 
                    new Date().toString().substring(11, 19), timeStr, sender, message);
                
                chatArea.append(formattedMessage);
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            });
        }
        
        public void addSystemMessage(String message) {
            SwingUtilities.invokeLater(() -> {
                String formattedMessage = String.format("[%s] SYSTEM: %s\n", 
                    new Date().toString().substring(11, 19), message);
                
                chatArea.append(formattedMessage);
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            });
        }
        
        public void updateUserList(String[] users) {
            SwingUtilities.invokeLater(() -> {
                userListModel.clear();
                for (String user : users) {
                    if (!user.trim().isEmpty()) {
                        userListModel.addElement(user.trim());
                    }
                }
            });
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Client().setVisible(true);
        });
    }
}
