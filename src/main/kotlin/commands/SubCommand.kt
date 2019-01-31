package space.mori.fakebungee.commands

import org.bukkit.command.CommandSender

interface SubCommand {
    val name: String
    val parameter: String
    val description: String
    fun execute(sender: CommandSender, args: List<String>)
}