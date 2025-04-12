package org.joeri.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageUtils {
    private static final Logger LOGGER = Logger.getLogger(ImageUtils.class.getName());

    public static String getFileHash(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02X", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to hash file: " + file.getAbsolutePath(), e);
            return null;
        }
    }
}
