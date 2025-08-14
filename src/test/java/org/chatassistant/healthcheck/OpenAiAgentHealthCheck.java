package org.chatassistant.healthcheck;

import org.chatassistant.ai.agent.OpenAiAgent;

public class OpenAiAgentHealthCheck implements HealthCheck {
    public static void main(String[] args){
        final OpenAiAgent agent = OpenAiAgent.getInstance();
        agent.ask("Hello world!");
        agent.kill();
    }
}
