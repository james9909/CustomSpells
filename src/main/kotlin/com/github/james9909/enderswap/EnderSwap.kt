package com.github.james9909.enderswap

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class EnderSwap() : JavaPlugin() {
    override fun onEnable() {
        getCommand("enderswap")!!.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            return true
        }

        if (args.isEmpty() || args.size > 1) {
            return true
        }

        val row = args[0].toIntOrNull()
        if (row == null || row < 1 || row > 3) {
            return true
        }

        val hotbar = sender.inventory
        val enderChest = sender.enderChest
        val enderChestStartIndex = (row - 1) * 9

        for (i in 0..8) {
            val tmp = hotbar.getItem(i)
            hotbar.setItem(i, enderChest.getItem(enderChestStartIndex + i))
            enderChest.setItem(enderChestStartIndex + i, tmp)
        }

        sender.updateInventory()
        return true
    }
}
