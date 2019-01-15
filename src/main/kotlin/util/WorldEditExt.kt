package space.mori.fakebungee.util

import com.sk89q.worldedit.LocalSession
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import com.sk89q.worldedit.Vector as WorldEditVector
import com.sk89q.worldedit.entity.Player as WorldEditPlayer
import com.sk89q.worldedit.world.World as WorldEditWorld


val worldEditPlugin: WorldEditPlugin by lazy {
    WorldEditPlugin.getPlugin(WorldEditPlugin::class.java)
}

val Player.session: LocalSession
    get() = worldEditPlugin.getSession(this)

val Player.worldEdit: WorldEditPlayer
    get() = worldEditPlugin.wrapPlayer(this)

val World.worldEdit: WorldEditWorld
    get() = BukkitWorld(this)

val WorldEditWorld.bukkit: World
    get() = (this as BukkitWorld).world

val WorldEditVector.bukkit: Vector
    get() = Vector(this.x, this.y, this.z)
