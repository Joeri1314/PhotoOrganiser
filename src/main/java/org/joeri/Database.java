package org.joeri;

import java.sql.*;
import java.io.File;
import java.util.*;

public class Database {
    private final Connection conn;

    public Database(String dbPath) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        initializeDatabase();
    }
    private void initializeDatabase() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS images (filepath TEXT PRIMARY KEY, hash TEXT, tags TEXT)");
    }

    public void addImage(File file) throws SQLException {
        file = file.getAbsoluteFile();
        String filePath = file.getAbsolutePath();
        String hash = ImageUtils.getFileHash(file);
        if (hash == null) return;

        PreparedStatement pstmt = conn.prepareStatement("SELECT filepath FROM images WHERE hash = ?");
        pstmt.setString(1, hash);
        ResultSet rs = pstmt.executeQuery();

        boolean safeToDelete = false;
        boolean alreadyInDB = false;

        while (rs.next()) {
            String existingPath = rs.getString("filepath");
            File existingFile = new File(existingPath);

            if (existingFile.exists()) {
                if (!existingFile.getAbsolutePath().equals(filePath)) {
                    System.out.println("Valid duplicate found on disk + DB: " + existingPath);
                    safeToDelete = true;
                } else {
                    alreadyInDB = true;
                }
            } else {
                System.out.println("Removing stale DB entry: " + existingPath);
                PreparedStatement cleanupStmt = conn.prepareStatement("DELETE FROM images WHERE filepath = ?");
                cleanupStmt.setString(1, existingPath);
                cleanupStmt.executeUpdate();
            }
        }

        if (safeToDelete) {
            System.out.println("Deleting duplicate file: " + filePath);
            file.delete();
        } else {
            if (!alreadyInDB) {
                System.out.println("Inserting new image into DB: " + filePath);
                pstmt = conn.prepareStatement("INSERT INTO images (filepath, hash, tags) VALUES (?, ?, ?)");
                pstmt.setString(1, filePath);
                pstmt.setString(2, hash);
                pstmt.setString(3, "");
                pstmt.executeUpdate();
            } else {
                System.out.println("File already tracked and valid: " + filePath);
            }
        }
    }


    public void addTags(String filepath, String newTags) throws SQLException {
        String existingTags = getTags(filepath);
        Set<String> tagSet = new HashSet<>();

        if (existingTags != null && !existingTags.isEmpty()) {
            tagSet.addAll(Arrays.asList(existingTags.split(",")));
        }

        tagSet.addAll(Arrays.asList(newTags.split(",")));

        String mergedTags = String.join(",", tagSet);

        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO images (filepath, tags) VALUES (?, ?) ON CONFLICT (filepath) DO UPDATE SET tags = ?");
        pstmt.setString(1, filepath);
        pstmt.setString(2, mergedTags);
        pstmt.setString(3, mergedTags);
        pstmt.executeUpdate();
    }

    public void removeTags(String filepath, String tagsToRemove) throws SQLException {
        String existingTags = getTags(filepath);
        if (existingTags == null || existingTags.isEmpty()) return;

        Set<String> tagSet = new HashSet<>(Arrays.asList(existingTags.split(",")));
        Set<String> removeSet = new HashSet<>(Arrays.asList(tagsToRemove.split(",")));

        tagSet.removeAll(removeSet);

        String updatedTags = String.join(",", tagSet);

        PreparedStatement pstmt = conn.prepareStatement("UPDATE images SET tags = ? WHERE filepath = ?");
        pstmt.setString(1, updatedTags);
        pstmt.setString(2, filepath);
        pstmt.executeUpdate();
    }

    public String getTags(String filepath) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT tags FROM images WHERE filepath = ?");
        pstmt.setString(1, filepath);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getString("tags") : null;
    }

    public void searchByTags(String[] tags) throws SQLException {
        if (tags == null || tags.length == 0) return;

        StringBuilder queryBuilder = new StringBuilder("SELECT filepath FROM images WHERE ");
        for (int i = 0; i < tags.length; i++) {
            queryBuilder.append("tags LIKE ?");
            if (i < tags.length - 1) {
                queryBuilder.append(" OR ");
            }
        }

        PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString());
        for (int i = 0; i < tags.length; i++) {
            pstmt.setString(i + 1, "%" + tags[i].trim() + "%");
        }

        ResultSet rs = pstmt.executeQuery();

        System.out.println("Images matching any of the tags: " + String.join(", ", tags));
        boolean found = false;
        while (rs.next()) {
            System.out.println(rs.getString("filepath"));
            found = true;
        }
        if (!found) {
            System.out.println("No images matched the tags.");
        }
    }


    public void close() throws SQLException {
        conn.close();
    }
}
