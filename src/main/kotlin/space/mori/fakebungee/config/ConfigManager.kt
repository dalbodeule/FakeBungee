package space.mori.fakebungee.config

import org.bukkit.event.Listener
import space.mori.fakebungee.FakeBungee
import space.mori.fakebungee.config.Config as ConfigType
import space.mori.fakebungee.util.gson
import space.mori.fakebungee.util.serializeJsonString
import java.nio.file.Files
import java.nio.file.Path

object ConfigManager : Listener {
    var Config: ConfigType = ConfigType()

    private val target: Path = FakeBungee.instance.dataFolder.toPath().resolve("config.json")

    internal fun load() {
        if (this.target.toFile().exists()) {
            Config = gson.fromJson(
                Files.readAllBytes(this.target).toString(Charsets.UTF_8),
                ConfigType::class.java
            )
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