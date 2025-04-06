package org.joeri;

import java.io.File;
import java.util.Date;

public abstract class MediaFile {
    protected File file;

    public MediaFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public abstract String getType();

    public abstract Date getDateTaken();
}
