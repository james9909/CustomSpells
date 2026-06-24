package com.github.james9909.customspells;

import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.config.ConfigData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class AbstractDialogueSpell extends InstantSpell {

    protected static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    protected final int clearLines;

    public AbstractDialogueSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        this.clearLines = getConfigInt("clear-lines", 20);
    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state != SpellCastState.NORMAL || !(caster instanceof Player player)) {
            return PostCastAction.HANDLE_NORMALLY;
        }

        DialogueSession session = createSession(player, power, args, new SpellData(player, power, args));
        if (session == null) {
            return PostCastAction.HANDLE_NORMALLY;
        }

        // Opening a new dialogue invalidates any earlier one and starts holding chat for catch-up.
        DialogueManager.setActive(session);
        session.open(player);

        return PostCastAction.HANDLE_NORMALLY;
    }

    // Builds the session to show, or null if there's nothing to display (e.g. misconfigured).
    protected abstract DialogueSession createSession(Player player, float power, String[] args, SpellData data);

    // Pushes old chat out of view so a re-render appears to update in place.
    protected void clearScreen(Player player) {
        for (int i = 0; i < clearLines; i++) {
            player.sendMessage(Component.empty());
        }
    }

    protected Component line(ConfigData<String> text, SpellData data) {
        return LEGACY.deserialize(text.get(data));
    }
}
