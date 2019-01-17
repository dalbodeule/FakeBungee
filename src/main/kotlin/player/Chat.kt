package space.mori.fakebungee.player


import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.region.currentRegions

class Chat constructor(private val plugin: JavaPlugin) {
    @EventHandler(priority = EventPriority.HIGHEST)
    internal fun onChat(event: AsyncPlayerChatEvent) {
        val region : String = getRegionName(event.player)

        if (region == "null") {

        } else {
            for (data in RegionManager.playerRegionMap) {
                if (data.value.first().name == region) {
                    Bukkit.getPlayer(data.key).sendMessage(event.message)
                } else {
                    continue
                }
            }
        }
    }

    private fun getRegionName(player: Player): String {
        val mapName : String? = player.currentRegions.map { it.name }.firstOrNull()

        return when (mapName) {
            null -> "null"
            else -> mapName
        }
    }
}