package org.chatassistant.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("agent")
@Setter
@Getter
public class AiAgentConfig {
    private String promptPath;
    private String modelName;
}
