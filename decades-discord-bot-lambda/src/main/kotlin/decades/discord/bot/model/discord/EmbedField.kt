package decades.discord.bot.model.discord

data class EmbedField(
    val name: String,
    val value: String,
    val inline: Boolean? = false,
)
