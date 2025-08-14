package org.chatassistant.ai.agent;

import com.google.genai.Client;
import com.google.genai.types.*;
import com.google.genai.types.Tool;
import org.chatassistant.Main;
import org.chatassistant.Util;

import java.util.ArrayList;
import java.util.List;

import java.nio.file.Files;
import java.nio.file.Path;

public class GeminiAgent implements AiAgent {
    private static final String MODEL_NAME = "gemini-2.0-flash";
    private static GeminiAgent instance;
    public static GeminiAgent getInstance(){
        if(instance == null){
            instance = new GeminiAgent();
        }
        return instance;
    }

    private final Client client;
    private final GenerateContentConfig config;

    private GeminiAgent(){
        client = new Client();
        config = getConfig();
    }

    private GenerateContentConfig getConfig(){
        return GenerateContentConfig.builder()
                .tools(List.of(
                        Tool.builder().functions(AiAgent.getAllTools()).build()))
                .systemInstruction(Content.fromParts(Part.fromText(Util.readFile(Main.EXTRACT_RECEIPT_PROMPT_PATH))))
                .build();
    }

    @Override
    public String ask(final String prompt){
        return client.models.generateContent(MODEL_NAME, prompt, config).text();
    }

    @Override
    public String ask(final String prompt, final List<String> imagePaths){
        final List<Part> parts = new ArrayList<>(List.of(Part.fromText(prompt)));
        for(String imagePath : imagePaths){
            parts.add(imagePartOf(imagePath));
        }
        final Content content = Content.builder()
                .parts(parts)
                .role("user")
                .build();

        final GenerateContentResponse response = client.models.generateContent(MODEL_NAME, content, config);
        return response.text();
    }

    @Override
    public void kill(){
        client.close();
    }

    private static Part imagePartOf(final String imagePath){
        try{
            final String extension = imagePath.substring(imagePath.lastIndexOf('.'));
            final String updatedImagePath = imagePath.replace("~", "/Users/georgesheng");
            return Part.fromBytes(Files.readAllBytes(Path.of(updatedImagePath)), "image/" + extension);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

}