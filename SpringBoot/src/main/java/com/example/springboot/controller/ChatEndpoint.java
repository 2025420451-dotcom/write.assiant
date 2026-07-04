/*package com.example.springboot.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/chat")
public class ChatEndpoint {

    // 存储用户会话
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    // 存储消息历史（生产环境应使用数据库）
    private static final Map<String, List<Message>> messageHistory = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        try {
            // 从session中获取用户信息
            Map<String, List<String>> params = session.getRequestParameterMap();
            String username = null;
            
            if (params != null && params.containsKey("username")) {
                List<String> usernameList = params.get("username");
                if (usernameList != null && !usernameList.isEmpty()) {
                    username = usernameList.get(0);
                }
            }
            
            if (username == null || username.trim().isEmpty()) {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Username is required"));
                return;
            }

            handleExistingConnection(username);
            session.getUserProperties().put("username", username);
            sessions.put(username, session);
            messageHistory.putIfAbsent(username, new ArrayList<>());

            log.info("[CONNECT] {} connected. Online: {}", username, sessions.size());
            broadcastUserList();

        } catch (Exception e) {
            log.error("Connection failed: ", e);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Connection failed"));
            } catch (IOException ex) {
                log.error("Failed to close session: ", ex);
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        try {
            String username = getUsername(session);
            if (username != null && sessions.remove(username) != null) {
                log.info("[DISCONNECT] {} disconnected. Remaining: {}", username, sessions.size());
                broadcastUserList();
            }
        } catch (Exception e) {
            log.error("Error during connection close: ", e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject msg = JSONUtil.parseObj(message);
            String msgType = msg.getStr("type", "chat");
            String fromUser = getUsername(session);

            switch (msgType) {
                case "chat":
                    handleChatMessage(fromUser, msg);
                    break;
                case "history":
                    handleHistoryRequest(session, msg);
                    break;
                default:
                    sendError(session, "Unsupported message type");
            }
        } catch (Exception e) {
            log.error("Message processing failed: ", e);
            sendError(session, "Invalid message format");
        }
    }

    private void handleChatMessage(String fromUser, JSONObject msg) {
        String messageType = msg.getStr("messageType", "private");
        String content = msg.getStr("content");

        if ("private".equals(messageType)) {
            String toUser = msg.getStr("to");
            sendPrivateMessage(fromUser, toUser, content);
        } else {
            broadcastMessage(fromUser, content);
        }
    }

    private void sendPrivateMessage(String fromUser, String toUser, String content) {
        Session targetSession = sessions.get(toUser);
        if (targetSession == null) {
            sendError(sessions.get(fromUser), "User " + toUser + " is offline");
            return;
        }

        JSONObject message = buildMessage(fromUser, toUser, content);
        sendText(targetSession, message.toString());
        sendText(sessions.get(fromUser), message.toString());
        storeMessage(fromUser, toUser, content);
    }

    private void broadcastMessage(String fromUser, String content) {
        JSONObject message = buildMessage(fromUser, null, content);
        sessions.forEach((user, session) -> {
            if (!user.equals(fromUser) && session.isOpen()) {
                sendText(session, message.toString());
            }
        });
    }

    private JSONObject buildMessage(String from, String to, String content) {
        return new JSONObject()
                .set("type", "message")
                .set("from", from)
                .set("to", to)
                .set("content", content)
                .set("timestamp", System.currentTimeMillis());
    }

    private void handleHistoryRequest(Session session, JSONObject msg) {
        String username = getUsername(session);
        String targetUser = msg.getStr("target");

        List<Message> history = messageHistory.getOrDefault(username, Collections.emptyList())
                .stream()
                .filter(m -> m.getTo().equals(targetUser) || m.getFrom().equals(targetUser))
                .sorted(Comparator.comparingLong(Message::getTimestamp))
                .toList();

        JSONObject response = new JSONObject()
                .set("type", "history")
                .set("target", targetUser)
                .set("data", history);

        sendText(session, response.toString());
    }

    // Helper methods
    private String generateUniqueUsername() {
        String username;
        do {
            username = "用户_" + UUID.randomUUID().toString().substring(0, 6);
        } while (sessions.containsKey(username));
        return username;
    }

    private void handleExistingConnection(String username) throws IOException {
        if (sessions.containsKey(username)) {
            Session oldSession = sessions.get(username);
            oldSession.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY,
                    "New connection established"
            ));
            sessions.remove(username);
        }
    }

    private void broadcastUserList() {
        JSONObject message = new JSONObject()
                .set("type", "userlist")
                .set("data", new JSONObject()
                        .set("onlineUsers", sessions.keySet())
                        .set("total", sessions.size()));

        sessions.forEach((user, session) -> sendText(session, message.toString()));
    }

    private void storeMessage(String from, String to, String content) {
        Message message = new Message(from, to, content, System.currentTimeMillis());
        messageHistory.computeIfAbsent(from, k -> new ArrayList<>()).add(message);
        messageHistory.computeIfAbsent(to, k -> new ArrayList<>()).add(message);
    }

    private String getUsername(Session session) {
        return (String) session.getUserProperties().get("username");
    }

    private void sendText(Session session, String message) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            log.error("Message send failed: ", e);
        }
    }

    private void sendError(Session session, String error) {
        JSONObject errorMsg = new JSONObject()
                .set("type", "error")
                .set("message", error);
        sendText(session, errorMsg.toString());
    }

    // Message entity
    private static class Message {
        private final String from;
        private final String to;
        private final String content;
        private final long timestamp;

        public Message(String from, String to, String content, long timestamp) {
            this.from = from;
            this.to = to;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getFrom() { return from; }
        public String getTo() { return to; }
        public String getContent() { return content; }
        public long getTimestamp() { return timestamp; }
    }
}*/