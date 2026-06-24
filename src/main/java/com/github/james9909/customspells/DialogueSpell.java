package com.github.james9909.customspells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.castmodifiers.ModifierSet;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.util.config.ConfigDataUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.configuration.ConfigurationSection;
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
public class DialogueSpell extends AbstractDialogueSpell {

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
            if (optionSection.contains("text-selected")) {
                option.textSelected = ConfigDataUtil.getString(optionSection.getString("text-selected"));
            }
            option.modifiers = optionSection.getStringList("modifiers");
            option.spellOnClickName = optionSection.getString("spell-on-click");
            option.spellOnSelectName = optionSection.getString("spell-on-select");
            option.spellOnRightClickName = optionSection.getString("spell-on-right-click");
            option.spellOnLeftClickName = optionSection.getString("spell-on-left-click");
            this.options.add(option);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        for (DialogueOption option : this.options) {
            option.spellOnClick = initSub(option.spellOnClickName);
            option.spellOnSelect = initSub(option.spellOnSelectName);
            option.spellOnRightClick = initSub(option.spellOnRightClickName);
            option.spellOnLeftClick = initSub(option.spellOnLeftClickName);
        }
    }

    private Subspell initSub(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return initSubspell(name, "DialogueSpell '" + internalName + "' has an invalid subspell: " + name);
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
    protected DialogueSession createSession(Player player, float power, String[] args, SpellData data) {
        if (this.headings.isEmpty()) {
            MagicSpells.error("DialogueSpell '" + internalName + "' has no headings to show.");
            return null;
        }

        List<DialogueOption> visible = new ArrayList<>();
        for (DialogueOption option : this.options) {
            if (!option.hasAnyAction()) {
                continue;
            }
            if (option.modifierSet != null && !option.modifierSet.check(player)) {
                continue;
            }
            visible.add(option);
        }

        ConfigData<String> heading = this.headings.get(this.random.nextInt(this.headings.size()));
        return new MenuSession(this, player.getUniqueId(), power, args, data, heading, visible);
    }

    // Prints (or re-prints) the dialogue: a screenful of blank lines, the heading, then each option.
    void render(Player player, MenuSession session) {
        clearScreen(player);
        player.sendMessage(line(session.heading, session.data));

        List<DialogueOption> opts = session.options;
        for (int i = 0; i < opts.size(); i++) {
            DialogueOption option = opts.get(i);

            boolean selected = i == session.selected;
            ConfigData<String> textData = selected && option.textSelected != null ? option.textSelected : option.text;

            player.sendMessage(buildOptionComponent(line(textData, session.data), player, session, option));
        }
    }

    void navigate(Player player, MenuSession session, int direction) {
        int size = session.options.size();
        if (size <= 1) {
            return;
        }

        session.selected = Math.floorMod(session.selected + direction, size);
        render(player, session);
        fireSelect(session);
    }

    void triggerOptionClick(MenuSession session, boolean rightClick) {
        DialogueOption option = session.selectedOption();
        if (option == null) {
            return;
        }
        Subspell spell = rightClick ? option.spellOnRightClick : option.spellOnLeftClick;
        if (spell != null) {
            spell.subcast(session.data);
        }
    }

    void fireSelect(MenuSession session) {
        DialogueOption option = session.selectedOption();
        if (option != null && option.spellOnSelect != null) {
            option.spellOnSelect.subcast(session.data);
        }
    }

    private Component buildOptionComponent(Component base, Player player, MenuSession session, DialogueOption option) {
        Subspell spellOnClick = option.spellOnClick;
        if (spellOnClick == null) {
            return base;
        }

        UUID token = session.token;
        UUID playerId = session.playerId;
        float power = session.power;
        String[] args = session.args;

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
}
