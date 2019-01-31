package space.mori.fakebungee

import org.bukkit.plugin.java.JavaPlugin

import space.mori.fakebungee.commands.Ping
import space.mori.fakebungee.commands.RegionCommand
import space.mori.fakebungee.commands.ResourceCommand
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.player.Chat
import space.mori.fakebungee.player.PlayerList
import space.mori.fakebungee.player.ResourcePack
import space.mori.fakebungee.resourcepack.ResourcePackManager as RPM

class FakeBungee : JavaPlugin() {
    companion object {
        lateinit var instance: FakeBungee
    }

    override fun onEnable() {
        val chat = Chat(this)
        val playerList = PlayerList(this)
        val rpm = ResourcePack(this)

        FakeBungee.instance = this
        getCommand("ping").executor = Ping
        getCommand("fregion").executor = RegionCommand
        getCommand("fresource").executor = ResourceCommand

        RegionManager.load()
        RegionManager.runTaskTimer(this, 0, 20)

        RPM.load()


        chat.chat()
        playerList.playerList()
        rpm.resourcePack()

        this.server.pluginManager.registerEvents(chat, this)
        this.server.pluginManager.registerEvents(playerList, this)
        this.server.pluginManager.registerEvents(rpm, this)
        this.server.pluginManager.registerEvents(RegionManager, this)

        logger.info("${this.description.name} v${this.description.version} was loaded successfully.")
    }

    override fun onDisable() {
        RegionManager.save()
        RPM.save()

        logger.info("${this.description.name} is unloaded")
    }
}
