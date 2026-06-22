package com.github.james9909.customspells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.castmodifiers.ModifierSet;
import com.nisovin.magicspells.events.MagicSpellsGenericPlayerEvent;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DialogueSpell extends InstantSpell {
    private List<String> headings;
    private List<DialogueOption> options;
    private Random random;

    public DialogueSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        this.random = new Random();
        this.headings = getConfigStringList("headings", null);

        ConfigurationSection options = getConfigSection("options");
        if (options == null || options.getKeys(false).isEmpty()) {
            MagicSpells.error("DialogueSpell '" + spellName + "' has no dialogue options.");
            return;
        }

        this.options = new ArrayList<>();
        options.getValues(true).forEach((key, value) -> {
            if (value == null) {
                return;
            }
            if (!(value instanceof ConfigurationSection optionSection)) {
                return;
            }

            DialogueOption dialogueOption = new DialogueOption();
            dialogueOption.text = optionSection.getString("text");
            dialogueOption.modifiers = optionSection.getStringList("modifiers");
            dialogueOption.spellOnClickName = optionSection.getString("spell-on-click");
            this.options.add(dialogueOption);
        });
    }

    protected void initialize() {
        super.initialize();

        for (DialogueOption option : this.options) {
            option.spellOnClick = initSubspell(option.spellOnClickName, "Invalid subspell %s".formatted(option.spellOnClickName));
        }
    }

    protected void initializeModifiers() {
        super.initializeModifiers();

        for (DialogueOption option : this.options) {
            option.modifierSet = new ModifierSet(option.modifiers, this);
        }
    }

    public Spell.PostCastAction castSpell(final LivingEntity caster, final Spell.SpellCastState state, final float power, final String[] args) {
        if (!(caster instanceof Player)) {
            return PostCastAction.HANDLE_NORMALLY;
        }

        Player player = (Player) caster;
        int index = this.random.nextInt(this.headings.size());
        String title = this.headings.get(index);
        List<ActionButton> buttons = new ArrayList<>();
        for (DialogueOption option : this.options) {
            MagicSpellsGenericPlayerEvent event = new MagicSpellsGenericPlayerEvent(player);
            option.modifierSet.apply(event);
            if (!event.isCancelled()) {
                buttons.add(ActionButton.builder(Component.text(ChatColor.translateAlternateColorCodes('&', option.text)))
                        .action(DialogAction.customClick(Key.key("customspells:%s".formatted(option.id)), null))
                        .build());
            }
        }

        Dialog dialog = Dialog.create(builder ->
                builder.empty()
                        .base(DialogBase.builder(Component.text(ChatColor.translateAlternateColorCodes('&', title))).build())
                        .type(DialogType.multiAction(buttons).build())
        );
        caster.sendMessage();
        caster.showDialog(dialog);

        return PostCastAction.HANDLE_NORMALLY;
    }

    @EventHandler
    public void onHandleDialog(PlayerCustomClickEvent event) {
        System.out.println(event.getIdentifier());
    }

    private static class DialogueOption {
        private UUID id;
        private String text;
        private List<String> modifiers;
        private ModifierSet modifierSet;
        private String spellOnClickName;
        private Subspell spellOnClick;
    }

    private class DialogueSpellConversation extends Prompt
}
