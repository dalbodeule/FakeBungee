package space.mori.fakebungee.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import space.mori.fakebungee.resourcepack.ResourcePack
import space.mori.fakebungee.resourcepack.ResourcePackManager as RPM

object ResourceCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("fb.fresource")) {
            return true
        }

        if (args.isEmpty() || args[0] == "help") {
            sender.sendMessage(" :: ${ChatColor.GOLD}Resource Command")
            for (subCommand in subCommands.values) {
                sender.sendMessage(" ${ChatColor.GREEN}* ${ChatColor.WHITE}/$label ${subCommand.parameter} ${ChatColor.DARK_GRAY}- ${ChatColor.GRAY}${subCommand.description}")
            }
            return true
        }

        val subCommand = subCommands[args[0]]

        if (subCommand == null) {
            sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Unknown subcommand. Type ${ChatColor.YELLOW}/$label help${ChatColor.RESET} for help.")
            return true
        }

        subCommand.execute(sender, args.drop(1))

        return true
    }

    private val subCommands = listOf(object : SubCommand {
        override val name = "create"
        override val parameter: String = "create <asset's name> <url> <hash>"
        override val description: String =
            "Create a resource which named the supplied name parameter based"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the parameters spaces are not allowed.")
                return
            }

            val name = args[0]

            if (name in RPM.resourcePackMap) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Resource $name is exists, Please use another name.")
                return
            }

            RPM.resourcePackMap[name] = ResourcePack(args[1], args[2].toLowerCase())
            RPM.save()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Resource `$name` has successfully added.")
        }
    }, object : SubCommand {
        override val name = "list"
        override val parameter: String = "list"
        override val description: String = "Show the list of created resources"

        override fun execute(sender: CommandSender, args: List<String>) {
            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}The resources of server:")
            sender.sendMessage(RPM.resourcePackMap.keys.map { " ${ChatColor.GREEN}* ${ChatColor.WHITE} $it" }.toTypedArray())
        }
    }, object : SubCommand {
        override val name = "delete"
        override val parameter: String = "delete <asset's name>"
        override val description: String = "Delete a resource which named the supplied name parameter based"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the name of resource, spaces are not allowed.")
                return
            }

            val name = args[0]

            if (name == "default") {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You can not delete the default Resource.")
                return
            }

            if (name !in RPM.resourcePackMap) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Resource $name is not exists, Please use another name.")
                return
            }

            RPM.resourcePackMap.remove(name)
            RPM.save()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Resource `$name` has successfully deleted.")
        }
    }, object : SubCommand {
        override val name = "reload"
        override val parameter: String = "reload"
        override val description: String = "Reload the Resource settings."

        override fun execute(sender: CommandSender, args: List<String>) {
            RPM.load()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Resource settings have been successfully reloaded.")
        }
    }).associateBy { it.name }
}
