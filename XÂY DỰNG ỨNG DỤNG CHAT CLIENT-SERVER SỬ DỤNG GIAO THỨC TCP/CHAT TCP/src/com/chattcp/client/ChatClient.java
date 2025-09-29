package com.chattcp.client;

import com.chattcp.shared.Message;
import com.chattcp.shared.MessageType;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatClient extends JFrame {
    private String serverHost = "localhost";
    private int serverPort = 5000;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // UI Components
    private CardLayout cards = new CardLayout();
    private JPanel root = new JPanel(cards);
    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private JPasswordField regPasswordField = new JPasswordField(20);
    private JPasswordField regPasswordConfirmField = new JPasswordField(20);
    private JTextField regUsernameField = new JTextField(20);
    private DefaultListModel<String> onlineModel = new DefaultListModel<>();
    private JList<String> onlineList = new JList<>(onlineModel);
    private DefaultListModel<String> groupModel = new DefaultListModel<>();
    private JList<String> groupList = new JList<>(groupModel);
    private DefaultListModel<String> friendsModel = new DefaultListModel<>();
    private JList<String> friendsList = new JList<>(friendsModel);
    private JTextArea chatArea = new JTextArea();
    private JTextField messageField = new JTextField(30);
    private String username;
    private String chatWith;
    private List<Message> sessionHistory = new ArrayList<>();

    public ChatClient() {
        // Cấu hình cơ bản cửa sổ
        setTitle("Chat TCP Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600); // Tăng kích thước cửa sổ
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 400));

        // Tùy chỉnh giao diện
        customizeUI();

        // Thêm các panel
        root.add(buildLoginPanel(), "login");
        root.add(buildRegisterPanel(), "register");
        root.add(buildMainPanel(), "main");
        add(root);
        cards.show(root, "login");
    }

    private void customizeUI() {
        try {
            // Sử dụng Nimbus Look and Feel với tùy chỉnh
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("nimbusBase", new Color(0x1E90FF)); // Màu xanh dương đậm
            UIManager.put("nimbusBlueGrey", new Color(0x4682B4)); // Màu nền xám xanh
            UIManager.put("control", new Color(0xF5F7FA)); // Màu nền sáng
            UIManager.put("textForeground", new Color(0x333333)); // Màu chữ tối
            UIManager.put("nimbusSelectionBackground", new Color(0x87CEEB)); // Màu chọn
            UIManager.put("nimbusFocus", new Color(0xFFD700)); // Màu viền focus
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.border", new LineBorder(new Color(0x4682B4), 1));
            UIManager.put("Button.background", new Color(0x1E90FF));
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tùy chỉnh giao diện cho các thành phần
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(new Color(0xFFFFFF));
        chatArea.setForeground(new Color(0x333333));
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        regUsernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        regPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        regPasswordConfirmField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xF5F7FA));
        JPanel center = new JPanel(new GridBagLayout());
        center.setBorder(new EmptyBorder(30, 30, 30, 30));
        center.setBackground(new Color(0xF5F7FA));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Tiêu đề
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel title = new JLabel("Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0x1E90FF));
        center.add(title, c);

        // Trường nhập
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        center.add(createStyledLabel("Username:"), c);
        c.gridx = 1;
        center.add(usernameField, c);
        c.gridx = 0;
        c.gridy = 2;
        center.add(createStyledLabel("Password:"), c);
        c.gridx = 1;
        center.add(passwordField, c);

        // Nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(0xF5F7FA));
        JButton loginBtn = createStyledButton("Login");
        loginBtn.addActionListener(e -> doLogin());
        JButton goReg = createStyledButton("Register");
        goReg.addActionListener(e -> cards.show(root, "register"));
        buttonPanel.add(loginBtn);
        buttonPanel.add(goReg);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        center.add(buttonPanel, c);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xF5F7FA));
        JPanel center = new JPanel(new GridBagLayout());
        center.setBorder(new EmptyBorder(30, 30, 30, 30));
        center.setBackground(new Color(0xF5F7FA));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Tiêu đề
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel title = new JLabel("Register New Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0x1E90FF));
        center.add(title, c);

        // Trường nhập
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        center.add(createStyledLabel("Username:"), c);
        c.gridx = 1;
        center.add(regUsernameField, c);
        c.gridx = 0;
        c.gridy = 2;
        center.add(createStyledLabel("Password:"), c);
        c.gridx = 1;
        center.add(regPasswordField, c);
        c.gridx = 0;
        c.gridy = 3;
        center.add(createStyledLabel("Confirm Password:"), c);
        c.gridx = 1;
        center.add(regPasswordConfirmField, c);

        // Nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(0xF5F7FA));
        JButton regBtn = createStyledButton("Register");
        regBtn.addActionListener(e -> doRegister());
        JButton back = createStyledButton("Back to Login");
        back.addActionListener(e -> cards.show(root, "login"));
        buttonPanel.add(regBtn);
        buttonPanel.add(back);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        center.add(buttonPanel, c);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xF5F7FA));

        // Left: Tabbed pane with online users, groups, and friends
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setBackground(new Color(0xFFFFFF));
        tabs.setForeground(new Color(0x333333));

        // Online Users Panel
        JPanel onlinePanel = new JPanel(new BorderLayout());
        onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        onlineList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        onlineList.setBackground(new Color(0xFFFFFF));
        onlineList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = onlineList.getSelectedValue();
                    if (sel != null && !sel.equals(username)) {
                        openChatWith(sel);
                    }
                }
            }
        });
        onlinePanel.add(new JScrollPane(onlineList), BorderLayout.CENTER);
        JPanel onlineBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        onlineBtns.setBackground(new Color(0xF5F7FA));
        JButton addFriendBtn = createStyledButton("Add Friend");
        addFriendBtn.addActionListener(e -> {
            String sel = onlineList.getSelectedValue();
            if (sel != null) {
                try {
                    Message m = new Message(MessageType.FRIEND_REQUEST);
                    m.setFrom(username);
                    m.setTo(sel);
                    m.setText(username + " wants to be your friend");
                    out.writeObject(m);
                    out.flush();
                    JOptionPane.showMessageDialog(ChatClient.this, "Friend request sent to " + sel);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        onlineBtns.add(addFriendBtn);
        onlinePanel.add(onlineBtns, BorderLayout.SOUTH);
        tabs.addTab("Online", onlinePanel);

        // Groups Panel
        JPanel groupsPanel = new JPanel(new BorderLayout());
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        groupList.setBackground(new Color(0xFFFFFF));
        groupList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = groupList.getSelectedValue();
                    if (sel != null) {
                        openGroupChat(sel);
                    }
                }
            }
        });
        groupsPanel.add(new JScrollPane(groupList), BorderLayout.CENTER);
        JPanel grpButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        grpButtons.setBackground(new Color(0xF5F7FA));
        JButton createG = createStyledButton("Create");
        createG.addActionListener(e -> {
            String g = JOptionPane.showInputDialog(ChatClient.this, "Group name:");
            if (g != null && !g.trim().isEmpty()) {
                try {
                    Message m = new Message(MessageType.GROUP_CREATE);
                    m.setFrom(username);
                    m.setGroupName(g.trim());
                    out.writeObject(m);
                    out.flush();
                    groupModel.addElement(g.trim());
                    openGroupChat(g.trim());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        JButton deleteG = createStyledButton("Delete");
        deleteG.addActionListener(e -> {
            String sel = groupList.getSelectedValue();
            if (sel != null) {
                try {
                    Message m = new Message(MessageType.GROUP_DELETE);
                    m.setFrom(username);
                    m.setGroupName(sel);
                    out.writeObject(m);
                    out.flush();
                    groupModel.removeElement(sel);
                    if (chatWith != null && chatWith.equals("GROUP:" + sel)) {
                        chatWith = null;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        JButton joinG = createStyledButton("Join");
        joinG.addActionListener(e -> {
            String sel = groupList.getSelectedValue();
            if (sel != null) {
                try {
                    Message m = new Message(MessageType.GROUP_JOIN);
                    m.setFrom(username);
                    m.setGroupName(sel);
                    out.writeObject(m);
                    out.flush();
                    openGroupChat(sel);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        JButton leaveG = createStyledButton("Leave");
        leaveG.addActionListener(e -> {
            String sel = groupList.getSelectedValue();
            if (sel != null) {
                try {
                    Message m = new Message(MessageType.GROUP_LEAVE);
                    m.setFrom(username);
                    m.setGroupName(sel);
                    out.writeObject(m);
                    out.flush();
                    groupModel.removeElement(sel);
                    if (chatWith != null && chatWith.equals("GROUP:" + sel)) {
                        chatWith = null;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        JButton inviteBtn = createStyledButton("Invite Friend");
        inviteBtn.addActionListener(e -> {
            String selGroup = groupList.getSelectedValue();
            String selFriend = friendsList.getSelectedValue();
            if (selGroup != null && selFriend != null) {
                try {
                    Message m = new Message(MessageType.GROUP_ADD_MEMBER);
                    m.setFrom(username);
                    m.setTo(selFriend);
                    m.setGroupName(selGroup);
                    out.writeObject(m);
                    out.flush();
                    JOptionPane.showMessageDialog(ChatClient.this, "Invitation sent to " + selFriend);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(ChatClient.this, "Select a group and a friend to invite");
            }
        });
        grpButtons.add(createG);
        grpButtons.add(joinG);
        grpButtons.add(leaveG);
        grpButtons.add(inviteBtn);
        groupsPanel.add(grpButtons, BorderLayout.SOUTH);
        tabs.addTab("Groups", groupsPanel);

        // Friends Panel
        JPanel friendsPanel = new JPanel(new BorderLayout());
        friendsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        friendsList.setBackground(new Color(0xFFFFFF));
        friendsPanel.add(new JScrollPane(friendsList), BorderLayout.CENTER);
        tabs.addTab("Friends", friendsPanel);

        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(250, 0));
        left.setBackground(new Color(0xF5F7FA));
        left.add(tabs, BorderLayout.CENTER);
        panel.add(left, BorderLayout.WEST);

        // Center: Chat Area
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(new Color(0xFFFFFF));
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        center.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        bottom.setBackground(new Color(0xF5F7FA));
        messageField.setPreferredSize(new Dimension(400, 30));
        bottom.add(messageField);
        JButton send = createStyledButton("Send");
        send.addActionListener(e -> sendTextMessage());
        bottom.add(send);
        JButton emojiBtn = createStyledButton("Emoji");
        emojiBtn.addActionListener(e -> showEmojiMenu());
        bottom.add(emojiBtn);
        JButton imgBtn = createStyledButton("Image");
        imgBtn.addActionListener(e -> sendImage());
        bottom.add(imgBtn);
        center.add(bottom, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);

        // Top: User Info + Logout
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0x1E90FF));
        JLabel userLabel = new JLabel("User: ", SwingConstants.LEFT);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setBorder(new EmptyBorder(5, 10, 5, 0));
        top.add(userLabel, BorderLayout.WEST);
        JButton logout = createStyledButton("Logout");
        logout.addActionListener(e -> doLogout());
        top.add(logout, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(0x333333));
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(new Color(0x1E90FF));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(new Color(0x4682B4), 1));
        button.setPreferredSize(new Dimension(100, 30));
        // Hiệu ứng hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0x4682B4));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0x1E90FF));
            }
        });
        return button;
    }

    private void doLogin() {
        try {
            username = usernameField.getText().trim();
            String pw = new String(passwordField.getPassword());
            if (username.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter username and password");
                return;
            }
            socket = new Socket(serverHost, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Message m = new Message(MessageType.LOGIN);
            m.setFrom(username);
            m.setPassword(pw);
            out.writeObject(m);
            out.flush();

            Message resp = (Message) in.readObject();
            if (resp.getType() == MessageType.LOGIN_OK) {
                cards.show(root, "main");
                startReaderThread();
                setTitle("Chat - " + username);
            } else {
                JOptionPane.showMessageDialog(this, "Login failed: " + resp.getText());
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to connect to server: " + e.getMessage());
        }
    }

    private void doRegister() {
        try {
            String user = regUsernameField.getText().trim();
            String pw = new String(regPasswordField.getPassword());
            String pw2 = new String(regPasswordConfirmField.getPassword());
            if (!pw.equals(pw2)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match");
                return;
            }
            if (user.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter username and password");
                return;
            }
            Socket s = new Socket(serverHost, serverPort);
            ObjectOutputStream o = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream i = new ObjectInputStream(s.getInputStream());
            Message m = new Message(MessageType.REGISTER);
            m.setFrom(user);
            m.setPassword(pw);
            o.writeObject(m);
            o.flush();
            Message resp = (Message) i.readObject();
            if (resp.getType() == MessageType.REGISTER_OK) {
                JOptionPane.showMessageDialog(this, "Registration successful. Please login.");
                cards.show(root, "login");
            } else {
                JOptionPane.showMessageDialog(this, "Register failed: " + resp.getText());
            }
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void startReaderThread() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    Message m = (Message) in.readObject();
                    if (m == null) {
                        break;
                    }
                    handleIncoming(m);
                }
            } catch (Exception e) {
                // Connection closed
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void handleIncoming(Message m) {
        switch (m.getType()) {
            case USER_LIST:
                SwingUtilities.invokeLater(() -> updateOnlineList(m.getText()));
                break;
            case FRIEND_LIST:
                SwingUtilities.invokeLater(() -> {
                    friendsModel.clear();
                    if (m.getText() != null && !m.getText().isEmpty()) {
                        for (String s : m.getText().split(",")) {
                            if (!s.trim().isEmpty()) {
                                friendsModel.addElement(s.trim());
                            }
                        }
                    }
                });
                break;
            case GROUP_LIST:
                SwingUtilities.invokeLater(() -> {
                    groupModel.clear();
                    if (m.getText() != null && !m.getText().isEmpty()) {
                        for (String s : m.getText().split("\\n")) {
                            if (!s.trim().isEmpty()) {
                                groupModel.addElement(s.trim());
                            }
                        }
                    }
                });
                break;
            case PRIVATE_MESSAGE:
            case FILE_TRANSFER:
                String from = m.getFrom();
                String ts = m.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                if (m.getType() == MessageType.PRIVATE_MESSAGE) {
                    String text = String.format("[%s] %s: %s\n", ts, from, m.getText());
                    sessionHistory.add(m);
                    SwingUtilities.invokeLater(() -> chatArea.append(text));
                } else if (m.getType() == MessageType.FILE_TRANSFER) {
                    try {
                        byte[] data = m.getFileBytes();
                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                        if (img != null) {
                            SwingUtilities.invokeLater(() -> showImageInChat(img, from, ts));
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            case FRIEND_REQUEST:
                SwingUtilities.invokeLater(() -> {
                    int r = JOptionPane.showConfirmDialog(this, m.getFrom() + " wants to be your friend. Accept?",
                            "Friend Request", JOptionPane.YES_NO_OPTION);
                    try {
                        Message resp = new Message(MessageType.FRIEND_RESPONSE);
                        resp.setFrom(username);
                        resp.setTo(m.getFrom());
                        resp.setText(r == JOptionPane.YES_OPTION ? "ACCEPT" : "REJECT");
                        out.writeObject(resp);
                        out.flush();
                        if (r == JOptionPane.YES_OPTION) {
                            friendsModel.addElement(m.getFrom());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                break;
            case FRIEND_RESPONSE:
                SwingUtilities.invokeLater(() -> {
                    if ("ACCEPT".equalsIgnoreCase(m.getText())) {
                        friendsModel.addElement(m.getFrom());
                        JOptionPane.showMessageDialog(this, m.getFrom() + " accepted your friend request");
                    } else {
                        JOptionPane.showMessageDialog(this, m.getFrom() + " rejected your friend request");
                    }
                });
                break;
            case GROUP_ADD_MEMBER:
                SwingUtilities.invokeLater(() -> {
                    String group = m.getGroupName();
                    if (group != null && !groupModel.contains(group)) {
                        groupModel.addElement(group);
                    }
                    JOptionPane.showMessageDialog(this, "You were invited to group: " + group + " by " + m.getFrom());
                });
                break;
            case HISTORY_RESPONSE:
                SwingUtilities.invokeLater(() -> chatArea.setText(m.getText()));
                break;
            default:
                break;
        }
    }

    private void updateOnlineList(String text) {
        onlineModel.clear();
        if (text == null || text.isEmpty()) {
            return;
        }
        String[] parts = text.split("\\n");
        for (String s : parts) {
            onlineModel.addElement(s);
        }
    }

    private void openChatWith(String sel) {
        chatWith = sel;
        chatArea.setText("");
        setTitle("Chat - " + username + " (chatting with " + chatWith + ")");
        try {
            String a = username;
            String b = sel;
            String conv = a.compareTo(b) <= 0 ? (a + "_" + b) : (b + "_" + a);
            Message req = new Message(MessageType.HISTORY_REQUEST);
            req.setFrom(username);
            req.setText("private_" + conv);
            out.writeObject(req);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openGroupChat(String group) {
        chatWith = "GROUP:" + group;
        chatArea.setText("");
        setTitle("Chat - " + username + " (group: " + group + ")");
        try {
            Message req = new Message(MessageType.HISTORY_REQUEST);
            req.setFrom(username);
            req.setText("group_" + group);
            out.writeObject(req);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendTextMessage() {
        if (chatWith == null) {
            JOptionPane.showMessageDialog(this, "Double-click a user or group to start chat");
            return;
        }
        String txt = messageField.getText().trim();
        if (txt.isEmpty()) {
            return;
        }
        try {
            Message m;
            if (chatWith.startsWith("GROUP:")) {
                String group = chatWith.substring(6);
                m = new Message(MessageType.GROUP_MESSAGE);
                m.setGroupName(group);
                m.setText(txt);
            } else {
                m = new Message(MessageType.PRIVATE_MESSAGE);
                m.setTo(chatWith);
                m.setText(txt);
            }
            m.setFrom(username);
            out.writeObject(m);
            out.flush();
            String ts = m.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            chatArea.append(String.format("[%s] %s: %s\n", ts, username, txt));
            sessionHistory.add(m);
            messageField.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showEmojiMenu() {
        String[] emojis = {"(^-^)", "(≧▽≦)", "(T_T)", "(づ｡◕‿‿◕｡)づ", "(ಥ﹏ಥ)", "(☞ ͡° ͜ʖ ͡°)☞"};
        String emoji = (String) JOptionPane.showInputDialog(this, "Choose emoji:", "Emoji", JOptionPane.PLAIN_MESSAGE,
                null, emojis, emojis[0]);
        if (emoji != null) {
            messageField.setText(messageField.getText() + emoji);
        }
    }

    private void sendImage() {
        if (chatWith == null) {
            JOptionPane.showMessageDialog(this, "Choose user to send image to");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
        int r = chooser.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedImage img = ImageIO.read(f);
                ImageIO.write(img, "png", baos);
                baos.flush();
                byte[] bytes = baos.toByteArray();
                Message m = new Message(MessageType.FILE_TRANSFER);
                m.setFrom(username);
                m.setTo(chatWith);
                m.setFileBytes(bytes);
                m.setFileName(f.getName());
                out.writeObject(m);
                out.flush();
                String ts = m.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                chatArea.append(String.format("[%s] %s sent image: %s\n", ts, username, f.getName()));
                sessionHistory.add(m);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showImageInChat(BufferedImage img, String from, String ts) {
        chatArea.append(String.format("[%s] %s sent image:\n", ts, from));
        ImageIcon icon = new ImageIcon(img.getScaledInstance(200, -1, Image.SCALE_SMOOTH));
        JOptionPane.showMessageDialog(this, new JLabel(icon), "Image from " + from, JOptionPane.PLAIN_MESSAGE);
    }

    private void doLogout() {
        try {
            Message m = new Message(MessageType.LOGOUT);
            m.setFrom(username);
            out.writeObject(m);
            out.flush();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClient c = new ChatClient();
            c.setVisible(true);
        });
    }
}