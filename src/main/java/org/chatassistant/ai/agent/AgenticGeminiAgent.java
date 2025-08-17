package org.chatassistant.ai.agent;

import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.*;
import org.chatassistant.Main;
import org.chatassistant.Util;
import org.chatassistant.config.AiAgentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component("chatAssistantAgent")
public class AgenticGeminiAgent implements AiAgent {
    private final Chat chat;
    private final Client client;

    @Autowired
    public AgenticGeminiAgent(final AiAgentConfig aiAgentConfig){
        client = new Client();
        this.chat = client.chats.create(aiAgentConfig.getModelName(), getConfig(Util.readFile(aiAgentConfig.getPromptPath())));
    }

    private GenerateContentConfig getConfig(final String prompt){
        return GenerateContentConfig.builder()
                .tools(List.of(
                        Tool.builder().functions(AiAgent.getAllTools()).build()))
                .systemInstruction(Content.fromParts(Part.fromText(prompt)))
                .build();
    }

    @Override
    public String ask(String prompt) {
        final GenerateContentResponse response = chat.sendMessage(prompt);
        return response.text();
    }

    @Override
    public String ask(String prompt, List<String> imagePaths) {
        final List<Part> parts = new ArrayList<>(List.of(Part.fromText(prompt)));
        for(final String imagePath : imagePaths){
            parts.add(imagePartOf(imagePath));
        }
        final Content content = Content.builder()
                .parts(parts)
                .role("user")
                .build();
        final GenerateContentResponse response = chat.sendMessage(content);
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
