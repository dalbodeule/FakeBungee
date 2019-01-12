package space.mori.fakebungee

import org.bukkit.plugin.java.JavaPlugin

import space.mori.fakebungee.commands.Ping
import space.mori.fakebungee.region.RegionManager

class FakeBungee : JavaPlugin() {
   override fun onEnable() {
       getCommand("ping").executor = Ping

       RegionManager.runTaskTimer(this, 0, 20)

       logger.info("${this.description.name} v${this.description.version} was loaded successfully.")
    }

    override fun onDisable() {
        logger.info("${this.description.name} is unloaded")
    }
}
