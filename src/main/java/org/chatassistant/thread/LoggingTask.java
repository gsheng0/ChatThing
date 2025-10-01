package org.chatassistant.thread;

import org.chatassistant.config.LoggingConfigurationProperties;
import org.chatassistant.entities.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.BlockingDeque;

@Component
public class LoggingTask implements Runnable {
    private final BlockingDeque<Pair<Integer, String>> loggingQueue;
    private static final String OUTPUT_PATH = "/Users/georgesheng/proj/scheduler2/logs/";
    private final File outputFile;
    private final FileOutputStream fileOutputStream;

    @Autowired
    public LoggingTask(final LoggingConfigurationProperties loggingConfigurationProperties, final BlockingDeque<Pair<Integer, String>> loggingQueue) {
        this.loggingQueue = loggingQueue;
        outputFile = new File(OUTPUT_PATH + loggingConfigurationProperties.getOutputFolder() + "/"  + Calendar.getInstance().getTime());
        try{
            fileOutputStream = new FileOutputStream(outputFile);
        } catch(final Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(){
        System.out.println("Logging thread started");
        while(true){
            while(!loggingQueue.isEmpty()){
                try {
                    fileOutputStream.write(loggingQueue.pollFirst().getSecond().getBytes());
                    fileOutputStream.write("\n".getBytes());
                    fileOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                fileOutputStream.getFD().sync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
