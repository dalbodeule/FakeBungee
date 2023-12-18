package space.mori.fakebungee.player

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import org.bukkit.GameMode
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
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.firstOrNull


class PlayerListUL(private val plugin: JavaPlugin, private val logger: Logger) : Listener {
    private val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()
    val isCitizensActivated = plugin.server.pluginManager.isPluginEnabled("Citizens")

    internal fun playerList() {

        this.protocolManager.addPacketListener(object :
            PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            override fun onPacketSending(event: PacketEvent) {
                // https://www.spigotmc.org/threads/pre-1-16-protocollib-player_info-packet-issue.579972/

                val packet = event.packet
                val action = packet.playerInfoAction.readSafely(0)
                if(action == PlayerInfoAction.ADD_PLAYER || action == PlayerInfoAction.UPDATE_DISPLAY_NAME) {
                    if (isCitizensActivated) {
                        Citizens(logger).filterCitizensNPC(event)
                        event.isCancelled = true
                    }
                }
            }
        })

        logger.info("PlayerListUL module initializing... success!")
        if (isCitizensActivated) {
            logger.info("Citizens plugin is activated!")
        } else {
            logger.info("Citizens plugin is deactivated!")
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
        makePlayerList(event.player)
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
                player.ping,
                EnumWrappers.NativeGameMode.fromBukkit(player.gameMode),
                WrappedChatComponent.fromText(player.displayName)
        )

        // https://www.spigotmc.org/threads/1-19-3-add_player-and-remove_player-with-protocollib.585490/
    }

    private fun getPlayerEntity(player: Player): PacketContainer? {
        val packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO)

        logger.debug("${player.name}'s entity id: ${player.entityId}")

        packet.integers.writeSafely(0, player.entityId)
        packet.uuiDs.writeSafely(0, player.uniqueId)
        packet.doubles.writeSafely(0, player.location.x)
        packet.doubles.writeSafely(1, player.location.y)
        packet.doubles.writeSafely(2, player.location.z)
        packet.bytes.writeSafely(0, ((player.location.yaw * 256.0F) / 360.0F).toInt().toByte())
        packet.bytes.writeSafely(1, ((player.location.pitch * 256.0F) / 360.0F).toInt().toByte())

        return packet
    }

    private fun getPlayerEntityDestroyPacket(player: Player): PacketContainer {
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
            val region = RegionManager.getRegionName(player) ?: "default"

            for (data in plugin.server.onlinePlayers) {
                if (data == player) continue
                else if (region == (RegionManager.getRegionName(data) ?: "default")) {
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
            val region = RegionManager.getRegionName(player) ?: "default"
            val packetPL = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
            val packetEP = getPlayerEntity(player)

            playerInfoDataList.add(getPlayerInfo(player))

            // https://github.com/dmulloy2/ProtocolLib/issues/2351
            packetPL.playerInfoActions.write(
                0,
                EnumSet.of(
                    PlayerInfoAction.ADD_PLAYER,
                    PlayerInfoAction.UPDATE_DISPLAY_NAME,
                    PlayerInfoAction.UPDATE_GAME_MODE,
                    PlayerInfoAction.UPDATE_LATENCY,
                    PlayerInfoAction.UPDATE_LISTED
                )
            )
            packetPL.playerInfoDataLists.write(
                1, listOf(
                    PlayerInfoData(
                        WrappedGameProfile.fromPlayer(player),
                        player.ping,
                        run {
                            when(player.gameMode) {
                                GameMode.CREATIVE -> EnumWrappers.NativeGameMode.CREATIVE
                                GameMode.ADVENTURE -> EnumWrappers.NativeGameMode.ADVENTURE
                                GameMode.SURVIVAL -> EnumWrappers.NativeGameMode.SURVIVAL
                                GameMode.SPECTATOR -> EnumWrappers.NativeGameMode.SPECTATOR
                                else -> EnumWrappers.NativeGameMode.NOT_SET
                            }
                        },
                        WrappedChatComponent.fromText(player.displayName)
                    )
                )
            )

            for (data in plugin.server.onlinePlayers) {
                if (data == player) continue
                else if (region == (RegionManager.getRegionName(data) ?: "default")) {
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
        }, 10L)
    }

    private fun makePlayerList(player: Player) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
            val playerInfoDataList = ArrayList<PlayerInfoData>()
            val region = RegionManager.getRegionName(player) ?: "default"
            val lists = ArrayList<Player>()

            for (data in plugin.server.onlinePlayers) {
                if (region == (RegionManager.getRegionName(data) ?: "default")) {
                    playerInfoDataList.add(getPlayerInfo(data))
                    lists.add(data)
                }
            }

            // https://github.com/dmulloy2/ProtocolLib/issues/2351
            val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO)
            packet.playerInfoActions.write(
                0,
                EnumSet.of(
                    PlayerInfoAction.ADD_PLAYER,
                    PlayerInfoAction.UPDATE_DISPLAY_NAME,
                    PlayerInfoAction.UPDATE_GAME_MODE,
                    PlayerInfoAction.UPDATE_LATENCY,
                    PlayerInfoAction.UPDATE_LISTED
                )
            )
            packet.playerInfoDataLists.write(
                1, listOf(
                    PlayerInfoData(
                        WrappedGameProfile.fromPlayer(player),
                        player.ping,
                        run {
                            when(player.gameMode) {
                                GameMode.CREATIVE -> EnumWrappers.NativeGameMode.CREATIVE
                                GameMode.ADVENTURE -> EnumWrappers.NativeGameMode.ADVENTURE
                                GameMode.SURVIVAL -> EnumWrappers.NativeGameMode.SURVIVAL
                                GameMode.SPECTATOR -> EnumWrappers.NativeGameMode.SPECTATOR
                                else -> EnumWrappers.NativeGameMode.NOT_SET
                            }
                        },
                        WrappedChatComponent.fromText(player.displayName)
                    )
                )
            )

            try {
                protocolManager.sendServerPacket(player, packet, false)
                logger.debug("send ul:MAKE packet for ${player.name} with region $region")
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }, 10L)
    }

    private fun deletePlayerList(player: Player, pastRegion: Region?) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
            val playerInfoDataList = ArrayList<PlayerInfoData>()
            val region = RegionManager.getRegionName(player) ?: "default"
            val pastRegionName = pastRegion?.name ?: "default"

            val playerInfoDataSelf = ArrayList<PlayerInfoData>()
            playerInfoDataSelf.add(getPlayerInfo(player))
            val packetSELF = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE)
            packetSELF.uuidLists.write(0, Collections.singletonList(player.uniqueId))
            val packetED = getPlayerEntityDestroyPacket(player)

            for (data in plugin.server.onlinePlayers) {
                if (region == pastRegion?.name) {
                    playerInfoDataList.add(getPlayerInfo(data))

                    protocolManager.sendServerPacket(data, packetSELF, false)
                    logger.debug("send ul:REM_SELF packet for ${data.name} with region $pastRegionName due to ${player.name}")

                    protocolManager.sendServerPacket(data, packetED, true)
                    logger.debug("send ENTITY_DESTROY packet for `${data.name}` with region `$pastRegionName` and destory player is `${player.name}`")

                    protocolManager.sendServerPacket(player, getPlayerEntityDestroyPacket(data), true)
                    logger.debug("send ENTITY_DESTROY packet for `${player.name}` with region `$pastRegionName` and destory player is `${data.name}`")
                }
            }

            val packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE)
            packet.uuidLists.write(0, Collections.singletonList(player.uniqueId))

            try {
                protocolManager.sendServerPacket(player, packet, false)
                logger.debug("send ul:REM packet for ${player.name} with region $pastRegionName")
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }, 10L)
    }
}
