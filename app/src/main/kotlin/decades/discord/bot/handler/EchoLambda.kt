package decades.discord.bot.handler

import com.amazonaws.services.lambda.runtime.Context
import decades.discord.bot.model.EchoInput
import decades.discord.bot.model.EchoOutput

class EchoLambda : LambdaHandler<EchoInput, EchoOutput> {
    override val inputType: Class<EchoInput> = EchoInput::class.java

    override fun handle(
        input: EchoInput,
        context: Context,
    ): EchoOutput? =
        EchoOutput(
            output = input.input,
        )
}
