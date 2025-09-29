package com.chattcp.server;

import com.chattcp.shared.Message;
import com.chattcp.shared.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends Thread {
    private String username;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ChatServer server;
    private volatile boolean running = true;

    public ClientHandler(String username, Socket socket, ObjectInputStream in, ObjectOutputStream out, ChatServer server) {
        this.username = username;
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.server = server;
    }

    public void run() {
        try {
            while (running) {
                try {
                    Message m = (Message) in.readObject();
                    if (m == null) break;
                    switch (m.getType()) {
                        case LOGOUT:
                            running = false;
                            break;
                        case GROUP_CREATE:
                            server.groups.computeIfAbsent(m.getGroupName(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(username);
                            server.broadcastGroupList();
                            server.saveGroups();
                            break;
                        case GROUP_JOIN:
                            server.groups.computeIfAbsent(m.getGroupName(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(username);
                            server.broadcastGroupList();
                            server.saveGroups();
                            break;
                        case GROUP_LEAVE:
                            Set<String> s = server.groups.get(m.getGroupName());
                            if (s != null) s.remove(username);
                            server.broadcastGroupList();
                            server.saveGroups();
                            break;
                        case PRIVATE_MESSAGE:
                        case FILE_TRANSFER:
                            server.forwardMessage(m);
                            // append private history
                            try { server.history.append("private_" + m.getFrom() + "_" + m.getTo(), m.getTimestamp() + " " + m.getFrom() + ": " + m.getText()); } catch (Exception ex) { ex.printStackTrace(); }
                            break;
                        case GROUP_MESSAGE:
                            server.forwardMessage(m);
                            break;
                        case FRIEND_REQUEST:
                            server.sendFriendRequest(m);
                            break;
                        case FRIEND_RESPONSE:
                            // forward response
                            server.forwardMessage(m);
                            break;
                        case GROUP_ADD_MEMBER:
                            if (m.getTo() != null) {
                                // m.to contains username to add, m.groupName contains group
                                server.addMemberToGroup(m.getGroupName(), m.getTo());
                                server.broadcastGroupList();
                                // notify the added user if online
                                ClientHandler dest = server.clients.get(m.getTo());
                                if (dest != null) dest.send(m);
                                server.saveGroups();
                            }
                            break;
                        case GROUP_DELETE:
                            if (m.getGroupName() != null) {
                                server.groups.remove(m.getGroupName());
                                server.broadcastGroupList();
                                server.saveGroups();
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Error processing message for user " + username + ": " + e.getMessage());
                    e.printStackTrace();
                    break; // exit loop and cleanup
                }
            }
        } catch (Exception e) {
            System.err.println("Client handler outer error for user " + username + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public synchronized void send(Message m) {
        try {
            out.writeObject(m);
            out.flush();
        } catch (IOException e) {
            // ignore
        }
    }

    private void cleanup() {
        try { socket.close(); } catch (Exception e) {}
        server.removeClient(username);
    }
}
