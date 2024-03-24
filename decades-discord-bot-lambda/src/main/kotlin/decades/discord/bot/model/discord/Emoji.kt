package decades.discord.bot.model.discord

data class Emoji(
    val id: String? = null,
    val name: String? = null,
) {
    override fun toString(): String = "<:$name:$id>"
}
