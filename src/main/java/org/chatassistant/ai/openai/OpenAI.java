package org.chatassistant.ai.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.chatassistant.Main;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.chatassistant.Util.*;

public class OpenAI {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String MODEL_NAME = "gpt-4o"; // supports text + image
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String API_URL = "https://api.openai.com/v1/responses";
    public static String sendRequest(String prompt, List<String> imagePaths){
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
                    String base64Image = getJpegBase64String(imagePath);
                    ObjectNode imageNode = mapper.createObjectNode();
                    imageNode.put("type", "input_image");
                    imageNode.put("image_url", "data:image/jpeg;base64," + base64Image);
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
//            ArrayNode toolsNode = buildToolsArray();

            ObjectNode body = mapper.createObjectNode();
            body.put("model", MODEL_NAME);
            body.set("input", messages);
//            if (toolsNode != null && !toolsNode.isEmpty()) {
////                body.set("tools", toolsNode);
//            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
//            System.out.println(body.toPrettyString());

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = mapper.readTree(response.body());
            return json.path("output").get(0).path("content").get(0).path("text").asText();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
