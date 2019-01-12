package space.mori.fakebungee.region

import org.bukkit.entity.Player

interface Region {
    operator fun contains(player: Player): Boolean
}
