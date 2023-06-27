package dev.helmocraft.mcassetindexextractor.util;

import javax.swing.*;
import java.io.File;

public class PathPreset extends JMenuItem {

    private final File path;

    public PathPreset(String name, File path) {
        super(name);

        this.path = path;
    }

    public File getPath() {
        return path;
    }
}
