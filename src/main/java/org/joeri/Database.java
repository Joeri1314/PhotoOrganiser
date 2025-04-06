package org.joeri;

import java.sql.*;
import java.io.File;

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


    public void addTags(String filepath, String tags) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO images (filepath, tags) VALUES (?, ?) ON CONFLICT (filepath) DO UPDATE SET tags = ?");
        pstmt.setString(1, filepath);
        pstmt.setString(2, tags);
        pstmt.setString(3, tags);
        pstmt.executeUpdate();
    }

    public String getTags(String filepath) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT tags FROM images WHERE filepath = ?");
        pstmt.setString(1, filepath);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getString("tags") : null;
    }

    public void searchByTag(String tag) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT filepath FROM images WHERE tags LIKE ?");
        pstmt.setString(1, "%" + tag + "%");
        ResultSet rs = pstmt.executeQuery();

        System.out.println("Images with tag '" + tag + "':");
        boolean found = false;
        while (rs.next()) {
            System.out.println(rs.getString("filepath"));
            found = true;
        }
        if (!found) {
            System.out.println("No images with tag '" + tag + "'");
        }
    }

    public void close() throws SQLException {
        conn.close();
    }
}
