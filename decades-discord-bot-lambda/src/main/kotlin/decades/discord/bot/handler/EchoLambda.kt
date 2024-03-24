package decades.discord.bot.handler

import com.amazonaws.services.lambda.runtime.Context
import decades.discord.bot.dagger.DaggerLambdaComponent
import decades.discord.bot.model.EchoInput
import decades.discord.bot.model.EchoOutput
import decades.discord.bot.service.DiscordService
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

class EchoLambda : LambdaHandler<EchoInput, EchoOutput> {
    @Inject
    lateinit var discordService: DiscordService

    constructor() {
        DaggerLambdaComponent.create().inject(this)
    }

    internal constructor(
        discordService: DiscordService,
    ) {
        this.discordService = discordService
    }

    override val inputType: Class<EchoInput> = EchoInput::class.java

    companion object {
        private val LOG = LogManager.getLogger(EchoLambda::class.java)
    }

    override fun handle(
        input: EchoInput,
        context: Context?,
    ): EchoOutput? {
        LOG.info("Input: $input")
        return EchoOutput(
            output = input.input,
        )
    }
}
