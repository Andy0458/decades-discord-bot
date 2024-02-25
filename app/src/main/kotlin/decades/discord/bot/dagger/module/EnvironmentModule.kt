package decades.discord.bot.dagger.module

import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class EnvironmentModule {
    companion object {
        const val ENV_DISCORD_BOT_TOKEN_SECRET_ID = "DiscordBotTokenSecretId"
    }

    @Provides
    @Singleton
    @Named(ENV_DISCORD_BOT_TOKEN_SECRET_ID)
    fun providesDiscordBotTokenSecretId(): String = System.getenv(ENV_DISCORD_BOT_TOKEN_SECRET_ID)
}
