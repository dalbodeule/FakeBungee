package space.mori.fakebungee.player

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.PlayerInfoData
import net.citizensnpcs.api.CitizensAPI
import space.mori.fakebungee.util.Logger
import java.util.ArrayList

class Citizens (private val logger: Logger) {
    private val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

    internal fun filterCitizensNPC(event: PacketEvent) {
        val packet = event.packet.playerInfoDataLists.read(0)
        val newPacketList = ArrayList<PlayerInfoData>()
        val npcList = CitizensAPI.getNPCRegistry().sorted()

        for (data in packet) {
            val npc = npcList.filter { it.name == data.profile.name }
            if(npc.isNotEmpty()) {
                logger.debug("${data.profile.name} and ${npc[0].fullName} from ${data.profile.name}")
            } else {
                logger.debug("${data.profile.name} isn't npc")
                newPacketList.add(data)
            }
        }

        val newPacket = event.packet
        newPacket.playerInfoDataLists.writeSafely(0, newPacketList)

        protocolManager.sendServerPacket(event.player, newPacket, false)
    }
}