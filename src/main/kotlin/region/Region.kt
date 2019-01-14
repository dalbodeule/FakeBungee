package space.mori.fakebungee.region.impl

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class Region(
    val name: String, private val min: Vector, private val max: Vector, private val worldName: String
) {
    companion object {
        @JvmStatic
        fun fromLocation(name: String, loc1: Location, loc2: Location): Region {
            if (loc1.world.name != loc2.world.name) {
                throw IllegalArgumentException("The locations' world must be same, but actual value is not: $loc1, $loc2")
            }

            val min = Vector(minOf(loc1.x, loc2.x), minOf(loc1.y, loc2.y), minOf(loc1.z, loc2.z))
            val max = Vector(maxOf(loc1.x, loc2.x), maxOf(loc1.y, loc2.y), maxOf(loc1.z, loc2.z))
            val world = loc1.world

            return Region(name, min, max, world.name)
        }
    }

    operator fun contains(player: Player): Boolean =
        player.world.name == worldName && player.location.toVector().isInAABB(min, max)
}
