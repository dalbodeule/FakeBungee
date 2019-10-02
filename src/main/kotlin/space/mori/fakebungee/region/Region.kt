package space.mori.fakebungee.region

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import space.mori.fakebungee.regiondata.RegionData
import space.mori.fakebungee.regiondata.RegionDataManager

class Region(
    val name: String, private val min: Vector, private val max: Vector, private val worldName: String
) {
    companion object {
        @JvmStatic
        fun fromLocation(name: String, loc1: Location, loc2: Location): Region {
            if (loc1.world!!.name != loc2.world!!.name) {
                throw IllegalArgumentException("The locations' world must be same, but actual value is not: $loc1, $loc2")
            }

            val min = Vector(minOf(loc1.x, loc2.x), minOf(loc1.y, loc2.y), minOf(loc1.z, loc2.z))
            val max = Vector(maxOf(loc1.x, loc2.x), maxOf(loc1.y, loc2.y), maxOf(loc1.z, loc2.z))
            val world = loc1.world

            return Region(name, min, max, world!!.name)
        }
    }

    operator fun contains(player: Player): Boolean =
        player.world.name == worldName && player.location.toVector().isInAABB(min, max)
}

var Region.header: String
    get() = RegionDataManager.RegionData[this.name]?.UserListHeader.let { header -> return header ?: "default" }
    set(value) {
        RegionDataManager.RegionData[this.name].let { region ->
            if (region != null) {
                region.UserListHeader = value
            } else {
                RegionDataManager.RegionData[this.name] = RegionData("default", value, "default")
            }
        }
    }

var Region.footer: String
    get() = RegionDataManager.RegionData[this.name]?.UserListFooter.let { footer -> return footer ?: "default" }
    set(value) {
        RegionDataManager.RegionData[this.name].let { region ->
            if (region != null) {
                region.UserListFooter = value
            } else {
                RegionDataManager.RegionData[this.name] = RegionData("default", "default", value)
            }
        }
    }


var Region.resourcepack: String
    get() = RegionDataManager.RegionData[this.name]?.ResourcePack.let { resourcepack -> return resourcepack ?: "default"}
    set(value) {
        RegionDataManager.RegionData[this.name].let { region ->
            if (region != null) {
                region.ResourcePack = value
            } else {
                RegionDataManager.RegionData[this.name] = RegionData(value, "default", "default")
            }
        }
    }
