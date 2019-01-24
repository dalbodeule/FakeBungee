package space.mori.fakebungee.player

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import space.mori.fakebungee.region.Region
import space.mori.fakebungee.region.currentRegions
import space.mori.fakebungee.region.event.RegionEnterEvent
import space.mori.fakebungee.region.event.RegionExitEvent
import java.lang.reflect.InvocationTargetException

class ResourcePack constructor(private val plugin: JavaPlugin) {
    val RegionResourcePackMap : MutableMap<Region, ResourcePackType> = mutableMapOf()
    private val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

    internal fun resourcePack() {
        plugin.logger.info("ResourcePack module initializing... success!")
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionEnter(event: RegionEnterEvent) {
        sendResourcePack(event.player)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionExit(event: RegionExitEvent) {
        sendResourcePack(event.player)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        sendResourcePack(event.player)
    }

    private fun getRegionName(player: Player): String {
        val mapName : String? = player.currentRegions.map { it.name }.firstOrNull()

        return when (mapName) {
            null -> "null"
            else -> mapName
        }
    }

    private fun getResourcePack(regionName: String): ResourcePackType {
        return when (regionName) {
            "test" -> ResourcePackType("https://sapixcraft.sapixmedia.com/downloads/sapixcraft-java/files/SapixCraft%20256x%20MC1.12.zip", "BBFF303BC21FEAE056D9FA7CE4BBA3B3347FD85F")
            else -> ResourcePackType("ASDF", "HASH")
        }
    }

    private fun sendResourcePack(player: Player) {
        var rsPacket = PacketContainer(PacketType.Play.Server.RESOURCE_PACK_SEND)
        var userRSP = getResourcePack(getRegionName(player))
        rsPacket.strings.write(0, userRSP.getURL()).write(1, userRSP.getHash())

        try {
            protocolManager.sendServerPacket(player, rsPacket)
            plugin.logger.info("send rs packet for ${player.name}")
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}

class ResourcePackType {
    private val hash: String
    private val url: String

    constructor (url: String, hash: String) {
        this.hash = hash.toLowerCase()
        this.url = url
    }

    fun getURL() : String {
        return url
    }

    fun getHash(): String {
        return hash
    }
}