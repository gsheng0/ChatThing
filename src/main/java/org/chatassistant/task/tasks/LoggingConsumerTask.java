package org.chatassistant.task.tasks;

import org.chatassistant.config.LoggingConfigurationProperties;
import org.chatassistant.entities.Pair;
import org.chatassistant.task.ConsumerTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class LoggingConsumerTask implements ConsumerTask<Pair<Integer, String>> {
    private static final String OUTPUT_PATH = "/Users/georgesheng/proj/scheduler2/logs/";
    private final FileOutputStream fileOutputStream;

    public LoggingConsumerTask(final LoggingConfigurationProperties loggingConfig) {
        final File outputFile = new File(OUTPUT_PATH + loggingConfig.getOutputFolder() + "/" + Calendar.getInstance().getTime());
        try {
            fileOutputStream = new FileOutputStream(outputFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void consume(final Pair<Integer, String> entry) {
        try {
            fileOutputStream.write(entry.getSecond().getBytes());
            fileOutputStream.write("\n".getBytes());
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getIdentifier() {
        return "LoggingConsumerTask";
    }
}
