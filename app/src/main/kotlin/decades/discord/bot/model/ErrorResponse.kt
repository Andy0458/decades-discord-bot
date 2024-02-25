package decades.discord.bot.model

interface ErrorResponse {
    val statusCode: Int?
        get() = null
    val message: String
}
