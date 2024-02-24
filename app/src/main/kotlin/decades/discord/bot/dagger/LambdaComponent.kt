package decades.discord.bot.dagger

import dagger.Component
import decades.discord.bot.dagger.module.ServiceModule
import decades.discord.bot.handler.EchoLambda
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ServiceModule::class,
    ],
)
interface LambdaComponent {
    fun inject(echoLambda: EchoLambda)
}
