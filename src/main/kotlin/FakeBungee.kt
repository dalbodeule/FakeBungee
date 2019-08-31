package space.mori.fakebungee

import org.bukkit.plugin.java.JavaPlugin

import space.mori.fakebungee.commands.*
import space.mori.fakebungee.config.ConfigManager
import space.mori.fakebungee.footer.FooterManager
import space.mori.fakebungee.header.HeaderManager
import space.mori.fakebungee.region.RegionManager
import space.mori.fakebungee.player.Chat
import space.mori.fakebungee.player.PlayerListHF
import space.mori.fakebungee.player.PlayerListUL
import space.mori.fakebungee.player.ResourcePack
import space.mori.fakebungee.regiondata.RegionDataManager as RDM
import space.mori.fakebungee.util.Logger
import space.mori.fakebungee.resourcepack.ResourcePackManager as RPM

class FakeBungee : JavaPlugin() {
    companion object {
        lateinit var instance: FakeBungee
    }

    override fun onEnable() {
        val logger = Logger(this)
        val chat = Chat(this, logger)
        val playerListHF = PlayerListHF(this, logger)
        val playerListUL = PlayerListUL(this, logger)
        val rpm = ResourcePack(this, logger)

        instance = this
        getCommand("ping")!!.setExecutor(Ping)
        getCommand("fregion")!!.setExecutor(RegionCommand)
        getCommand("fresource")!!.setExecutor(ResourceCommand)
        getCommand("fconfig")!!.setExecutor(ConfigCommand)
        getCommand("fheader")!!.setExecutor(HeaderCommand)
        getCommand("ffooter")!!.setExecutor(FooterCommand)
        getCommand("resource")!!.setExecutor(ResourceApplyCommand(this, logger))

        // RegionManager
        RegionManager.load()
        RegionManager.runTaskTimer(this, 0, 20)

        // RegionDataManager
        RDM.load()

        // ResourcePackManager
        RPM.load()

        // ConfigManager
        ConfigManager.load()

        // HeaderManager
        HeaderManager.load()

        // FooterManager
        FooterManager.load()

        chat.chat()
        playerListHF.playerList()
        playerListUL.playerList()
        rpm.resourcePack()

        this.server.pluginManager.registerEvents(chat, this)
        this.server.pluginManager.registerEvents(playerListHF, this)
        this.server.pluginManager.registerEvents(playerListUL, this)
        this.server.pluginManager.registerEvents(rpm, this)
        this.server.pluginManager.registerEvents(RegionManager, this)

        logger.info("${this.description.name} v${this.description.version} was loaded successfully.")

        if (ConfigManager.Config.debug) {
            logger.info("debug mode activated")
        }
    }

    override fun onDisable() {
        // RegionManager
        RegionManager.save()

        // RegionDataManager
        RDM.save()

        // ResourcePackManager
        RPM.save()

        // ConfigManager
        ConfigManager.save()

        // HeaderManager
        HeaderManager.save()

        // FooterManager
        FooterManager.save()

        logger.info("${this.description.name} is unloaded")
    }
}
