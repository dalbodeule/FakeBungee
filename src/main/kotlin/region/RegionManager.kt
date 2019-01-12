package space.mori.fakebungee.region

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object RegionManager : BukkitRunnable() {
    val regions: MutableSet<Region> = mutableSetOf()
    val playerRegionMap: MutableMap<UUID, Set<Region>> = mutableMapOf()

    override fun run() {
        // O(playerCount * regionCount)
        for (player in Bukkit.getOnlinePlayers()) {
            val playerRegions = mutableSetOf<Region>()
            for (region in RegionManager.regions) {
                if (player in region) {
                    playerRegions += region
                }
            }
            RegionManager.playerRegionMap[player.uniqueId] = playerRegions
        }
    }
}

val Player.currentRegions: Set<Region>
    get() = RegionManager.playerRegionMap.getOrDefault(this.uniqueId, emptySet())
