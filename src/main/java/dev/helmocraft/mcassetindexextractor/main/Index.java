package dev.helmocraft.mcassetindexextractor.main;

import dev.helmocraft.mcassetindexextractor.gui.MainWindow;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

public class Index {

    private static final Index indexInstance = new Index();
    public static Index getInstance() {
        return indexInstance;
    }

    private JSONObject loadedIndex;
    private final ArrayList<String> assetNames = new ArrayList<>();
    private File loaded;


    public void loadIndex(File path) {
        loaded = path;
        try {
            loadedIndex = new JSONObject(new JSONTokener(new FileReader(path)));
            assetNames.clear();
            assetNames.addAll(loadedIndex.getJSONObject("objects").keySet());

            // Sort the asset names
            String[] sortedAssetNames = new String[assetNames.size()];
            assetNames.toArray(sortedAssetNames);

            Arrays.sort(sortedAssetNames);

            assetNames.clear();
            assetNames.addAll(Arrays.asList(sortedAssetNames));

            MainWindow.getInstance().setAssetSearchResults(search(""));
        } catch (Exception e) {
            MainWindow.getInstance().showErrorMessage("Unable to load file", "There was an error loading the index." +
                    "See the console output for details.");
            e.printStackTrace();
        }
    }

    public String[] search(String query) {
        if(loadedIndex != null) {
            long startTime = System.nanoTime();

            ArrayList<String> matches = new ArrayList<>();
            assetNames.forEach(assetName -> {
                if(assetName.contains(query)) {
                    matches.add(assetName);
                }
            });

            String[] result = new String[matches.size()];
            matches.toArray(result);

            long endTime = System.nanoTime();
            MainWindow.printDebug("Search took " + (endTime - startTime) + " nanoseconds");

            return result;

        } else return new String[]{};
    }

    public void reload() {
        if(loaded != null) {
            loadIndex(loaded);
        }
    }

    public void saveAsset(String assetPath, File location) {
        String hash = loadedIndex.getJSONObject("objects").getJSONObject(assetPath).getString("hash");
        File path = new File(loaded.getParentFile().getParentFile().getAbsolutePath() + "/objects/" + hash.toCharArray()[0] + hash.toCharArray()[1] + "/" + hash);

        try {
            Files.copy(path.toPath(), location.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception x) {
            MainWindow.getInstance().showErrorMessage("Error", "There was an error copying the asset. See console" +
                    " for details");
        }
    }

    public static String fileNameOfAsset(String assetPath) {
        String[] split = assetPath.split("/");
        return split[split.length-1];
    }
}
