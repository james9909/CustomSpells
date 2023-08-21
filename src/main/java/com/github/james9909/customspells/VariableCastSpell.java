package com.github.james9909.customspells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.spells.TargetedEntityFromLocationSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class VariableCastSpell extends com.nisovin.magicspells.spells.instant.VariableCastSpell implements TargetedLocationSpell, TargetedEntitySpell, TargetedEntityFromLocationSpell {
    public VariableCastSpell(MagicConfig config, String spellName) {
        super(config, spellName);
    }

    public Subspell getSubSpell(LivingEntity entity) {
        if (entity instanceof Player player) {
            String variableName = getVariableName();
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

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        Subspell s = this.getSubSpell(caster);
        if (s != null) {
            s.subcast(caster, power, args);
        }
        return PostCastAction.HANDLE_NORMALLY;
    }

    @Override
    public boolean castAtEntityFromLocation(LivingEntity caster, Location from, LivingEntity target, float power, String[] args) {
        Subspell s = this.getSubSpell(caster);
        if (s != null) {
            return s.subcast(caster, from ,target, power, args);
        }
        return true;
    }

    @Override
    public boolean castAtEntityFromLocation(Location from, LivingEntity target, float power, String[] args) {
        return false;
    }

    @Override
    public boolean castAtEntityFromLocation(LivingEntity caster, Location from, LivingEntity target, float power) {
        Subspell s = this.getSubSpell(caster);
        if (s != null) {
            return s.subcast(caster, from, target, power, null);
        }
        return true;
    }

    @Override
    public boolean castAtEntityFromLocation(Location from, LivingEntity target, float power) {
        return false;
    }

    @Override
    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power, String[] args) {
        Subspell s = this.getSubSpell(caster);
        if (s != null) {
            return s.subcast(caster, target, power, args);
        }
        return true;
    }

    @Override
    public boolean castAtEntity(LivingEntity target, float power, String[] args) {
        return false;
    }

    @Override
    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
        Subspell s = this.getSubSpell(caster);
        if (s != null) {
            return s.subcast(caster, target, power, null);
        }
        return true;
    }

    @Override
    public boolean castAtEntity(LivingEntity caster, float power) {
        Subspell s = this.getSubSpell(caster);
        if (s != null) {
            return s.subcast(caster, caster.getLocation(), power, null);
        }
        return true;
    }

    @Override
    public boolean castAtLocation(LivingEntity caster, Location target, float power, String[] args) {
        Subspell s = this.getSubSpell(caster);
        if (s != null) {
            return s.subcast(caster, target, power, args);
        }
        return true;
    }

    @Override
    public boolean castAtLocation(Location target, float power, String[] args) {
        return false;
    }

    @Override
    public boolean castAtLocation(LivingEntity caster, Location target, float power) {
        Subspell s = this.getSubSpell(caster);
        if (s != null) {
            return s.subcast(caster, target, power, null);
        }
        return true;
    }

    @Override
    public boolean castAtLocation(Location target, float power) {
        return false;
    }
}
