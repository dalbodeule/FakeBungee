package space.mori.fakebungee.regiondata

import org.bukkit.event.Listener
import space.mori.fakebungee.FakeBungee
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.regiondata.RegionData as RegionDataType
import space.mori.fakebungee.util.parseJsonTo
import space.mori.fakebungee.util.serializeJsonString
import java.nio.file.Files
import java.nio.file.Path

object RegionDataManager : Listener {
    var RegionData: MutableMap<String, RegionDataType> = mutableMapOf()

    private val target: Path = FakeBungee.instance.dataFolder.toPath().resolve("regiondata.json")

    internal fun load() {
        if (this.target.toFile().exists()) {
            Files.readAllBytes(target).toString(Charsets.UTF_8).parseJsonTo<Map<String, RegionDataType>>()?.let {
                for (data in it) {
                    if (RegionManager.regions.containsKey(data.key)) {
                        RegionData[data.key] = data.value
                    } else {
                        continue
                    }
                }
            }
        }
    }

    internal fun save() {
        if (!this.target.toFile().exists()) {
            Files.createDirectories(this.target.parent)
            Files.createFile(this.target)
        }

        Files.write(this.target, RegionDataManager.RegionData.serializeJsonString().toByteArray())
    }
}