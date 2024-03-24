package decades.discord.bot.model.discord

data class Embed(
    val title: String? = null,
    val type: String? = "rich",
    val description: String? = null,
    val fields: List<EmbedField>? = null,
)
