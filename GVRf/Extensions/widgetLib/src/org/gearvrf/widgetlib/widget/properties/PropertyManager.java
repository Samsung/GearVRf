package org.gearvrf.widgetlib.widget.properties;

import android.content.Context;
import android.support.annotation.NonNull;

import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.log.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PropertyManager {

    @NonNull
    public UnmodifiableJSONObject getInstanceProperties(Class<?> clazz, String name) {
        JSONObject properties = buildInstanceProperties(name, clazz);
        return new UnmodifiableJSONObject(properties);
    }

    @NonNull
    public UnmodifiableJSONObject getWidgetProperties(Widget widget) {
        return getInstanceProperties(widget.getClass(), widget.getName());
    }

    public PropertyManager(Context context, String asset) throws JSONException {
        this(context, asset, null);
    }

    public PropertyManager(Context context, String defaultPropertiesAsset,
                           String customPropertiesAsset) throws JSONException {
        loadClassProperties(context, defaultPropertiesAsset, customPropertiesAsset);
        loadInstanceProperties(context);
    }

    private JSONObject buildInstanceProperties(String name, Class<?> clazz) {
        final JSONObject properties = mInstanceJson.optJSONObject(name);
        final UnmodifiableJSONObject defaultMetadata = getClassProperties(clazz, name);

        if (defaultMetadata == null) {
            if (properties == null) {
                return new JSONObject();
            }
            return properties;
        } else if (properties == null) {
            return defaultMetadata;
        }

        // Overwrite class properties for this widget type with instance-specific properties
        return JSONHelpers.merge(properties, defaultMetadata);
    }

    private String getCanonicalName(Class<?> clazz) {
        String canonicalName = mCanonicalNames.get(clazz);
        if (canonicalName == null) {
            canonicalName = clazz.getCanonicalName();
            mCanonicalNames.put(clazz, canonicalName);
        }
        return canonicalName;
    }

    /* package */
    @SuppressWarnings("unchecked")
    private UnmodifiableJSONObject getClassProperties(Class<?> clazz, String name) {
        final String canonicalName = getCanonicalName(clazz);
        // TODO: Why does caching class properties in mClassProperties not work?
        UnmodifiableJSONObject classProperties = buildClassProperties(clazz, name, canonicalName);

        return classProperties;
    }

    private UnmodifiableJSONObject buildClassProperties(Class<?> clazz, String name, String canonicalName) {
        // Recursively check for class properties up the class hierarchy
        if (name == null || name.isEmpty()) {
            name = clazz.getSimpleName();
        }
        final UnmodifiableJSONObject superProperties;
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            superProperties = getClassProperties(superclass, name);
        } else {
            superProperties = new UnmodifiableJSONObject();
        }
        Log.d(Log.SUBSYSTEM.JSON, TAG,
                "buildClassProperties(%s): getting super properties for %s: %s",
                name, canonicalName, superProperties);

        UnmodifiableJSONObject classProperties = mClassJson.optJSONObject(canonicalName);
        Log.d(Log.SUBSYSTEM.JSON, TAG,
                "buildClassProperties(%s): getting class properties for %s: %s",
                name, canonicalName, classProperties);
        if (classProperties == null) {
            classProperties = new UnmodifiableJSONObject();
        } else {
            Log.d(Log.SUBSYSTEM.JSON, TAG, "buildClassProperties(%s): copy class properties for %s ...", name,
                    canonicalName);
            classProperties = new UnmodifiableJSONObject(JSONHelpers.copy(classProperties));
        }

        JSONObject mergedProperties = JSONHelpers.merge(classProperties, superProperties, name);
        Log.d(Log.SUBSYSTEM.JSON, TAG,
                "buildClassProperties(%s): getting merged properties for %s: %s",
                name, canonicalName, mergedProperties);
        return new UnmodifiableJSONObject(mergedProperties);
    }

    private static boolean isWidgetClass(Class<?> clazz) {
        return Widget.class.isAssignableFrom(clazz);
    }

    private JSONObject loadClassProperties(Context context, String defaultPropertyAsset,
                                           String customPropertiesAsset) throws JSONException {
        JSONObject properties = JSONHelpers.loadJSONAsset(context, defaultPropertyAsset);
        JSONObject customProperties = JSONHelpers.loadJSONAsset(context, customPropertiesAsset);
        JSONObject publicJson = JSONHelpers.loadExternalJSONDocument(context, "user_default_metadata.json");
        Log.d(Log.SUBSYSTEM.JSON, TAG, "loadClassProperties(): public: %s", publicJson);

        JSONHelpers.merge(customProperties, properties);
        JSONHelpers.merge(publicJson, properties);

        Log.d(Log.SUBSYSTEM.JSON, TAG, "loadClassProperties(): %s", properties);
        mClassJson = new UnmodifiableJSONObject(properties.optJSONObject("objects"));
        return properties;
    }

    private void loadInstanceProperties(Context context)
            throws JSONException {
        final JSONObject json = JSONHelpers.loadJSONAsset(context, "objects.json");
        mInstanceJson = new UnmodifiableJSONObject(json.optJSONObject("objects"));
        Log.v(Log.SUBSYSTEM.JSON, TAG, "loadInstanceProperties(): loaded object properties: %s",
                mInstanceJson);
    }

    private final Map<Class<?>, String> mCanonicalNames = new HashMap<>();
    private UnmodifiableJSONObject mClassJson;
    private Map<Class<?>, JSONObject> mClassProperties = new HashMap<>();
    private JSONObject mInstanceJson;

    private static final String TAG = PropertyManager.class.getSimpleName();
}
