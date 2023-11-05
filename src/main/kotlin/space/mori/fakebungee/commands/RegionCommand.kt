package space.mori.fakebungee.commands

import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.mori.fakebungee.footer.FooterManager
import space.mori.fakebungee.header.HeaderManager
import space.mori.fakebungee.region.*
import space.mori.fakebungee.regiondata.RegionDataManager
import space.mori.fakebungee.resourcepack.ResourcePackManager
import space.mori.fakebungee.util.bukkit
import space.mori.fakebungee.util.session
import space.mori.fakebungee.util.worldEdit

object RegionCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("fb.fregion")) {
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
        override val parameter: String = "create <region's name>"
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

            if (name == "null") {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Region null is not allowed, Please use another name.")
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
            RegionManager.regions.values.map { " ${ChatColor.GREEN}* ${ChatColor.WHITE}${it.name}" }.toTypedArray().forEach {
                sender.sendMessage(it)
            }

        }
    }, object : SubCommand {
        override val name = "delete"
        override val parameter: String = "delete <region's name>"
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
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Region $name is not exists, Please use another name.")
                return
            }

            RegionManager.regions.remove(name)
            RegionManager.save()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Region `$name` has successfully deleted.")
        }
    }, object : SubCommand {
        override val name = "reload"
        override val parameter: String = "reload"
        override val description: String = "Reload the Region settings."

        override fun execute(sender: CommandSender, args: List<String>) {
            RegionManager.load()
            RegionDataManager.load()

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Region settings have been successfully reloaded.")
        }
    }, object : SubCommand {
        override val name = "debug"
        override val parameter: String = "debug"
        override val description: String = "debugging command"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (sender !is Player) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You can't run this command because you are not player.")
                return
            }

            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}The regions of your:")
            sender.currentRegions.map { " ${ChatColor.GREEN}* ${ChatColor.WHITE}${it.name}" }.toTypedArray().forEach {
                sender.sendMessage(it)
            }
        }
    }, object : SubCommand {
        override val name = "option"
        override val parameter: String = "option <region's name> <header|footer|resource> <asset's name, if blank set default>"
        override val description: String = "region option command"

        override fun execute(sender: CommandSender, args: List<String>) {
            if (args.isEmpty()) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You must write the name of region, spaces are not allowed.")
                return
            }

            val regionName = args[0]
            val assetType = args[1]
            val assetName = args[2].let { name:String? -> return@let name ?: "default" }

            if (regionName !in RegionManager.regions) {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Region $regionName is not exists, Please use another name.")
                return
            }

            if (assetType != "header" && assetType != "footer" && assetType != "resource") {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Asset Type $assetType is an unspecified type, Please use another type.")
                return
            }

            if (when(assetType) {
                    "header" -> HeaderManager.headerMap[assetName]
                    "footer" -> FooterManager.footerMap[assetName]
                    "resource" -> {
                        ResourcePackManager.resourcePackMap[assetName] as Any
                    }
                    else -> null
                } == null) {

                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Asset $assetName is not exists, Please use another name.")
                return
            }

            if (regionName == "null") {
                sender.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}Region null is not allowed, Please use another name.")
            }

            when (assetType) {
                "header" -> RegionManager.regions[regionName]!!.header = assetName
                "footer" -> RegionManager.regions[regionName]!!.footer = assetName
                "resource" -> RegionManager.regions[regionName]!!.resourcepack = assetName
            }

            RegionManager.save()
            RegionDataManager.save()
            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}Region `$regionName` has `$assetName` in the `$assetType` option.")
        }
    }).associateBy { it.name }
}
