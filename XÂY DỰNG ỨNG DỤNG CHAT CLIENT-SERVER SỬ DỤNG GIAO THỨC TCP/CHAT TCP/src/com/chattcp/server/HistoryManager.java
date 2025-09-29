package com.chattcp.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class HistoryManager {
    private final Path base;

    public HistoryManager(Path base) throws IOException {
        this.base = base;
        if (!Files.exists(base)) Files.createDirectories(base);
    }

    private Path convPath(String name) { return base.resolve(name + ".txt"); }

    public synchronized void append(String conversation, String line) throws IOException {
        Path p = convPath(conversation);
        Files.write(p, (line + System.lineSeparator()).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public synchronized String readAll(String conversation) throws IOException {
        Path p = convPath(conversation);
        if (!Files.exists(p)) return "";
        List<String> lines = Files.readAllLines(p);
        StringBuilder sb = new StringBuilder();
        for (String l : lines) sb.append(l).append(System.lineSeparator());
        return sb.toString();
    }
}
