package space.mori.fakebungee.player

import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import space.mori.fakebungee.config.ConfigManager
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.util.Logger

class Chat (private val plugin: JavaPlugin, private val logger: Logger) : Listener {
    internal fun chat() {
       logger.info("Chat module initializing... success!")
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    internal fun onChat(event: AsyncPlayerChatEvent) {
        val region: String? = RegionManager.getRegionName(event.player)

        if (region == null) {
            event.player.sendMessage("${ChatColor.RED}[!] ${ChatColor.WHITE}You can't chat.")
        } else {
            System.out.println("[region: $region] ${ConfigManager.Config.chatFormat
                .replace("{displayname}", event.player.displayName)
                .replace("{name}", event.player.name)
                .replace("{message}", event.message)}")

            for (data in plugin.server.onlinePlayers) {
                if (RegionManager.getRegionName(data) == region) {
                    data.sendMessage(
                        ConfigManager.Config.chatFormat
                            .replace("{displayname}", event.player.displayName)
                            .replace("{name}", event.player.name)
                            .replace("{message}", event.message)
                    )
                } else {
                    continue
                }
            }
        }

        event.isCancelled = true
    }
}