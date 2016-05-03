package org.gearvrf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.os.Environment;

/**
 * GVRf runtime preference
 */
public class GVRPreference {
    /**
     * Specifies the preference file name under sdcard directory
     */
    public static final String FILENAME = ".gvrf_prefs";

    public static final String KEY_DEBUG_STATS = "debug_stats";
    public static final String KEY_DEBUG_STATS_PERIOD_MS = "debug_stats_period_ms";
    public static final String KEY_STATS_FORMAT = "stats_format";

    /**
     * The singleton instance.
     */
    private static GVRPreference sInstance;

    /**
     * Gets the singleton.
     * @return The GVRPreference instance.
     */
    protected static GVRPreference get() {
        if (sInstance != null) {
            return sInstance;
        }

        synchronized (GVRPreference.class) {
            sInstance = new GVRPreference();
        }

        return sInstance;
    }

    /**
     * Gets a property.
     * @param key The key string.
     * @return The property string.
     */
    public String getProperty(String key) {
        return mProperties.getProperty(key);
    }

    /**
     * Gets a property with a default value.
     * @param key
     *         The key string.
     * @param defaultValue
     *         The default value.
     * @return The property string.
     */
    public String getProperty(String key, String defaultValue) {
        return mProperties.getProperty(key, defaultValue);
    }

    /**
     * Gets a boolean property with a boolean value.
     * @param key The key string.
     * @param defaultValue The default value.
     * @return The boolean property value.
     */
    public Integer getIntegerProperty(String key, Integer defaultValue) {
        String val = getProperty(key, defaultValue.toString());
        try {
            Integer intVal = Integer.parseInt(val);
            return intVal;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a boolean property with a boolean value.
     * @param key The key string.
     * @param defaultValue The default value.
     * @return The boolean property value.
     */
    public Float getFloatProperty(String key, Float defaultValue) {
        String val = getProperty(key, defaultValue.toString());
        try {
            Float intVal = Float.parseFloat(val);
            return intVal;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a boolean property with a boolean value.
     * @param key The key string.
     * @param defaultValue The default value.
     * @return The boolean property value.
     */
    public Boolean getBooleanProperty(String key, Boolean defaultValue) {
        String val = getProperty(key, defaultValue.toString());
        Boolean booleanVal = Boolean.parseBoolean(val);
        return booleanVal;
    }

    private GVRPreference() {
        mProperties = new Properties();

        String prefPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FILENAME;
        try {
            InputStream inputStream = new FileInputStream(prefPath);
            mProperties.load(inputStream);
        } catch (FileNotFoundException e) {
            // nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Properties mProperties;
}
