package dev.helmocraft.mcassetindexextractor.util;

import dev.helmocraft.mcassetindexextractor.gui.MainWindow;
import dev.helmocraft.mcassetindexextractor.main.Index;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class PathPresets implements ActionListener {

    private static final PathPresets instance = new PathPresets();
    public static PathPresets getInstance() { return instance; }

    private final JSONObject pathsConfig;
    private final JMenu loadFromMenu = new JMenu("Load from...");


    private PathPresets() {
        pathsConfig = new JSONObject(new JSONTokener(Objects.requireNonNull(getClass().getResourceAsStream("/paths.json"))));

        pathsConfig.keys().forEachRemaining(pool -> {
            // Iteration for one registered path pool (for example 'vanilla' or 'prismlauncher_flatpak')
            JSONArray possiblePaths = pathsConfig.getJSONObject(pool).getJSONArray("paths");

            // Sort out the paths that exist
            ArrayList<File> validPaths = new ArrayList<>();
            for(int i = 0; i < possiblePaths.length(); i++) {
                File path = new File(possiblePaths.getString(i).replace("~", System.getProperty("user.home"))); // Replace ~ by the user's home directory
                if(path.exists()) {
                    validPaths.add(path);
                }
            }

            // Determine the final path
            if(!validPaths.isEmpty()) {
                File lastModifiedPath = validPaths.get(0); // We'll start with the 0th item of the list...
                for(int j = 1; j < validPaths.size(); j++) {  // ...and then iterate through the rest of the list, starting with the 1st item
                    if(validPaths.get(j).lastModified() > lastModifiedPath.lastModified()) {
                        lastModifiedPath = validPaths.get(j);
                    }
                }
                System.out.println("Path " + lastModifiedPath + " of pool " + pool + " was last modified and will be used.");

                PathPreset finalMenuItem = new PathPreset(pathsConfig.getJSONObject(pool).getString("name"), lastModifiedPath);
                finalMenuItem.addActionListener(this);

                loadFromMenu.add(finalMenuItem);

            } else {
                System.out.println("No valid paths found for pool " + pool);
            }
        });
    }

    public JMenu getLoadFromMenu() {
        return loadFromMenu;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        // Handle interactions with the "Load..." and "Load from... > Submenu" menu entries
        PathPreset source = (PathPreset) event.getSource();
        File selected = MainWindow.getInstance().showFileChooser(source.getPath(), new FileNameExtensionFilter[]{MainWindow.jsonExtensionFilter});

        if(selected != null) {
            if(selected.exists()) {
                MainWindow.getInstance().getIndex().loadIndex(selected);
            } else {
                MainWindow.getInstance().showErrorMessage("Error opening file", "Unable to open " + selected);
            }
        }

    }
}
