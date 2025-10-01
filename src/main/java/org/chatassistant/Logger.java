package org.chatassistant;

import org.chatassistant.entities.Pair;

import java.util.Calendar;
import java.util.concurrent.BlockingDeque;
import java.util.regex.Matcher;

public class Logger {
    private static final Calendar CALENDAR = Calendar.getInstance();
    private static BlockingDeque<Pair<Integer, String>> loggingQueue = null;

    public static void setLoggingQueue(final BlockingDeque<Pair<Integer, String>> loggingQueue){
        if(Logger.loggingQueue != null){
            return;
        }
        Logger.loggingQueue = loggingQueue;
    }

    private final String className;
    private final int spec;
    public static <T> Logger of(final Class<T> clazz){
        return new Logger(clazz.getSimpleName(), 0);
    }
    public static <T> Logger of(final Class<T> clazz, final int spec){
        return new Logger(clazz.getSimpleName(), spec);
    }
    private Logger (final String className, final int spec){
        this.className = className;
        this.spec = spec;
    }

    private void _log(final String string){
        final StringBuilder builder = new StringBuilder();
        builder.append(CALENDAR.getTimeInMillis());
        builder.append(" [");
        builder.append(className);
        builder.append("]: ");
        builder.append(string);
        loggingQueue.addLast(Pair.of(spec, builder.toString()));
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
