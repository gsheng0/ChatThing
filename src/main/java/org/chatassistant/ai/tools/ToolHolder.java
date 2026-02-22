package org.chatassistant.ai.tools;

import org.chatassistant.ai.tools.annotation.AiAgentTestTool;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
public class ToolHolder {
    private final Map<String, Method> realToolMap;
    private final Map<String, Method> testToolMap;

    public ToolHolder() {
        realToolMap = loadToolMap(true);
        testToolMap = loadToolMap(false);
    }

    public Map<String, Method> getToolMap(final boolean realTools) {
        return realTools ? realToolMap : testToolMap;
    }

    private Map<String, Method> loadToolMap(final boolean realTools) {
        final Reflections reflections = new Reflections("org.chatassistant.ai.tools");
        final Map<String, Method> methods = new HashMap<>();
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(realTools ? AiAgentTool.class : AiAgentTestTool.class);
        for(Class<?> clazz : annotatedClasses) {
            Arrays.stream(clazz.getDeclaredMethods()).filter(m -> !m.isSynthetic()).forEach(m -> methods.put(m.getName(), m));
        }
        return methods;
    }
}
