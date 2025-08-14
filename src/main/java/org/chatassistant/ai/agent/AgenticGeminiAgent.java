package org.chatassistant.ai.agent;

import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.*;
import org.chatassistant.Main;
import org.chatassistant.Util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AgenticGeminiAgent implements AiAgent {
    private static final String MODEL_NAME = "gemini-2.0-flash";

    private static AgenticGeminiAgent instance;
    public static AgenticGeminiAgent getInstance(){
        if(instance == null){
            instance = new AgenticGeminiAgent();
        }
        return instance;
    }

    private final Chat chat;
    private final Client client;
    private AgenticGeminiAgent(){
        client = new Client();
        this.chat = client.chats.create(MODEL_NAME, getConfig());
    }

    private GenerateContentConfig getConfig(){
        return GenerateContentConfig.builder()
                .tools(List.of(
                        Tool.builder().functions(AiAgent.getAllTools()).build()))
                .systemInstruction(Content.fromParts(Part.fromText(Util.readFile(Main.PROMPT))))
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
    public static void main(String[] args){
        AgenticGeminiAgent agent = AgenticGeminiAgent.getInstance();
        System.out.println(agent.ask("tell me about the different goat candidates for starcraft 2 and the stains on each case"));
    }
}
