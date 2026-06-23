package com.github.james9909.customspells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.managers.VariableManager;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StoreNbtAsVariableSpell extends InstantSpell {
    private HashMap<String, String> nbtToVariableMapping;
    private List<String> variables;
    private String position;

    public StoreNbtAsVariableSpell(final MagicConfig config, final String spellName) {
        super(config, spellName);
        this.nbtToVariableMapping = new HashMap<>();
        this.variables = this.getConfigStringList("variables", new ArrayList());
        this.position = this.getConfigString("slot", "hand");
    }

    protected void initialize() {
        super.initialize();
        this.variables.forEach(variable -> {
            String[] split = variable.split(" ");
            if (split.length != 2) {
                MagicSpells.error(String.format("Invalid variable format %s", variable));
            } else {
                this.nbtToVariableMapping.put(split[0], split[1]);
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
        this.nbtToVariableMapping.forEach((nbtKey, msVariable) -> {
            NBT.get(item, nbt -> {
                NBTType nbtType = nbt.getType(nbtKey);
                if (nbtType == null) {
                    return;
                }
                switch (nbtType) {
                    case NBTTagDouble -> variableManager.set(msVariable, player, nbt.getDouble(nbtKey));
                    case NBTTagByte -> variableManager.set(msVariable, player, nbt.getByte(nbtKey));
                    case NBTTagString -> variableManager.set(msVariable, player, nbt.getString(nbtKey));
                    case NBTTagShort -> variableManager.set(msVariable, player, nbt.getShort(nbtKey));
                    case NBTTagInt -> variableManager.set(msVariable, player, nbt.getInteger(nbtKey));
                    case NBTTagFloat -> variableManager.set(msVariable, player, nbt.getFloat(nbtKey));
                    case NBTTagLong -> variableManager.set(msVariable, player, nbt.getLong(nbtKey));
                }
            });
        });
        return Spell.PostCastAction.HANDLE_NORMALLY;
    }
}
