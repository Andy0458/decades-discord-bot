package decades.discord.bot.model.discord

data class User(
    val id: String,
    val username: String? = null,
    val bot: Boolean = false,
)
