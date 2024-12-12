package bg.sofia.uni.fmi.mjt.imagekit.filesystem;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LocalFileSystemImageManager implements FileSystemImageManager {
    @Override
    public BufferedImage loadImage(File imageFile) throws IOException {
        if (imageFile == null) {
            throw new IllegalArgumentException("File cannot be null!");
        }
        if (!imageFile.exists()) {
            throw new IOException("The provided file doesn't exist!");
        }
        if (!imageFile.isFile()) {
            throw new IOException("The provided argument should be a file!");
        }
        if (!Set.of("bmp", "png", "jpeg").contains(imageFile.getName().substring(
                imageFile.getName().lastIndexOf('.')))) {
            throw new IOException("Invalid file format!");
        }

        BufferedImage in = ImageIO.read(imageFile);
        BufferedImage result = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g = result.createGraphics();
        g.drawImage(in, 0, 0, null);
        g.dispose();
        return result;
    }

    @Override
    public List<BufferedImage> loadImagesFromDirectory(File imagesDirectory) throws IOException {
        if (imagesDirectory == null) {
            throw new IllegalArgumentException("Directory cannot be null!");
        }
        if (!imagesDirectory.exists()) {
            throw new IOException("The provided directory doesn't exist!");
        }
        if (!imagesDirectory.isDirectory()) {
            throw new IOException("The provided argument is not a directory!");
        }

        List<BufferedImage> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(imagesDirectory.toPath())) {
            for (Path filePath : stream) {
                if (filePath == null) {
                    throw new IllegalArgumentException("File cannot be null!");
                }
                result.add(loadImage(filePath.toFile()));
            }
        } catch (IOException | IllegalArgumentException e) {
            throw e;
        }

        return result;
    }

    @Override
    public void saveImage(BufferedImage image, File imageFile) throws IOException {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null!");
        }
        if (imageFile == null) {
            throw new IllegalArgumentException("Image file cannot be null!");
        }
        if (!imageFile.getParentFile().exists()) {
            throw new IOException("Image file's parent directory doesn't exist");
        }
        if (imageFile.exists()) {
            throw new IOException("Image file already exists!");
        }
        if (!Set.of("bmp", "png", "jpeg").contains(imageFile.getName().substring(
                imageFile.getName().lastIndexOf('.')))) {
            throw new IOException("Invalid file format!");
        }

        ImageIO.write(image,
                imageFile.getAbsolutePath().substring(imageFile.getAbsolutePath().lastIndexOf('.') + 1), imageFile);
    }
}