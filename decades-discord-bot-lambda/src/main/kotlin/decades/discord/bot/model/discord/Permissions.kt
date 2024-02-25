package decades.discord.bot.model.discord

/**
 * https://discord.com/developers/docs/topics/permissions#permissions
 */
typealias Permissions = Int

const val ADD_REACTIONS: Permissions = 1 shl 6
const val VIEW_CHANNEL: Permissions = 1 shl 10
const val SEND_MESSAGES: Permissions = 1 shl 11
const val MENTION_EVERYONE: Permissions = 1 shl 17
const val USE_EXTERNAL_EMOJIS: Permissions = 1 shl 18
const val SEND_MESSAGES_IN_THREADS: Permissions = 1 shl 38

const val DEFAULT_PERMISSIONS: Permissions =
    ADD_REACTIONS or VIEW_CHANNEL or
        SEND_MESSAGES or MENTION_EVERYONE or
        USE_EXTERNAL_EMOJIS or SEND_MESSAGES_IN_THREADS
