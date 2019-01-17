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
import org.bukkit.plugin.java.JavaPlugin
import space.mori.fakebungee.region.currentRegions
import space.mori.fakebungee.region.event.RegionEnterEvent
import space.mori.fakebungee.region.event.RegionExitEvent
import java.lang.reflect.InvocationTargetException

class PlayerList constructor(private val plugin: JavaPlugin) {
    private val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

    internal fun playerList() {
        this.protocolManager.addPacketListener(object :
            PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            override fun onPacketSending(event: PacketEvent) {
                if (event.packetType == PacketType.Play.Server.PLAYER_INFO) {
                    makePlayerListHF(event)
                }
            }
        })
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionEnter(event: RegionEnterEvent) {
        plugin.logger.info("player ${event.player} has enter region ${event.region}")
        makePlayerListHF(event.player)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal  fun onRegionExit(event: RegionExitEvent) {
        plugin.logger.info("player ${event.player} has exit region ${event.region}")
        makePlayerListHF(event.player)
    }

    internal fun makePlayerListHF(event: PacketEvent) {
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

    private fun getRegionName(player: Player) : String {
        val mapName : String? = player.currentRegions.map { it.name }.lastOrNull()

        return when (mapName) {
            null -> "null"
            else -> mapName
        }
    }

    private fun makePlayerListHF(player: Player) {
        val hfPacket = PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER)

        hfPacket.chatComponents.write(
            0, WrappedChatComponent.fromText(
                getHeader(player, getRegionName(player))
            )
        )
        hfPacket.chatComponents.write(
                1, WrappedChatComponent.fromText(
                    getFooter(player, getRegionName(player))
            )
        )

        try {
            protocolManager.sendServerPacket(player, hfPacket)
            plugin.logger.info("send packet for ${player.displayName}")
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}
