package com.github.james9909.customspells;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
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

public class StoreNbtAsVariableSpell extends InstantSpell
{
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
        if (!(caster instanceof Player)) {
            return Spell.PostCastAction.HANDLE_NORMALLY;
        }
        final Player player = (Player)caster;
        final VariableManager variableManager = MagicSpells.getVariableManager();
        final ItemStack item = player.getInventory().getItemInMainHand();
        this.nbtToVariableMapping.forEach((nbtKey, msVariable) -> {
            if (NBTEditor.contains(item, nbtKey)) {
                String nbtValue = NBTEditor.getString(item, nbtKey);
                try {
                    variableManager.set(msVariable, player, nbtValue);
                } catch (Exception e) {}
            }
        });
        return Spell.PostCastAction.HANDLE_NORMALLY;
    }
}
