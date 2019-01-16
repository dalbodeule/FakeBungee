package space.mori.fakebungee.region.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import space.mori.fakebungee.region.Region

class RegionExitEvent(player: Player, val region: Region) : PlayerEvent(player) {
    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = RegionExitEvent.handlers
    }

    override fun getHandlers(): HandlerList = RegionExitEvent.handlers
}
