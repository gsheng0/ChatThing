package org.chatassistant.healthcheck;

import org.chatassistant.ai.agent.GeminiAgent;

public class GeminiAgentHealthCheck implements HealthCheck {
    public static void main(String[] args){
        final GeminiAgent agent = GeminiAgent.getInstance();
        agent.ask("Hello world!");
        agent.kill();
    }

}
