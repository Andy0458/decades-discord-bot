import { Duration, Stack, StackProps } from 'aws-cdk-lib';
import { ApiDefinition, SpecRestApi } from 'aws-cdk-lib/aws-apigateway';
import { Effect, PolicyStatement, ServicePrincipal } from 'aws-cdk-lib/aws-iam';
import { Code, Function, Runtime } from 'aws-cdk-lib/aws-lambda';
import { Secret } from 'aws-cdk-lib/aws-secretsmanager';
import { Construct } from 'constructs';
import { readFileSync } from 'fs';
import path = require('path');

export interface DecadesDiscordBotInfrastructureStackProps extends StackProps {
  readonly stage: string
}

export class DecadesDiscordBotInfrastructureStack extends Stack {
  constructor(scope: Construct, id: string, props: DecadesDiscordBotInfrastructureStackProps) {
    super(scope, id, props);
    const discordBotTokenSecret = new Secret(this, 'DiscordBotToken');

    const apiDefinition = this.getApiDefinition();
    const handlers = this.createApiOperationHandlers(apiDefinition, {
        DiscordBotTokenSecretId: discordBotTokenSecret.secretName,
    });

    const api = new SpecRestApi(this, 'RestApi', {
      deploy: true,
      deployOptions: {
        stageName: props.stage
      },
      apiDefinition: ApiDefinition.fromInline(apiDefinition),
    });
    handlers.forEach((handler: Function) => {
      handler.addPermission(`APIGInvoke`, {
        principal: new ServicePrincipal("apigateway.amazonaws.com"),
        sourceArn: `arn:${this.partition}:execute-api:${this.region}:${this.account}:${api.restApiId}/*/*/*`,
      });
      handler.addToRolePolicy(new PolicyStatement({
        effect: Effect.ALLOW,
        actions: ["secretsmanager:GetSecretValue"],
        resources: [discordBotTokenSecret.secretArn],
      }));
    });
  }

  private getApiDefinition() {
    return JSON.parse(
      readFileSync(
        path.join(__dirname, '../../decades-discord-bot-model/build/smithyprojections/decades-discord-bot-model/apigateway/openapi/DecadesDiscordBotService.openapi.json'),
        'utf8'
      )
    );
  }

  private createApiOperationHandlers(apiDefinition: any, env?: any): Map<String, Function> {
    const map = new Map()
    for (const p in apiDefinition.paths) {
      for (const operation in apiDefinition.paths[p]) {
        const op = apiDefinition.paths[p][operation]
        const opId = op.operationId;
        const handler = new Function(this, `${opId}ApiHandler`, {
          runtime: Runtime.JAVA_21,
          code: Code.fromAsset(
            path.join(__dirname, '../../decades-discord-bot-lambda/build/libs/decades-discord-bot-lambda-all.jar'),
          ),
          handler: `decades.discord.bot.handler.${opId}ApiHandler::handleRequest`,
          timeout: Duration.seconds(30),
          environment: env,
        });
        const integration = op["x-amazon-apigateway-integration"];
        integration.uri = `arn:${this.partition}:apigateway:${this.region}:lambda:path/2015-03-31/functions/${handler.functionArn}/invocations`;
        map.set(op.operationId, handler);
      }
    }
    return map;
  }
}
