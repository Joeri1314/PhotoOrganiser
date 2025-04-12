package org.joeri.dao;

import java.io.File;
import java.sql.*;
import java.util.*;

public class SQLiteImageDAO implements ImageDAO {
    private final Connection conn;

    public SQLiteImageDAO(String dbPath) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS images (filepath TEXT PRIMARY KEY, hash TEXT, tags TEXT)");
        }
    }

    @Override
    public Optional<String> getFileByHash(String hash) throws SQLException {
        String sql = "SELECT filepath FROM images WHERE hash = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hash);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(rs.getString("filepath")) : Optional.empty();
        }
    }

    @Override
    public void insertImage(File file, String hash) throws SQLException {
        String sql = "INSERT INTO images (filepath, hash, tags) VALUES (?, ?, '')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, file.getAbsolutePath());
            stmt.setString(2, hash);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateTags(String filepath, Set<String> tags) throws SQLException {
        String tagString = String.join(",", tags);
        String sql = "UPDATE images SET tags = ? WHERE filepath = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tagString);
            stmt.setString(2, filepath);
            stmt.executeUpdate();
        }
    }

    @Override
    public Set<String> getTags(String filepath) throws SQLException {
        String sql = "SELECT tags FROM images WHERE filepath = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, filepath);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String tags = rs.getString("tags");
                return tags.isEmpty() ? new HashSet<>() : new HashSet<>(Arrays.asList(tags.split(",")));
            }
        }
        return new HashSet<>();
    }

    @Override
    public List<String> searchByTags(Set<String> tags) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT filepath FROM images WHERE ");
        List<String> conditions = new ArrayList<>();
        tags.forEach(tag -> conditions.add("tags LIKE ?"));
        sql.append(String.join(" OR ", conditions));

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int i = 1;
            for (String tag : tags) {
                stmt.setString(i++, "%" + tag + "%");
            }
            ResultSet rs = stmt.executeQuery();
            List<String> files = new ArrayList<>();
            while (rs.next()) {
                files.add(rs.getString("filepath"));
            }
            return files;
        }
    }

    @Override
    public List<String> listAllImages() throws SQLException {
        List<String> images = new ArrayList<>();
        String sql = "SELECT filepath FROM images";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                images.add(rs.getString("filepath"));
            }
        }
        return images;
    }

    @Override
    public Set<String> listAllTags() throws SQLException {
        Set<String> tags = new HashSet<>();
        String sql = "SELECT tags FROM images";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String tagString = rs.getString("tags");
                if (!tagString.isEmpty()) {
                    tags.addAll(Arrays.asList(tagString.split(",")));
                }
            }
        }
        return tags;
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}
