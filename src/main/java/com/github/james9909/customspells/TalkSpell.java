package com.github.james9909.customspells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.util.config.ConfigDataUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TalkSpell extends AbstractDialogueSpell {

    private final List<ConfigData<String>> lines = new ArrayList<>();
    private final String spellOnEndName;
    private Subspell spellOnEnd;

    public TalkSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        this.spellOnEndName = getConfigString("spell-on-end", null);

        ConfigurationSection optionsSection = getConfigSection("options");
        if (optionsSection == null || optionsSection.getKeys(false).isEmpty()) {
            MagicSpells.error("TalkSpell '" + spellName + "' has no text options.");
            return;
        }

        for (String key : optionsSection.getKeys(false)) {
            String line = optionsSection.getString(key);
            if (line == null) {
                continue;
            }
            this.lines.add(ConfigDataUtil.getString(line));
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        if (spellOnEndName != null && !spellOnEndName.isEmpty()) {
            spellOnEnd = initSubspell(spellOnEndName,
                    "TalkSpell '" + internalName + "' has an invalid spell-on-end: " + spellOnEndName);
        }
    }

    @Override
    protected DialogueSession createSession(Player player, float power, String[] args, SpellData data) {
        if (this.lines.isEmpty()) {
            MagicSpells.error("TalkSpell '" + internalName + "' has no lines to show.");
            return null;
        }
        return new TalkSession(this, player.getUniqueId(), data);
    }

    void display(Player player, TalkSession session) {
        clearScreen(player);
        player.sendMessage(line(this.lines.get(session.index), session.data));
    }

    void advance(Player player, TalkSession session) {
        session.index++;
        if (session.index < this.lines.size()) {
            display(player, session);
            return;
        }

        if (spellOnEnd != null) {
            spellOnEnd.subcast(session.data);
        }

        // If spell-on-end didn't open a new dialogue, this talk is over: end it and replay held chat.
        if (DialogueManager.isActive(session.playerId, session.token)) {
            List<Component> held = DialogueManager.endSession(session.playerId);
            held.forEach(player::sendMessage);
        }
    }
}
