package com.gamelibrary2d.animations;

import java.io.IOException;
import java.io.InputStream;

public interface AnimationReader {
    AnimationMetadata read(InputStream stream) throws IOException;
}
