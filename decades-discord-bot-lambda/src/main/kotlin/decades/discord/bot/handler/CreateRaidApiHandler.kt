package decades.discord.bot.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.fasterxml.jackson.core.JsonProcessingException
import decades.discord.bot.dagger.DaggerLambdaComponent
import decades.discord.bot.manager.RaidManager
import decades.discord.bot.model.api.CreateRaidInput
import decades.discord.bot.model.api.CreateRaidOutput
import java.lang.Exception
import javax.inject.Inject

class CreateRaidApiHandler : LambdaHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    override val inputType: Class<APIGatewayProxyRequestEvent>
        get() = APIGatewayProxyRequestEvent::class.java

    @Inject
    lateinit var raidManager: RaidManager

    constructor() {
        DaggerLambdaComponent.create().inject(this)
    }

    constructor(raidManager: RaidManager) {
        this.raidManager = raidManager
    }

    override fun handle(
        input: APIGatewayProxyRequestEvent,
        context: Context?,
    ): APIGatewayProxyResponseEvent? =
        try {
            val createRaidInput = objectMapper.readValue(input.body, CreateRaidInput::class.java)
            val createRaidOutput = raidManager.createRaid(createRaidInput)
            APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(
                    objectMapper.writeValueAsString(
                        createRaidOutput,
                    ),
                )
        } catch (e: JsonProcessingException) {
            APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody(
                    objectMapper.writeValueAsString(
                        CreateRaidOutput(
                            message = "Invalid Input.",
                        ),
                    ),
                )
        } catch (e: Exception) {
            APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody(
                    objectMapper.writeValueAsString(
                        CreateRaidOutput(
                            message = e.toString(),
                        ),
                    ),
                )
        }
}
