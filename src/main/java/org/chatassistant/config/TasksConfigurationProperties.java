package org.chatassistant.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties("tasks")
public class TasksConfigurationProperties {
    private String adminChat;
    private Map<String, CapabilityConfig> capabilities = new HashMap<>();

    @Getter
    @Setter
    public static class CapabilityConfig {
        private String provider = "gemini";
        private String promptPath;
        private String modelName;
        private boolean realToolSet;
        private List<String> chats = new ArrayList<>();
    }
}
