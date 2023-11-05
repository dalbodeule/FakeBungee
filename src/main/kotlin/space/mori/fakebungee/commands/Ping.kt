package space.mori.fakebungee.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Ping: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("pong!")
        } else {
            if (sender.hasPermission("fb.ping")) {
                sender.sendMessage(
                    "${ChatColor.GREEN}[!] ${ChatColor.WHITE}Pong ${sender.displayName}${ChatColor.WHITE}, you'r ping is ${sender.ping}!"
                )
            } else {
                sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}You don't have permissions")
            }
        }
        return true
    }
}