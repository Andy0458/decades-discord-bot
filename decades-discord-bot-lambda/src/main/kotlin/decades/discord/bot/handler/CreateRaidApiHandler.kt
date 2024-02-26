package decades.discord.bot.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import decades.discord.bot.dagger.DaggerLambdaComponent
import decades.discord.bot.model.api.CreateRaidInput
import decades.discord.bot.model.api.CreateRaidOutput
import decades.discord.bot.service.DiscordService
import javax.inject.Inject

class CreateRaidApiHandler : LambdaHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    override val inputType: Class<APIGatewayProxyRequestEvent>
        get() = APIGatewayProxyRequestEvent::class.java

    @Inject
    lateinit var discordService: DiscordService

    constructor() {
        DaggerLambdaComponent.create().inject(this)
    }

    override fun handle(
        input: APIGatewayProxyRequestEvent,
        context: Context,
    ): APIGatewayProxyResponseEvent? {
        val createRaidInput = objectMapper.readValue(input.body, CreateRaidInput::class.java)
        val newChannel =
            discordService.createPrivateThread(
                name = createRaidInput.name,
                parentChannelId = createRaidInput.parentChannelId,
            )
        val message =
            "Your raid has been locked!\n".plus {
                if (createRaidInput.additionalMessage != null) {
                    "Additional note from your raid leader: '${createRaidInput.additionalMessage}'"
                } else {
                    null
                }
            }
        discordService.sendMessage(
            channelId = newChannel.id,
            message = message,
            mentions = createRaidInput.raiders.map { it.userId },
        )

        return APIGatewayProxyResponseEvent()
            .withStatusCode(200)
            .withBody(
                objectMapper.writeValueAsString(
                    CreateRaidOutput(
                        message = "Successfully created raid.",
                    ),
                ),
            )
    }
}
