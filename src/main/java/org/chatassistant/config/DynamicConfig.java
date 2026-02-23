package org.chatassistant.config;

import java.util.HashMap;
import java.util.Map;

public class DynamicConfig {
    private Map<String, Capability> capabilities = new HashMap<>();

    public static class Capability {
        private String provider = "claude";
        private String promptPath;
        private String modelName;
        private boolean realToolSet = true;
        private String chat;
        private boolean enabled = true;

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }

        public String getPromptPath() { return promptPath; }
        public void setPromptPath(String promptPath) { this.promptPath = promptPath; }

        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }

        public boolean isRealToolSet() { return realToolSet; }
        public void setRealToolSet(boolean realToolSet) { this.realToolSet = realToolSet; }

        public String getChat() { return chat; }
        public void setChat(String chat) { this.chat = chat; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public Map<String, Capability> getCapabilities() { return capabilities; }
    public void setCapabilities(Map<String, Capability> capabilities) { this.capabilities = capabilities; }
}
