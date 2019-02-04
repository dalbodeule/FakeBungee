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
            for (npc in npcList) {
                if (data.profile.name != npc.fullName) {
                    logger.debug("${data.profile.name} and ${npc.fullName} from ${data.profile.name}")
                    continue
                } else {
                    logger.debug("${npc.fullName} is npc")
                    newPacketList.add(data)
                    break
                }
            }
        }

        val newPacket = event.packet
        newPacket.playerInfoDataLists.write(0, newPacketList)

        protocolManager.sendServerPacket(event.player, newPacket, false)
    }
}