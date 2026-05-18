package com.admin.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class NoticeWebSocketSessionManager {

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void add(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void remove(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            userSessions.remove(userId);
        }
    }

    public int connectionCount() {
        return userSessions.values().stream().mapToInt(Set::size).sum();
    }

    public void broadcast(String json) {
        TextMessage message = new TextMessage(json);
        userSessions.values().forEach(sessions ->
                sessions.forEach(session -> send(session, message)));
    }

    /** 强退指定会话：推送下线消息并关闭对应 WebSocket */
    public void kickBySessionId(String sessionId, String json) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        TextMessage message = new TextMessage(json);
        userSessions.values().forEach(sessions -> sessions.forEach(session -> {
            Object sid = session.getAttributes().get(JwtHandshakeInterceptor.ATTR_SESSION_ID);
            if (!sessionId.equals(sid)) {
                return;
            }
            send(session, message);
            close(session, CloseStatus.NORMAL);
        }));
    }

    private void close(WebSocketSession session, CloseStatus status) {
        if (!session.isOpen()) {
            return;
        }
        try {
            session.close(status);
        } catch (IOException e) {
            log.debug("WebSocket close failed: {}", e.getMessage());
        }
    }

    private void send(WebSocketSession session, TextMessage message) {
        if (!session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
        } catch (IOException e) {
            log.debug("WebSocket send failed: {}", e.getMessage());
        }
    }
}
