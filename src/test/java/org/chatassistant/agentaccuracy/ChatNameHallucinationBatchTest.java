package org.chatassistant.agentaccuracy;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.*;
import com.anthropic.models.messages.batches.*;
import com.fasterxml.jackson.core.type.TypeReference;
import org.chatassistant.Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Submits 5000 pre-primed conversations to the Anthropic Batch API and measures how often
 * the expenses agent calls sendTextMessage with an incorrect chat name.
 *
 * Uses:
 *   - Batch API (50% cost reduction vs synchronous)
 *   - Prompt caching via CacheControlEphemeral on the system prompt (further ~90% input savings
 *     on the cached portion across the batch)
 *
 * Each request is pre-primed: the conversation history already contains the initial user message
 * and the agent's action tool call with its result, so the model's next required response is
 * always a sendTextMessage confirmation. This lets us evaluate chatName accuracy in a single
 * API call per trial without needing multi-turn execution.
 *
 * Run manually — requires ANTHROPIC_API_KEY in env. Takes roughly 30–60 minutes.
 */
public class ChatNameHallucinationBatchTest {

    private static final String EXPECTED_CHAT = "Sewerslide Pack";
    private static final String MODEL = "claude-sonnet-4-6";
    private static final String PROMPT_PATH = "src/main/resources/expenseTrackingPrompt2";
    private static final int TRIALS = 1000;
    private static final long POLL_INTERVAL_MS = 30_000;
    private static final String OUTPUT_DIR = "hallucination-results";

    private record Scenario(
        String userMessage,
        String priorToolName,
        Map<String, Object> priorToolInput,
        String priorToolResult
    ) {}

    @Test
    @Timeout(value = 3, unit = TimeUnit.HOURS)
    void runBatchHallucinationTest() throws Exception {
        final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Files.createDirectories(Path.of(OUTPUT_DIR));
        final Path outPath = Path.of(OUTPUT_DIR, "batch-" + timestamp + ".txt");
        final PrintWriter out = new PrintWriter(Files.newBufferedWriter(outPath));

        final AnthropicClient client = AnthropicOkHttpClient.fromEnv();
        final String systemPrompt = Util.readFile(PROMPT_PATH);

        // System prompt with prompt caching — one cache write, ~5000 cache reads
        final List<TextBlockParam> systemBlocks = List.of(
            TextBlockParam.builder()
                .text(systemPrompt)
                .cacheControl(CacheControlEphemeral.builder().build())
                .build()
        );

        final List<Tool> tools = buildTools();
        final List<Scenario> scenarios = buildScenarios();

        // Build 5000 requests, cycling through the 5 scenarios
        final List<BatchCreateParams.Request> requests = new ArrayList<>(TRIALS);
        for (int i = 0; i < TRIALS; i++) {
            requests.add(buildRequest("trial-" + i, systemBlocks, tools, scenarios.get(i % scenarios.size())));
        }

        // Submit batch
        final MessageBatch batch = client.messages().batches().create(
            BatchCreateParams.builder().requests(requests).build()
        );
        log(out, "[Batch] Submitted %d requests. Batch ID: %s", TRIALS, batch.id());
        log(out, "[Batch] Output file: %s", outPath.toAbsolutePath());

        // Poll until complete
        MessageBatch current = batch;
        while (current.processingStatus().value() != MessageBatch.ProcessingStatus.Value.ENDED) {
            Thread.sleep(POLL_INTERVAL_MS);
            current = client.messages().batches().retrieve(batch.id());
            final MessageBatchRequestCounts c = current.requestCounts();
            log(out, "[Batch] %s — processing=%d  succeeded=%d  errored=%d",
                current.processingStatus(), c.processing(), c.succeeded(), c.errored());
        }

        // Stream and analyse results (array cells used for lambda capture)
        final int[] sendTextMessageCalls = {0};
        final int[] wrongChatName = {0};

        try (final var stream = client.messages().batches().resultsStreaming(batch.id())) {
            stream.stream().forEach(response -> {
                if (!response.result().isSucceeded()) return;

                for (final ContentBlock block : response.result().asSucceeded().message().content()) {
                    if (!block.isToolUse() || !block.asToolUse().name().equals("sendTextMessage")) continue;

                    sendTextMessageCalls[0]++;
                    final Map<String, JsonValue> input = block.asToolUse()._input()
                        .convert(new TypeReference<>() {});
                    final String chatName = input.containsKey("chatName")
                        ? input.get("chatName").convert(String.class)
                        : "";

                    if (!EXPECTED_CHAT.equals(chatName)) {
                        wrongChatName[0]++;
                        log(out, "[HALLUCINATION] trial=%s  got chatName=\"%s\"",
                            response.customId(), chatName);
                    }
                }
            });
        }

        log(out, "");
        log(out, "=== Batch Hallucination Report ===");
        log(out, "Timestamp              : %s", timestamp);
        log(out, "Batch ID               : %s", batch.id());
        log(out, "Model                  : %s", MODEL);
        log(out, "Trials submitted       : %d", TRIALS);
        log(out, "sendTextMessage calls  : %d", sendTextMessageCalls[0]);
        log(out, "Wrong chatName         : %d", wrongChatName[0]);
        log(out, "Hallucination rate     : %.2f%%",
            sendTextMessageCalls[0] > 0 ? 100.0 * wrongChatName[0] / sendTextMessageCalls[0] : 0.0);

        out.close();
    }

    // ── pre-primed scenarios ──────────────────────────────────────────────────

    private List<Scenario> buildScenarios() {
        return List.of(
            new Scenario(
                "[Chat: Sewerslide Pack]\nGeorge: I had the pizza, $18",
                "recordExpense",
                Map.of("name", "George", "amount", 18.0),
                "null"
            ),
            new Scenario(
                "[Chat: Sewerslide Pack]\nBob: Alice paid me back $30",
                "recordPayment",
                Map.of("sender", "Alice", "receiver", "Bob", "amount", 30.0),
                "null"
            ),
            new Scenario(
                "[Chat: Sewerslide Pack]\nAlice: Bob and I split the drinks, $24 total",
                "recordSplitExpense",
                Map.of("amount", 24.0, "names", "Alice, Bob"),
                "null"
            ),
            new Scenario(
                "[Chat: Sewerslide Pack]\nGeorge: What's the balance?",
                "getSummary",
                Map.of(),
                "\"George: +$18.00\\nBob: -$18.00\""
            ),
            new Scenario(
                "[Chat: Sewerslide Pack]\nAlice: Let's settle up",
                "settle",
                Map.of(),
                "\"Bob should pay George $18.00\""
            )
        );
    }

    // ── request builder ───────────────────────────────────────────────────────

    private BatchCreateParams.Request buildRequest(
        final String id,
        final List<TextBlockParam> systemBlocks,
        final List<Tool> tools,
        final Scenario s
    ) {
        // Build the pre-primed tool-use input
        final ToolUseBlockParam.Input.Builder inputBuilder = ToolUseBlockParam.Input.builder();
        s.priorToolInput().forEach((k, v) -> inputBuilder.putAdditionalProperty(k, JsonValue.from(v)));

        final BatchCreateParams.Request.Params.Builder params = BatchCreateParams.Request.Params.builder()
            .model(MODEL)
            .maxTokens(256L)
            .systemOfTextBlockParams(systemBlocks);
        tools.forEach(params::addTool);

        // 1. User message
        params.addUserMessage(s.userMessage());

        // 2. Assistant: pre-filled action tool call
        params.addAssistantMessageOfBlockParams(List.of(
            ContentBlockParam.ofToolUse(
                ToolUseBlockParam.builder()
                    .id("toolu_0001")
                    .name(s.priorToolName())
                    .input(inputBuilder.build())
                    .build()
            )
        ));

        // 3. User: tool result — next response must be sendTextMessage
        params.addUserMessageOfBlockParams(List.of(
            ContentBlockParam.ofToolResult(
                ToolResultBlockParam.builder()
                    .toolUseId("toolu_0001")
                    .content("{\"ok\":true,\"data\":" + s.priorToolResult() + "}")
                    .build()
            )
        ));

        return BatchCreateParams.Request.builder()
            .customId(id)
            .params(params.build())
            .build();
    }

    // ── tool definitions ──────────────────────────────────────────────────────

    private List<Tool> buildTools() {
        return List.of(
            tool("sendTextMessage", Map.of(
                "chatName", "string",
                "message", "string"
            ), List.of("chatName", "message")),
            tool("recordExpense", Map.of(
                "name", "string",
                "amount", "number"
            ), List.of("name", "amount")),
            tool("recordPayment", Map.of(
                "sender", "string",
                "receiver", "string",
                "amount", "number"
            ), List.of("sender", "receiver", "amount")),
            tool("recordSplitExpense", Map.of(
                "amount", "number",
                "names", "string"
            ), List.of("amount", "names")),
            tool("getSummary", Map.of(), List.of()),
            tool("settle", Map.of(), List.of())
        );
    }

    private static void log(final PrintWriter out, final String fmt, final Object... args) {
        final String line = args.length == 0 ? fmt : String.format(fmt, args);
        System.out.println(line);
        out.println(line);
        out.flush();
    }

    private Tool tool(final String name, final Map<String, String> paramTypes, final List<String> required) {
        final Map<String, Object> properties = new LinkedHashMap<>();
        paramTypes.forEach((k, type) -> properties.put(k, Map.of("type", type)));

        final Tool.InputSchema.Builder schema = Tool.InputSchema.builder()
            .putAdditionalProperty("properties", JsonValue.from(properties));
        required.forEach(schema::addRequired);

        return Tool.builder()
            .name(name)
            .description(name)
            .inputSchema(schema.build())
            .build();
    }
}
