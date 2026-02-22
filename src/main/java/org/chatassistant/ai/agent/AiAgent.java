package org.chatassistant.ai.agent;

import java.util.List;

public interface AiAgent<C> {
    String ask(C context, String prompt);
    String ask(C context, String prompt, List<String> imagePaths);
    void kill();

}
