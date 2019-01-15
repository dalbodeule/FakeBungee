package space.mori.fakebungee.player

import org.bukkit.plugin.java.JavaPlugin

import space.mori.fakebungee.region.currentRegions

import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedChatComponent
import org.bukkit.entity.Player
import org.bukkit.ChatColor

import java.lang.reflect.InvocationTargetException

class PlayerList constructor(plugin: JavaPlugin) {
    private val protocolManager : ProtocolManager = ProtocolLibrary.getProtocolManager()
    private val plugin : JavaPlugin = plugin

    internal fun playerList() {
        this.protocolManager.addPacketListener(
            object: PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
                @Override
                override fun onPacketSending(event: PacketEvent) {
                    if (event.packetType == PacketType.Play.Server.PLAYER_INFO) {
                        val hfPacket : PacketContainer =
                            PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER)

                        hfPacket.chatComponents.write(
                            0, WrappedChatComponent.fromText(
                            getHeader(event.player, "test")))
                        hfPacket.chatComponents.write(
                            1, WrappedChatComponent.fromText(
                            getFooter(event.player, "test")))

                        try {
                            protocolManager.sendServerPacket(event.player, hfPacket)
                        } catch (e: InvocationTargetException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        )
    }

    private fun getHeader(player: Player, area: String): String {
        return ChatColor.translateAlternateColorCodes('&', "&d{player}, &6{area}, Test Header"
            .replace("{player}", player.displayName).replace("{area}", area))
    }

    private fun getFooter(player: Player, area: String): String {
        return ChatColor.translateAlternateColorCodes('&', "&6Test Footer"
            .replace("{player}", player.displayName).replace("{area}", area))
    }
}