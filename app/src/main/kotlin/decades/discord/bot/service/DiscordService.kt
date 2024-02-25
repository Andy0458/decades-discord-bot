package decades.discord.bot.service

import decades.discord.bot.dagger.module.ServiceModule.Companion.DISCORD_BOT_TOKEN
import decades.discord.bot.model.ErrorResponse
import decades.discord.bot.model.discord.*
import software.amazon.awssdk.utils.Either
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

        private val botUser: User by lazy { getUser() }

        fun createPrivateThread(
            name: String,
            parentChannelId: String,
        ): Channel {
            return handleResponse(
                post<Channel>(
                    resource = "channels/$parentChannelId/threads",
                    body =
                        mutableMapOf(
                            "name" to name,
                            "type" to PRIVATE_THREAD,
                        ),
                ),
            )
        }

        fun createChannel(
            guildId: String,
            name: String,
            parentChannelId: String? = null,
            members: List<String>? = null,
        ): Channel {
            val channelMembers = mutableListOf(botUser.id)
            members?.let { channelMembers.plus(members) }
            return handleResponse(
                post<Channel>(
                    resource = "guilds/$guildId/channels",
                    body =
                        mutableMapOf(
                            "name" to name,
                            "type" to GUILD_TEXT_CHANNEL,
                            "permission_overwrites" to
                                channelMembers.map {
                                    mapOf(
                                        "id" to it,
                                        "type" to 1, // Member
                                        "allow" to DEFAULT_PERMISSIONS.toString(),
                                    )
                                },
                        ).apply {
                            // Optionals
                            parentChannelId?.let { this["parent_id"] = parentChannelId }
                        },
                ),
            )
        }

        fun sendMessage(
            channelId: String,
            message: String,
            mentions: List<String>? = null,
        ) = handleResponse(
            post(
                resource = "channels/$channelId/messages",
                body =
                    mutableMapOf(
                        "content" to message,
                    ).apply {
                        mentions?.let {
                            this["content"] = this["content"] + "\n" + mentions.joinToString("") { "<@$it>" }
                        }
                    },
            ),
        )

        fun getGuild(id: String): Guild =
            handleResponse(
                get<Guild>(
                    resource = "guilds",
                    id = id,
                ),
            )

        fun getUser(id: String? = null) =
            handleResponse(
                get<User>(
                    resource = "users",
                    id = id ?: "@me",
                ),
            )

        private fun <ErrorType : ErrorResponse, ResponseType> handleResponse(response: Either<ErrorType, ResponseType>): ResponseType {
            if (response.left().isPresent) throw Error(response.left().get().message)
            return response.right().get()
        }
    }
