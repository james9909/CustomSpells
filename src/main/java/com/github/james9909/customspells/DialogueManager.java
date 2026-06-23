package com.github.james9909.customspells;

import net.kyori.adventure.text.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Tracks the single active dialogue session per player and, while a session is open, holds back that
// player's incoming chat so it can be replayed once the dialogue ends.
final class DialogueManager {
    static final int DEFAULT_CATCH_UP_LIMIT = 50;
    private static volatile int catchUpLimit = DEFAULT_CATCH_UP_LIMIT;

    private static final ConcurrentHashMap<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    private DialogueManager() {}

    private static final class Session {
        private volatile UUID token;
        private final Deque<Component> held = new ArrayDeque<>();

        private Session(UUID token) {
            this.token = token;
        }
    }

    static void setCatchUpLimit(int limit) {
        catchUpLimit = limit;
    }

    static UUID startSession(UUID playerId) {
        UUID token = UUID.randomUUID();
        SESSIONS.compute(playerId, (id, existing) -> {
            Session session = existing != null ? existing : new Session(token);
            session.token = token;
            return session;
        });
        return token;
    }

    static boolean isActive(UUID playerId, UUID token) {
        Session session = SESSIONS.get(playerId);
        return session != null && token.equals(session.token);
    }

    static boolean hasActiveSession(UUID playerId) {
        return SESSIONS.containsKey(playerId);
    }

    static boolean hasAnyActiveSessions() {
        return !SESSIONS.isEmpty();
    }

    static boolean hold(UUID playerId, Component message) {
        Session session = SESSIONS.get(playerId);
        if (session == null) {
            return false;
        }
        synchronized (session.held) {
            int limit = catchUpLimit;
            if (limit > 0) {
                while (session.held.size() >= limit) {
                    session.held.pollFirst();
                }
            }
            session.held.addLast(message);
        }
        return true;
    }

    static List<Component> endSession(UUID playerId) {
        Session session = SESSIONS.remove(playerId);
        if (session == null) {
            return List.of();
        }
        synchronized (session.held) {
            return new ArrayList<>(session.held);
        }
    }
}
