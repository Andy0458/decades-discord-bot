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
        raiders: Raiders
        @required
        startTime: Timestamp
        @required
        parentChannelId: String
        additionalMessage: String
    }
    output := {
        message: String
    }
}
