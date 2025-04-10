package org.joeri.dao;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ImageDAO extends AutoCloseable {
    Optional<String> getFileByHash(String hash) throws SQLException;
    void insertImage(File file, String hash) throws SQLException;
    void updateTags(String filepath, Set<String> tags) throws SQLException;
    Set<String> getTags(String filepath) throws SQLException;
    List<String> searchByTags(Set<String> tags) throws SQLException;
    List<String> listAllImages() throws SQLException;
    Set<String> listAllTags() throws SQLException;
    void close() throws SQLException;
}
