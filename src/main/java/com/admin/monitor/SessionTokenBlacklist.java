package com.admin.monitor;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionTokenBlacklist {

    private final Set<String> blockedSessionIds = ConcurrentHashMap.newKeySet();

    public void block(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            blockedSessionIds.add(sessionId);
        }
    }

    public boolean isBlocked(String sessionId) {
        return sessionId != null && blockedSessionIds.contains(sessionId);
    }

    public int size() {
        return blockedSessionIds.size();
    }
}
