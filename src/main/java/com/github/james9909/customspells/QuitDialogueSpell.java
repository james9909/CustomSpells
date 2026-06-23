package com.github.james9909.customspells;

import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class QuitDialogueSpell extends InstantSpell {

    public QuitDialogueSpell(MagicConfig config, String spellName) {
        super(config, spellName);
    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state != SpellCastState.NORMAL || !(caster instanceof Player player)) {
            return PostCastAction.HANDLE_NORMALLY;
        }

        // End the session and replay any chat that was held while the dialogue was open.
        List<Component> held = DialogueManager.endSession(player.getUniqueId());
        held.forEach(player::sendMessage);
        return PostCastAction.HANDLE_NORMALLY;
    }
}
