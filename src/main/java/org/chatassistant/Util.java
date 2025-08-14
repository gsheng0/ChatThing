package org.chatassistant;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class Util {
    public static String TEST_JPEG_IMAGE_PATH = "~/Library/Messages/Attachments/22/02/039C5023-914B-42A5-8BA0-16EA9BA5CBB5/51EFA309-9EF7-4D19-B021-6DFBDBBA7E90.JPG";
    public static String TEST_HEIC_IMAGE_PATH = "~/Library/Messages/Attachments/ac/12/650BF5D2-59E2-4D16-9AF8-6175A9232598/IMG_6959.heic";

//    static {
//        try {
//            String libPath = Paths.get(Util.class.getResource("/libheicconverter.dylib").toURI())
//                    .toAbsolutePath()
//                    .toString();
//            System.load(libPath); // Load the native library
//        } catch (URISyntaxException e) {
//            throw new RuntimeException("Failed to load native library", e);
//        }
//    }

    public static String readFile(String filePath) {
        try{
            return Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static String encodeImage(String imagePath) {
        try{
            Path path = Path.of(imagePath.replace("~", System.getProperty("user.home")));
            byte[] bytes = Files.readAllBytes(path);
            return Base64.getEncoder().encodeToString(bytes);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static String getJpegBase64String(final String imagePath){
        final Path path = Path.of(imagePath.replace("~", System.getProperty("user.home")));
        try{
            if(imagePath.endsWith("heic")){
                final String output = Base64.getEncoder().encodeToString(convertHeicToJpeg(Files.readAllBytes(path)));
                return output;
            } else {
                return Base64.getEncoder().encodeToString((Files.readAllBytes(path)));
            }
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    public static byte[] convertHeicToJpeg(byte[] heicBytes) {
        if (heicBytes == null || heicBytes.length == 0) {
            throw new IllegalArgumentException("Input HEIC bytes cannot be null or empty.");
        }
        File tempHeicFile = null;
        File tempJpgFile = null;
        try {
            tempHeicFile = File.createTempFile("heic_input_", ".heic");
            try (FileOutputStream fos = new FileOutputStream(tempHeicFile)) {
                fos.write(heicBytes);
            }
            tempJpgFile = File.createTempFile("jpg_output_", ".jpg");
            ConvertCmd cmd = new ConvertCmd(true);
            IMOperation op = new IMOperation();
            op.addImage(tempHeicFile.getAbsolutePath());
            op.addImage(tempJpgFile.getAbsolutePath());
            cmd.run(op);
            return java.nio.file.Files.readAllBytes(tempJpgFile.toPath());

        } catch(Exception e){
            throw new RuntimeException(e);
        } finally {
            if (tempHeicFile != null) {
                tempHeicFile.delete();
            }
            if (tempJpgFile != null) {
                tempJpgFile.delete();
            }
        }
    }

//    public static native byte[] convertHeicBytesToJpegBytes(byte[] heicData);
}
