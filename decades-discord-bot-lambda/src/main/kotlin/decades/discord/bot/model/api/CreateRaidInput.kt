package decades.discord.bot.model.api

data class CreateRaidInput(
    val parentChannelId: String,
    val name: String,
    val raiders: List<Raider>,
    val startTime: Double,
    val additionalMessage: String? = null,
)
