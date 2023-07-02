package com.gamelibrary2d.demos.networkgame.client.objects.network.decoration;

import com.gamelibrary2d.demos.networkgame.client.ResourceManager;
import com.gamelibrary2d.demos.networkgame.common.ObjectTypes;
import com.gamelibrary2d.sound.SoundManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;

import java.util.regex.Pattern;

public class SoundMap {
    private final SoundManager soundManager;
    private final ResourceManager resourceManager;
    private final Hashtable<Byte, Hashtable<Byte, String>> sound = new Hashtable<>();

    public SoundMap(SoundManager soundManager, ResourceManager resourceManager) {
        this.soundManager = soundManager;
        this.resourceManager = resourceManager;
    }

    private static Byte tryParseByte(String s) {
        try {
            return Byte.parseByte(s);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private static Byte readFileNameAsByte(Path path) {
        String name = path.getFileName().toString();
        int pos = name.lastIndexOf(".");
        return pos > 0 ? tryParseByte(name.substring(0, pos)) : null;
    }

    private Hashtable<Byte, String> load(String resourceFolder, String regex) throws IOException {
        Hashtable<Byte, String> sounds = new Hashtable<>();
        Pattern pattern = regex != null ? Pattern.compile(regex) : null;
        List<String> paths = resourceManager.list(resourceFolder);
        for (String path : paths) {
            if (pattern == null || pattern.matcher(path).find()) {
                Byte key = readFileNameAsByte(Paths.get(path));
                if (key != null) {
                    try (InputStream is = resourceManager.open(path)) {
                        soundManager.loadBuffer(path, is, "ogg");
                        sounds.put(key, path);
                    }
                }
            }
        }

        return sounds;
    }

    private void initializeDestroyedObjects() throws IOException {
        Hashtable<Byte, String> sounds = load("sounds/obstacles/destroyed/", "^*.ogg$");
        this.sound.put(ObjectTypes.OBSTACLE, sounds);
    }

    public void initialize() throws IOException {
        initializeDestroyedObjects();
    }

    public String getDestroyedSound(Byte primaryType, Byte secondaryType) {
        Hashtable<Byte, String> sounds = this.sound.get(primaryType);
        if (sounds != null) {
            String sound = sounds.get(secondaryType);
            return sound != null ? sound : sounds.get((byte) 0);
        } else {
            return null;
        }
    }
}