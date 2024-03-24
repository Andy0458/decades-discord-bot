package decades.discord.bot.model.discord

data class Member(
    val user: User? = null,
    val roles: List<String>,
)
