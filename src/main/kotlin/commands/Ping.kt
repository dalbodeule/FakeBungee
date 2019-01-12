package space.mori.fakebungee.commands

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
                sender.sendMessage("Pong ${sender.displayName}!")
            } else {
                sender.sendMessage("You don't have permissions")
            }
        }
        return true
    }
}