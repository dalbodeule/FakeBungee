package space.mori.fakebungee.player

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedGameProfile
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.plugin.java.JavaPlugin
import space.mori.fakebungee.region.RegionManager
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
                if (event.packet.playerInfoAction.read(0) == EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
                    makePlayerListUL(event)
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
    internal fun onRegionExit(event: RegionExitEvent) {
        plugin.logger.info("player ${event.player} has exit region ${event.region}")
        makePlayerListHF(event.player)
    }

    internal fun makePlayerListHF(event: PacketEvent) {
        makePlayerListHF(event.player)
    }

    internal fun makePlayerListUL(event: PacketEvent) {
        makePlayerListUL(event.player)
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

    private fun getRegionName(player: Player): String {
        val mapName : String? = player.currentRegions.map { it.name }.firstOrNull()

        return when (mapName) {
            null -> "null"
            else -> mapName
        }
    }

    private fun getPlayerInfodata(player: Player): PlayerInfoData {
        return PlayerInfoData(
            WrappedGameProfile.fromPlayer(player),
            0,
            EnumWrappers.NativeGameMode.fromBukkit(player.gameMode),
            WrappedChatComponent.fromText(player.displayName)
        )
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
            plugin.logger.info("send hf packet for ${player.name}")
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    private fun makePlayerListUL(player: Player) {
        val ulPacket = PacketContainer(PacketType.Play.Server.PLAYER_INFO)
        val playerInfoDataList = ArrayList<PlayerInfoData>()

        if (getRegionName(player) == "null") {
            playerInfoDataList.add(getPlayerInfodata(player))
        } else {
            for (data in RegionManager.playerRegionMap) {
                if (getRegionName(Bukkit.getPlayer(data.key))
                        == getRegionName(Bukkit.getPlayer(player.uniqueId))) {
                    playerInfoDataList.add(getPlayerInfodata(Bukkit.getPlayer(data.key)))
                }
            }
        }

        ulPacket.playerInfoDataLists.write(0, playerInfoDataList)

        try {
            // protocolManager.sendServerPacket(player, ulPacket)
            plugin.logger.info("send ul packet for ${player.name}")
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}
