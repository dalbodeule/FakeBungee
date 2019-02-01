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
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.region.event.RegionEnterEvent
import space.mori.fakebungee.region.event.RegionExitEvent
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

    private fun getHeader(player: Player, area: String): String {
        return ChatColor.translateAlternateColorCodes(
            '&', "&d{player}, &6{area}, Test Header".replace("{player}", player.displayName).replace("{area}", area)
        )
    }

    private fun getFooter(player: Player, area: String): String {
        return ChatColor.translateAlternateColorCodes(
                '&', "&6Test Footer".replace("{player}", player.displayName).replace("{area}", area)
        )
    }

    private fun makePlayerListHF(player: Player) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, object : Runnable {
            override fun run() {
                val hfPacket = PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER)
                val region: String = RegionManager.getRegionName(player)?.let { it } ?: "null"

                hfPacket.chatComponents.write(
                        0, WrappedChatComponent.fromText(
                        getHeader(player, region)
                    )
                )
                hfPacket.chatComponents.write(
                        1, WrappedChatComponent.fromText(
                        getFooter(player, region)
                    )
                )

                try {
                    protocolManager.sendServerPacket(player, hfPacket)
                    logger.debug("send hf packet for ${player.name}")
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            }
        }, 10L)
    }
}
