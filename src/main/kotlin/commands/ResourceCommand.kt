package space.mori.fakebungee.commands

import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.resourcepack.ResourcePack
import space.mori.fakebungee.resourcepack.ResourcePackManager

object ResourceCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("fb.resource")) {
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
        override val parameter: String = "create <url> <hash>"
        override val description: String =
            "Create a region which named the supplied name parameter based on the selection of WorldEdit"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the name of region, spaces are not allowed.")
                return
            }

            val name = args[0]

            if (name !in RegionManager.regions) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Region $name is not exists, Please use another name.")
                return
            }

            ResourcePackManager.regionResourcePackMap[name] = ResourcePack(args[1], args[2])
            ResourcePackManager.save()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Region `$name` has successfully added.")
        }
    }, object : SubCommand {
        override val name = "list"
        override val parameter: String = "list"
        override val description: String = "Show the list of created regions"

        override fun execute(sender: CommandSender, args: List<String>) {
            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}The regions of server:")
            sender.sendMessage(ResourcePackManager.regionResourcePackMap.keys.map { " ${ChatColor.GREEN}* ${ChatColor.WHITE} $it" }.toTypedArray())
        }
    }, object : SubCommand {
        override val name = "delete"
        override val parameter: String = "delete <name>"
        override val description: String = "Delete a region which named the supplied name parameter based"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the name of region, spaces are not allowed.")
                return
            }

            val name = args[0]

            if (name !in ResourcePackManager.regionResourcePackMap) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Region $name is not exists, Please use another name.")
                return
            }

            ResourcePackManager.regionResourcePackMap.remove(name)
            ResourcePackManager.save()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Region `$name` has successfully deleted.")
        }
    }, object : SubCommand {
        override val name = "reload"
        override val parameter: String = "reload"
        override val description: String = "Reload the Region settings."

        override fun execute(sender: CommandSender, args: List<String>) {
            RegionManager.load()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Region settings have been successfully reloaded.")
        }
    }).associateBy { it.name }
}
