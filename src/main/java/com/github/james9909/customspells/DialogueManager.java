package com.github.james9909.customspells;

import net.kyori.adventure.text.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Tracks the single active dialogue session per player and, while a session is open, holds back that
// player's incoming chat so it can be replayed ("caught up") once the dialogue ends.
final class DialogueManager {

    // Max chat messages held per player; oldest are dropped past this so a long dialogue can't grow
    // memory without bound.
    private static final int MAX_HELD = 50;

    private static final ConcurrentHashMap<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    private DialogueManager() {}

    private static final class Session {
        private volatile UUID token;
        private final Deque<Component> held = new ArrayDeque<>();

        private Session(UUID token) {
            this.token = token;
        }
    }

    // Begins (or renews) a dialogue session for the player, invalidating any previous one, and
    // returns the token identifying it. Held chat is preserved across renewals so chaining one
    // dialogue into another keeps the pause going until the player actually leaves.
    static UUID startSession(UUID playerId) {
        UUID token = UUID.randomUUID();
        SESSIONS.compute(playerId, (id, existing) -> {
            Session session = existing != null ? existing : new Session(token);
            session.token = token;
            return session;
        });
        return token;
    }

    // Returns true if the given token is the player's current active dialogue session.
    static boolean isActive(UUID playerId, UUID token) {
        Session session = SESSIONS.get(playerId);
        return session != null && token.equals(session.token);
    }

    static boolean hasActiveSession(UUID playerId) {
        return SESSIONS.containsKey(playerId);
    }

    // Holds a chat message for a paused player. Returns true if it was held (player is in a dialogue),
    // false otherwise. Safe to call off the main thread (chat events fire async).
    static boolean hold(UUID playerId, Component message) {
        Session session = SESSIONS.get(playerId);
        if (session == null) {
            return false;
        }
        synchronized (session.held) {
            while (session.held.size() >= MAX_HELD) {
                session.held.pollFirst();
            }
            session.held.addLast(message);
        }
        return true;
    }

    // Ends the player's session and returns the chat that was held while it was open, in arrival
    // order. The caller decides whether to replay it. Returns an empty list if no session was active.
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
