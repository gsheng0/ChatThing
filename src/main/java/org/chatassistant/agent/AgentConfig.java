package org.chatassistant.agent;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class AgentConfig {
    private String provider = "claude";
    private String promptPath;
    private String modelName;
    private List<String> chats = new ArrayList<>();
    private boolean enabled = true;
}
