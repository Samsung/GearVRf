package org.gearvrf.script;

import java.io.IOException;
import java.util.Arrays;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.utility.TextFile;

import com.google.gson.Gson;

/**
 * Represents a script bundle loaded from a JSON file, and its storage
 * volume.
 */
public class GVRScriptBundle {
    /**
     * The content of the bundle file is loaded from a JSON file.
     */
    public static class GVRScriptBundleFile {
        public String name;
        public GVRScriptBindingEntry[] binding;

        @Override
        public String toString() {
            return "GVRScriptBundleFile [name=" + name + ", binding="
                    + Arrays.toString(binding) + "]";
        }
    }

    protected GVRContext gvrContext;

    /**
     * The contents of the script bundle from a JSON file.
     */
    protected GVRScriptBundleFile file;

    /**
     * The volume of the script bundle. The script bundle
     * is loaded from this volume, and it also serves as the default
     * volume for scripts referenced in the bundle.
     */
    protected GVRResourceVolume volume;

    /**
     * Returns the contents of the bundle.
     * @return The {@link GVRScriptBundleFile} object.
     */
    public GVRScriptBundleFile getContent() {
        return file;
    }

    /**
     * Loads a {@link GVRScriptBundle} from a file.
     * @param scriptManager
     *         The script manager.
     * @param filePath
     *         The file name of the script bundle in JSON format.
     * @param volume
     *         The {@link GVRResourceVolume} from which to load script bundle.
     * @return
     *         The {@link GVRScriptBundle} object with contents from the JSON file.
     *
     * @throws IOException
     */
    public static GVRScriptBundle loadFromFile(GVRContext gvrContext, String filePath,
            GVRResourceVolume volume) throws IOException {
        GVRAndroidResource fileRes = volume.openResource(filePath);
        String fileText = TextFile.readTextFile(fileRes.getStream());
        fileRes.closeStream();

        GVRScriptBundle bundle = new GVRScriptBundle();
        Gson gson = new Gson();
        try {
            bundle.gvrContext = gvrContext;
            bundle.file = gson.fromJson(fileText, GVRScriptBundleFile.class);
            bundle.volume = volume;
            return bundle;
        } catch (Exception e) {
            throw new IOException("Cannot load the script bundle", e);
        }
    }

    @Override
    public String toString() {
        return "GVRScriptBundle [file=" + file + ", volume=" + volume + "]";
    }
}
