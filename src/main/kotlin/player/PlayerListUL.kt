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
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
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
        val isCitizensActivated = plugin.server.pluginManager.isPluginEnabled("Citizens")
        this.protocolManager.addPacketListener(object :
            PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            override fun onPacketSending(event: PacketEvent) {
                when(event.packet.playerInfoAction.read(0)) {
                    EnumWrappers.PlayerInfoAction.ADD_PLAYER -> {
                        if (isCitizensActivated) {
                            Citizens(logger).filterCitizensNPC(event)
                        }
                        event.isCancelled = true
                    }
                    else -> return
                }
            }
        })
        logger.info("PlayerListUL module initializing... success!")
        if (isCitizensActivated) {
            logger.info("Citizens plugin is activated!")
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionEnter(event: RegionEnterEvent) {
        deletePlayerList(event.player, event.player.currentRegions.firstOrNull())
        makePlayerList(event.player)
        addPlayerList(event.player)
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, { renderPlayers(event.player) }, 20L)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onRegionExit(event: RegionExitEvent) {
        deletePlayerList(event.player, event.player.currentRegions.firstOrNull())
        addPlayerList(event.player)
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, { renderPlayers(event.player) }, 20L)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onPlayerJoin(event: PlayerJoinEvent) {
        makePlayerList(event.player)
        addPlayerList(event.player)
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, { renderPlayers(event.player) }, 20L)
    }

    private fun getPlayerInfo(player: Player): PlayerInfoData {
        return PlayerInfoData(
                WrappedGameProfile.fromPlayer(player),
                Ping().get(player),
                EnumWrappers.NativeGameMode.fromBukkit(player.gameMode),
                WrappedChatComponent.fromText(player.displayName)
        )
    }

    private fun getPlayerEntity(player: Player): PacketContainer? {
        val packet = protocolManager.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN)

        logger.debug("${player.name}'s entity id: ${player.entityId}")

        packet.integers.writeSafely(0, player.entityId)
        packet.uuiDs.writeSafely(0, player.uniqueId)
        packet.doubles.writeSafely(0, player.location.x)
        packet.doubles.writeSafely(1, player.location.y)
        packet.doubles.writeSafely(2, player.location.z)
        packet.bytes.writeSafely(0, ((player.location.yaw * 256.0F) / 360.0F).toByte())
        packet.bytes.writeSafely(1, ((player.location.pitch * 256.0F) / 360.0F).toByte())

        return packet
    }

    private fun getPlayerEntityDestoryPacket(player: Player): PacketContainer {
        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY)
        val content = IntArray(1)
        content[0] = player.entityId

        packet.integers.writeSafely(0, content.size)
        packet.integerArrays.writeSafely(0, content)

        return packet
    }

    // https://www.spigotmc.org/threads/protocollib-named_entity_spawn-body-rotation-weird.322482/
    // https://www.spigotmc.org/threads/protocollib-hide-change-player-npc-name.298555/

    private fun renderPlayers(player: Player) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
            val region = RegionManager.getRegionName(player)

            for (data in plugin.server.onlinePlayers) {
                if (data == player) continue
                else if (region == RegionManager.getRegionName(data)) {
                    try {
                        protocolManager.sendServerPacket(player, getPlayerEntity(data), false)
                        logger.debug("send NAMED_ENTITY_SPAWN packet for `${player.name}` with region `$region` and render player is `${data.name}`")
                    } catch (e: InvocationTargetException) {
                        e.printStackTrace()
                    }
                } else continue
            }
        }, 20L)
    }

    private fun addPlayerList(player: Player) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, Runnable {
            val playerInfoDataList = ArrayList<PlayerInfoData>()
            val region = RegionManager.getRegionName(player)
            val packetPL = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO)
            val packetEP = getPlayerEntity(player)

            playerInfoDataList.add(getPlayerInfo(player))

            packetPL.playerInfoAction.write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER)
            packetPL.playerInfoDataLists.write(0, playerInfoDataList)

            if (region == null) {
                return@Runnable
            } else {
                for (data in plugin.server.onlinePlayers) {
                    if (data == player) continue
                    else if (region == RegionManager.getRegionName(data)) {
                        try {
                            protocolManager.sendServerPacket(data, packetPL, false)
                            logger.debug("send ul:ADD packet for ${data.name} with region $region due to ${player.name}")

                            protocolManager.sendServerPacket(data, packetEP, false)
                            logger.debug("send NAMED_ENTITY_SPAWN packet for `${data.name}` with region `$region` and render player is `${player.name}`")
                        } catch (e: InvocationTargetException) {
                            e.printStackTrace()
                        }
                    } else continue
                }
            }
        }, 10L)
    }

    private fun makePlayerList(player: Player) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
            val playerInfoDataList = ArrayList<PlayerInfoData>()
            val region = RegionManager.getRegionName(player)
            val lists = ArrayList<Player>()

            if (region == null) {
                playerInfoDataList.add(getPlayerInfo(player))
            } else {
                for (data in plugin.server.onlinePlayers) {
                    if (region == RegionManager.getRegionName(data)) {
                        playerInfoDataList.add(getPlayerInfo(data))
                        lists.add(data)
                    }
                }
            }

            val packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO)

            packet.playerInfoAction.write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER)
            packet.playerInfoDataLists.write(0, playerInfoDataList)

            try {
                protocolManager.sendServerPacket(player, packet, false)
                logger.debug("send ul:MAKE packet for ${player.name} with region $region")
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }, 10L)
    }

    private fun deletePlayerList(player: Player, pastRegion: Region?) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, Runnable {
            val playerInfoDataList = ArrayList<PlayerInfoData>()
            val region = RegionManager.getRegionName(player)
            val pastRegionName = pastRegion?.name

            if (pastRegion == null && region == null) return@Runnable

            val playerInfoDataSelf = ArrayList<PlayerInfoData>()
            playerInfoDataSelf.add(getPlayerInfo(player))
            val packetSELF = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO)
            packetSELF.playerInfoAction.write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER)
            packetSELF.playerInfoDataLists.write(0, playerInfoDataSelf)
            val packetED = getPlayerEntityDestoryPacket(player)

            if (region == null) {
                for (data in plugin.server.onlinePlayers) {
                    if (data != player) {
                        playerInfoDataList.add(getPlayerInfo(data))
                    }
                    if (RegionManager.getRegionName(data) == pastRegion?.name) {
                        protocolManager.sendServerPacket(data, packetSELF, false)
                        logger.debug("send ul:REM_SELF packet for ${data.name} with region $pastRegionName due to ${player.name}")

                        protocolManager.sendServerPacket(data, packetED, true)
                        logger.debug("send ENTITY_DESTROY packet for `${data.name}` with region `$pastRegionName` and destory player is `${player.name}`")

                        protocolManager.sendServerPacket(player, getPlayerEntityDestoryPacket(data), true)
                        logger.debug("send ENTITY_DESTROY packet for `${player.name}` with region `$pastRegionName` and destory player is `${data.name}`")

                    }
                }
            } else {
                for (data in plugin.server.onlinePlayers) {
                    if (RegionManager.getRegionName(data) == pastRegion?.name) {
                        playerInfoDataList.add(getPlayerInfo(data))

                        protocolManager.sendServerPacket(data, packetSELF, false)
                        logger.debug("send ul:REM_SELF packet for ${data.name} with region $pastRegionName due to ${player.name}")

                        protocolManager.sendServerPacket(data, packetED, true)
                        logger.debug("send ENTITY_DESTROY packet for `${data.name}` with region `$pastRegionName` and destory player is `${player.name}`")

                        protocolManager.sendServerPacket(player, getPlayerEntityDestoryPacket(data), true)
                        logger.debug("send ENTITY_DESTROY packet for `${player.name}` with region `$pastRegionName` and destory player is `${data.name}`")
                    }
                }
            }

            val packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO)

            packet.playerInfoAction.write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER)
            packet.playerInfoDataLists.write(0, playerInfoDataList)

            try {
                protocolManager.sendServerPacket(player, packet, false)
                logger.debug("send ul:REM packet for ${player.name} with region $pastRegionName")
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }, 10L)
    }
}
