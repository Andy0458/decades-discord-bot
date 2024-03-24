import { Duration, Stack, StackProps } from 'aws-cdk-lib';
import { ApiDefinition, SpecRestApi } from 'aws-cdk-lib/aws-apigateway';
import { LambdaDeploymentConfig, LambdaDeploymentGroup } from 'aws-cdk-lib/aws-codedeploy';
import { Effect, PolicyStatement, ServicePrincipal } from 'aws-cdk-lib/aws-iam';
import { Alias, Code, Function, Runtime, SnapStartConf } from 'aws-cdk-lib/aws-lambda';
import { Secret } from 'aws-cdk-lib/aws-secretsmanager';
import { Construct } from 'constructs';
import { readFileSync } from 'fs';
import path = require('path');

export interface DecadesDiscordBotInfrastructureStackProps extends StackProps {
  readonly stage: string
  readonly serverId: string
}

export class DecadesDiscordBotInfrastructureStack extends Stack {
  constructor(scope: Construct, id: string, props: DecadesDiscordBotInfrastructureStackProps) {
    super(scope, id, props);
    const discordBotTokenSecret = new Secret(this, 'DiscordBotToken');

    const apiDefinition = this.getApiDefinition();
    const handlers = this.createApiOperationHandlers(apiDefinition, {
        DiscordBotTokenSecretId: discordBotTokenSecret.secretName,
        ServerId: props.serverId,
    });

    const api = new SpecRestApi(this, 'RestApi', {
      deploy: true,
      deployOptions: {
        stageName: props.stage
      },
      apiDefinition: ApiDefinition.fromInline(apiDefinition),
    });
    handlers.forEach((handler: Alias) => {
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

  private createApiOperationHandlers(apiDefinition: any, env?: any): Map<String, Alias> {
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
          memorySize: 512,
          timeout: Duration.seconds(30),
          environment: env,
          snapStart: SnapStartConf.ON_PUBLISHED_VERSIONS,
        });
        const version = handler.currentVersion;
        const alias = new Alias(this, `${opId}ApiHandlerLambdaAlias`, {
          aliasName: 'live',
          version: version,
        });
        new LambdaDeploymentGroup(this, `${opId}ApiHandlerLambdaDeploymentGroup`, {
          alias: alias,
          deploymentConfig: LambdaDeploymentConfig.ALL_AT_ONCE,
        });
        const integration = op["x-amazon-apigateway-integration"];
        integration.uri = `arn:${this.partition}:apigateway:${this.region}:lambda:path/2015-03-31/functions/${alias.functionArn}/invocations`;
        map.set(op.operationId, alias);
      }
    }
    return map;
  }
}
