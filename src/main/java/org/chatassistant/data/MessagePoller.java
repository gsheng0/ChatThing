package org.chatassistant.data;

import org.chatassistant.entities.Message;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class MessagePoller {
    private static final String DELIMITER = "````%";
    private static MessagePoller instance;
    private final Process process;
    private final BufferedWriter writer;
    private final BufferedReader reader;

    public MessagePoller() {
        final ProcessBuilder pb = new ProcessBuilder(
                "/Users/georgesheng/proj/scheduler2/venv/bin/python3",
                "/Users/georgesheng/proj/scheduler2/decode2.py");
        try {
            process = pb.start();
        } catch (IOException e){
            throw new RuntimeException(e);
        }

        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    public List<Message> getRecentMessages() {
        try{
            writer.write("\n");
            writer.flush();

            final String[] response =  reader.readLine().split(DELIMITER);
            final List<Message> messages = new ArrayList<>();
            for(final String line: response){
                System.out.println(line);
                final String entry = decodeBase64Bytes(line);
                final String[] fields = entry.split("\\|");
                messages.add(new Message(fields[0], fields[2] == null || fields[2].isEmpty() ? fields[3] : fields[2], fields[1], fields[4], fields[5]));
            }
            return messages;
        } catch(Exception e){
            e.printStackTrace();
            return List.of();
        }
    }

    private static String decodeBase64Bytes(final String b64Encoded) {
        byte[] bytes = Base64.getDecoder().decode(b64Encoded.substring(2, b64Encoded.length() - 1));
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
