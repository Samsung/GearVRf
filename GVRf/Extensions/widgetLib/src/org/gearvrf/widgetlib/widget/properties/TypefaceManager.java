package org.gearvrf.widgetlib.widget.properties;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Environment;

import org.gearvrf.widgetlib.log.Log;

import org.gearvrf.GVRContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.*;

public class TypefaceManager {
    /**
     * Creates TypefaceManager
     */
    public TypefaceManager(GVRContext gvrContext) {
        mGvrContext = gvrContext;
    }

    private enum Attributes {
        resource_type, path, family, style
    }

    public Typeface getTypeface(JSONObject json) {
        try {
            final Typeface tf;
            if (hasString(json, Attributes.resource_type)) {
                Log.d(TAG, "getTypeface(): getting resource typeface");
                tf = getTypefaceResource(json);
                Log.d(TAG, "getTypeface(): got typeface: %s", tf);
            } else {
                Log.d(TAG, "getTypeface(): getting installed typeface");
                tf = getTypefaceInstalled(json);
            }
            return tf;
        } catch (JSONException e) {
            throw new RuntimeException("Failed to load typeface from JSON: " + json, e);
        }
    }

    private static class TypefaceSpec {
        final public String family;
        final public int style;

        TypefaceSpec(final JSONObject json) throws JSONException {
            family = optString(json, Attributes.family);
            style = getInt(json, Attributes.style);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TypefaceSpec)) return false;

            TypefaceSpec that = (TypefaceSpec) o;

            if (style != that.style) return false;
            return family != null ? family.equals(that.family) : that.family == null;

        }

        @Override
        public int hashCode() {
            int result = family != null ? family.hashCode() : 0;
            result = 31 * result + style;
            return result;
        }
    }

    private Typeface getTypefaceInstalled(JSONObject json) throws JSONException {
        final TypefaceSpec ts;
        ts = new TypefaceSpec(json);
        Typeface tf = mTypefaceCache.get(ts);
        if (tf == null) {
            if (ts.family == null || ts.family.isEmpty()) {
                tf = Typeface.defaultFromStyle(ts.style);
            } else {
                tf = Typeface.create(ts.family, ts.style);
            }
            mTypefaceCache.put(ts, tf);
        }
        return tf;
    }

    private enum ResourceType {
        asset {
            @Override
            public Typeface getTypeface(Context context, String path) {
                AssetManager am = context.getAssets();
                return Typeface.createFromAsset(am, path);
            }
        },
        file {
            @Override
            public Typeface getTypeface(Context context, String path) {
                File extFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                File f = new File(extFilesDir, path);
                Log.d(TAG, "getTypeface(): creating from file '%s'", f.getPath());
                Typeface typeface = Typeface.createFromFile(f);
                Log.d(TAG, "getTypeface(): typeface '%s': %s", path, typeface);
                return typeface;
            }
        };

        abstract public Typeface getTypeface(Context context, String path);
    }

    private Typeface getTypefaceResource(JSONObject json) throws JSONException {
        final String path = getString(json, Attributes.path);
        Log.d(TAG, "getTypefaceResource(): getting typeface at path %s", path);
        Typeface tf = mTypefaceCache.get(path);
        if (tf == null) {
            Log.d(TAG, "getTypefaceResource(): typeface not in cache");
            final ResourceType resourceType = getResourceType(json);
            Log.d(TAG, "getTypefaceResource(): typeface resource type: %s", resourceType);
            tf = resourceType.getTypeface(mGvrContext.getContext(), path);
            Log.d(TAG, "getTypefaceResource(): typeface for '%s': %s", path, tf);
            mTypefaceCache.put(path, tf);
        }
        return tf;
    }

    private ResourceType getResourceType(JSONObject json) {
        return optEnum(json, Attributes.resource_type, ResourceType.class);
    }

    private final GVRContext mGvrContext;
    private final Map<Object, Typeface> mTypefaceCache = new HashMap<>();

    private static final String TAG = TypefaceManager.class.getSimpleName();
}
