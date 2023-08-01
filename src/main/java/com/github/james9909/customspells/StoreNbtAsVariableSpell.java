package com.github.james9909.customspells;

import com.saicone.rtag.RtagItem;
import org.bukkit.inventory.ItemStack;
import com.nisovin.magicspells.util.managers.VariableManager;
import org.bukkit.entity.Player;
import com.nisovin.magicspells.Spell;
import org.bukkit.entity.LivingEntity;
import com.nisovin.magicspells.MagicSpells;
import java.util.ArrayList;
import com.nisovin.magicspells.util.MagicConfig;
import java.util.List;
import java.util.HashMap;
import com.nisovin.magicspells.spells.InstantSpell;

public class StoreNbtAsVariableSpell extends InstantSpell {
    private HashMap<String, String> nbtToVariableMapping;
    private List<String> variables;

    public StoreNbtAsVariableSpell(final MagicConfig config, final String spellName) {
        super(config, spellName);
        this.nbtToVariableMapping = new HashMap<String, String>();
        this.variables = this.getConfigStringList("variables", new ArrayList());
    }

    protected void initialize() {
        super.initialize();
        this.variables.forEach(variable -> {
            String[] split = variable.split(" ");
            if (split.length != 2) {
                MagicSpells.error(String.format("Invalid variable format %s", variable));
            }
            else {
                this.nbtToVariableMapping.put(split[0], split[1]);
            }
        });
    }

    public Spell.PostCastAction castSpell(final LivingEntity caster, final Spell.SpellCastState state, final float power, final String[] args) {
        if (!(caster instanceof Player player)) {
            return Spell.PostCastAction.HANDLE_NORMALLY;
        }
        final VariableManager variableManager = MagicSpells.getVariableManager();
        final ItemStack item = player.getInventory().getItemInMainHand();
        this.nbtToVariableMapping.forEach((nbtKey, msVariable) -> {
            RtagItem tag = new RtagItem(item);
            Object value = tag.get(nbtKey);
            if (value instanceof Double) {
                variableManager.set(msVariable, player, (Double) value);
            } else if (value instanceof Byte) {
                variableManager.set(msVariable, player, (Byte) value);
            } else if (value instanceof String) {
                variableManager.set(msVariable, player, (String) value);
            } else if (value instanceof Short) {
                variableManager.set(msVariable, player, (Short) value);
            } else if (value instanceof Integer) {
                variableManager.set(msVariable, player, (Integer) value);
            } else if (value instanceof Float) {
                variableManager.set(msVariable, player, (Float) value);
            }
        });
        return Spell.PostCastAction.HANDLE_NORMALLY;
    }
}
