package decades.discord.bot.handler

import com.amazonaws.services.lambda.runtime.RequestHandler
import decades.discord.bot.model.EchoOutput
import decades.discord.bot.model.EchoInput


class LambdaHandler: RequestHandler<EchoInput, EchoOutput