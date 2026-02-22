package org.chatassistant.garbage;

import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.*;
import org.chatassistant.Util;
import org.chatassistant.ai.tools.ToolHolder;
import org.chatassistant.config.AiAgentConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Deprecated
("chatAssistantAgent")
public class AgenticGeminiAgent implements AiAgent<Void> {
    private final Chat chat;
    private final Client client;
    private final Map<String, Method> toolMap;

    
    public AgenticGeminiAgent(final AiAgentConfigurationProperties aiAgentConfig, final ToolHolder toolHolder) {
        client = new Client();
        this.toolMap = toolHolder.getToolMap(aiAgentConfig.isRealToolSet());
        this.chat = client.chats.create(
            aiAgentConfig.getModelName(),
            getConfig(
                Util.readFile(aiAgentConfig.getPromptPath()),
                new ArrayList<>(toolMap.values())
            ));
    }

    private GenerateContentConfig getConfig(final String prompt, final List<Method> tools){
        return GenerateContentConfig.builder()
                .tools(List.of(
                        Tool.builder().functions(tools).build()))
                .systemInstruction(Content.fromParts(Part.fromText(prompt)))
                .build();
    }

    @Override
    public String ask(Void context, String prompt) {
        final GenerateContentResponse response = chat.sendMessage(prompt);
        return response.text();
    }

    @Override
    public String ask(Void context, String prompt, List<String> imagePaths) {
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
