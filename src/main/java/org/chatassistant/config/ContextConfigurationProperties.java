package org.chatassistant.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("context")
public class ContextConfigurationProperties {
    private String storagePath;
    private int verbatimChars;
    private String summarizationModel;
    private String summarizationPromptPath;
}
