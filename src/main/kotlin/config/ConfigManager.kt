package space.mori.fakebungee.config

import org.bukkit.event.Listener
import space.mori.fakebungee.FakeBungee
import space.mori.fakebungee.util.parseJsonTo
import space.mori.fakebungee.util.serializeJsonString
import java.nio.file.Files
import java.nio.file.Path

object ConfigManager : Listener {
    val Config : MutableMap<String, Any> = mutableMapOf()

    private val target: Path = FakeBungee.instance.dataFolder.toPath().resolve("config.json")

    internal fun load() {
        if (this.target.toFile().exists()) {
            Files.readAllBytes(this.target).toString(Charsets.UTF_8).parseJsonTo<Map<String, Any>>()?.let {
                ConfigManager.Config.putAll(it)
            }
        }
    }

    internal fun save() {
        if (!this.target.toFile().exists()) {
            Files.createDirectories(this.target.parent)
            Files.createFile(this.target)
        }

        Files.write(this.target, ConfigManager.Config.serializeJsonString().toByteArray())
    }
}