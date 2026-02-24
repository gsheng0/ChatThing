package org.chatassistant.config;

import lombok.Getter;
import lombok.Setter;
import org.chatassistant.agent.AgentConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties("agents")
public class AgentsConfigurationProperties {
    private String adminChat;
    private Map<String, AgentConfig> agents = new HashMap<>();
}
