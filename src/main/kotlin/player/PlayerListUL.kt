package space.mori.fakebungee.player

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedGameProfile
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import space.mori.fakebungee.region.Region
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.region.currentRegions
import space.mori.fakebungee.region.event.RegionEnterEvent
import space.mori.fakebungee.region.event.RegionExitEvent
import space.mori.fakebungee.util.Logger
import space.mori.fakebungee.util.Ping
import java.lang.reflect.InvocationTargetException
import java.util.*



class PlayerListUL (private val plugin: JavaPlugin, private val logger: Logger) : Listener {
    private val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

    internal fun playerList() {
        this.protocolManager.addPacketListener(object :
            PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            override fun onPacketSending(event: PacketEvent) {
                when(event.packet.playerInfoAction.read(0)) {
                    EnumWrappers.PlayerInfoAction.ADD_PLAYER -> makePlayerList(event)
                    else -> return
                }
            }
        })

        logger.info("PlayerListUL module initializing... success!")
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionEnter(event: RegionEnterEvent) {
        makePlayerList(event.player)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionExit(event: RegionExitEvent) {
        makePlayerList(event.player)
        deletePlayerList(event.player, event.player.currentRegions.firstOrNull())
    }

    private fun makePlayerList(event: PacketEvent) {
        makePlayerList(event.player)
    }

    private fun getPlayerInfo(player: Player): PlayerInfoData {
        return PlayerInfoData(
                WrappedGameProfile.fromPlayer(player),
                Ping().get(player),
                EnumWrappers.NativeGameMode.fromBukkit(player.gameMode),
                WrappedChatComponent.fromText(player.displayName)
        )
    }

    private fun makePlayerList(player: Player) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, object : Runnable {
            override fun run() {
                val playerInfoDataList = ArrayList<PlayerInfoData>()
                val region = RegionManager.getRegionName(player)

                if (region == null) {
                    playerInfoDataList.add(getPlayerInfo(player))
                } else {
                    for (data in RegionManager.playerRegionMap) {
                        if (
                                region == RegionManager.getRegionName(Bukkit.getPlayer(player.uniqueId))
                        ) {
                            playerInfoDataList.add(getPlayerInfo(Bukkit.getPlayer(data.key)))
                        }
                    }
                }

                val packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO)

                packet.playerInfoAction.write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
                packet.playerInfoDataLists.write(0, playerInfoDataList);

                try {
                    protocolManager.sendServerPacket(player, packet, false)
                    logger.debug("send ul:ADD packet for ${player.name} with region ${player.currentRegions.firstOrNull().let { it?.name }.let { "null" }}")
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            }
        }, 10L)
    }

    private fun deletePlayerList(player: Player, pastRegion: Region?) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, object : Runnable {
            override fun run() {
                val playerInfoDataList = ArrayList<PlayerInfoData>()
                val region = RegionManager.getRegionName(player)

                if (
                    pastRegion == null &&
                    region == null
                ) return

                if (region == null) {
                    for (data in plugin.server.onlinePlayers) {
                        if (data.uniqueId != player.uniqueId) {
                            playerInfoDataList.add(getPlayerInfo(data))
                        }
                    }
                } else {
                    for (data in plugin.server.onlinePlayers) {
                        if (data.currentRegions.firstOrNull() == pastRegion) {
                            playerInfoDataList.add(getPlayerInfo(data))
                        }
                    }
                }

                val packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO)

                packet.playerInfoAction.write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER)
                packet.playerInfoDataLists.write(0, playerInfoDataList)

                try {
                    protocolManager.sendServerPacket(player, packet, false)
                    logger.debug("send ul:REM packet for ${player.name} with region $region")
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            }
        }, 10L)
    }
}
