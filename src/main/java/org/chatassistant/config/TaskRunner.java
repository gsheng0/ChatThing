package org.chatassistant.config;

import lombok.Getter;
import lombok.Setter;
import org.chatassistant.thread.LoggingTask;
import org.chatassistant.thread.PollingTask;
import org.chatassistant.thread.ProcessingTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class TaskRunner {
    private final PollingTask pollingTask;
    private final ProcessingTask processingTask;
    private final LoggingTask loggingTask;

    @Getter
    private final Map<String, Thread> threads;

    @Autowired
    public TaskRunner(final PollingTask pollingTask, final ProcessingTask processingTask, final LoggingTask loggingTask){
        this.pollingTask = pollingTask;
        this.processingTask = processingTask;
        this.loggingTask = loggingTask;
        threads = new HashMap<String, Thread>();
    }

    @PostConstruct
    public void runTasks(){
        runAsync(pollingTask, "PollingThread");
        runAsync(processingTask, "ProcessingThread");
        runAsync(loggingTask, "LoggingThread");
    }

    @Async
    public void runAsync(Runnable task, String name){
        final Thread thread = new Thread(task, name);
        thread.start();
        threads.put(name, thread);
    }
}
