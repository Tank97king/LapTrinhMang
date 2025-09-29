package com.chattcp.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private String from;
    private String to; // optional, for private messages
    private String text;
    private String password; // optional, for register/login
    private byte[] fileBytes; // optional for image transfer
    private String fileName;
    private String groupName; // optional for group messages
    private LocalDateTime timestamp;

    public Message(MessageType type) {
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    // getters and setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public byte[] getFileBytes() { return fileBytes; }
    public void setFileBytes(byte[] fileBytes) { this.fileBytes = fileBytes; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
