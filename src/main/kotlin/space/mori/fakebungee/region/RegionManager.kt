package space.mori.fakebungee.region

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import space.mori.fakebungee.FakeBungee
import space.mori.fakebungee.region.event.RegionEnterEvent
import space.mori.fakebungee.region.event.RegionExitEvent
import space.mori.fakebungee.util.parseJsonTo
import space.mori.fakebungee.util.serializeJsonString
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

object RegionManager : BukkitRunnable(), Listener {
    val regions: MutableMap<String, Region> = mutableMapOf()
    val playerRegionMap: MutableMap<UUID, Set<Region>> = mutableMapOf()
    private val target: Path = FakeBungee.instance.dataFolder.toPath().resolve("region.json")

    override fun run() {
        // O(playerCount * regionCount)
        for (player in Bukkit.getOnlinePlayers()) {
            val before = playerRegionMap[player.uniqueId] ?: emptySet()
            val after = mutableSetOf<Region>()
            for (region in regions.values) {
                if (player in region) {
                    after += region
                }
            }
            val entered = after.filter { it !in before }
            val exited = before.filter { it !in after }
            entered.forEach {
                Bukkit.getPluginManager().callEvent(RegionEnterEvent(player, it))
            }
            exited.forEach {
                Bukkit.getPluginManager().callEvent(RegionExitEvent(player, it))
            }
            playerRegionMap[player.uniqueId] = after
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

    internal fun getRegionName(player: Player): String? {
        return player.currentRegions.map { it.name }.firstOrNull()
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onQuit(event: PlayerQuitEvent) {
        this.playerRegionMap.remove(event.player.uniqueId)
    }
}

val Player.currentRegions: Set<Region>
    get() = RegionManager.playerRegionMap.getOrDefault(this.uniqueId, emptySet())
