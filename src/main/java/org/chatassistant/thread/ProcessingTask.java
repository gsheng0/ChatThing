package org.chatassistant.thread;

import org.chatassistant.ai.agent.AiAgent;
import org.chatassistant.ai.agent.GeminiAgent;
import org.chatassistant.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

@Component
public class ProcessingTask implements Runnable {
    private final BlockingDeque<List<Message>> messageDeque;
    private volatile boolean running;

    private final AiAgent chatAssistantAgent;
    private final AiAgent imageParserAgent;

    @Autowired
    public ProcessingTask(
            final BlockingDeque<List<Message>> messageDeque,
            @Qualifier("chatAssistantAgent") final AiAgent chatAssistantAgent,
            @Qualifier("imageParserAgent") final AiAgent imageParserAgent) {
        this.messageDeque = messageDeque;
        this.running = true;
        this.chatAssistantAgent = chatAssistantAgent;
        this.imageParserAgent = imageParserAgent;
    }

    @Override
    public void run() {
        System.out.println("Processing Thread: Waiting for messages or changes...");
        while(running) {
            try{
                if(messageDeque.isEmpty()){
                    Thread.sleep(1000);
                    continue;
                }
                final List<Message> messages = messageDeque.pollFirst();
                final List<String> imagePaths = new ArrayList<>();
                final StringBuilder builder = new StringBuilder();
                for(Message message : messages){
                    if(message.getImagePath() != null && !message.getImagePath().isEmpty() && !message.getImagePath().equals("None")){
                        imagePaths.add(message.getImagePath());
                    }
                    if(message.getText() != null && !message.getText().isEmpty()){
                        builder
                                .append(message.getSender() + ": ")
                                .append(message.getText())
                                .append("\n");
                    }
                }
                if(!imagePaths.isEmpty()){
                    String receiptResponse = imageParserAgent.ask("", imagePaths);
                    if(!receiptResponse.isEmpty()){
                        builder.append(receiptResponse).append("\n");
                    }
                }
                if(!builder.isEmpty()){
                    String response = chatAssistantAgent.ask(builder.toString());
                    System.out.println("Asked " + builder);
                    System.out.println("Responed: " + response);
                }
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        running = false;
    }
}
