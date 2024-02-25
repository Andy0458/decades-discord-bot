package decades.discord.bot.model.discord

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Channel types
 * @see <a href=https://discord.com/developers/docs/resources/channel#channel-object-channel-types>Channel Types</a>
 */
typealias ChannelType = Int

const val GUILD_TEXT_CHANNEL: ChannelType = 0
const val PRIVATE_THREAD: ChannelType = 12

data class Channel(
    val id: String,
    val type: ChannelType,
    @JsonProperty("guild_id")
    val guildId: String,
    val name: String,
    @JsonProperty("parent_id")
    val parentId: String? = null,
)
