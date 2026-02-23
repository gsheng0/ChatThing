package org.chatassistant.ai.agent;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.beta.messages.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chatassistant.Util;
import org.chatassistant.ai.tools.ToolHolder;
import org.chatassistant.ai.tools.ToolHolder.InvocableMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class ClaudeAgent implements AiAgent<GeminiContext> {
    private final AnthropicClient client;
    private final String modelName;
    private final String systemPrompt;
    private final List<BetaTool> tools;
    private final Map<String, InvocableMethod> toolMap;
    private final ObjectMapper om = new ObjectMapper();

    // Per-context conversation history, keyed by GeminiContext identity
    private final IdentityHashMap<GeminiContext, MessageCreateParams.Builder> builders = new IdentityHashMap<>();

    public ClaudeAgent(final String modelName, final String promptPath, final boolean realToolSet, final ToolHolder toolHolder) {
        this.client = AnthropicOkHttpClient.fromEnv();
        this.modelName = modelName;
        this.systemPrompt = Util.readFile(promptPath);
        this.toolMap = toolHolder.getToolMap(realToolSet);
        this.tools = buildTools(toolMap);
    }

    @Override
    public String ask(final GeminiContext context, final String prompt) {
        return ask(context, prompt, List.of());
    }

    @Override
    public String ask(final GeminiContext context, final String prompt, final List<String> imagePaths) {
        final MessageCreateParams.Builder builder = builders.computeIfAbsent(context, k -> {
            final MessageCreateParams.Builder b = MessageCreateParams.builder()
                    .model(modelName)
                    .maxTokens(4096L)
                    .system(systemPrompt);
            tools.forEach(b::addTool);
            return b;
        });

        if (imagePaths.isEmpty()) {
            builder.addUserMessage(prompt);
        } else {
            // Summarize each image to text; store only the summary in history
            final StringBuilder userMessage = new StringBuilder();
            if (!prompt.isEmpty()) {
                userMessage.append(prompt).append("\n\n");
            }
            for (final String imagePath : imagePaths) {
                userMessage.append(summarizeImage(imagePath)).append("\n");
            }
            builder.addUserMessage(userMessage.toString().trim());
        }

        while (true) {
            final BetaMessage response = client.beta().messages().create(builder.build());

            final List<BetaToolUseBlock> toolUseBlocks = response.content().stream()
                    .flatMap(b -> b.toolUse().stream())
                    .toList();

            if (toolUseBlocks.isEmpty()) {
                return response.content().stream()
                        .flatMap(b -> b.text().stream())
                        .map(BetaTextBlock::text)
                        .findFirst()
                        .orElse(null);
            }

            // Add assistant's tool-use message to history
            final List<BetaContentBlockParam> assistantParts = response.content().stream()
                    .map(this::toParam)
                    .filter(Objects::nonNull)
                    .toList();
            builder.addAssistantMessageOfBetaContentBlockParams(assistantParts);

            // Invoke tools and collect results
            final List<BetaContentBlockParam> toolResults = new ArrayList<>();
            for (final BetaToolUseBlock toolUse : toolUseBlocks) {
                final String result = invokeTool(toolUse.name(), toolUse._input());
                toolResults.add(BetaContentBlockParam.ofToolResult(
                        BetaToolResultBlockParam.builder()
                                .toolUseId(toolUse.id())
                                .content(result)
                                .build()));
            }
            builder.addUserMessageOfBetaContentBlockParams(toolResults);
        }
    }

    @Override
    public void kill() {
        client.close();
    }

    private BetaContentBlockParam toParam(final BetaContentBlock block) {
        if (block.isText()) {
            return BetaContentBlockParam.ofText(
                    BetaTextBlockParam.builder().text(block.asText().text()).build());
        }
        if (block.isToolUse()) {
            final BetaToolUseBlock tu = block.asToolUse();
            return BetaContentBlockParam.ofToolUse(
                    BetaToolUseBlockParam.builder()
                            .id(tu.id())
                            .name(tu.name())
                            .input(tu._input())
                            .build());
        }
        return null;
    }

    private String invokeTool(final String toolName, final JsonValue inputJson) {
        final InvocableMethod invocable = toolMap.get(toolName);
        if (invocable == null) return "{\"ok\":false,\"error\":\"Unknown tool: " + toolName + "\"}";

        final Method m = invocable.method();
        try {
            final Map<String, JsonValue> jsonArgs = inputJson.convert(new com.fasterxml.jackson.core.type.TypeReference<>() {});
            final Parameter[] parameters = m.getParameters();
            final Object rawResult;

            if (parameters.length == 0) {
                rawResult = m.invoke(invocable.instance());
            } else {
                final Object[] argValues = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    final Class<?> type = parameters[i].getType();
                    final JsonValue val = jsonArgs.get(parameters[i].getName());
                    @SuppressWarnings("unchecked")
                    final Class<Object> boxed = (Class<Object>) (type.isPrimitive() ? box(type) : type);
                    argValues[i] = val == null ? null : val.convert(boxed);
                }
                rawResult = m.invoke(invocable.instance(), argValues);
            }

            final Map<String, Object> out = new LinkedHashMap<>();
            out.put("ok", true);
            out.put("data", rawResult);
            return om.writeValueAsString(out);

        } catch (InvocationTargetException ite) {
            final Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
            return "{\"ok\":false,\"error\":\"" + cause.getMessage() + "\"}";
        } catch (Exception e) {
            return "{\"ok\":false,\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private static Class<?> box(final Class<?> primitive) {
        if (primitive == double.class) return Double.class;
        if (primitive == float.class) return Float.class;
        if (primitive == int.class) return Integer.class;
        if (primitive == long.class) return Long.class;
        if (primitive == boolean.class) return Boolean.class;
        return primitive;
    }

    private String summarizeImage(final String imagePath) {
        final List<BetaContentBlockParam> parts = List.of(
            BetaContentBlockParam.ofText(BetaTextBlockParam.builder()
                .text("Describe the content of this image in detail. Include all visible text, " +
                      "numbers, dates, names, and any other relevant information.")
                .build()),
            buildImageBlock(imagePath)
        );

        final BetaMessage response = client.beta().messages().create(
            MessageCreateParams.builder()
                .model(modelName)
                .maxTokens(1024L)
                .addUserMessageOfBetaContentBlockParams(parts)
                .build()
        );

        return response.content().stream()
            .flatMap(b -> b.text().stream())
            .map(BetaTextBlock::text)
            .findFirst()
            .orElse("[Image: could not extract content]");
    }

    private BetaContentBlockParam buildImageBlock(final String imagePath) {
        final String b64 = Util.getJpegBase64String(imagePath);
        return BetaContentBlockParam.ofImage(BetaImageBlockParam.builder()
                .source(BetaImageBlockParam.Source.ofBase64(BetaBase64ImageSource.builder()
                        .mediaType(BetaBase64ImageSource.MediaType.IMAGE_JPEG)
                        .data(b64)
                        .build()))
                .build());
    }

    private List<BetaTool> buildTools(final Map<String, InvocableMethod> toolMap) {
        final List<BetaTool> result = new ArrayList<>();
        for (final Map.Entry<String, InvocableMethod> entry : toolMap.entrySet()) {
            final Method m = entry.getValue().method();
            result.add(BetaTool.builder()
                    .name(m.getName())
                    .description(m.getName())
                    .inputSchema(buildInputSchema(m))
                    .build());
        }
        return result;
    }

    private BetaTool.InputSchema buildInputSchema(final Method m) {
        final Parameter[] params = m.getParameters();
        final Map<String, Object> properties = new LinkedHashMap<>();
        final List<String> required = new ArrayList<>();

        for (final Parameter p : params) {
            properties.put(p.getName(), Map.of("type", javaTypeToJsonType(p.getType())));
            required.add(p.getName());
        }

        final BetaTool.InputSchema.Builder schema = BetaTool.InputSchema.builder()
                .properties(JsonValue.from(properties));
        if (!required.isEmpty()) {
            schema.putAdditionalProperty("required", JsonValue.from(required));
        }
        return schema.build();
    }

    private static String javaTypeToJsonType(final Class<?> type) {
        if (type == String.class) return "string";
        if (type == double.class || type == Double.class || type == float.class || type == Float.class) return "number";
        if (type == int.class || type == Integer.class || type == long.class || type == Long.class) return "integer";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        return "string";
    }
}
