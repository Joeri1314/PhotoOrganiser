package org.joeri;

import java.io.File;
import java.util.Date;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String directoryPath = "images";

        try {
            Database db = new Database("photo-organizer.db");

            while (true) {
                System.out.println("\nPhoto organizer menu, choose an option\n1. List all images\n2. Add tags to an image\n3. Search images by tag\n4. Remove duplicate images\n5. Exit");

                int choice = scanner.nextInt();
                scanner.nextLine();

                File directory = new File(directoryPath);
                File[] files = directory.listFiles();

                if (files == null || files.length == 0) {
                    System.out.println("No files found.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        System.out.println("Images and Metadata:");
                        for (File file : files) {
                            Date dateTaken = MetadataReader.getDateTaken(file);
                            String dateString = (dateTaken != null) ? dateTaken.toString() : "unknown";
                            String tags = db.getTags(file.getAbsolutePath());
                            System.out.println(file.getName() + " " + dateString + " " + tags);
                        }
                        break;
                    case 2:
                        System.out.println("Enter image name: ");
                        String imageName = scanner.nextLine();
                        File fileToTag = new File(directoryPath + "/" + imageName);
                        if (!fileToTag.exists()) {
                            System.out.println("Image not found: " + imageName);
                            break;
                        }
                        System.out.println("Enter tags (comma seperated): ");
                        String tags = scanner.nextLine();
                        db.addTags(fileToTag.getAbsolutePath(), tags);
                        System.out.println("Tags added: " + tags);
                        break;
                    case 3:
                        System.out.println("Enter tag to search: ");
                        String tagToSearch = scanner.nextLine();
                        db.searchByTag(tagToSearch);
                        break;
                    case 4:
                        System.out.println("Checking for duplicate images...");
                        for (File file : files) {
                            db.addImage(file);
                        }
                        System.out.println("Duplicate check completed.");
                        break;
                    case 5:
                        db.close();
                        scanner.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
