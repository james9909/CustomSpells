package com.github.james9909.customspells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.managers.VariableManager;
import de.tr7zw.nbtapi.NBT;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StoreVariableAsNbtSpell extends InstantSpell {
    private HashMap<String, String> variableToNbtMapping;
    private List<String> variables;
    private String position;

    public StoreVariableAsNbtSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        this.variableToNbtMapping = new HashMap<>();
        this.variables = this.getConfigStringList("variables", new ArrayList());
        this.position = this.getConfigString("slot", "hand");
    }

    protected void initialize() {
        super.initialize();
        this.variables.forEach(variable -> {
            String[] split = variable.split(" ");
            if (split.length != 2) {
                MagicSpells.error(String.format("Invalid variable format %s", variable));
            }
            else {
                this.variableToNbtMapping.put(split[0], split[1]);
            }
        });
    }

    public Spell.PostCastAction castSpell(final LivingEntity caster, final Spell.SpellCastState state, final float power, final String[] args) {
        if (!(caster instanceof Player player)) {
            return Spell.PostCastAction.HANDLE_NORMALLY;
        }

        VariableManager variableManager = MagicSpells.getVariableManager();
        PlayerInventory inventory = player.getInventory();
        ItemStack item;
        if (this.position.equals("hand")) {
            item = inventory.getItemInMainHand();
        } else if (this.position.equals("offhand")) {
            item = inventory.getItemInOffHand();
        } else {
            // Try to cast to number
            try {
                int position = Integer.parseInt(this.position);
                item = inventory.getItem(position);
            } catch (Exception e) {
                return PostCastAction.HANDLE_NORMALLY;
            }
        }
        if (item == null) {
            return PostCastAction.HANDLE_NORMALLY;
        }
        NBT.modify(item, nbt -> {
            this.variableToNbtMapping.forEach((msVariableName, nbtKey) -> {
                String msVariable = variableManager.getStringValue(msVariableName, player);
                nbt.setString(nbtKey, msVariable);
            });
        });
        return Spell.PostCastAction.HANDLE_NORMALLY;
    }
}
