package space.mori.fakebungee.player

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.region.currentRegions

class Chat (private val plugin: JavaPlugin) : Listener {
    internal fun chat() {
        plugin.logger.info("Chat module initializing... success!")
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    internal fun onChat(event: AsyncPlayerChatEvent) {
        val region : String? = RegionManager.getRegionName(event.player)

        if (region == null) {
            event.player.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You can't chat.")
        } else {
            plugin.logger.info("[$region] ${event.player.displayName} : ${event.message}")
            for (data in RegionManager.playerRegionMap) {
                if (data.value.first().name == region) {
                    Bukkit.getPlayer(data.key).sendMessage(
                        "${event.player.displayName}: ${event.message}"
                    )
                } else {
                    continue
                }
            }
        }

        event.isCancelled = true
    }
}