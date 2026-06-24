package com.github.james9909.customspells;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

abstract class DialogueSession {
    final UUID token = UUID.randomUUID();
    final UUID playerId;
    final Deque<Component> held = new ArrayDeque<>();

    DialogueSession(UUID playerId) {
        this.playerId = playerId;
    }

    abstract void open(Player player);

    abstract void scroll(Player player, int direction);

    abstract void click(Player player, boolean rightClick);
}
