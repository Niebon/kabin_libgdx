package dev.kabin.util.pools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImagePool {


    // References to images for image pool.
    private static final Map<String, BufferedImage> bufferedImages = new HashMap<>();


    /**
     * Handling buffered image objects in order to save memory.
     * The buffered image will be read with original size.
     */
    public static BufferedImage findBufferedImage(String imagePath) {
        if (!bufferedImages.containsKey(imagePath)) {
            try {
                bufferedImages.put(imagePath, ImageIO.read(new File(imagePath)));
            } catch (IOException e) {
                throw new RuntimeException("Caught exception while reading image from the path '" + imagePath
                        + "'.", e.getCause());
            }
        }
        return bufferedImages.get(imagePath);
    }

    public static int width(String imagePath) {
        return findBufferedImage(imagePath).getWidth();
    }

    public static int height(String imagePath) {
        return findBufferedImage(imagePath).getHeight();
    }

}
