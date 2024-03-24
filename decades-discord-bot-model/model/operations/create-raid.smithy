$version: "2.0"

namespace decades.discord.bot

@aws.apigateway#integration(
    type: "aws_proxy",
    httpMethod: "POST",
    uri: ""
)
@http(method: "POST", uri: "/raids")
operation CreateRaid {
    input := {
        @required
        name: String
        @required
        team: String
        @required
        leader: String
        @required
        raiders: Raiders
        @required
        startTime: Integer
        parentChannelId: String
        threadId: String
        additionalMessage: String
    }
    output := {
        message: String
    }
}
