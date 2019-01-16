package space.mori.fakebungee.region

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import space.mori.fakebungee.FakeBungee
import space.mori.fakebungee.util.parseJsonTo
import space.mori.fakebungee.util.serializeJsonString
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

object RegionManager : BukkitRunnable() {
    val regions: MutableMap<String, Region> = mutableMapOf()
    val playerRegionMap: MutableMap<UUID, Set<Region>> = mutableMapOf()
    private val target: Path = FakeBungee.instance.dataFolder.toPath().resolve("region.json")

    override fun run() {
        // O(playerCount * regionCount)
        for (player in Bukkit.getOnlinePlayers()) {
            val playerRegions = mutableSetOf<Region>()
            for (region in RegionManager.regions.values) {
                if (player in region) {
                    playerRegions += region
                }
            }
            RegionManager.playerRegionMap[player.uniqueId] = playerRegions
        }
    }

    fun load() {
        if (target.toFile().exists()) {
            Files.readAllBytes(target).toString(Charsets.UTF_8).parseJsonTo<Map<String, Region>>()?.let {
                regions.putAll(it)
            }
        }
    }

    fun save() {
        if (!target.toFile().exists()) {
            Files.createDirectories(target.parent)
            Files.createFile(target)
        }

        Files.write(target, regions.serializeJsonString().toByteArray())
    }
}

val Player.currentRegions: Set<Region>
    get() = RegionManager.playerRegionMap.getOrDefault(this.uniqueId, emptySet())
