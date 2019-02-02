package space.mori.fakebungee.footer

import org.bukkit.event.Listener
import space.mori.fakebungee.FakeBungee
import space.mori.fakebungee.util.parseJsonTo
import space.mori.fakebungee.util.serializeJsonString
import space.mori.fakebungee.resourcepack.ResourcePack as ResourcePackType
import java.nio.file.Files
import java.nio.file.Path

object FooterManager : Listener {
    val footerMap: MutableMap<String, String> = mutableMapOf()

    private val target: Path = FakeBungee.instance.dataFolder.toPath().resolve("footer.json")

    internal fun load() {
        if (this.target.toFile().exists()) {
            Files.readAllBytes(this.target).toString(Charsets.UTF_8).parseJsonTo<Map<String, String>>()?.let {
                footerMap.putAll(it)
            }
        }

        if (footerMap["default"] == null) {
            footerMap["default"] = "&6Default Footer"
        }

        this.save()
    }

    internal fun save() {
        if (!this.target.toFile().exists()) {
            Files.createDirectories(this.target.parent)
            Files.createFile(this.target)
        }

        Files.write(this.target,  footerMap.serializeJsonString().toByteArray())
    }
}