package space.mori.fakebungee.resourcepack

import java.util.*

class ResourcePack (private val url: String, private val hash: String) {
    fun getURL() : String {
        return url
    }

    fun getHash(): String {
        return hash.lowercase(Locale.getDefault())
    }

    override fun toString(): String {
        return "$url; ${hash.lowercase(Locale.getDefault())}"
    }
}
