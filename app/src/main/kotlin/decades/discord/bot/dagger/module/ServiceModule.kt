package decades.discord.bot.dagger.module

import dagger.Module
import dagger.Provides
import decades.discord.bot.service.DiscordService
import javax.inject.Singleton

@Module
class ServiceModule {
    @Provides
    @Singleton
    fun providesDiscordService(): DiscordService = DiscordService()
}
