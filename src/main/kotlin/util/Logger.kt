package space.mori.fakebungee.util

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import space.mori.fakebungee.config.ConfigManager

class Logger (private val plugin: JavaPlugin) : Listener {
    internal  fun info (msg: String) {
        plugin.logger.info(msg)

        return
    }

    internal  fun debug (msg: String) {
        if (ConfigManager.Config.debug) {
            plugin.logger.info(msg)
        }
        return
    }
}