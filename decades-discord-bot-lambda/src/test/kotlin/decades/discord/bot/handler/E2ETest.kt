package decades.discord.bot.handler

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.fasterxml.jackson.databind.ObjectMapper
import decades.discord.bot.manager.RaidManager
import decades.discord.bot.model.api.CreateRaidInput
import decades.discord.bot.model.api.Raider
import decades.discord.bot.model.api.Role
import decades.discord.bot.service.DiscordService
import org.junit.jupiter.api.Test
import java.net.http.HttpClient

class E2ETest {
    @Test
    fun `E2E Test`() {
        // Replace with botToken value
        val botToken = ""
        val guildId = ""
        val discordService =
            DiscordService(
                httpClient = HttpClient.newHttpClient(),
                guildId = guildId,
                botToken = botToken,
            )
        val raidManager =
            RaidManager(
                discordService = discordService,
            )
        val handler =
            CreateRaidApiHandler(
                raidManager = raidManager,
            )
        val resp =
            handler.handle(
                APIGatewayProxyRequestEvent()
                    .withBody(
                        ObjectMapper().writeValueAsString(
                            CreateRaidInput(
                                name = "Gnomeregan Lockout 13",
                                team = "Super Goobers",
                                leader = "138726522037993472",
                                startTime = 1711500886,
                                raiders =
                                    listOf(
                                        Raider(
                                            userId = "138726522037993472",
                                            characterName = "Ophemia",
                                            characterClass = "Shaman",
                                            characterRole = Role.HEALER,
                                        ),
                                        Raider(
                                            userId = "138726522037993472",
                                            characterName = "Ophemia",
                                            characterClass = "Shaman",
                                            characterRole = Role.RANGED_DPS,
                                        ),
                                        Raider(
                                            userId = "138726522037993472",
                                            characterName = "Ophemia",
                                            characterClass = "Shaman",
                                            characterRole = Role.RANGED_DPS,
                                        ),
                                        Raider(
                                            userId = "138726522037993472",
                                            characterName = "Ophemia",
                                            characterClass = "Shaman",
                                            characterRole = Role.MELEE_DPS,
                                        ),
                                        Raider(
                                            userId = "138726522037993472",
                                            characterName = "Ophemia",
                                            characterClass = "Shaman",
                                            characterRole = Role.MELEE_DPS,
                                        ),
                                        Raider(
                                            userId = "138726522037993472",
                                            characterName = "Ophemia",
                                            characterClass = "Shaman",
                                            characterRole = Role.MELEE_DPS,
                                        ),
                                        Raider(
                                            userId = "138726522037993472",
                                            characterName = "Ophemia",
                                            characterClass = "Shaman",
                                            characterRole = Role.RANGED_DPS,
                                        ),
                                        Raider(
                                            userId = "138726522037993472",
                                            characterName = "Ophemia",
                                            characterClass = "Shaman",
                                            characterRole = Role.RANGED_DPS,
                                        ),
                                        Raider(
                                            userId = "138726522037993472",
                                            characterName = "Ophemia",
                                            characterClass = "Shaman",
                                            characterRole = Role.TANK,
                                        ),
                                        Raider(
                                            userId = "138726522037993472",
                                            characterName = "Ophemia",
                                            characterClass = "Shaman",
                                            characterRole = Role.HEALER,
                                        ),
                                    ),
                                additionalMessage = "Additional message",
                                parentChannelId = "1221178421371994286",
//                                threadId = "1221178606617890826",
                            ),
                        ),
                    ),
                null,
            )
        println(resp.toString())
    }
}
