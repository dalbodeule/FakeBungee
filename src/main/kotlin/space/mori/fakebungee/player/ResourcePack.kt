package space.mori.fakebungee.player

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import space.mori.fakebungee.config.ConfigManager
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.region.event.RegionEnterEvent
import space.mori.fakebungee.region.event.RegionExitEvent
import space.mori.fakebungee.region.resourcepack
import space.mori.fakebungee.resourcepack.ResourcePack
import space.mori.fakebungee.resourcepack.ResourcePackManager.playerResourcePackMap
import space.mori.fakebungee.resourcepack.ResourcePackManager.resourcePackMap
import space.mori.fakebungee.util.Logger
import java.lang.reflect.InvocationTargetException

class ResourcePack (private val plugin: JavaPlugin, private val logger: Logger) : Listener {
    private val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

    internal fun resourcePack() {
        logger.info("ResourcePack module initializing... success!")
        if (ConfigManager.Config.resourcePackEnabled) {
            logger.info("ResourcePack module is ENABLED")
        } else {
            logger.info("ResourcePack module is DISABLED")
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionEnter(event: RegionEnterEvent) {
        if (ConfigManager.Config.resourcePackEnabled) {
            sendResourcePack(event.player, false)
        } else return
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionExit(event: RegionExitEvent) {
        if (ConfigManager.Config.resourcePackEnabled) {
            sendResourcePack(event.player, true)
        } else return
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onPlayerJoin(event: PlayerJoinEvent) {
        if (ConfigManager.Config.resourcePackEnabled) {
            sendResourcePack(event.player, true)
        } else return
    }

    private fun getResourcePack(regionName: String?): ResourcePack {
        val resource = RegionManager.regions[regionName].let { region ->
            return@let region?.resourcepack ?: "default"
        }

        logger.debug("region $regionName's resource pack is $resource")
        return resourcePackMap[resource]!!
    }

    private fun sendResourcePack(player: Player, onExit: Boolean) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, Runnable {
            val regionName = RegionManager.getRegionName(player)

            if (onExit && regionName != null) return@Runnable

            val userRSP = getResourcePack(regionName)

            if (
                playerResourcePackMap[player] == null ||
                playerResourcePackMap[player]?.getURL() != userRSP.getURL()
            ) {
                val rsPacket = PacketContainer(PacketType.Play.Server.RESOURCE_PACK_SEND)

                logger.debug("region: $regionName, resource: ${userRSP.getURL()}")

                rsPacket.strings.write(0, userRSP.getURL()).write(1, userRSP.getHash())
                playerResourcePackMap[player] = userRSP

                player.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}After a while the new resource pack will be applied.")
                plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
                    try {
                        protocolManager.sendServerPacket(player, rsPacket)

                        if (ConfigManager.Config.resourcePackMessage) {
                            player.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}A new resource pack has been applied.")
                            if (player.hasPermission("fb.resource")) {
                                player.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}If the resource pack does not work properly, use command.")
                                player.sendMessage( run {
                                    val text = TextComponent("/resource")
                                    text.color = net.md_5.bungee.api.ChatColor.UNDERLINE
                                    text.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/resource")
                                    text.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(run {
                                        val text1 = TextComponent("Click on the message to execute the command.")
                                        text1.color = net.md_5.bungee.api.ChatColor.GREEN

                                        return@run text1
                                    }))

                                    return@run text.toPlainText()
                                })
                            }
                        }
                        logger.debug("send rs packet for ${player.name}")
                    } catch (e: InvocationTargetException) {
                        e.printStackTrace()
                    }
                }, 20L*5)
            }
        }, 20L)
    }
}