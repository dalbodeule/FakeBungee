package space.mori.fakebungee.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import space.mori.fakebungee.footer.FooterManager

object FooterCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("fb.ffooter")) {
            return true
        }

        if (args.isEmpty() || args[0] == "help") {
            sender.sendMessage(" :: ${ChatColor.GOLD}Footer Command")
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
        override val parameter: String = "create <asset's name> <content(allow spaces)>"
        override val description: String =
            "Create a Footer which named the supplied name parameter based"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the parameters spaces are not allowed without content.")
                return
            }

            val name = args[0]

            if (name in FooterManager.footerMap) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Footer $name is exists, Please use another name.")
                return
            }

            FooterManager.footerMap[name] = args.drop(1).joinToString("")
            FooterManager.save()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Footer `$name` has successfully added.")
        }
    }, object : SubCommand {
        override val name = "list"
        override val parameter: String = "list"
        override val description: String = "Show the list of created Footers"

        override fun execute(sender: CommandSender, args: List<String>) {
            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}The Footers of server:")
            sender.sendMessage(FooterManager.footerMap.keys.map { " ${ChatColor.GREEN}* ${ChatColor.WHITE} $it" }.toTypedArray())
        }
    }, object : SubCommand {
        override val name = "delete"
        override val parameter: String = "delete <asset's name>"
        override val description: String = "Delete a Footer which named the supplied name parameter based"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the name of footer, spaces are not allowed.")
                return
            }

            val name = args[0]

            if (name == "default") {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You can not delete the default Footer.")
                return
            }

            if (name !in FooterManager.footerMap) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Footer $name is not exists, Please use another name.")
                return
            }

            FooterManager.footerMap.remove(name)
            FooterManager.save()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Footer `$name` has successfully deleted.")
        }
    }, object : SubCommand {
        override val name = "reload"
        override val parameter: String = "reload"
        override val description: String = "Reload the Footer settings."

        override fun execute(sender: CommandSender, args: List<String>) {
            FooterManager.load()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Footer settings have been successfully reloaded.")
        }
    }).associateBy { it.name }
}
