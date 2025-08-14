package org.chatassistant.healthcheck;

import org.chatassistant.ai.agent.AgenticGeminiAgent;

public class AgenticGeminiAgentHealthCheck implements HealthCheck {
    public static void main(String[] args){
        final AgenticGeminiAgent agent = AgenticGeminiAgent.getInstance();
        agent.ask("Hello world!");
        agent.kill();
    }
}
