package space.mori.fakebungee.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import space.mori.fakebungee.header.HeaderManager

object HeaderCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("fb.fheader")) {
            return true
        }

        if (args.isEmpty() || args[0] == "help") {
            sender.sendMessage(" :: ${ChatColor.GOLD}Header Command")
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
        override val parameter: String = "create <asset's name> <content(allow spaces, newlines)>"
        override val description: String =
            "Create a header which named the supplied name parameter based"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the parameters spaces are not allowed without content.")
                return
            }

            val name = args[0]

            if (name in HeaderManager.headerMap) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Header $name is exists, Please use another name.")
                return
            }

            HeaderManager.headerMap[name] = args.drop(1).joinToString("").replace("\\n", "\n")
            HeaderManager.save()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Header `$name` has successfully added.")
        }
    }, object : SubCommand {
        override val name = "list"
        override val parameter: String = "list"
        override val description: String = "Show the list of created Headers"

        override fun execute(sender: CommandSender, args: List<String>) {
            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}The Headers of server:")
            HeaderManager.headerMap.keys.map { " ${ChatColor.GREEN}* ${ChatColor.WHITE} $it" }.toTypedArray().forEach {
                sender.sendMessage(it)
            }
        }
    }, object : SubCommand {
        override val name = "view"
        override val parameter = "view <asset's name>"
        override val description = "View a header which named the supplied name parameter based"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the name of Footer, spaces are not allowed.")
                return
            }

            val name = args[0]

            if (name !in HeaderManager.headerMap) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Footer $name is not exists, Please use another name.")
                return
            } else {
                sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Header name: `$name`")
                HeaderManager.headerMap[name]?.let { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', it)) }
            }
        }
    }, object : SubCommand {
        override val name = "edit"
        override val parameter: String = "edit <asset's name> <content(allow spaces, newlines)>"
        override val description: String = "Edit a header which named the supplied name parameter based"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the name of Header, spaces are not allowed.")
                return
            }

            val name = args[0]

            if (name !in HeaderManager.headerMap) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Header $name is not exists, Please use another name.")
                return
            }

            HeaderManager.headerMap[name] = args.drop(1).joinToString("").replace("\\n", "\n")
            HeaderManager.save()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Header `$name` has successfully edited.")
        }
    }, object : SubCommand {
        override val name = "delete"
        override val parameter: String = "delete <asset's name>"
        override val description: String = "Delete a Header which named the supplied name parameter based"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the name of Header, spaces are not allowed.")
                return
            }

            val name = args[0]

            if (name == "default") {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You can not delete the default Header.")
                return
            }

            if (name !in HeaderManager.headerMap) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Header $name is not exists, Please use another name.")
                return
            }

            HeaderManager.headerMap.remove(name)
            HeaderManager.save()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Header `$name` has successfully deleted.")
        }
    }, object : SubCommand {
        override val name = "reload"
        override val parameter: String = "reload"
        override val description: String = "Reload the Header settings."

        override fun execute(sender: CommandSender, args: List<String>) {
            HeaderManager.load()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Header settings have been successfully reloaded.")
        }
    }).associateBy { it.name }
}
