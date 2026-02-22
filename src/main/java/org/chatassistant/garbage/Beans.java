package org.chatassistant.garbage;

import org.chatassistant.Logger;
import org.chatassistant.entities.Message;
import org.chatassistant.entities.Pair;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


public class Beans {
    @Bean
    public BlockingDeque<List<Message>> messageDeque(){
        return new LinkedBlockingDeque<>();
    }

    @Bean
    public BlockingDeque<Pair<Integer, String>> loggingQueue(){
        final BlockingDeque<Pair<Integer, String>> loggingQueue = new LinkedBlockingDeque<>();
        Logger.setLoggingQueue(loggingQueue);
        return loggingQueue;
    }
}
