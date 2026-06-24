package com.github.james9909.customspells;

import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.config.ConfigData;
import net.kyori.adventure.text.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

// A live dialogue shown to a single player.
final class DialogueSession {
    final UUID token = UUID.randomUUID();

    final DialogueSpell spell;
    final UUID playerId;
    final float power;
    final String[] args;
    final SpellData data;

    final ConfigData<String> heading;
    final List<DialogueOption> options;

    int selected;

    final Deque<Component> held = new ArrayDeque<>();

    DialogueSession(DialogueSpell spell, UUID playerId, float power, String[] args, SpellData data,
                    ConfigData<String> heading, List<DialogueOption> options) {
        this.spell = spell;
        this.playerId = playerId;
        this.power = power;
        this.args = args;
        this.data = data;
        this.heading = heading;
        this.options = options;
        this.selected = options.isEmpty() ? -1 : 0;
    }

    DialogueOption selectedOption() {
        if (selected < 0 || selected >= options.size()) return null;
        return options.get(selected);
    }
}
