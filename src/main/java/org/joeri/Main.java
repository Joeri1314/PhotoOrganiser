package org.joeri;

import org.joeri.cli.PhotoOrganizerCLI;
import org.joeri.dao.ImageDAO;
import org.joeri.dao.SQLiteImageDAO;
import org.joeri.service.ImageManager;

public class Main {
    public static void main(String[] args) {
        try (ImageDAO dao = new SQLiteImageDAO("photo-organizer.db")) {
            ImageManager manager = new ImageManager(dao);
            PhotoOrganizerCLI cli = new PhotoOrganizerCLI(manager, dao);
            cli.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
