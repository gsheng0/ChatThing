package org.chatassistant.ai.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.chatassistant.Main;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class OpenAiAgent implements AiAgent {

    private static final String MODEL_NAME = "gpt-4o"; // supports text + image
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String API_URL = "https://api.openai.com/v1/responses";

    private static OpenAiAgent instance;
    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    private OpenAiAgent() {
        this.mapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    public static OpenAiAgent getInstance() {
        if (instance == null) {
            instance = new OpenAiAgent();
        }
        return instance;
    }

    @Override
    public String ask(String prompt) {
        return sendRequest(prompt, null);
    }

    @Override
    public String ask(String prompt, List<String> imagePaths) {
        return sendRequest(prompt, imagePaths);
    }

    @Override
    public void kill(){
        return;
    }

    private String sendRequest(String prompt, List<String> imagePaths) {
        try {
            ArrayNode contentArray = mapper.createArrayNode();

            // Add text
            ObjectNode textNode = mapper.createObjectNode();
            textNode.put("type", "input_text");
            textNode.put("text", prompt);
            contentArray.add(textNode);

            // Add images (if provided)
            if (imagePaths != null) {
                for (String imagePath : imagePaths) {
                    String base64Image = encodeImage(imagePath);
                    ObjectNode imageNode = mapper.createObjectNode();
                    imageNode.put("type", "image_url");
                    ObjectNode imageUrlNode = mapper.createObjectNode();
                    imageUrlNode.put("url", "data:image/jpeg;base64," + base64Image);
                    imageNode.set("image_url", imageUrlNode);
                    contentArray.add(imageNode);
                }
            }

            // Build messages array
            ArrayNode messages = mapper.createArrayNode();

            // Optional: add system prompt from file if you want the same behavior as your Gemini version
            String systemInstruction = readFile(Main.PROMPT);
            messages.add(mapper.createObjectNode()
                    .put("role", "system")
                    .put("content", systemInstruction));

            messages.add(mapper.createObjectNode()
                    .put("role", "user")
                    .set("content", contentArray));

            // Add tools as function definitions
            ArrayNode toolsNode = buildToolsArray();

            ObjectNode body = mapper.createObjectNode();
            body.put("model", MODEL_NAME);
            body.set("input", messages);
            if (toolsNode != null && !toolsNode.isEmpty()) {
//                body.set("tools", toolsNode);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            System.out.println(body.toPrettyString());

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode json = mapper.readTree(response.body());
            return json.path("output").get(0).path("content").get(0).path("text").asText();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String encodeImage(String imagePath) throws IOException {
        Path path = Path.of(imagePath.replace("~", System.getProperty("user.home")));
        byte[] bytes = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String readFile(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    private ArrayNode buildToolsArray() {
        List<Method> methods = AiAgent.getAllTools();
        ArrayNode toolsArray = mapper.createArrayNode();

        for (Method method : methods) {
            ObjectNode toolNode = mapper.createObjectNode();
            toolNode.put("type", "function");
            ObjectNode functionNode = mapper.createObjectNode();
            functionNode.put("name", method.getName());
            functionNode.put("description", "Auto-generated tool from AiAgentTool");

            // Simple example: no params metadata
            functionNode.set("parameters", mapper.createObjectNode()
                    .put("type", "object")
                    .set("properties", mapper.createObjectNode()
                    .set("required", mapper.createArrayNode())));

            toolNode.set("function", functionNode);
            toolsArray.add(toolNode);
        }

        return toolsArray;
    }
}
