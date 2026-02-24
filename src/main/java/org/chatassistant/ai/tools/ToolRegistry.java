package org.chatassistant.ai.tools;

import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.ai.tools.annotation.ToolMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class ToolRegistry {
    public record InvocableMethod(Object instance, Method method) {}

    private final ApplicationContext ctx;
    private Map<String, InvocableMethod> toolMap;

    @Autowired
    public ToolRegistry(final ApplicationContext ctx) {
        this.ctx = ctx;
    }

    public synchronized Map<String, InvocableMethod> getToolMap() {
        if (toolMap == null) {
            toolMap = loadToolMap();
        }
        return toolMap;
    }

    private Map<String, InvocableMethod> loadToolMap() {
        final Map<String, InvocableMethod> methods = new HashMap<>();
        ctx.getBeansWithAnnotation(AiAgentTool.class).values().forEach(bean ->
            Arrays.stream(bean.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(ToolMethod.class))
                .forEach(m -> {
                    final String name = m.getName();
                    if (methods.containsKey(name)) {
                        throw new IllegalStateException(
                            "Duplicate @ToolMethod name '" + name + "' — found in both " +
                            methods.get(name).instance().getClass().getSimpleName() +
                            " and " + bean.getClass().getSimpleName());
                    }
                    methods.put(name, new InvocableMethod(bean, m));
                })
        );
        System.out.println("[ToolRegistry] Discovered " + methods.size() + " tools: " + methods.keySet());
        return methods;
    }
}
