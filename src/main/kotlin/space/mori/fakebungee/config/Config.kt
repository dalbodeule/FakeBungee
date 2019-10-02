package space.mori.fakebungee.config

data class Config (
    val debug: Boolean = false,
    val resourcePackMessage: Boolean = true,
    val resourcePackEnabled: Boolean = true,
    val chatFormat: String = "{displayname}: {message}"
)
