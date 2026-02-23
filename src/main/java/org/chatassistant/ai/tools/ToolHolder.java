package org.chatassistant.ai.tools;

import org.chatassistant.ai.tools.annotation.AiAgentTestTool;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class ToolHolder {
    public record InvocableMethod(Object instance, Method method) {}

    private final ApplicationContext ctx;
    private Map<String, InvocableMethod> realToolMap;
    private Map<String, InvocableMethod> testToolMap;

    @Autowired
    public ToolHolder(final ApplicationContext ctx) {
        this.ctx = ctx;
    }

    public Map<String, InvocableMethod> getToolMap(final boolean realTools) {
        if (realTools) {
            if (realToolMap == null) {
                realToolMap = loadToolMap(ctx, AiAgentTool.class);
            }
            return realToolMap;
        } else {
            if (testToolMap == null) {
                testToolMap = loadToolMap(ctx, AiAgentTestTool.class);
            }
            return testToolMap;
        }
    }

    private Map<String, InvocableMethod> loadToolMap(final ApplicationContext ctx, final Class<? extends Annotation> annotation) {
        final Map<String, InvocableMethod> methods = new HashMap<>();
        ctx.getBeansWithAnnotation(annotation).values().forEach(bean ->
            Arrays.stream(bean.getClass().getDeclaredMethods())
                .filter(m -> !m.isSynthetic())
                .forEach(m -> methods.put(m.getName(), new InvocableMethod(bean, m)))
        );
        return methods;
    }
}
