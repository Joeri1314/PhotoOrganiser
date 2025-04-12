package org.joeri.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetadataReader {
    private static final Logger LOGGER = Logger.getLogger(MetadataReader.class.getName());

    public static Date getDateTaken(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            if (directory != null) {
                return directory.getDateOriginal();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not get date for file: " + file.getAbsolutePath(), e);
        }
        return null;
    }
}
