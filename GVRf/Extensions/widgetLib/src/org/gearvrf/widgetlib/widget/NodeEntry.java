package org.gearvrf.widgetlib.widget;

import org.gearvrf.widgetlib.log.Log;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.RuntimeAssertion;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingFormatArgumentException;
import java.util.Set;

/**
 * The class is used for parsing Widget model. The root node has to be always named as
 * {@link #ROOT_NODE_NAME} in the model. Each node is represented by scene object with set of
 * parameters specifying the widget type, look and feel.
 */
@Deprecated
public class NodeEntry {
    private final static Set<String> mandatoryKeys = new HashSet<>();
    private final static Set<String> caseSensitiveKeys = new HashSet<>();
    private static final String KEY_NAME = "name";
    private static final String KEY_CLASS_NAME = "class";
    private static final String ROOT_NODE_NAME = "RootNode";
    private static final String ROOT_NODE_CLASS_NAME = GroupWidget.class.getName();


    static {
        mandatoryKeys.add(KEY_NAME);

        caseSensitiveKeys.add(KEY_NAME);
        caseSensitiveKeys.add(KEY_CLASS_NAME);
    }

    /**
     * Create the node based on the scene object. The mandatory keys should always be valid and
     * assigned based on the model parsing.
     *
     * @param sceneObject {@link GVRSceneObject} to create the node from
     * @throws IllegalFormatException if the properties encoded in the scene object's {@linkplain
     * GVRSceneObject#getName() name} don't include the mandatory 'name' property.
     */
    public NodeEntry(GVRSceneObject sceneObject) throws IllegalFormatException {
        String name = sceneObject.getName();
        Log.d(TAG, "NodeEntry(): %s", name);
        if (ROOT_NODE_NAME.equals(name)) {
            properties = new HashMap<>();
            properties.put(KEY_CLASS_NAME, ROOT_NODE_CLASS_NAME);
            properties.put(KEY_NAME, ROOT_NODE_NAME);
        } else {
            properties = NameDemangler.demangleString(name);
        }

        if (properties == null || properties.get(KEY_NAME) == null) {
            if (properties == null) {
                properties = new HashMap<>();
            } else {
                properties.clear();
            }
            properties.put(KEY_NAME, name);
        }

        // Validation
        if (properties != null) {
            for (String key: mandatoryKeys) {
                if (!properties.containsKey(key)) {
                    throw new MissingFormatArgumentException("Incorrect widget properties format for: " + name +
                            " the mandatory key <" + key + "> does not exist!");
                }
            }
            this.name = properties.get(KEY_NAME);
            className = properties.get(KEY_CLASS_NAME);
        }
    }

    /**
     * Gets Widget class name property
     * @return Widget class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets node name property
     * @return node name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the property by key
     * @param key property key
     * @return node property
     */
    public String getProperty(String key) {
        return properties == null ? null : properties.get(key);
    }

    /**
     * Gets the property by key
     * @param key property key
     * @return node property
     */
    public String getProperty(Enum<?> key) {
        return getProperty(key, true);
    }

    /**
     * Gets the property by key converted to lowercase if requested
     * @param key property key
     * @param lowerCase convert property to lowercase if it is true, keep the original one if it is
     *                  false
     * @return node property
     */
    @SuppressWarnings("WeakerAccess")
    public String getProperty(Enum<?> key, boolean lowerCase) {
        final String keyName;
        if (lowerCase) {
            keyName = key.name().toLowerCase(Locale.ENGLISH);
        } else {
            keyName = key.name();
        }
        return getProperty(keyName);
    }

    /**
     * Converts the node to JSON
     * @return JSON object
     */
    public JSONObject toJSON() {
        try {
            return new JSONObject(properties).putOpt("name", getName());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e, "toJSON()");
            throw new RuntimeAssertion("NodeEntry.toJSON() failed for '%s'", this);
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("name: ").append(name).append(',');
        b.append("className: ").append(className).append(',');
        if (properties != null) {
            for (Entry<String, String> entry : properties.entrySet()) {
                b.append(entry.getKey()).append(": ").append(entry.getValue())
                        .append(',');
            }
        }
        return b.toString();
    }

    static class NameDemangler {
        private static final String ENTRY_SEPERATOR_REGEX = "__";
        private static final String KEY_VALUE_SEPERATOR = "_";
        private static final String KEY_VALUE_SEPERATOR_REGEX = "_";

        /**
         * Returns a {@code Map<String, String>} containing key value pairs
         * from a mangled string. The format of the mangled string is
         * "key1_value1__key2_value2". The values can be null: for example,
         * "key1_value1__key3__key4" is also valid. Keys with null values
         * can be seen as tags.
         *
         * @param mangledString The mangled string.
         *
         * @return The {@code Map<String, String>} containing key-value pairs. It
         * returns null if {@code mangledString} is not a mangled string.
         */
        static Map<String,String> demangleString(String mangledString) {
            Map<String,String> res = new HashMap<>();

            String[] entries = mangledString.split(ENTRY_SEPERATOR_REGEX);
            if (entries.length == 1 && !entries[0].contains(KEY_VALUE_SEPERATOR)) {
                return null;
            }

            for (String entry : entries) {
                String[] keyValuePair = entry.split(KEY_VALUE_SEPERATOR_REGEX);
                String key = keyValuePair[0];

                // The value can be null
                String value = keyValuePair.length >= 2 ? keyValuePair[1] : null;

                if (value != null && !caseSensitiveKeys.contains(key)) {
                    value = value.toLowerCase();
                }

                res.put(key.toLowerCase(), value);
            }

            return res;
        }
    }

    protected String name;
    private String className;
    private Map<String, String> properties;
    @SuppressWarnings("deprecation")
    private static final String TAG = NodeEntry.class.getSimpleName();
}
