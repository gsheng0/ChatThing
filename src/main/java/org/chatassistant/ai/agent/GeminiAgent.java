package org.chatassistant.ai.agent;

import com.google.genai.Client;
import com.google.genai.types.*;
import com.google.genai.types.Tool;
import org.chatassistant.Main;
import org.chatassistant.Util;
import org.chatassistant.config.AiAgentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import java.nio.file.Files;
import java.nio.file.Path;

@Component("imageParserAgent")
public class GeminiAgent implements AiAgent {
    private final Client client;
    private final GenerateContentConfig config;
    private final String modelName;

    @Autowired
    public GeminiAgent(final AiAgentConfig aiAgentConfig){
        this.modelName = aiAgentConfig.getModelName();
        client = new Client();
        config = getConfig(aiAgentConfig.getPromptPath());
    }

    private GenerateContentConfig getConfig(final String promptPath){
        return GenerateContentConfig.builder()
                .tools(List.of(
                        Tool.builder().functions(AiAgent.getAllTools()).build()))
                .systemInstruction(Content.fromParts(Part.fromText(Util.readFile(promptPath))))
                .build();
    }

    @Override
    public String ask(final String prompt){
        return client.models.generateContent(modelName, prompt, config).text();
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

        final GenerateContentResponse response = client.models.generateContent(modelName, content, config);
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