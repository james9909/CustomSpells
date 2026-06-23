package com.github.james9909.customspells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.castmodifiers.ModifierSet;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.util.config.ConfigDataUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Prints a randomly-chosen heading followed by a list of clickable options to chat. Clicking an
 * option casts its configured spell.
 *
 * Dialogue1:
 *     spell-class: ".DialogueSpell"
 *     headings:
 *         - "&amp;fJames: Hello1"
 *         - "&amp;fJames: Hello2"
 *     options:
 *         option1:
 *             text: "Hello World!"
 *             modifiers:
 *                 - sneaking required
 *             spell-on-click: Dialogue2
 *         option2:
 *             text: "Goodbye."
 *             spell-on-click: Dialogue3
 */
public class DialogueSpell extends InstantSpell {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    // Headings and option text are stored as ConfigData so they resolve MagicSpells variables (global
    // and per-caster) at cast time, while still going through the legacy '&' color-code serializer.
    private final List<ConfigData<String>> headings = new ArrayList<>();
    private final List<DialogueOption> options = new ArrayList<>();
    private final String strExpired;
    private final Random random = new Random();

    public DialogueSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        for (String heading : getConfigStringList("headings", new ArrayList<>())) {
            this.headings.add(ConfigDataUtil.getString(heading));
        }
        this.strExpired = getConfigString("str-expired", "That dialogue is no longer available.");

        ConfigurationSection optionsSection = getConfigSection("options");
        if (optionsSection == null || optionsSection.getKeys(false).isEmpty()) {
            MagicSpells.error("DialogueSpell '" + spellName + "' has no dialogue options.");
            return;
        }

        for (String key : optionsSection.getKeys(false)) {
            ConfigurationSection optionSection = optionsSection.getConfigurationSection(key);
            if (optionSection == null) {
                continue;
            }

            DialogueOption option = new DialogueOption();
            option.text = ConfigDataUtil.getString(optionSection.getString("text", ""));
            option.modifiers = optionSection.getStringList("modifiers");
            option.spellOnClickName = optionSection.getString("spell-on-click");
            this.options.add(option);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        for (DialogueOption option : this.options) {
            option.spellOnClick = initSubspell(
                    option.spellOnClickName,
                    "DialogueSpell '" + internalName + "' has an invalid spell-on-click: " + option.spellOnClickName
            );
        }
    }

    @Override
    protected void initializeModifiers() {
        super.initializeModifiers();

        for (DialogueOption option : this.options) {
            if (option.modifiers != null && !option.modifiers.isEmpty()) {
                option.modifierSet = new ModifierSet(option.modifiers, this);
            }
        }
    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state != SpellCastState.NORMAL || !(caster instanceof Player player)) {
            return PostCastAction.HANDLE_NORMALLY;
        }
        if (this.headings.isEmpty()) {
            MagicSpells.error("DialogueSpell '" + internalName + "' has no headings to show.");
            return PostCastAction.HANDLE_NORMALLY;
        }

	// Invalidate any old dialogues.
        UUID token = DialogueManager.startSession(player.getUniqueId());

        SpellData data = new SpellData(player, power, args);

        String heading = this.headings.get(this.random.nextInt(this.headings.size())).get(data);
        player.sendMessage(LEGACY.deserialize(heading));

        for (DialogueOption option : this.options) {
            if (option.spellOnClick == null) {
                continue;
            }
            if (option.modifierSet != null && !option.modifierSet.check(player)) {
                continue;
            }
            Component base = LEGACY.deserialize(option.text.get(data));
            player.sendMessage(buildOptionComponent(base, player, token, option, power, args));
        }

        return PostCastAction.HANDLE_NORMALLY;
    }

    private Component buildOptionComponent(Component base, Player player, UUID token, DialogueOption option, float power, String[] args) {
        Subspell spellOnClick = option.spellOnClick;
        UUID playerId = player.getUniqueId();

        ClickCallback<net.kyori.adventure.audience.Audience> callback = audience -> {
            // Re-resolve the player: the click arrives later and the cached reference may be stale.
            Player clicker = player.getServer().getPlayer(playerId);
            if (clicker == null) {
                return;
            }
            if (!DialogueManager.isActive(playerId, token)) {
                if (!strExpired.isEmpty()) {
                    sendMessage(clicker, strExpired);
                }
                return;
            }
            spellOnClick.subcast(new SpellData(clicker, power, args));
        };

        ClickEvent clickEvent = ClickEvent.callback(
                callback,
                ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build()
        );

        return base.clickEvent(clickEvent);
    }

    private static class DialogueOption {
        private ConfigData<String> text;
        private List<String> modifiers;
        private ModifierSet modifierSet;
        private String spellOnClickName;
        private Subspell spellOnClick;
    }
}
