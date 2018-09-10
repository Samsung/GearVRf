package org.gearvrf.widgetlib.widget.properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UnmodifiableJSONArray extends JSONArray {

    public UnmodifiableJSONArray(final JSONArray wrapped) {
        if (wrapped == null) {
            mWrapped = new JSONArray();
        } else {
            mWrapped = wrapped;
        }
    }

    @Override
    public boolean equals(Object o) {
        return mWrapped.equals(o);
    }

    @Override
    public Object get(int index) throws JSONException {
        Object o = mWrapped.get(index);
        if (o instanceof JSONArray) {
            o = new UnmodifiableJSONArray((JSONArray) o);
        } else if (o instanceof JSONObject) {
            o = new UnmodifiableJSONObject((JSONObject) o);
        }
        return o;
    }

    @Override
    public boolean getBoolean(int index) throws JSONException {
        return mWrapped.getBoolean(index);
    }

    @Override
    public double getDouble(int index) throws JSONException {
        return mWrapped.getDouble(index);
    }

    @Override
    public int getInt(int index) throws JSONException {
        return mWrapped.getInt(index);
    }

    @Override
    public JSONArray getJSONArray(int index) throws JSONException {
        return new UnmodifiableJSONArray(mWrapped.getJSONArray(index));
    }

    @Override
    public JSONObject getJSONObject(int index) throws JSONException {
        return new UnmodifiableJSONObject(mWrapped.getJSONObject(index));
    }

    @Override
    public long getLong(int index) throws JSONException {
        return mWrapped.getLong(index);
    }

    @Override
    public String getString(int index) throws JSONException {
        return mWrapped.getString(index);
    }

    @Override
    public int hashCode() {
        return mWrapped.hashCode();
    }

    @Override
    public boolean isNull(int index) {
        return mWrapped.isNull(index);
    }

    @Override
    public String join(String separator) throws JSONException {
        return mWrapped.join(separator);
    }

    @Override
    public int length() {
        return mWrapped.length();
    }

    @Override
    public Object opt(int index) {
        Object opt = mWrapped.opt(index);
        if (opt instanceof JSONObject) {
            opt = new UnmodifiableJSONObject((JSONObject) opt);
        } else if (opt instanceof JSONArray) {
            opt = new UnmodifiableJSONArray((JSONArray) opt);
        }
        return opt;
    }

    @Override
    public boolean optBoolean(int index, boolean fallback) {
        return mWrapped.optBoolean(index, fallback);
    }

    @Override
    public boolean optBoolean(int index) {
        return mWrapped.optBoolean(index);
    }

    @Override
    public double optDouble(int index, double fallback) {
        return mWrapped.optDouble(index, fallback);
    }

    @Override
    public double optDouble(int index) {
        return mWrapped.optDouble(index);
    }

    @Override
    public int optInt(int index, int fallback) {
        return mWrapped.optInt(index, fallback);
    }

    @Override
    public int optInt(int index) {
        return mWrapped.optInt(index);
    }

    @Override
    public JSONArray optJSONArray(int index) {
        return new UnmodifiableJSONArray(mWrapped.optJSONArray(index));
    }

    @Override
    public JSONObject optJSONObject(int index) {
        return new UnmodifiableJSONObject(mWrapped.optJSONObject(index));
    }

    @Override
    public long optLong(int index, long fallback) {
        return mWrapped.optLong(index, fallback);
    }

    @Override
    public long optLong(int index) {
        return mWrapped.optLong(index);
    }

    @Override
    public String optString(int index, String fallback) {
        return mWrapped.optString(index, fallback);
    }

    @Override
    public String optString(int index) {
        return mWrapped.optString(index);
    }

    @Override
    public JSONArray put(boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONArray put(double value) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONArray put(int index, boolean value) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONArray put(int index, double value) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONArray put(int index, int value) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONArray put(int index, long value) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONArray put(int index, Object value) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONArray put(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONArray put(long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONArray put(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject toJSONObject(JSONArray names) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return mWrapped.toString();
    }

    @Override
    public String toString(int indentSpaces) throws JSONException {
        return mWrapped.toString(indentSpaces);
    }

    private final JSONArray mWrapped;
}
