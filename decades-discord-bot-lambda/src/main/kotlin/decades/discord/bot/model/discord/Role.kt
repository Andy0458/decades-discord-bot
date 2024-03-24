package decades.discord.bot.model.discord

data class Role(
    val name: String,
    val id: String,
) {
    fun mention(): String = "<@&$id>"
}
