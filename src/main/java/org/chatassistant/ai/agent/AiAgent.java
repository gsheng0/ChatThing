package org.chatassistant.ai.agent;

import org.chatassistant.ai.tools.annotation.AiAgentTestTool;
import org.reflections.Reflections;
import org.chatassistant.ai.tools.annotation.AiAgentTool;

import java.lang.reflect.Method;
import java.util.*;

public interface AiAgent {
    String ask(String prompt);
    String ask(String prompt, List<String> imagePaths);
    void kill();

    static List<Method> getAllTools(final boolean realTools){
        final Reflections reflections = new Reflections("org.chatassistant.ai.tools");
        final List<Method> methods = new ArrayList<>();
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(realTools ? AiAgentTool.class : AiAgentTestTool.class);
        for(Class<?> clazz : annotatedClasses) {
            methods.addAll(Arrays.stream(clazz.getDeclaredMethods()).filter(m -> !m.isSynthetic()).toList());
        }
        return methods;
    }

    static Map<String, Method> getToolMap(final boolean realTools) {
        final Reflections reflections = new Reflections("org.chatassistant.ai.tools");
        final Map<String, Method> methods = new HashMap<>();
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(realTools ? AiAgentTool.class : AiAgentTestTool.class);
        for(Class<?> clazz : annotatedClasses) {
            Arrays.stream(clazz.getDeclaredMethods()).filter(m -> !m.isSynthetic()).forEach(m -> methods.put(m.getName(), m));
        }
        return methods;
    }
}
