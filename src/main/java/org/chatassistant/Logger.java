package org.chatassistant;

import java.util.Calendar;
import java.util.concurrent.BlockingDeque;
import java.util.regex.Matcher;

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

    public void log(final String string, Object... args){
        String formatted = string;
        for(final Object arg : args){
            int index = formatted.indexOf("{}");
            if(index == -1){
                break;
            }
            formatted = formatted.replaceFirst("\\{\\}", Matcher.quoteReplacement(arg.toString()));
        }
        log(formatted);
    }

}
