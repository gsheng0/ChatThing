package org.chatassistant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;

public class Logger {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static PrintWriter writer = null;

    public static void init(final String filePath) {
        if (writer != null) return;
        try {
            new File(filePath).getParentFile().mkdirs();
            writer = new PrintWriter(new FileWriter(filePath, true), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final String className;

    public static <T> Logger of(final Class<T> clazz) {
        return new Logger(clazz.getSimpleName());
    }

    private Logger(final String className) {
        this.className = className;
    }

    public void log(final String string) {
        _log(string);
    }

    public void log(final String string, final Object... args) {
        String formatted = string;
        for (final Object arg : args) {
            final int index = formatted.indexOf("{}");
            if (index == -1) break;
            formatted = formatted.replaceFirst("\\{\\}", Matcher.quoteReplacement(arg.toString()));
        }
        log(formatted);
    }

    private synchronized void _log(final String string) {
        final String line = DATE_FORMAT.format(new Date()) + " [" + className + "]: " + string;
        System.out.println(line);
        if (writer != null) writer.println(line);
    }
}
