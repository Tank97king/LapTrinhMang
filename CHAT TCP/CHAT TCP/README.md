# ChatTCP - Java TCP Chat (Eclipse importable)

This is a simple client-server chat application using TCP sockets and Java Swing UI. It's designed to be importable into Eclipse as a Java project.

Features included:
- Login / Register (username-based)
- Display online users (updates when users connect/disconnect)
- Private 1-1 chat (text, emoji, image files)
- Message timestamps and session history (in-memory for session)
- Multi-threaded server to serve many clients

Files added:
- `src/com/chattcp/shared` - shared protocol classes (Message, MessageType, UserInfo)
- `src/com/chattcp/server` - `ChatServer`, `ClientHandler`
- `src/com/chattcp/client` - `ChatClient` (Swing UI)

How to import into Eclipse:
1. Open Eclipse -> File -> Import -> Existing Projects into Workspace.
2. Choose root directory: select the project folder (the folder containing `.project`, `.classpath`, and `src/`).
3. Import the project.

How to run:
- Run `com.chattcp.server.ChatServer` first (right-click -> Run as -> Java Application). Server listens on port 5000 by default.
- Run `com.chattcp.client.ChatClient` for each client instance.

Notes & suggestions:
- This is a minimal, educational implementation. It uses Java serialization for Message objects. For production, consider a safer serialization (JSON) and authentication.
- Friend feature suggestion: implement friend lists persisted to disk (e.g., simple JSON file per user). Add MessageType.FRIEND_REQUEST and FRIEND_RESPONSE handling on server to route and store friend relationships. UI: add a "Friends" tab and allow sending friend requests from online list.
- Message history persistence: save to local files or a simple database (SQLite) per conversation.

Security and limitations:
- No encryption (plaintext TCP). For real apps, use TLS.
- No user password. Add password hashing and verification for accounts.

If you want, I can extend this further to:
- Persist accounts and friends to files or a lightweight database.
- Add group chat rooms.
- Replace Java serialization with JSON over sockets and add message validation.
