package org.joeri;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import java.io.File;
import java.util.Date;

public class MetadataReader {
    public static Date getDateTaken(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (directory != null) {
                return directory.getDate(ExifIFD0Directory.TAG_DATETIME_ORIGINAL);
            }
        } catch (Exception e) {
            System.out.println("Could not get date: " + e.getMessage());
        }
        return null;
    }
}
