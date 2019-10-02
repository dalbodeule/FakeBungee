package space.mori.fakebungee.resourcepack

class ResourcePack (private val url: String, private val hash: String) {
    fun getURL() : String {
        return url
    }

    fun getHash(): String {
        return hash.toLowerCase()
    }

    override fun toString(): String {
        return "$url; ${hash.toLowerCase()}"
    }
}
