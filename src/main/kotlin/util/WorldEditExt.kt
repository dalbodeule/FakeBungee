package space.mori.fakebungee.util

import com.sk89q.worldedit.LocalSession
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import com.sk89q.worldedit.entity.Player as WorldEditPlayer
import com.sk89q.worldedit.math.BlockVector3 as WorldEditBlockVector3
import com.sk89q.worldedit.world.World as WorldEditWorld


val worldEditPlugin: WorldEditPlugin by lazy {
    WorldEditPlugin.getPlugin(WorldEditPlugin::class.java)
}

val Player.session: LocalSession
    get() = worldEditPlugin.getSession(this)

val Player.worldEdit: WorldEditPlayer
    get() = BukkitAdapter.adapt(this)

val World.worldEdit: WorldEditWorld
    get() = BukkitAdapter.adapt(this)

val WorldEditWorld.bukkit: World
    get() = BukkitAdapter.adapt(this)

val WorldEditBlockVector3.bukkit: Vector
    get() = Vector(this.x, this.y, this.z)
