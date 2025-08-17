package org.chatassistant;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.BlockingDeque;

public class Logger {
    private static final Calendar CALENDAR = Calendar.getInstance();
    private static BlockingDeque<String> loggingQueue = null;

    public static void setLoggingQueue(final BlockingDeque<String> loggingQueue){
        if(Logger.loggingQueue != null){
            return;
        }
        Logger.loggingQueue = loggingQueue;
    }

    private final String className;
    public static <T> Logger of(final Class<T> clazz){
        return new Logger(clazz.getSimpleName());
    }
    private Logger (final String className){
        this.className = className;
    }

    private void _log(final String string){
        final StringBuilder builder = new StringBuilder();
        builder.append(CALENDAR.getTimeInMillis());
        builder.append(" [");
        builder.append(className);
        builder.append("]: ");
        builder.append(string);
        loggingQueue.addLast(builder.toString());
    }

    public void log(final String string){
        _log(string);
    }

    public void log(final String string, String... args){
        String formatted = string;
        for(final String arg : args){
            int index = formatted.indexOf("{}");
            if(index == -1){
                break;
            }
            formatted = formatted.replaceFirst("\\{}", arg);
        }
        log(formatted);
    }

}
