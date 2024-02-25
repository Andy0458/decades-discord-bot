package decades.discord.bot.model.discord

import decades.discord.bot.model.ErrorResponse

data class DiscordErrorResponse(
    override val statusCode: Int,
    override var message: String,
    var errors: Any? = null,
) : ErrorResponse
