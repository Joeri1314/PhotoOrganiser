package org.joeri.cli;

import org.joeri.service.ImageManager;
import org.joeri.dao.ImageDAO;
import org.joeri.util.MetadataReader;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

public class PhotoOrganizerCLI {
    private final ImageManager manager;
    private final ImageDAO dao;
    private final Scanner scanner = new Scanner(System.in);
    private final File imagesDir = new File("images");

    public PhotoOrganizerCLI(ImageManager manager, ImageDAO dao) {
        this.manager = manager;
        this.dao = dao;
    }

    public void start() throws SQLException {
        while (true) {
            System.out.println("\n1. List all images\n2. Tag images\n3. List all tags\n4. Search images by tags\n5. Remove duplicate images\n6. Remove tags from image\n7. Exit");

            List<String> images = dao.listAllImages();

            switch (scanner.nextLine().trim()) {
                case "1":
                    for (int i = 0; i < images.size(); i++) {
                        String imagePath = images.get(i);
                        File imageFile = new File(imagePath).isAbsolute() ? new File(imagePath) : new File(imagesDir, imagePath);
                        Date dateTaken = MetadataReader.getDateTaken(imageFile);
                        String metadataInfo = (dateTaken != null) ? " (Taken on: " + dateTaken + ")" : " (No metadata)";
                        System.out.println((i + 1) + ". " + imageFile.getName() + metadataInfo);
                    }
                    break;
                case "2":
                    images = dao.listAllImages();
                    for (int i = 0; i < images.size(); i++) {
                        System.out.println((i + 1) + ". " + images.get(i));
                    }
                    System.out.println("Select image numbers to tag (comma-separated):");
                    String[] imgSelections = scanner.nextLine().split(",");
                    System.out.println("Enter tags to add (comma-separated):");
                    Set<String> tagsToAdd = new HashSet<>(Arrays.asList(scanner.nextLine().split(",")));
                    for (String sel : imgSelections) {
                        int idx = Integer.parseInt(sel.trim()) - 1;
                        if (idx >= 0 && idx < images.size()) {
                            manager.addTags(images.get(idx), tagsToAdd);
                        }
                    }
                    break;
                case "3":
                    Set<String> tags = dao.listAllTags();
                    int idx = 1;
                    for (String tag : tags) {
                        System.out.println(idx++ + ". " + tag);
                    }
                    break;
                case "4":
                    System.out.println("Enter tags to search (comma-separated):");
                    Set<String> searchTags = new HashSet<>(Arrays.asList(scanner.nextLine().split(",")));
                    List<String> foundImages = manager.searchImagesByTags(searchTags);
                    foundImages.forEach(System.out::println);
                    break;
                case "5":
                    File[] files = imagesDir.listFiles(File::isFile);
                    if (files != null) {
                        for (File file : files) manager.addImage(file);
                        System.out.println("Duplicate check completed.");
                    }
                    break;
                case "6":
                    images = dao.listAllImages();
                    for (int i = 0; i < images.size(); i++) {
                        System.out.println((i + 1) + ". " + images.get(i));
                    }
                    System.out.println("Select image number to remove tags from:");
                    idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    if (idx >= 0 && idx < images.size()) {
                        Set<String> currentTags = dao.getTags(images.get(idx));
                        List<String> tagList = new ArrayList<>(currentTags);
                        for (int i = 0; i < tagList.size(); i++) {
                            System.out.println((i + 1) + ". " + tagList.get(i));
                        }
                        System.out.println("Select tag numbers to remove (comma-separated):");
                        String[] tagSelections = scanner.nextLine().split(",");
                        Set<String> tagsToRemove = new HashSet<>();
                        for (String sel : tagSelections) {
                            int tagIdx = Integer.parseInt(sel.trim()) - 1;
                            if (tagIdx >= 0 && tagIdx < tagList.size()) {
                                tagsToRemove.add(tagList.get(tagIdx));
                            }
                        }
                        manager.removeTags(images.get(idx), tagsToRemove);
                    }
                    break;
                case "7":
                    return;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }
}
