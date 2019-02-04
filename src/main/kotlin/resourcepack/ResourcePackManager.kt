package space.mori.fakebungee.resourcepack

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import space.mori.fakebungee.FakeBungee
import space.mori.fakebungee.util.parseJsonTo
import space.mori.fakebungee.util.serializeJsonString
import space.mori.fakebungee.resourcepack.ResourcePack as ResourcePackType
import java.nio.file.Files
import java.nio.file.Path

object ResourcePackManager : Listener {
    val resourcePackMap : MutableMap<String, ResourcePackType> = mutableMapOf()
    val playerResourcePackMap : MutableMap<Player, ResourcePackType> = mutableMapOf()

    private val target: Path = FakeBungee.instance.dataFolder.toPath().resolve("resourcepack.json")

    internal fun load() {
        if (this.target.toFile().exists()) {
            Files.readAllBytes(this.target).toString(Charsets.UTF_8).parseJsonTo<Map<String, ResourcePackType>>()?.let {
                resourcePackMap.putAll(it)
            }
        }

        if (resourcePackMap["default"] == null) {
            resourcePackMap["default"] = ResourcePackType(
                "https://download.nodecdn.net/containers/nodecraft/minepack/64b9bee7569109cdfe51aa433352d3d4.zip",
                "d448c8c7e4cc57fa6e786f7811204a1c99bdc64e"
            )
        }

        this.save()
    }

    internal fun save() {
        if (!this.target.toFile().exists()) {
            Files.createDirectories(this.target.parent)
            Files.createFile(this.target)
        }

        Files.write(this.target, resourcePackMap.serializeJsonString().toByteArray())
    }

    @EventHandler(priority = EventPriority.NORMAL)
    internal fun onQuit(event: PlayerQuitEvent) {
        playerResourcePackMap.remove(event.player)
    }
}