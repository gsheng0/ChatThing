package org.chatassistant.config;

import org.chatassistant.Logger;
import org.chatassistant.entities.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Configuration
public class Beans {
    @Bean
    public BlockingDeque<List<Message>> messageDeque(){
        return new LinkedBlockingDeque<>();
    }

    @Bean
    public BlockingDeque<String> loggingQueue(){
        final BlockingDeque<String> loggingQueue = new LinkedBlockingDeque<>();
        Logger.setLoggingQueue(loggingQueue);
        return loggingQueue;
    }
}
