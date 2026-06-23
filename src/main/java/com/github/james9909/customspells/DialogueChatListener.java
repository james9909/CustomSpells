package com.github.james9909.customspells;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Holds back chat for players who are mid-dialogue and discards their session on disconnect. The held
// chat is replayed by QuitDialogueSpell (or the session timeout) once the dialogue ends.
public class DialogueChatListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Set<Audience> viewers = event.viewers();

        List<Player> paused = new ArrayList<>();
        for (Audience viewer : viewers) {
            if (viewer instanceof Player player && DialogueManager.hasActiveSession(player.getUniqueId())) {
                paused.add(player);
            }
        }
        if (paused.isEmpty()) {
            return;
        }

        Player source = event.getPlayer();
        Component displayName = source.displayName();
        for (Player player : paused) {
            // Render the line exactly as this viewer would have seen it, then hold it for replay.
            Component rendered = event.renderer().render(source, displayName, event.message(), player);
            DialogueManager.hold(player.getUniqueId(), rendered);
        }
        viewers.removeAll(paused);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Player is gone; drop the session and any messages it was holding.
        DialogueManager.endSession(event.getPlayer().getUniqueId());
    }
}
