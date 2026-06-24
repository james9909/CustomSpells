package com.github.james9909.customspells;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;

public class DialogueInputListener implements Listener {

    @EventHandler
    public void handlePlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        DialogueSession session = DialogueManager.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        int previousSlot = event.getPreviousSlot();
        int newSlot = event.getNewSlot();

        int direction;
        if (newSlot == previousSlot) {
            return;
        } else if (previousSlot == 8 && newSlot == 0) {
            direction = 1;
        } else if (previousSlot == 0 && newSlot == 8) {
            direction = -1;
        } else if (newSlot > previousSlot) {
            direction = 1;
        } else {
            direction = -1;
        }

        session.scroll(player, direction);
    }

    @EventHandler
    public void handlePlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        DialogueSession session = DialogueManager.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            // Ignore offhand events.
            return;
        }

        Action action = event.getAction();
        boolean right = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        boolean left = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        if (!right && !left) {
            return;
        }

        session.click(player, right);
    }
}
