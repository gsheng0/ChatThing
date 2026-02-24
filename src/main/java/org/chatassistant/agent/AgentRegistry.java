package org.chatassistant.agent;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class AgentRegistry {
    private Map<String, AgentConfig> agents = new HashMap<>();
}
