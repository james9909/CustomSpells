package com.github.james9909.customspells;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Tracks the single active dialogue session per player and, while a session is open, holds back that
// player's incoming chat so it can be replayed once the dialogue ends.
final class DialogueManager {
    static final int DEFAULT_CATCH_UP_LIMIT = 50;
    private static volatile int catchUpLimit = DEFAULT_CATCH_UP_LIMIT;

    private static final ConcurrentHashMap<UUID, DialogueSession> SESSIONS = new ConcurrentHashMap<>();

    private DialogueManager() {}

    static void setCatchUpLimit(int limit) {
        catchUpLimit = limit;
    }

    static void setActive(DialogueSession session) {
        SESSIONS.compute(session.playerId, (id, existing) -> {
            if (existing != null) {
                synchronized (existing.held) {
                    session.held.addAll(existing.held);
                }
            }
            return session;
        });
    }

    static DialogueSession get(UUID playerId) {
        return SESSIONS.get(playerId);
    }

    static boolean isActive(UUID playerId, UUID token) {
        DialogueSession session = SESSIONS.get(playerId);
        return session != null && token.equals(session.token);
    }

    static boolean hasActiveSession(UUID playerId) {
        return SESSIONS.containsKey(playerId);
    }

    static boolean hasAnyActiveSessions() {
        return !SESSIONS.isEmpty();
    }

    static boolean hold(UUID playerId, Component message) {
        DialogueSession session = SESSIONS.get(playerId);
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
        DialogueSession session = SESSIONS.remove(playerId);
        if (session == null) {
            return List.of();
        }
        synchronized (session.held) {
            return new ArrayList<>(session.held);
        }
    }
}
