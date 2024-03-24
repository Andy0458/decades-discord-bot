package decades.discord.bot.manager

import decades.discord.bot.model.api.CreateRaidInput
import decades.discord.bot.model.api.CreateRaidOutput
import decades.discord.bot.model.api.Raider
import decades.discord.bot.model.api.Role
import decades.discord.bot.model.discord.Embed
import decades.discord.bot.model.discord.EmbedField
import decades.discord.bot.service.DiscordService
import javax.inject.Inject

class RaidManager
    @Inject
    constructor(
        private val discordService: DiscordService,
    ) {
        fun createRaid(createRaidInput: CreateRaidInput): CreateRaidOutput {
            val channelId: String =
                createRaidInput.threadId
                    ?: createRaidInput.parentChannelId?.let {
                        discordService.getForumThreadByName(
                            name = createRaidInput.team,
                            parentChannelId = createRaidInput.parentChannelId,
                        )?.id ?: discordService.createForumThread(
                            name = createRaidInput.team,
                            parentChannelId = createRaidInput.parentChannelId,
                        ).id
                    }
                    ?: throw Error("Either threadId or parentChanelId must be specified.")

            val role =
                discordService.createOrUpdateRole(
                    name = createRaidInput.team,
                    members = createRaidInput.raiders.map { it.userId },
                )

            discordService.sendMessage(
                channelId = channelId,
                message = role.mention(),
                embeds =
                    getRaidNotificationEmbeds(
                        name = createRaidInput.name,
                        description =
                            "Your raid has been **LOCKED**! If you have an availability " +
                                "issue, please reach out to your raid leader.",
                        leader = createRaidInput.leader,
                        time = createRaidInput.startTime,
                        raiders = createRaidInput.raiders,
                        additionalMessage = createRaidInput.additionalMessage,
                    ),
            )

            return CreateRaidOutput(
                message = "Successfully created raid.",
            )
        }

        private fun getRaidNotificationEmbeds(
            name: String,
            description: String? = null,
            leader: String,
            time: Int,
            raiders: List<Raider>,
            additionalMessage: String? = null,
        ): List<Embed> {
            val fields =
                mutableListOf<EmbedField>(
                    // Header
                    EmbedField(
                        name = "Raid Leader",
                        value = "<@$leader>",
                        inline = true,
                    ),
                    EmbedField(
                        name = "Start Time",
                        value = "<t:$time:F>",
                        inline = true,
                    ),
                ).apply {
                    // Optional header
                    additionalMessage?.let {
                        this.add(
                            EmbedField(
                                name = "Notes from your raid leader",
                                value = it,
                                inline = false, // Explicit false to ensure additional message/raid comp is below
                            ),
                        )
                    }

                    // Raid Comp
                    val tanks = raiders.filter { it.characterRole == Role.TANK }
                    val meleeDps = raiders.filter { it.characterRole == Role.MELEE_DPS }
                    val rangedDps = raiders.filter { it.characterRole == Role.RANGED_DPS }
                    val healers = raiders.filter { it.characterRole == Role.HEALER }
                    this.add(
                        EmbedField(
                            name = "Tanks (${tanks.size})",
                            value =
                                tanks.joinToString(
                                    separator = "\n",
                                ) {
                                    getRaiderLabel(it)
                                },
                            inline = true,
                        ),
                    )
                    this.add(
                        EmbedField(
                            name = "Melee DPS (${meleeDps.size})",
                            value =
                                meleeDps.joinToString(
                                    separator = "\n",
                                ) {
                                    getRaiderLabel(it)
                                },
                            inline = true,
                        ),
                    )
                    this.add(
                        EmbedField(
                            name = "Ranged DPS (${rangedDps.size})",
                            value =
                                rangedDps.joinToString(
                                    separator = "\n",
                                ) {
                                    getRaiderLabel(it)
                                },
                            inline = true,
                        ),
                    )
                    this.add(
                        EmbedField(
                            name = "Healers (${healers.size})",
                            value =
                                healers.joinToString(
                                    separator = "\n",
                                ) {
                                    getRaiderLabel(it)
                                },
                            inline = true,
                        ),
                    )
                }
            return listOf(
                Embed(
                    title = name,
                    description = description,
                    fields = fields,
                ),
            )
        }

        private fun getRaiderLabel(raider: Raider): String {
            val roleEmoji = discordService.getEmoji("${raider.characterClass.lowercase()}_${raider.characterRole.name.lowercase()}")
            val emojiString = roleEmoji?.toString()
            return (emojiString ?: "").plus(" ${raider.characterName} (<@${raider.userId}>)")
        }
    }
