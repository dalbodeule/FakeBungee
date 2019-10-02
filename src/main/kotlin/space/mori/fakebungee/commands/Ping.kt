package space.mori.fakebungee.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.mori.fakebungee.util.Ping

object Ping: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("pong!")
        } else {
            if (sender.hasPermission("fb.ping")) {
                sender.sendMessage(
                    "${ChatColor.GREEN}[!] ${ChatColor.WHITE}Pong ${sender.displayName}${ChatColor.WHITE}, you'r ping is ${Ping().get(sender)}!"
                )
            } else {
                sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}You don't have permissions")
            }
        }
        return true
    }
}