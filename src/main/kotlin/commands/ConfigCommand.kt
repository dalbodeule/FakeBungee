package space.mori.fakebungee.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import space.mori.fakebungee.config.ConfigManager


object ConfigCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("fb.config")) {
            return true
        }

        ConfigManager.load()
        sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Config have been successfully reloaded.")

        if(ConfigManager.Config.debug) {
            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}debug mode activated")
        }

        return true
    }
}
