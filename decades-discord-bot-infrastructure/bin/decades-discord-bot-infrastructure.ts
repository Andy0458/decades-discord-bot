#!/usr/bin/env node
import 'source-map-support/register';
import { DecadesDiscordBotInfrastructureStack } from '../lib/decades-discord-bot-infrastructure-stack';
import { App } from 'aws-cdk-lib';

const stages = [
  {
    stage: 'personal',
    account: process.env.PERSONAL_ACCOUNT_ID,
    region: 'us-east-1',
    serverId: '1089073001728856195',
  },
  {
    stage: 'prod',
    account: '533267375856',
    region: 'us-east-1',
    serverId: '170966255409430528',
  }
]

const app = new App();
stages.forEach((stage) => {
  new DecadesDiscordBotInfrastructureStack(app, `${stage.stage}-DecadesDiscordBotInfrastructureStack`, {
    ...stage,
    env: { 
      account: stage.account,
      region: stage.region,
    },
  });
});
