package com.chattcp.server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class AccountManager {
    private Path file;
    private Map<String, String> accounts = new HashMap<>();
    private Map<String, Set<String>> friends = new HashMap<>();

    public AccountManager(Path file) throws IOException {
        this.file = file;
        if (!Files.exists(file)) Files.createFile(file);
        load();
        loadFriends();
    }

    private void load() throws IOException {
        for (String line : Files.readAllLines(file)) {
            String[] p = line.split(":", 2);
            if (p.length == 2) accounts.put(p[0], p[1]);
        }
    }

    private void save(String username, String hash) throws IOException {
        String line = username + ":" + hash + System.lineSeparator();
        Files.write(file, line.getBytes(), StandardOpenOption.APPEND);
    }

    public synchronized boolean register(String username, String password) throws Exception {
        if (accounts.containsKey(username)) return false;
        String h = hash(password);
        accounts.put(username, h);
        save(username, h);
        // init empty friend set
        friends.put(username, new HashSet<>());
        return true;
    }

    private void loadFriends() throws IOException {
        Path parent = file.getParent();
        if (parent == null) parent = file.toAbsolutePath().getParent();
        if (parent == null) parent = Paths.get(".");
        Path f = parent.resolve("friends.txt");
        if (!Files.exists(f)) return;
        for (String line : Files.readAllLines(f)) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String user = parts[0];
                Set<String> set = new HashSet<>();
                if (!parts[1].trim().isEmpty()) {
                    for (String s : parts[1].split(",")) set.add(s.trim());
                }
                friends.put(user, set);
            }
        }
    }

    private void saveFriends() throws IOException {
        Path parent = file.getParent();
        if (parent == null) parent = file.toAbsolutePath().getParent();
        if (parent == null) parent = Paths.get(".");
        Path f = parent.resolve("friends.txt");
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Set<String>> e : friends.entrySet()) {
            sb.append(e.getKey()).append(":");
            sb.append(String.join(",", e.getValue()));
            sb.append(System.lineSeparator());
        }
        Files.write(f, sb.toString().getBytes());
    }

    public synchronized boolean addFriend(String user, String friend) throws IOException {
        friends.computeIfAbsent(user, k -> new HashSet<>()).add(friend);
        friends.computeIfAbsent(friend, k -> new HashSet<>()).add(user);
        saveFriends();
        return true;
    }

    public synchronized Set<String> getFriends(String user) {
        return friends.getOrDefault(user, new HashSet<>());
    }

    public synchronized boolean authenticate(String username, String password) throws Exception {
        String h = accounts.get(username);
        if (h == null) return false;
        return h.equals(hash(password));
    }

    private String hash(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] b = md.digest(s.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}
