package org.joeri;

import java.io.File;
import java.util.Date;

public class ImageFile extends MediaFile {

    public ImageFile(File file) {
        super(file);
    }

    @Override
    public String getType() {
        return "Image";
    }

    @Override
    public Date getDateTaken() {
        return MetadataReader.getDateTaken(file);
    }
}
