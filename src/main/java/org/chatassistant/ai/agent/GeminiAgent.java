package org.chatassistant.ai.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.google.genai.types.Tool;
import org.chatassistant.Util;
import org.chatassistant.ai.tools.ToolHolder;
import org.chatassistant.config.AiAgentConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Qualifier("gemini")
public class GeminiAgent implements AiAgent<GeminiContext> {
    private final Client client;
    private final GenerateContentConfig config;
    private final String modelName;
    private final ObjectMapper om = new ObjectMapper();
    private final Map<String, Method> toolMap;

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    @Autowired
    public GeminiAgent(final AiAgentConfigurationProperties aiAgentConfig, final ToolHolder toolHolder){
        this.modelName = aiAgentConfig.getModelName();
        this.client = new Client();
        this.toolMap = toolHolder.getToolMap(aiAgentConfig.isRealToolSet());
        this.config = getConfig(
                aiAgentConfig.getPromptPath(),
                new ArrayList<>(toolMap.values()));
    }

    private GenerateContentConfig getConfig(final String promptPath, final List<Method> tools){
        return GenerateContentConfig.builder()
                .tools(List.of(
                        Tool.builder().functions(tools).build()))
                .systemInstruction(Content.fromParts(Part.fromText(Util.readFile(promptPath))))
                .build();
    }

    @Override
    public String ask(final GeminiContext context, final String prompt) {
        return ask(context, prompt, List.of());
    }

    @Override
    public String ask(final GeminiContext context, final String prompt, final List<String> imagePaths) {
        final List<Content> history = context.getHistory();
        final List<Part> parts = new ArrayList<>(List.of(Part.fromText(prompt)));
        System.out.println("HERE:" + prompt.substring(0, Math.min(prompt.length(), 100)));

        for(String imagePath : imagePaths){
            parts.add(imagePartOf(imagePath));
        }

        history.add(Content.builder()
                .role("user")
                .parts(parts)
                .build());

        while (true) {
            final GenerateContentResponse resp = client.models.generateContent(modelName, history, config);

            final Content modelContent = resp.candidates().orElseThrow(() -> new RuntimeException("Missing response candidates"))
                    .getFirst().content().orElseThrow(() -> new RuntimeException("Missing content in response candidate"));
            history.add(modelContent);

            final List<Part> callParts = modelContent.parts().orElseThrow(() -> new RuntimeException("Missing parts in response")).stream()
                    .filter(p -> p.functionCall().isPresent()).toList();

            if (callParts.isEmpty()) {
                return resp.text();
            }

            for (Part p : callParts) {
                final FunctionCall fc = p.functionCall().orElseThrow();
                final String toolName = fc.name().orElse("Unknown Tool");
                final Map<String, Object> argsJson = fc.args().orElse(Map.of());

                final Map<String, Object> resultJson = invokeStaticTool(toolName, argsJson);

                history.add(Content.builder()
                        .role("user")
                        .parts(List.of(Part.fromFunctionResponse(toolName, resultJson)))
                        .build());
            }
        }
    }

    @Override
    public void kill(){
        client.close();
    }

    private static Part imagePartOf(final String imagePath){
        try{
            final String extension = imagePath.substring(imagePath.lastIndexOf('.') + 1);
            final String updatedImagePath = imagePath.replace("~", "/Users/georgesheng");
            return Part.fromBytes(Files.readAllBytes(Path.of(updatedImagePath)), "image/" + extension);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    private Map<String, Object> invokeStaticTool(String toolName, Map<String, Object> args) {
        final Method m = toolMap.get(toolName);
        if (m == null) {
            return error("UNKNOWN_TOOL", "Unknown tool: " + toolName, null);
        }
        if (!Modifier.isStatic(m.getModifiers())) {
            return error("INVALID_TOOL", "Tool method is not static: " + toolName, null);
        }

        try {
            Object rawResult;

            Class<?>[] params = m.getParameterTypes();
            if (params.length == 0) {
                rawResult = m.invoke(null);
            } else if (params.length == 1 && Map.class.isAssignableFrom(params[0])) {
                rawResult = m.invoke(null, args == null ? Map.of() : args);
            } else if (params.length == 1 && params[0] == String.class) {
                String json = om.writeValueAsString(args == null ? Map.of() : args);
                rawResult = m.invoke(null, json);
            } else {
                return error(
                        "UNSUPPORTED_SIGNATURE",
                        "Unsupported signature for " + toolName + ": " + signatureOf(m),
                        null
                );
            }

            // Normalize to Map<String,Object> payload
            Object data = normalizeToJsonFriendly(rawResult);

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ok", true);
            out.put("data", data);
            return out;

        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
            return error("TOOL_EXCEPTION", String.valueOf(cause), stackSnippet(cause));
        } catch (Exception e) {
            return error("INVOKE_FAILED", String.valueOf(e), stackSnippet(e));
        }
    }

    /**
     * Converts tool return values into JSON-friendly Java objects:
     * - Map stays Map
     * - String stays String
     * - primitives/lists/maps stay
     * - POJOs become Map via Jackson
     */
    private Object normalizeToJsonFriendly(Object raw) throws Exception {
        if (raw == null) return null;
        if (raw instanceof Map<?, ?>) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) raw;
                return m;
            } catch (ClassCastException cce) {
                return om.convertValue(raw, MAP_TYPE);
            }
        }
        if (raw instanceof String) return raw;
        if (raw instanceof Number || raw instanceof Boolean) return raw;
        // For lists/arrays/POJOs: convert into generic JSON tree (Map/List primitives)
        return om.convertValue(raw, Object.class);
    }

    private Map<String, Object> error(String code, String message, String stack) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", false);

        Map<String, Object> err = new LinkedHashMap<>();
        err.put("code", code);
        err.put("message", message);
        if (stack != null) err.put("stack", stack);

        out.put("error", err);
        return out;
    }

    private static String signatureOf(Method m) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getReturnType().getSimpleName()).append(" ")
                .append(m.getDeclaringClass().getSimpleName()).append(".")
                .append(m.getName()).append("(");
        Class<?>[] ps = m.getParameterTypes();
        for (int i = 0; i < ps.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(ps[i].getSimpleName());
        }
        sb.append(")");
        return sb.toString();
    }

    private static String stackSnippet(Throwable t) {
        StackTraceElement[] st = t.getStackTrace();
        int n = Math.min(st.length, 8);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(st[i]).append("\n");
        return sb.toString().trim();
    }
}