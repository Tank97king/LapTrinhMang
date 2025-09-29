package com.chattcp.server;

import com.chattcp.shared.Message;
import com.chattcp.shared.MessageType;
import com.chattcp.shared.UserInfo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class ChatServer {
    private int port;
    // username -> handler
    Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    // group name -> set of usernames
    Map<String, Set<String>> groups = new ConcurrentHashMap<>();
    HistoryManager history;
    private Path groupsFile = Paths.get("groups.txt");

    public ChatServer(int port) { this.port = port; }

    private AccountManager accounts;

    {
        try {
            accounts = new AccountManager(Paths.get("accounts.txt"));
            history = new HistoryManager(Paths.get("history"));
            // load groups from file
            if (Files.exists(groupsFile)) {
                for (String line : Files.readAllLines(groupsFile, StandardCharsets.UTF_8)) {
                    String g = line.trim();
                    if (!g.isEmpty()) groups.put(g, Collections.newSetFromMap(new ConcurrentHashMap<>()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("ChatServer started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClientSocket(clientSocket)).start();
        }
    }

    private void handleClientSocket(Socket socket) {
        try {
            // create ObjectOutputStream first then ObjectInputStream to avoid stream header deadlock
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // expect a LOGIN or REGISTER message first
            Message msg = (Message) in.readObject();
            if (msg.getType() == MessageType.REGISTER) {
                String username = msg.getFrom();
                String pw = msg.getPassword();
                Message resp = new Message(MessageType.REGISTER_FAIL);
                try {
                    if (username == null || username.trim().isEmpty() || pw == null || pw.isEmpty()) {
                        resp.setText("Invalid username or password");
                        out.writeObject(resp);
                        socket.close();
                        return;
                    }
                    boolean ok = accounts.register(username, pw);
                    if (!ok) {
                        resp.setText("Username already exists");
                        out.writeObject(resp);
                        socket.close();
                        return;
                    } else {
                        resp = new Message(MessageType.REGISTER_OK);
                        out.writeObject(resp);
                        socket.close();
                        return;
                    }
                } catch (Exception e) {
                    resp.setText("Error: " + e.getMessage());
                    out.writeObject(resp);
                    socket.close();
                    return;
                }
            } else if (msg.getType() == MessageType.LOGIN) {
                String username = msg.getFrom();
                String pw = msg.getPassword();
                Message resp = new Message(MessageType.LOGIN_FAIL);
                synchronized (clients) {
                    try {
                        if (username == null || username.trim().isEmpty() || clients.containsKey(username) || !accounts.authenticate(username, pw)) {
                            resp.setText("Invalid credentials or already in use");
                            out.writeObject(resp);
                            socket.close();
                            return;
                        } else {
                            ClientHandler handler = new ClientHandler(username, socket, in, out, this);
                            clients.put(username, handler);
                            resp = new Message(MessageType.LOGIN_OK);
                            out.writeObject(resp);
                            out.flush();
                            // send friend list
                            Message fl = new Message(MessageType.FRIEND_LIST);
                            StringBuilder sb = new StringBuilder();
                            for (String f : accounts.getFriends(username)) {
                                if (sb.length() > 0) sb.append(",");
                                sb.append(f);
                            }
                            fl.setText(sb.toString());
                            out.writeObject(fl);
                            out.flush();
                            // send group list
                            Message gl = new Message(MessageType.GROUP_LIST);
                            gl.setText(String.join("\n", groups.keySet()));
                            out.writeObject(gl);
                            out.flush();
                            handler.start();
                            broadcastUserList();
                            System.out.println(username + " logged in");
                        }
                    } catch (Exception e) {
                        resp.setText("Server error");
                        out.writeObject(resp);
                        socket.close();
                        return;
                    }
                }
            } else {
                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void removeClient(String username) {
        clients.remove(username);
        broadcastUserList();
        System.out.println(username + " disconnected");
    }

    void forwardMessage(Message m) {
        // route private message
        if (m.getType() == MessageType.PRIVATE_MESSAGE || m.getType() == MessageType.FILE_TRANSFER
                || m.getType() == MessageType.FRIEND_REQUEST || m.getType() == MessageType.FRIEND_RESPONSE) {
            if (m.getTo() != null) {
                ClientHandler dest = clients.get(m.getTo());
                if (dest != null) dest.send(m);
                // append private history in normalized conversation id
                try {
                    String a = m.getFrom();
                    String b = m.getTo();
                    String conv = a.compareTo(b) <= 0 ? (a + "_" + b) : (b + "_" + a);
                    history.append("private_" + conv, m.getTimestamp() + " " + m.getFrom() + ": " + m.getText());
                } catch (Exception e) { }
            }
        } else if (m.getType() == MessageType.GROUP_MESSAGE) {
            String g = m.getGroupName();
            if (g == null) return;
            Set<String> members = groups.get(g);
            if (members == null) return;
            for (String member : members) {
                ClientHandler dest = clients.get(member);
                if (dest != null) dest.send(m);
            }
            // append to group history
            try {
                history.append("group_" + g, m.getTimestamp() + " " + m.getFrom() + ": " + m.getText());
            } catch (IOException e) { e.printStackTrace(); }
        }
        else if (m.getType() == MessageType.HISTORY_REQUEST) {
            try {
                String conv = m.getText(); // conversation id
                String all = history.readAll(conv);
                Message resp = new Message(MessageType.HISTORY_RESPONSE);
                resp.setText(all);
                ClientHandler dest = clients.get(m.getFrom());
                if (dest != null) dest.send(resp);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    void broadcastGroupList() {
        try {
            Message gl = new Message(MessageType.GROUP_LIST);
            gl.setText(String.join("\n", groups.keySet()));
            for (ClientHandler h : clients.values()) h.send(gl);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // route friend request to recipient if online, otherwise ignore (could store as pending)
    void sendFriendRequest(Message m) {
        if (m.getTo() != null) {
            ClientHandler dest = clients.get(m.getTo());
            if (dest != null) dest.send(m);
        }
    }

    public boolean addMemberToGroup(String group, String user) {
        Set<String> members = groups.computeIfAbsent(group, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        return members.add(user);
    }

    public synchronized void saveGroups() {
        try {
            StringBuilder sb = new StringBuilder();
            for (String g : groups.keySet()) sb.append(g).append(System.lineSeparator());
            Files.write(groupsFile, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) { e.printStackTrace(); }
    }

    void broadcastUserList() {
        try {
            Message m = new Message(MessageType.USER_LIST);
            List<String> names = new ArrayList<>(clients.keySet());
            m.setText(String.join("\n", names));
            for (ClientHandler h : clients.values()) {
                h.send(m);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) throws IOException {
        int port = 5000;
        if (args != null && args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ex) { /* ignore */ }
        }
        ChatServer server = new ChatServer(port);
        server.start();
    }
}
