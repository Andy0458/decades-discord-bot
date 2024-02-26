$version: "2.0"

namespace decades.discord.bot

structure Raider {
    userId: String
    characterName: String
    characterClass: String
    characterRole: Role
}

list Raiders {
    member: Raider
}

enum Role {
    TANK,
    DPS,
    HEALER
}

structure NotificationConfiguration {

}
