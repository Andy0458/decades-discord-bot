package decades.discord.bot.model.api

data class CreateRaidInput(
    val name: String,
    val team: String,
    val raiders: List<Raider>,
    val startTime: Int,
    val leader: String,
    val parentChannelId: String? = null,
    val threadId: String? = null,
    val additionalMessage: String? = null,
)
