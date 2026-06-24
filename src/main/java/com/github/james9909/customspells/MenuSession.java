package com.github.james9909.customspells;

import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.config.ConfigData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

// A DialogueSpell session: a heading plus a scroll-selectable list of options.
final class MenuSession extends DialogueSession {
    final DialogueSpell spell;
    final float power;
    final String[] args;
    final SpellData data;

    final ConfigData<String> heading;
    final List<DialogueOption> options;

    int selected;

    MenuSession(DialogueSpell spell, UUID playerId, float power, String[] args, SpellData data,
                ConfigData<String> heading, List<DialogueOption> options) {
        super(playerId);
        this.spell = spell;
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

    @Override
    void open(Player player) {
        spell.render(player, this);
        spell.fireSelect(this);
    }

    @Override
    void scroll(Player player, int direction) {
        spell.navigate(player, this, direction);
    }

    @Override
    void click(Player player, boolean rightClick) {
        spell.triggerOptionClick(this, rightClick);
    }
}
