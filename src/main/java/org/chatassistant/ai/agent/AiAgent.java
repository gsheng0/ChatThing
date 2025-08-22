package org.chatassistant.ai.agent;

import org.chatassistant.ai.tools.annotation.AiAgentTestTool;
import org.reflections.Reflections;
import org.chatassistant.ai.tools.annotation.AiAgentTool;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
        methods.forEach(m -> System.out.println(m.getName()));
        return methods;
    }
}
