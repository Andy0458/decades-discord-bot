package decades.discord.bot.service

import decades.discord.bot.dagger.module.EnvironmentModule.Companion.SERVER_ID
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
        @Named(SERVER_ID)
        private val guildId: String,
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

        private val botUser: User by lazy { getUser()!! }
        private val emojis: List<Emoji> by lazy {
            handleResponse(
                get<List<Emoji>>(
                    resource = "guilds/$guildId/emojis",
                ),
            )!!
        }
        private val roles: List<Role> by lazy {
            handleResponse(
                get<List<Role>>(
                    resource = "guilds/$guildId/roles",
                ),
            )!!
        }
        private val members: List<Member> by lazy {
            handleResponse(
                get<List<Member>>(
                    resource = "guilds/$guildId/members",
                    queryParams =
                        mapOf(
                            "limit" to "1000",
                        ),
                ),
            )!!
        }

        fun createForumThread(
            name: String,
            parentChannelId: String,
        ): Channel {
            return handleResponse(
                post<Channel>(
                    resource = "channels/$parentChannelId/threads",
                    body =
                        mutableMapOf(
                            "name" to name,
                            "message" to
                                mapOf(
                                    "content" to "Creating thread for $name!",
                                ),
                        ),
                ),
            )!!
        }

        fun getForumThreadByName(
            parentChannelId: String,
            name: String,
        ): Channel? =
            handleResponse(
                get<ListActiveGuildThreadsResponse>(
                    resource = "guilds/$guildId/threads/active",
                ),
            )?.threads?.firstOrNull {
                it.name == name && it.parentId == parentChannelId
            }

        fun createChannel(
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
            )!!
        }

        fun getChannel(channelId: String): Channel =
            handleResponse(
                get<Channel>(
                    resource = "channels",
                    id = channelId,
                ),
            )!!

        fun sendMessage(
            channelId: String,
            message: String? = null,
            mentions: List<String>? = null,
            embeds: List<Embed>? = null,
        ) {
            handleResponse(
                post(
                    resource = "channels/$channelId/messages",
                    body =
                        mapOf(
                            "content" to
                                (message ?: "").apply {
                                    mentions?.let {
                                        this.plus("\n" + mentions.joinToString("") { "<@$it>" })
                                    }
                                },
                            "embeds" to embeds,
                        ),
                ),
            )
        }

        fun getRole(name: String): Role? =
            roles.firstOrNull {
                it.name == name
            }

        fun createRole(name: String): Role =
            handleResponse(
                post<Role>(
                    resource = "guilds/$guildId/roles",
                    body =
                        mapOf(
                            "name" to name,
                        ),
                ),
            )!!

        fun removeRoleFromUser(
            roleId: String,
            userId: String,
        ) {
            handleResponse(
                delete(
                    resource = "guilds/$guildId/members/$userId/roles",
                    id = roleId,
                ),
            )
        }

        fun addRoleToUser(
            roleId: String,
            userId: String,
        ) {
            handleResponse(
                put(
                    resource = "guilds/$guildId/members/$userId/roles",
                    id = roleId,
                ),
            )
        }

        fun createOrUpdateRole(
            name: String,
            members: List<String>? = null,
        ): Role {
            val role =
                getRole(
                    name = name,
                ) ?: createRole(
                    name = name,
                )
            // Remove existing role members
            this.members.forEach {
                if (it.roles.contains(role.id) && it.user != null) {
                    removeRoleFromUser(
                        roleId = role.id,
                        userId = it.user.id,
                    )
                }
            }
            // Add required members
            members?.distinct()?.forEach {
                addRoleToUser(
                    roleId = role.id,
                    userId = it,
                )
            }
            return role
        }

        fun getGuild(id: String): Guild =
            handleResponse(
                get<Guild>(
                    resource = "guilds",
                    id = id,
                ),
            )!!

        fun getUser(id: String? = null) =
            handleResponse(
                get<User>(
                    resource = "users",
                    id = id ?: "@me",
                ),
            )

        fun getEmoji(name: String): Emoji? =
            emojis.firstOrNull {
                it.name == name
            }

        private fun <ErrorType : ErrorResponse, ResponseType : Any?> handleResponse(
            response: Either<ErrorType, ResponseType>?,
        ): ResponseType? {
            return response?.let {
                if (response.left().isPresent) throw Error(response.left().get().message)
                return response.right().get()
            }
        }
    }
