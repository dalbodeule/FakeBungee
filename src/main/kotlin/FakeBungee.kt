package space.mori.fakebungee

import org.bukkit.plugin.java.JavaPlugin

import space.mori.fakebungee.commands.Ping

class FakeBungee : JavaPlugin() {
   override fun onEnable() {
       getCommand("ping").executor = Ping

       logger.info("${this.description.name} v${this.description.version} was loaded successfully.")
    }

    override fun onDisable() {
        logger.info("${this.description.name} is unloaded")
    }
}
