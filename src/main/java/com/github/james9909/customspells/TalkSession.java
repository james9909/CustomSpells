package com.github.james9909.customspells;

import com.nisovin.magicspells.util.SpellData;
import org.bukkit.entity.Player;

import java.util.UUID;

// A TalkSpell session: a linear sequence of text lines advanced by clicking. Scrolling does nothing.
final class TalkSession extends DialogueSession {
    final TalkSpell spell;
    final SpellData data;

    int index;

    TalkSession(TalkSpell spell, UUID playerId, SpellData data) {
        super(playerId);
        this.spell = spell;
        this.data = data;
        this.index = 0;
    }

    @Override
    void open(Player player) {
        spell.display(player, this);
    }

    @Override
    void scroll(Player player, int direction) {
    }

    @Override
    void click(Player player, boolean rightClick) {
        spell.advance(player, this);
    }
}
