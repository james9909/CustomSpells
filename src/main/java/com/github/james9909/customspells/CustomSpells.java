package com.github.james9909.customspells;

import org.bukkit.plugin.java.JavaPlugin;

public class CustomSpells extends JavaPlugin {
    public void onEnable() {
        this.getLogger().info("Loading custom MagicSpell classes");
        this.saveDefaultConfig();
        DialogueManager.setCatchUpLimit(this.getConfig().getInt("dialogue-spell-catch-up-limit", DialogueManager.DEFAULT_CATCH_UP_LIMIT));
        this.getServer().getPluginManager().registerEvents(new DialogueChatListener(), this);
    }
}
