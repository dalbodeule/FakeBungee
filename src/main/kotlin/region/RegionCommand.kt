package space.mori.fakebungee.region

import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.mori.fakebungee.region.impl.Region
import space.mori.fakebungee.util.bukkit
import space.mori.fakebungee.util.session
import space.mori.fakebungee.util.worldEdit

private interface SubCommand {
    val name: String
    val parameter: String
    val description: String
    fun execute(sender: CommandSender, args: List<String>)
}

object RegionCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("fb.region")) {
            return true
        }

        if (args.isEmpty() || args[0] == "help") {
            sender.sendMessage(" :: ${ChatColor.GOLD}Region Command")
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
        override val parameter: String = "create <name>"
        override val description: String =
            "Create a region which named the supplied name parameter based on the selection of WorldEdit"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (sender !is Player) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You can't run this command because you are not player.")
                return
            }

            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the name of region, spaces are not allowed.")
                return
            }

            val name = args[0]

            if (name in RegionManager.regions) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Region $name is already exists, Please use another name.")
                return
            }

            if (!sender.session.isSelectionDefined(sender.world.worldEdit)) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must run this command after you selected area")
                return
            }

            val selection = sender.session.getSelection(sender.world.worldEdit)

            if (selection !is CuboidRegion) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Your selection must be a cuboid region")
                return
            }

            val newRegion = Region(
                name, selection.minimumPoint.bukkit, selection.maximumPoint.bukkit, selection.world!!.name
            )

            RegionManager.regions[name] = newRegion
            RegionManager.save()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Region `$name` has successfully added.")
        }
    }, object : SubCommand {
        override val name = "list"
        override val parameter: String = "list"
        override val description: String = "Show the list of created regions"

        override fun execute(sender: CommandSender, args: List<String>) {
            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}The regions of server:")
            sender.sendMessage(RegionManager.regions.values.map { " ${ChatColor.GREEN}* ${ChatColor.WHITE}${it.name}" }.toTypedArray())
        }
    }, object : SubCommand {
        override val name = "delete"
        override val parameter : String = "delete <name>"
        override val description: String = "Delete a region which named the supplied name parameter based"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (sender !is Player) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You can't run this command because you are not player.")
                return
            }

            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the name of region, spaces are not allowed.")
                return
            }

            val name = args[0]

            if (name !in RegionManager.regions) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Region $name is already exists, Please use another name.")
                return
            }

            RegionManager.regions.remove(name)
            RegionManager.save()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Region `$name` has successfully deleted.")
        }
    }, object : SubCommand {
        override val name = "reload"
        override val parameter : String = "reload"
        override val description: String = "Reload the Region settings."

        override fun execute(sender: CommandSender, args: List<String>) {
            RegionManager.load()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Region settings have been successfully reloaded.")
        }
    }).associateBy { it.name }
}
