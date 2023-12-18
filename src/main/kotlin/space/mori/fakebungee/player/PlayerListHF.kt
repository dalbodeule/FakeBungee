package space.mori.fakebungee.player

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import space.mori.fakebungee.footer.FooterManager
import space.mori.fakebungee.header.HeaderManager
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.region.event.RegionEnterEvent
import space.mori.fakebungee.region.event.RegionExitEvent
import space.mori.fakebungee.region.footer
import space.mori.fakebungee.region.header
import space.mori.fakebungee.util.Logger
import java.lang.reflect.InvocationTargetException

class PlayerListHF (private val plugin: JavaPlugin, private val logger: Logger) : Listener {
    private val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

    internal fun playerList() {
        this.protocolManager.addPacketListener(object :
            PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            override fun onPacketSending(event: PacketEvent) {
                if (event.packetType == PacketType.Play.Server.PLAYER_INFO) {
                    makePlayerList(event)
                }
            }
        })

        logger.info("PlayerListHF module initializing... success!")
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionEnter(event: RegionEnterEvent) {
        makePlayerListHF(event.player)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionExit(event: RegionExitEvent) {
        makePlayerListHF(event.player)
    }

    private fun makePlayerList(event: PacketEvent) {
        makePlayerListHF(event.player)
    }

    private fun getHeader(player: Player, regionName: String): String {
        val header = RegionManager.regions[regionName].let { region ->
            return@let region?.header ?: "default"
        }

        val content = HeaderManager.headerMap[header]?.let {
            return@let ChatColor.translateAlternateColorCodes('&',
                    it.replace("{player}", player.displayName)
                            .replace("{area}", regionName)
            )
        } ?: ""

        logger.debug("region $regionName's header is $header")
        content.let { it.split("\n").map{ line -> logger.debug(line) } }


        return content.replace("{player}", player.displayName).replace("{area}", regionName)
    }

    private fun getFooter(player: Player, regionName: String): String {
        val footer = RegionManager.regions[regionName].let { region ->
            return@let region?.footer ?: "default"
        }

        val content = FooterManager.footerMap[footer]?.let {
            return@let ChatColor.translateAlternateColorCodes('&',
                it.replace("{player}", player.displayName)
                .replace("{area}", regionName)
            )
        } ?: ""

        logger.debug("region $regionName's footer is $footer")
        content.let { it.split("\n").map{ line -> logger.debug(line) } }

        return content
    }

    private fun makePlayerListHF(player: Player) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
            val hfPacket = PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER)
            val regionName: String = RegionManager.getRegionName(player) ?: "default"

            hfPacket.chatComponents.write(
                    0, WrappedChatComponent.fromText(
                    getHeader(player, regionName)
                )
            )
            hfPacket.chatComponents.write(
                    1, WrappedChatComponent.fromText(
                    getFooter(player, regionName)
                )
            )

            try {
                protocolManager.sendServerPacket(player, hfPacket)
                logger.debug("send hf packet for ${player.name}")
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }, 10L)
    }
}
