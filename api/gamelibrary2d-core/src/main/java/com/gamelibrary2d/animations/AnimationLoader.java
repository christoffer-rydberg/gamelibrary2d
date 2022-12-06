package com.gamelibrary2d.animations;

import com.gamelibrary2d.imaging.Image;
import com.gamelibrary2d.imaging.ImageReader;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.Runtime;
import com.gamelibrary2d.animations.formats.gif.GifAnimationReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AnimationLoader {
    private final static Map<String, AnimationReader> animationReaders = initializeAnimationReaders();

    private static Map<String, AnimationReader> initializeAnimationReaders() {
        HashMap<String, AnimationReader> animationLoaders = new HashMap<>();
        animationLoaders.put(StandardAnimationFormats.GIF, new GifAnimationReader());
        return animationLoaders;
    }

    private static List<Path> getFilePaths(Path folderPath, Pattern pattern) throws IOException {
        List<Path> filePaths = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(folderPath, 1)) {
            for (Iterator<Path> itr = stream.iterator(); itr.hasNext(); ) {
                Path filePath = itr.next();
                if (pattern.matcher(filePath.toString()).find()) {
                    filePaths.add(filePath);
                }
            }
        }

        filePaths.sort(Comparator.comparing(a -> a.getFileName().toString()));

        return filePaths;
    }

    /**
     * Sets the animation reader for the specified format.
     *
     * @param format The animation format.
     * @param reader The animation reader.
     */
    public static void setAnimationReader(String format, AnimationReader reader) {
        animationReaders.put(format.toLowerCase(), reader);
    }

    /**
     * Loads an animation from the specified stream.
     *
     * @param stream The animation stream.
     * @param format The animation format.
     */
    public static AnimationMetadata load(InputStream stream, String format) throws IOException {
        String lowerCaseFormat = format.toLowerCase();
        if (animationReaders.containsKey(lowerCaseFormat)) {
            return animationReaders.get(lowerCaseFormat).read(stream);
        } else {
            throw new IllegalArgumentException(String.format("Unsupported format: %s", format));
        }
    }

    /**
     * Loads an animation from the specified url.
     *
     * @param url    The animation url.
     * @param format The animation format.
     */
    public static AnimationMetadata load(URL url, String format) throws IOException {
        try (InputStream stream = url.openStream()) {
            return load(stream, format);
        }
    }

    /**
     * Loads an animation from all image files inside the specified folder that matches the specified pattern.
     */
    public static AnimationMetadata load(Path folderPath, Pattern filePattern, float frameDuration) throws IOException {
        return load(
                Runtime.getFramework().createDefaultImageReader(),
                folderPath,
                filePattern,
                frameDuration
        );
    }

    /**
     * Loads an animation from all image files inside the specified folder that matches the specified pattern.
     */
    public static AnimationMetadata load(ImageReader imageReader, Path folderPath, Pattern filePattern, float frameDuration) throws IOException {
        List<Path> filePaths = getFilePaths(folderPath, filePattern);
        List<AnimationFrameMetadata> frames = new ArrayList<>(filePaths.size());
        for (Path filePath : filePaths) {
            try (InputStream stream = filePath.toUri().toURL().openStream()) {
                Image image = imageReader.read(stream);
                frames.add(new AnimationFrameMetadata(
                        image,
                        new Rectangle(0, 0, 1, 1),
                        -image.getWidth() / 2f,
                        -image.getHeight() / 2f,
                        frameDuration,
                        false,
                        false));
            }

        }

        return new AnimationMetadata(frames);
    }
}
