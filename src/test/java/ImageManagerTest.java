import org.joeri.dao.ImageDAO;
import org.joeri.dao.SQLiteImageDAO;
import org.joeri.service.ImageManager;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ImageManagerTest {
    private ImageDAO dao;
    private ImageManager manager;
    private File testImage;

    @BeforeEach
    void setUp() throws Exception {
        dao = new SQLiteImageDAO(":memory:");
        manager = new ImageManager(dao);

        testImage = File.createTempFile("test-image", ".jpg");
        try (FileWriter writer = new FileWriter(testImage)) {
            writer.write("dummy content");
        }

        manager.addImage(testImage);
    }

    @AfterEach
    void tearDown() throws Exception {
        dao.close();
        if (testImage.exists()) testImage.delete();
    }

    @Test
    void testAddImage_insertsNewImage() throws SQLException {
        List<String> images = dao.listAllImages();
        assertEquals(1, images.size());
        assertTrue(images.get(0).contains(testImage.getName()));
    }

    @Test
    void testAddImage_duplicateIsDeleted() throws Exception {
        File duplicate = File.createTempFile("test-image-duplicate", ".jpg");
        Files.write(duplicate.toPath(), Files.readAllBytes(testImage.toPath()));

        assertTrue(duplicate.exists());
        manager.addImage(duplicate);
        assertFalse(duplicate.exists(), "Duplicate image should be deleted");

        duplicate.deleteOnExit();
    }

    @Test
    void testAddTags_addsNewTags() throws SQLException {
        Set<String> tags = new HashSet<>(Arrays.asList("nature", "vacation"));
        manager.addTags(testImage.getAbsolutePath(), tags);

        Set<String> storedTags = dao.getTags(testImage.getAbsolutePath());
        assertEquals(tags, storedTags);
    }

    @Test
    void testRemoveTags_removesSpecifiedTags() throws SQLException {
        Set<String> initialTags = new HashSet<>(Arrays.asList("nature", "vacation", "sunset"));
        manager.addTags(testImage.getAbsolutePath(), initialTags);

        manager.removeTags(testImage.getAbsolutePath(), Set.of("vacation"));

        Set<String> tags = dao.getTags(testImage.getAbsolutePath());
        assertEquals(Set.of("nature", "sunset"), tags);
    }

    @Test
    void testSearchImagesByTags_returnsCorrectImages() throws SQLException {
        manager.addTags(testImage.getAbsolutePath(), Set.of("beach", "sun"));

        List<String> results = manager.searchImagesByTags(Set.of("beach"));
        assertTrue(results.contains(testImage.getAbsolutePath()));
    }
}
