package com.verity.messenger.verityserver;

import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(),
                new ConcurrentWebSocketSessionDecorator(session, 10_000, 512 * 1024));
        System.out.println("connected "+session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        broadcast(message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        System.out.println("disconnected "+session.getId());
    }

    private void broadcast(TextMessage message) {
        for (WebSocketSession s : sessions.values()) {
            try {
                if (s.isOpen()) {
                    s.sendMessage(message);
                }
            } catch (IOException e) {
                sessions.remove(s.getId());
            }
        }
    }
}