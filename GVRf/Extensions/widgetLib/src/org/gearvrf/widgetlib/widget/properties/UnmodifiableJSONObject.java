package org.gearvrf.widgetlib.widget.properties;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class UnmodifiableJSONObject extends JSONObject {
    public UnmodifiableJSONObject() {
        this(new JSONObject());
    }

    public UnmodifiableJSONObject(JSONObject wrapped) {
        if (wrapped == null) {
            mWrapped = new JSONObject();
        } else {
            mWrapped = wrapped;
        }
    }

    private final JSONObject mWrapped;

    @Override
    public JSONObject accumulate(String name, Object value)
            throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(String name) throws JSONException {
        Object o = mWrapped.get(name);
        if (o instanceof JSONObject) {
            o = new UnmodifiableJSONObject((JSONObject) o);
        } else if (o instanceof JSONArray) {
            o = new UnmodifiableJSONArray((JSONArray) o);
        }
        return o;
    }

    @Override
    public boolean getBoolean(String name) throws JSONException {
        return mWrapped.getBoolean(name);
    }

    @Override
    public double getDouble(String name) throws JSONException {
        return mWrapped.getDouble(name);
    }

    @Override
    public int getInt(String name) throws JSONException {
        return mWrapped.getInt(name);
    }

    @Override
    public JSONArray getJSONArray(String name) throws JSONException {
        return new UnmodifiableJSONArray(mWrapped.getJSONArray(name));
    }

    @Override
    public JSONObject getJSONObject(String name) throws JSONException {
        return new UnmodifiableJSONObject(mWrapped.getJSONObject(name));
    }

    @Override
    public long getLong(String name) throws JSONException {
        return mWrapped.getLong(name);
    }

    @Override
    public String getString(String name) throws JSONException {
        return mWrapped.getString(name);
    }

    @Override
    public boolean has(String name) {
        return mWrapped.has(name);
    }

    @Override
    public boolean isNull(String name) {
        return mWrapped.isNull(name);
    }

    @Override
    public Iterator<String> keys() {
        return mWrapped.keys();
    }

    @Override
    public int length() {
        return mWrapped.length();
    }

    @Override
    public JSONArray names() {
        return new UnmodifiableJSONArray(mWrapped.names());
    }

    @Override
    public Object opt(String name) {
        Object opt = mWrapped.opt(name);
        if (opt instanceof JSONObject) {
            opt = new UnmodifiableJSONObject((JSONObject) opt);
        } else if (opt instanceof JSONArray) {
            opt = new UnmodifiableJSONArray((JSONArray) opt);
        }
        return opt;
    }

    @Override
    public boolean optBoolean(String name, boolean fallback) {
        return mWrapped.optBoolean(name, fallback);
    }

    @Override
    public boolean optBoolean(String name) {
        return mWrapped.optBoolean(name);
    }

    @Override
    public double optDouble(String name, double fallback) {
        return mWrapped.optDouble(name, fallback);
    }

    @Override
    public double optDouble(String name) {
        return mWrapped.optDouble(name);
    }

    @Override
    public int optInt(String name, int fallback) {
        return mWrapped.optInt(name, fallback);
    }

    @Override
    public int optInt(String name) {
        return mWrapped.optInt(name);
    }

    @Override
    public UnmodifiableJSONArray optJSONArray(String name) {
        JSONArray jsonArray = mWrapped.optJSONArray(name);
        if (jsonArray != null) {
            return new UnmodifiableJSONArray(jsonArray);
        }
        return null;
    }

    @Override
    public UnmodifiableJSONObject optJSONObject(String name) {
        final JSONObject jsonObject = mWrapped.optJSONObject(name);
        if (jsonObject != null) {
            return new UnmodifiableJSONObject(jsonObject);
        }
        return null;
    }

    @Override
    public long optLong(String name, long fallback) {
        return mWrapped.optLong(name, fallback);
    }

    @Override
    public long optLong(String name) {
        return mWrapped.optLong(name);
    }

    @Override
    public String optString(String name, String fallback) {
        return mWrapped.optString(name, fallback);
    }

    @Override
    public String optString(String name) {
        return mWrapped.optString(name);
    }

    @Override
    public JSONObject put(String name, boolean value) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject put(String name, double value) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject put(String name, int value) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject put(String name, long value) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject put(String name, Object value) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject putOpt(String name, Object value) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        return mWrapped.toJSONArray(names);
    }

    @Override
    public String toString() {
        return mWrapped.toString();
    }

    @Override
    public String toString(int indentSpaces) throws JSONException {
        return mWrapped.toString(indentSpaces);
    }
}
