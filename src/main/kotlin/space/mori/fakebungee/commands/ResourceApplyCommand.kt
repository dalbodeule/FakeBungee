package space.mori.fakebungee.commands

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import space.mori.fakebungee.config.ConfigManager
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.region.resourcepack
import space.mori.fakebungee.resourcepack.ResourcePack
import space.mori.fakebungee.resourcepack.ResourcePackManager
import space.mori.fakebungee.util.Logger
import java.lang.reflect.InvocationTargetException
import space.mori.fakebungee.FakeBungee.Companion.instance as plugin

object ResourceApplyCommand : CommandExecutor, TabCompleter {
    private val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()
    private val logger = Logger(plugin)

    override fun onCommand(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("fb.resource")) {
            return true
        }

        if(ConfigManager.Config.resourcePackEnabled) {
            this.sendResourcePack(sender as Player)
        } else {
            sender.sendMessage("${ChatColor.GREEN}[!] ${ChatColor.WHITE}debug mode activated")
        }

        return true
    }

    private fun getResourcePack(regionName: String?): ResourcePack {
        val resource = RegionManager.regions[regionName].let { region ->
            return@let region?.resourcepack ?: "default"
        }

        logger.debug("region $regionName's resource pack is $resource")
        return ResourcePackManager.resourcePackMap[resource]!!
    }

    private fun sendResourcePack(player: Player) {
        val regionName = RegionManager.getRegionName(player)
        val userRSP = getResourcePack(regionName)
        val rsPacket = PacketContainer(PacketType.Play.Server.RESOURCE_PACK_SEND)

        rsPacket.strings.write(0, userRSP.getURL()).write(1, userRSP.getHash())
        ResourcePackManager.playerResourcePackMap[player] = userRSP

        player.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}After a while the new resource pack will be applied.")
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
            try {
                protocolManager.sendServerPacket(player, rsPacket)

                if (ConfigManager.Config.resourcePackMessage) {
                    player.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}A new resource pack has been applied.")

                    if (player.hasPermission("fb.resource")) {
                        player.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}If you try to apply the resource pack again, enter the command.")
                        player.spigot().sendMessage( run {
                            val text = TextComponent("/resource")
                            text.color = net.md_5.bungee.api.ChatColor.UNDERLINE
                            text.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/resource")
                            text.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(run {
                                val text = TextComponent("Click on the message to execute the command.")
                                text.color = net.md_5.bungee.api.ChatColor.GREEN

                                return@run text
                            }))

                            return@run text
                        })
                    }
                }
                logger.debug("send rs packet for ${player.name}")
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }, 20L*5)
    }

    override fun onTabComplete(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): MutableList<String> = mutableListOf()
}
