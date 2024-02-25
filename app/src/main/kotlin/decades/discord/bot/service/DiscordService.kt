package decades.discord.bot.service

import decades.discord.bot.dagger.module.ServiceModule.Companion.DISCORD_BOT_TOKEN
import decades.discord.bot.model.discord.DiscordErrorResponse
import decades.discord.bot.model.discord.Guild
import java.net.http.HttpClient
import javax.inject.Inject
import javax.inject.Named

class DiscordService
    @Inject
    constructor(
        override val httpClient: HttpClient,
        @Named(DISCORD_BOT_TOKEN)
        private val botToken: String,
    ) : AbstractService<DiscordErrorResponse>(
            httpClient = httpClient,
            endpoint = ENDPOINT,
            errorType = DiscordErrorResponse::class.java,
            defaultHeaders =
                mapOf(
                    "Authorization" to "Bot $botToken",
                ),
        ) {
        companion object {
            private const val ENDPOINT = "https://discord.com/api/v10"
        }

        fun getGuild(id: String): Guild {
            val response =
                get<Guild>(
                    resource = "guilds",
                    id = id,
                )
            if (response.left().isPresent) {
                throw Error(response.left().get().message)
            } else {
                return response.right().get()
            }
        }
    }
