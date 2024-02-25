package decades.discord.bot.dagger.module

import dagger.Module
import dagger.Provides
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import javax.inject.Singleton

@Module
class AwsModule {
    @Provides
    @Singleton
    fun providesSecretsManagerClient(): SecretsManagerClient = SecretsManagerClient.create()
}
