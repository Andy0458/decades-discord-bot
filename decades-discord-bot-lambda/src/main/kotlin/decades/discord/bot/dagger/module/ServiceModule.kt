package decades.discord.bot.dagger.module

import dagger.Module
import dagger.Provides
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import java.net.http.HttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module(
    includes = [
        AwsModule::class,
        EnvironmentModule::class,
    ],
)
class ServiceModule {
    companion object {
        const val DISCORD_BOT_TOKEN = "DISCORD_BOT_TOKEN"
    }

    @Provides
    @Singleton
    fun providesHttpClient(): HttpClient = HttpClient.newHttpClient()

    @Provides
    @Singleton
    @Named(DISCORD_BOT_TOKEN)
    fun providesDiscordBotToken(
        awsSecretsManagerClient: SecretsManagerClient,
        @Named(EnvironmentModule.ENV_DISCORD_BOT_TOKEN_SECRET_ID)
        discordBotTokenSecretId: String,
    ): String =
        awsSecretsManagerClient.getSecretValue {
            it.secretId(discordBotTokenSecretId)
        }.secretString()
}
