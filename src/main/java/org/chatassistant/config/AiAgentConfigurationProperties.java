package org.chatassistant.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("agent")
public class AiAgentConfigurationProperties {
    private String promptPath;
    private String modelName;
    private boolean realToolSet;
}
