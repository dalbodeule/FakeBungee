package space.mori.fakebungee.player

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.region.currentRegions
import space.mori.fakebungee.region.event.RegionEnterEvent
import space.mori.fakebungee.region.event.RegionExitEvent
import space.mori.fakebungee.resourcepack.ResourcePack
import space.mori.fakebungee.resourcepack.ResourcePackManager.playerResourcePackMap
import space.mori.fakebungee.resourcepack.ResourcePackManager.regionResourcePackMap
import java.lang.reflect.InvocationTargetException

class ResourcePack (private val plugin: JavaPlugin) : Listener {
    private val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

    internal fun resourcePack() {
        plugin.logger.info("ResourcePack module initializing... success!")
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionEnter(event: RegionEnterEvent) {
        sendResourcePack(event.player, false)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionExit(event: RegionExitEvent) {
        sendResourcePack(event.player, true)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        sendResourcePack(event.player, false)
    }

    private fun getResourcePack(regionName: String?): ResourcePack {
        if (regionName == null) {
            return regionResourcePackMap["default"]!!
        } else if (regionResourcePackMap[regionName] == null) {
            return regionResourcePackMap["default"]!!
        } else {
            return regionResourcePackMap[regionName]!!
        }
    }

    private fun sendResourcePack(player: Player, onExit: Boolean) {
        val regionName = RegionManager.getRegionName(player)
        if (onExit && regionName != null) return

        val rsPacket = PacketContainer(PacketType.Play.Server.RESOURCE_PACK_SEND)
        val userRSP = getResourcePack(regionName)

        plugin.logger.info("before: ${playerResourcePackMap[player].toString()}")
        plugin.logger.info("after: ${userRSP.toString()}")

        if (
            playerResourcePackMap[player] == null ||
            playerResourcePackMap[player]!!.getURL() != userRSP.getURL()
        ) {
            rsPacket.strings.write(0, userRSP.getURL()).write(1, userRSP.getHash())
            playerResourcePackMap[player] = userRSP

            try {
                protocolManager.sendServerPacket(player, rsPacket)
                plugin.logger.info("send rs packet for ${player.name}")
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }
    }
}