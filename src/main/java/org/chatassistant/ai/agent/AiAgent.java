package org.chatassistant.ai.agent;

import org.reflections.Reflections;
import org.chatassistant.ai.tools.AiAgentTool;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface AiAgent {
    String ask(String prompt);
    String ask(String prompt, List<String> imagePaths);
    void kill();



    public static List<Method> getAllTools(){
        final Reflections reflections = new Reflections("org.scheduler.ai.tools");
        final Set<Class<? extends AiAgentTool>> tools = reflections.getSubTypesOf(AiAgentTool.class);
        final List<Method> methods = new ArrayList<>();

        for(Class<? extends AiAgentTool> tool : tools){
            methods.add(tool.getMethods()[0]);
        }
        return methods;
    }
}
