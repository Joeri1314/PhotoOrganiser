package org.joeri.service;

import org.joeri.dao.ImageDAO;
import org.joeri.util.ImageUtils;
import java.io.File;
import java.sql.SQLException;
import java.util.*;

public class ImageManager {
    private final ImageDAO dao;

    public ImageManager(ImageDAO dao) {
        this.dao = dao;
    }

    public void addImage(File file) throws SQLException {
        String hash = ImageUtils.getFileHash(file);
        if (hash == null) return;

        Optional<String> existing = dao.getFileByHash(hash);
        if (existing.isPresent() && !existing.get().equals(file.getAbsolutePath())) {
            file.delete();
        } else if (existing.isEmpty()) {
            dao.insertImage(file, hash);
        }
    }

    public void addTags(String filepath, Set<String> newTags) throws SQLException {
        Set<String> existingTags = dao.getTags(filepath);
        existingTags.addAll(newTags);
        dao.updateTags(filepath, existingTags);
    }

    public void removeTags(String filepath, Set<String> tagsToRemove) throws SQLException {
        Set<String> existingTags = dao.getTags(filepath);
        existingTags.removeAll(tagsToRemove);
        dao.updateTags(filepath, existingTags);
    }

    public List<String> searchImagesByTags(Set<String> tags) throws SQLException {
        return dao.searchByTags(tags);
    }
}
