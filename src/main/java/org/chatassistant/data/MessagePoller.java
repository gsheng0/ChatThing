package org.chatassistant.data;

import org.chatassistant.entities.Message;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class MessagePoller {
    private static final String DELIMITER = "````%";
    private final Process process;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private final Contact contact;

    public MessagePoller(@Lazy final Contact contact) {
        this.contact = contact;
        final ProcessBuilder pb = new ProcessBuilder(
                "/Users/georgesheng/proj/scheduler2/venv/bin/python3",
                "/Users/georgesheng/proj/scheduler2/decode2.py");
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    public List<Message> getRecentMessages() {
        try {
            writer.write("\n");
            writer.flush();

            final String[] response = reader.readLine().split(DELIMITER);
            final List<Message> messages = new ArrayList<>();
            for (final String line : response) {
                if (line.isEmpty()) continue;
                final String entry = decodeBase64Bytes(line);
                final String[] fields = entry.split("\\|");
                if (fields.length < 6) {
                    System.err.println("[MessagePoller] Skipping malformed entry (" + fields.length + " fields): " + entry);
                    continue;
                }
                final String rawSender = fields[1];
                final String resolvedSender = contact.getIdToNameMap().getOrDefault(rawSender, rawSender);
                final String text = fields[2].isEmpty() ? fields[3] : fields[2];
                messages.add(new Message(fields[0], text, resolvedSender, fields[4], fields[5]));
            }
            return messages;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private static String decodeBase64Bytes(final String b64Encoded) {
        byte[] bytes = Base64.getDecoder().decode(b64Encoded.substring(2, b64Encoded.length() - 1));
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
