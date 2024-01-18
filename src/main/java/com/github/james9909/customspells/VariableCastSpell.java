package com.github.james9909.customspells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.spells.TargetedEntityFromLocationSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.CastResult;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.config.ConfigData;
import org.bukkit.entity.Player;

public class VariableCastSpell extends com.nisovin.magicspells.spells.instant.VariableCastSpell implements TargetedLocationSpell, TargetedEntitySpell, TargetedEntityFromLocationSpell {
    private final ConfigData<String> variableName = this.getConfigDataString("variable-name", null);

    public VariableCastSpell(MagicConfig config, String spellName) {
        super(config, spellName);
    }

    public Subspell getSubSpell(SpellData data) {
        if (data.caster() instanceof Player player) {
            String variableName = this.variableName.get(data);
            if (variableName == null) return null;
            String value = MagicSpells.getVariableManager().getVariable(variableName).getStringValue(player);

            Subspell subSpell = new Subspell(value);
            if (!subSpell.process()) {
                this.sendMessage(player, this.getStrDoesntContainSpell());
                return null;
            }
            return subSpell;
        }
        return null;
    }

    public CastResult cast(SpellData data) {
        Subspell s = this.getSubSpell(data);
        if (s != null) {
            SpellCastResult result = s.subcast(data);
            return new CastResult(result.action, result.data);
        }
        return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
    }

    public SpellCastResult hardCast(SpellData data) {
        CastResult result = this.cast(data);
        return new SpellCastResult(SpellCastState.NORMAL, result.action());
    }

    @Override
    public CastResult castAtEntityFromLocation(SpellData data) {
        return this.cast(data);
    }

    @Override
    public CastResult castAtEntity(SpellData data) {
        return this.cast(data);
    }

    @Override
    public CastResult castAtLocation(SpellData data) {
        return this.cast(data);
    }
}
