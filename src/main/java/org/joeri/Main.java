package org.joeri;

import java.io.File;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String directoryPath = "images";

        try {
            Database db = new Database("photo-organizer.db");

            while (true) {
                System.out.println("\nPhoto organizer menu, choose an option\n1. List all images\n2. Tag images\n3. List all tags\n4. Search images by tag\n5. Remove duplicate images\n6. Remove tags from image\n7. Exit");

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
                            MediaFile media = new ImageFile(file);
                            String dateString = (media.getDateTaken() != null) ? media.getDateTaken().toString() : "unknown";
                            String tags = db.getTags(media.getFile().getAbsolutePath());
                            System.out.println(media.getFile().getName() + " " + dateString + " " + tags);
                        }
                        break;
                    case 2:
                        System.out.println("Available images:");
                        int index = 1;
                        File[] imageList = new File[files.length];
                        for (File value : files) {
                            if (value.isFile()) {
                                System.out.println(index + ". " + value.getName());
                                imageList[index - 1] = value;
                                index++;
                            }
                        }

                        if (index == 1) {
                            System.out.println("No images found.");
                            break;
                        }

                        System.out.println("Enter the numbers of the images to tag (comma separated): ");
                        String imageSelection = scanner.nextLine();
                        String[] selectedImages = imageSelection.split(",");

                        System.out.println("Enter new tag list (comma separated): ");
                        String tagsForImages = scanner.nextLine();

                        for (String images : selectedImages) {
                            try {
                                int imgIndex = Integer.parseInt(images.trim()) - 1;
                                if (imgIndex >= 0 && imgIndex < imageList.length && imageList[imgIndex] != null) {
                                    File selectedFile = imageList[imgIndex];
                                    db.addImage(selectedFile);
                                    db.addTags(selectedFile.getAbsolutePath(), tagsForImages);
                                    System.out.println("Tagged: " + selectedFile.getName());
                                } else {
                                    System.out.println("Invalid image number: " + (imgIndex + 1));
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input: " + images);
                            }
                        }
                        break;
                    case 3:
                        System.out.println("Tag list:");
                        Set<String> allTags = new HashSet<>();
                        for (File file : files) {
                            MediaFile media = new ImageFile(file);
                            String tagsString = db.getTags(media.getFile().getAbsolutePath());
                            if (tagsString != null && !tagsString.isEmpty()) {
                                String[] tagsArray = tagsString.split(",");
                                for (String tag : tagsArray) {
                                    allTags.add(tag.trim());
                                }
                            }
                        }
                        int i = 1;
                        for (String tag : allTags) {
                            System.out.println(i + ". " + tag);
                            i++;
                        }
                        break;
                    case 4:
                        System.out.println("Enter tag(s) to search (comma separated): ");
                        String tagInput = scanner.nextLine();
                        String[] tagsToSearch = tagInput.split(",");
                        db.searchByTags(tagsToSearch);
                        break;
                    case 5:
                        System.out.println("Checking for duplicate images...");
                        for (File file : files) {
                            MediaFile media = new ImageFile(file);
                            db.addImage(media.getFile());
                        }
                        System.out.println("Duplicate check completed.");
                        break;
                    case 6:
                        System.out.println("Available images:");
                        index = 1;
                        imageList = new File[files.length];
                        for (File value : files) {
                            if (value.isFile()) {
                                System.out.println(index + ". " + value.getName());
                                imageList[index - 1] = value;
                                index++;
                            }
                        }

                        System.out.println("Enter the number of the image to remove tags from: ");
                        int imgNum = Integer.parseInt(scanner.nextLine()) - 1;

                        if (imgNum >= 0 && imgNum < imageList.length && imageList[imgNum] != null) {
                            File selectedFile = imageList[imgNum];
                            String existingTagString = db.getTags(selectedFile.getAbsolutePath());

                            if (existingTagString == null || existingTagString.isEmpty()) {
                                System.out.println("No tags found for this image.");
                                break;
                            }

                            String[] tagArray = existingTagString.split(",");
                            for (int t = 0; t < tagArray.length; t++) {
                                System.out.println((t + 1) + ". " + tagArray[t].trim());
                            }

                            System.out.println("Enter the numbers of the tags to remove (comma separated): ");
                            String tagSelection = scanner.nextLine();
                            String[] selectedTagIndexes = tagSelection.split(",");

                            Set<String> tagsToRemove = new HashSet<>();
                            for (String tagIndexStr : selectedTagIndexes) {
                                try {
                                    int tagIndex = Integer.parseInt(tagIndexStr.trim()) - 1;
                                    if (tagIndex >= 0 && tagIndex < tagArray.length) {
                                        tagsToRemove.add(tagArray[tagIndex].trim());
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid input: " + tagIndexStr);
                                }
                            }

                            if (!tagsToRemove.isEmpty()) {
                                String tagsToRemoveStr = String.join(",", tagsToRemove);
                                db.removeTags(selectedFile.getAbsolutePath(), tagsToRemoveStr);
                                System.out.println("Removed selected tags from: " + selectedFile.getName());
                            } else {
                                System.out.println("No valid tags selected.");
                            }
                        } else {
                            System.out.println("Invalid image number.");
                        }
                        break;
                    case 7:
                        db.close();
                        scanner.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
