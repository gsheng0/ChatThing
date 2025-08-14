package org.chatassistant.garbage.typedstream;

import java.io.*;

public class ProcessTypedStreamDecoder implements TypedStreamDecoder {
    private static ProcessTypedStreamDecoder instance;
    public static ProcessTypedStreamDecoder getInstance() {
        if(instance == null){
            instance = new ProcessTypedStreamDecoder();
        }
        return instance;
    }

    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final Process process;
    public ProcessTypedStreamDecoder(){
        ProcessBuilder pb = new ProcessBuilder("python", "/Users/georgesheng/proj/scheduler2/decode.py");
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        System.out.println("Process started");
    }

    @Override
    public String decode(String typedStream) {
        System.out.println("Decoding");
        try{
            writer.write(typedStream);
            writer.newLine();
            writer.flush();
            System.out.println("FLUSHED");
            return reader.readLine();
        } catch(IOException ioe){
            throw new RuntimeException(ioe);
        }
    }
}
