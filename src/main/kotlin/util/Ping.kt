package space.mori.fakebungee.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player

class Ping {
    private val serverVersion = Bukkit.getServer().javaClass.getPackage().name.replace(".", ",").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3]

    internal fun get(player: Player): Int {
        try {
            val entityPlayer = Class.forName("org.bukkit.craftbukkit.$serverVersion.entity.CraftPlayer").getMethod("getHandle").invoke(player)
            return entityPlayer.javaClass.getDeclaredField("ping").get(entityPlayer) as Int
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
    }
}

// https://www.spigotmc.org/threads/getting-a-players-ping-w-reflection.196857/#post-2052860
