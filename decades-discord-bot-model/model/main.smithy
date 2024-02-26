$version: "2.0"

namespace decades.discord.bot

use aws.auth#sigv4
use aws.protocols#restJson1

@restJson1
@sigv4(name: "execute-api")
service DecadesDiscordBotService {
    version: "1.0"
    operations: [
        CreateRaid
    ]
}