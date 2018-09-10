package org.gearvrf.widgetlib.widget.properties;

import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gearvrf.utility.Colors;
import org.joml.Vector3f;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.gearvrf.widgetlib.log.Log;

import static org.gearvrf.utility.TextFile.readTextFile;

abstract public class JSONHelpers {

    /**
     * An {@link UnmodifiableJSONObject} that is returned by several methods if a {@link JSONObject}
     * is not mapped to a specified key.  Helps to avoid a lot of {@code null}-checking logic.
     */
    public static final JSONObject EMPTY_OBJECT = new UnmodifiableJSONObject(new JSONObject());
    /**
     * An {@link UnmodifiableJSONArray} that is returned by several methods if a {@link JSONArray}
     * is not mapped to a specified key.  Helps to avoid a lot of {@code null}-checking logic.
     */
    public static final JSONArray EMPTY_ARRAY = new UnmodifiableJSONArray(new JSONArray());

    public static <P extends Enum<P>> Object get(final JSONObject json, P e) {
        try {
            return json.get(e.name());
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>> Object opt(final JSONObject json, P e) {
        return json.opt(e.name());
    }

    public static final <P extends Enum<P>, R> R get(final JSONObject json, P e, Class<R> r) {
        try {
            return r.cast(json.get(e.name()));
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static final <P extends Enum<P>, R> R opt(final JSONObject json, P e, Class<R> r) {
        Object o = opt(json, e);
        if (o != null) {
            return r.cast(o);
        }
        return null;
    }

    public static final <P extends Enum<P>> JSONObject put(JSONObject json, P e, Object value) {
        try {
            return json.put(e.name().toLowerCase(Locale.ENGLISH), value);
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>> JSONObject putDefault(final JSONObject json, P e, Object value) {
        if (json != null && !has(json, e)) {
            put(json, e, value);
        }
        return json;
    }

    public static <P extends Enum<P>> boolean getBoolean(final JSONObject json, P e) {
        try {
            return json.getBoolean(e.name());
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>> boolean optBoolean(final JSONObject json,
                                                         P e) {
        return json.optBoolean(e.name());
    }

    public static <P extends Enum<P>> boolean optBoolean(final JSONObject json,
                                                         P e, boolean fallback) {
        return json.optBoolean(e.name(), fallback);
    }

    public static <P extends Enum<P>> JSONObject put(JSONObject json, P e, boolean value) {
        try {
            return json.put(e.name(), value);
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>> JSONObject putDefault(final JSONObject json, P e, boolean value) {
        if (!has(json, e)) {
            safePut(json, e, value);
        }
        return json;
    }

    public static <P extends Enum<P>> double getDouble(final JSONObject json, P e) {
        try {
            return json.getDouble(e.name());
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>> double optDouble(final JSONObject json,
                                                       P e) {
        return json.optDouble(e.name());
    }

    public static <P extends Enum<P>> double optDouble(final JSONObject json,
                                                       P e, double fallback) {
        return json.optDouble(e.name(), fallback);
    }

    /**
     * Maps {@code e} to {@code value}, clobbering any existing mapping with the same name.
     *
     * @param json  {@link JSONObject} to put data to
     * @param e     {@link Enum} labeling the data to put
     * @param value A {@code double} value to put. If {@link Float#NaN NaN} or
     *              {@link Float#POSITIVE_INFINITY positive} or
     *              {@link Float#NEGATIVE_INFINITY negative} infinity is specified, no mapping will
     *              be put
     * @return The {@code json} parameter, for chained calls
     */
    public static <P extends Enum<P>> JSONObject put(JSONObject json, P e, double value) {

        try {
            if (!Double.isNaN(value) && !Double.isInfinite(value)) {
                json.put(e.name(), value);
            }
            return json;
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>> JSONObject putDefault(final JSONObject json, P e, double value) {
        if (!has(json, e)) {
            put(json, e, value);
        }
        return json;
    }

    public static <P extends Enum<P>> float getFloat(final JSONObject json, P e) {
        try {
            return (float) json.getDouble(e.name());
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>> float optFloat(final JSONObject json,
                                                     P e) {
        return (float) json.optDouble(e.name());
    }

    public static <P extends Enum<P>> float optFloat(final JSONObject json,
                                                     P e, double fallback) {
        return (float) json.optDouble(e.name(), fallback);
    }

    public static <P extends Enum<P>> int getInt(final JSONObject json, P e) {
        try {
            return json.getInt(e.name());
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    /**
     * Returns the value mapped to {@code e} if it exists and is an {@code int} or can be coerced to
     * an {@code int}. Returns 0 otherwise.
     *
     * @param json {@link JSONObject} to get data from
     * @param e    {@link Enum} labeling the data to get
     */
    public static <P extends Enum<P>> int optInt(final JSONObject json, P e) {
        return json.optInt(e.name());
    }

    /**
     * Returns the value mapped to {@code e} if it exists and is an {@code int} or can be coerced to
     * an {@code int}. Returns {@code fallback} otherwise.
     *
     * @param json     {@link JSONObject} to get data from
     * @param e        {@link Enum} labeling the data to get
     * @param fallback Value to return if there is no {@code int} value mapped to {@code e}
     */
    public static <P extends Enum<P>> int optInt(final JSONObject json, P e,
                                                 int fallback) {
        return json.optInt(e.name(), fallback);
    }

    public static <P extends Enum<P>> JSONObject put(JSONObject json, P e, int value) {
        try {
            return json.put(e.name(), value);
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>> JSONObject putDefault(final JSONObject json, P e, int value) {
        if (!has(json, e)) {
            safePut(json, e, value);
        }
        return json;
    }

    public static <P extends Enum<P>> long getLong(final JSONObject json, P e) {
        try {
            return json.getLong(e.name());
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>> long optLong(final JSONObject json, P e) {
        return json.optLong(e.name());
    }

    public static <P extends Enum<P>> long optLong(final JSONObject json, P e,
                                                   long fallback) {
        return json.optLong(e.name(), fallback);
    }

    public static <P extends Enum<P>> JSONObject put(JSONObject json, P e, long value) {
        try {
            return json.put(e.name(), value);
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>> JSONObject putDefault(final JSONObject json, P e, long value) {
        if (!has(json, e)) {
            safePut(json, e, value);
        }
        return json;
    }

    public static <P extends Enum<P>> String getString(final JSONObject json, P e) {
        try {
            return json.getString(e.name());
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    /**
     * Returns {@link String} value mapped to {@code e}, or the empty string if the mapping doesn't
     * exist.
     *
     * @param json
     * @param e
     * @return
     */
    public static <P extends Enum<P>> String optString(final JSONObject json,
                                                       P e) {
        return json.optString(e.name());
    }

    public static <P extends Enum<P>> String optString(final JSONObject json,
                                                       P e, String fallback) {
        return json.optString(e.name(), fallback);
    }

    public static <P extends Enum<P>> JSONObject put(JSONObject json, P e, String value) {
        try {
            return json.put(e.name(), value);
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>, R extends Enum<R>> R getEnum(final JSONObject json,
                                                                   P e, Class<R> r) {
        return getEnum(json, e, r, false);
    }

    @NonNull
    public static <P extends Enum<P>, R extends Enum<R>> R getEnum(JSONObject json, P e, Class<R> r,
                                                                   boolean uppercase) {
        String value;
        try {
            value = json.getString(e.name());
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
        if (uppercase) {
            value = value.toUpperCase();
        }
        return Enum.valueOf(r, value);
    }

    public static <P extends Enum<P>, R extends Enum<R>> R optEnum(
            final JSONObject json, P e, Class<R> r) {
        return optEnum(json, e, r, false);
    }

    @Nullable
    public static <P extends Enum<P>, R extends Enum<R>> R optEnum(
            JSONObject json, P e, Class<R> r, boolean uppercase) {
        String value = json.optString(e.name());
        if (value == null) {
            return null;
        }
        if (uppercase) {
            value = value.toUpperCase();
        }
        return Enum.valueOf(r, value);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Enum<P>, R extends Enum<R>> R optEnum(
            final JSONObject json, P e, R fallback) {
        return optEnum(json, e, fallback, false);
    }

    public static <P extends Enum<P>, R extends Enum<R>> R optEnum(
            JSONObject json, P e, R fallback, boolean uppercase) {
        String value = json.optString(e.name(), fallback.name());
        if (uppercase) {
            value = value.toUpperCase();
        }

        return (R) Enum.valueOf(fallback.getDeclaringClass(), value);
    }

    public static <P extends Enum<P>, V extends Enum<V>> JSONObject put(JSONObject json, P e,
                                                                        V value) {
        return put(json, e, value, false);
    }

    public static <P extends Enum<P>, V extends Enum<V>> JSONObject put(JSONObject json, P e,
                                                                        V value, boolean lowerCase) {
        String strVal = value.name();
        if (lowerCase) {
            strVal = strVal.toLowerCase();
        }
        try {
            return json.put(e.name(), strVal);
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>, V extends Enum<V>> JSONObject putDefault(
            final JSONObject json, P e, V value) {
        if (!has(json, e)) {
            safePut(json, e, value);
        }
        return json;
    }

    public static <P extends Enum<P>, V extends Enum<V>> JSONObject putDefault(
            final JSONObject json, P e, V value, boolean lowerCase) {
        if (!has(json, e, lowerCase)) {
            safePut(json, e, value, lowerCase);
        }
        return json;
    }

    public static <P extends Enum<P>> JSONObject getJSONObject(final JSONObject json, P e) {
        try {
            return json.getJSONObject(e.name());
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    /**
     * Returns the value mapped by enum if it exists and is a {@link JSONObject}. If the value does
     * not exist returns {@code null}.
     *
     * @param json {@link JSONObject} to get data from
     * @param e    {@link Enum} labeling the data to get
     * @return A {@code JSONObject} if the mapping exists; {@code null} otherwise
     */
    public static <P extends Enum<P>> JSONObject optJSONObject(final JSONObject json, P e) {
        return json.optJSONObject(e.name());
    }

    /**
     * Returns the value mapped by enum if it exists and is a {@link JSONObject}. If a value is not
     * mapped by that enum, returns {@code fallback}.
     *
     * @param json     {@link JSONObject} to get data from
     * @param e        {@link Enum} labeling the data to get
     * @param fallback Value to return if there is no mapped data
     * @return A {@code JSONObject} if the mapping exists; {@code defValue} otherwise
     */
    public static <P extends Enum<P>> JSONObject optJSONObject(final JSONObject json, P e,
                                                               JSONObject fallback) {
        JSONObject jsonObject = optJSONObject(json, e);
        if (jsonObject == null) {
            jsonObject = fallback;
        }
        return jsonObject;
    }

    /**
     * Returns the value mapped by enum if it exists and is a {@link JSONObject}. If the value does
     * not exist by that enum, and {@code emptyForNull} is {@code true}, returns
     * {@link #EMPTY_OBJECT}. Otherwise, returns {@code null}.
     *
     * @param json         {@link JSONObject} to get data from
     * @param e            {@link Enum} labeling the data to get
     * @param emptyForNull {@code True} to return {@code EMPTY_OBJECT} if there is no mapped data,
     *                     {@code false} to return {@code null} in that case
     * @return A {@code JSONObject} if the mapping exists; {@code EMPTY_OBJECT} if it doesn't and
     * {@code emptyForNull} is {@code true}; {@code null} otherwise
     */
    public static <P extends Enum<P>> JSONObject optJSONObject(final JSONObject json, P e,
                                                               boolean emptyForNull) {
        JSONObject jsonObject = optJSONObject(json, e);
        if (jsonObject == null && emptyForNull) {
            jsonObject = EMPTY_OBJECT;
        }
        return jsonObject;
    }

    public static <P extends Enum<P>> JSONArray getJSONArray(final JSONObject json, P e) {
        try {
            return json.getJSONArray(e.name());
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    public static <P extends Enum<P>> JSONArray optJSONArray(
            final JSONObject json, P e) {
        return json.optJSONArray(e.name());
    }


    /**
     * Returns the value mapped by enum if it exists and is a {@link JSONArray}. If the value does not
     * exist by that enum, and {@code emptyForNull} is {@code true}, returns {@link #EMPTY_ARRAY}.
     * Otherwise, returns {@code null}.
     *
     * @param json         {@code JSONObject} to get data from
     * @param e            {@link Enum} labeling the data to get
     * @param emptyForNull {@code True} to return {@code EMPTY_ARRAY} if there is no mapped data,
     *                     {@code false} to return {@code null} in that case
     * @return A {@code JSONObject} if the mapping exists; {@code EMPTY_ARRAY} if it doesn't and
     * {@code emptyForNull} is {@code true}; {@code null} otherwise
     */
    public static <P extends Enum<P>> JSONArray optJSONArray(final JSONObject json, P e,
                                                             boolean emptyForNull) {
        JSONArray jsonArray = optJSONArray(json, e);
        if (jsonArray == null && emptyForNull) {
            jsonArray = EMPTY_ARRAY;
        }
        return jsonArray;
    }

    public static <P extends Enum<P>> Point getPoint(final JSONObject json, P e) {
        JSONObject value = getJSONObject(json, e);
        return asPoint(value);
    }

    /**
     * Return the value mapped by enum if it exists and is a {@link JSONObject} by mapping "x" and
     * "y" members into a {@link Point}.
     *
     * @param json {@code JSONObject} to get data from
     * @param e    {@link Enum} labeling the data to get
     * @return An instance of {@code Point} or {@null} if there is no object mapping for {@code e}.
     */
    public static <P extends Enum<P>> Point optPoint(final JSONObject json, P e) {
        return optPoint(json, e, null);
    }

    /**
     * Return the value mapped by enum if it exists and is a {@link JSONObject} by mapping "x" and
     * "y" members into a {@link Point}. The values in {@code fallback} are used if either field is
     * missing; if {@code fallback} is {@code null}, 0 (zero) is used.
     *
     * @param json     {@code JSONObject} to get data from
     * @param e        {@link Enum} labeling the data to get
     * @param fallback Default value to return if there is no mapping
     * @return An instance of {@code Point} or {@code fallback} if there is no object mapping for
     * {@code e}.
     */
    public static <P extends Enum<P>> Point optPoint(final JSONObject json, P e, Point fallback) {
        JSONObject value = optJSONObject(json, e);
        Log.d(Log.SUBSYSTEM.JSON, TAG, "optPoint(): raw: %s", value);
        Point p = asPoint(value, fallback);

        return p;
    }

    public static Point asPoint(JSONObject json) {
        return asPoint(json, new Point());
    }

    public static Point asPoint(JSONObject json, Point defValue) {
        Point p = defValue;
        if (json != null && isPoint(json)) {
            int x = json.optInt("x", defValue != null ? defValue.x : 0);
            int y = json.optInt("y", defValue != null ? defValue.y : 0);
            Log.d(Log.SUBSYSTEM.JSON, TAG, "optPoint(): x: %d, y: %d", x, y);
            p = new Point(x, y);
        }
        return p;
    }

    /**
     * Return the value mapped by enum if it exists and is a {@link JSONObject} by mapping "x" and
     * "y" members into a {@link Point}. If the value does not exist by that enum, and
     * {@code emptyForNull} is {@code true}, returns a default constructed {@code Point}. Otherwise,
     * returns {@code null}.
     *
     * @param json         {@code JSONObject} to get data from
     * @param e            {@link Enum} labeling the data to get
     * @param emptyForNull {@code True} to return a default constructed {@code Point} if there is no
     *                     mapped data, {@code false} to return {@code null} in that case
     * @return A {@code Point} if the mapping exists or {@code emptyForNull} is {@code true};
     * {@code null} otherwise
     */
    public static <P extends Enum<P>> Point optPoint(final JSONObject json, P e, boolean emptyForNull) {
        Point p = optPoint(json, e);
        if (p == null && emptyForNull) {
            p = new Point();
        }
        return p;
    }

    public static <P extends Enum<P>> PointF getPointF(final JSONObject json, P e) {
        JSONObject value = getJSONObject(json, e);
        return asPointF(value);
    }

    /**
     * Return the value mapped by enum if it exists and is a {@link JSONObject} by mapping "x" and
     * "y" members into a {@link PointF}.
     *
     * @param json {@code JSONObject} to get data from
     * @param e    {@link Enum} labeling the data to get
     * @return An instance of {@code PointF} or {@code null} if there is no object mapping for
     * {@code e}.
     */
    public static <P extends Enum<P>> PointF optPointF(final JSONObject json, P e) {
        return optPointF(json, e, null);
    }

    /**
     * Return the value mapped by enum if it exists and is a {@link JSONObject} by mapping "x" and
     * "y" members into a {@link PointF}.  The values in {@code fallback} are used if either field
     * is missing; if {@code fallback} is {@code null}, {@link Float#NaN} is used.
     *
     * @param json     {@code JSONObject} to get data from
     * @param e        {@link Enum} labeling the data to get
     * @param fallback Default value to return if there is no mapping
     * @return An instance of {@code PointF} or {@code fallback} if there is no object mapping for
     * {@code e}.
     */
    public static <P extends Enum<P>> PointF optPointF(final JSONObject json, P e, PointF fallback) {
        JSONObject value = optJSONObject(json, e);
        PointF p = asPointF(value, fallback);

        return p;
    }

    public static PointF asPointF(JSONObject json) {
        return asPointF(json, new PointF());
    }

    public static PointF asPointF(JSONObject json, PointF fallback) {
        PointF p = fallback;
        if (json != null && isPoint(json)) {
            float x = (float) json.optDouble("x", fallback != null ? fallback.x : Float.NaN);
            float y = (float) json.optDouble("y", fallback != null ? fallback.y : Float.NaN);

            p = new PointF(x, y);
        }
        return p;
    }

    /**
     * Return the value mapped by enum if it exists and is a {@link JSONObject} by mapping "x" and
     * "y" members into a {@link Point}. If the value does not exist by that enum, and
     * {@code emptyForNull} is {@code true}, returns a default constructed {@code Point}. Otherwise,
     * returns {@code null}.
     *
     * @param json         {@code JSONObject} to get data from
     * @param e            {@link Enum} labeling the data to get
     * @param emptyForNull {@code True} to return a default constructed {@code Point} if there is no
     *                     mapped data, {@code false} to return {@code null} in that case
     * @return A {@code Point} if the mapping exists or {@code emptyForNull} is {@code true};
     * {@code null} otherwise
     */
    public static <P extends Enum<P>> PointF optPointF(final JSONObject json, P e, boolean emptyForNull) {
        PointF p = optPointF(json, e);
        if (p == null && emptyForNull) {
            p = new PointF();
        }
        return p;
    }

    public static <P extends Enum<P>> JSONObject put(JSONObject json, P e, PointF defPoint) {
        JSONObject defJson = new JSONObject();
        try {
            defJson.put("x", defPoint.x);
            defJson.put("y", defPoint.y);
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
        safePut(json, e, defJson);
        return json;
    }

    /**
     * Checks the supplied {@link JSONObject} for a value mapped by an enum; if it does not exist,
     * or is not an object value, the specified default value will be mapped to the enum.  If the
     * value does exist, but either 'x' or 'y' field is missing, the missing field(s) will be set
     * using {@code defPoint}.  However, if the value exists and is a {@link Number}, it is
     * interpreted as implicitly defining both the 'x' and 'y' fields, and the value is left as-is.
     *
     * @param json     {@code JSONObject} to check for value
     * @param e        {@link Enum} mapping the value to check
     * @param defPoint The value to use as default
     * @return The {@code JSONObject} passed as '{@code json}', for call-chaining.
     */
    public static <P extends Enum<P>> JSONObject putDefault(JSONObject json, P e, PointF defPoint) {
        return putDefault(json, e, defPoint, true);
    }

    /**
     * Checks the supplied {@link JSONObject} for a value mapped by an enum; if it does not exist,
     * or is not an object value, the specified default value will be mapped to the enum.  If the
     * value does exist, but either 'x' or 'y' field is missing, the missing field(s) will be set
     * using {@code defPoint}.  However, if {@code allowImplicitPoint} is {@code true}, and the
     * value exists and is a {@link Number}, then the value is left as-is.
     *
     * @param json               {@code JSONObject} to check for value
     * @param e                  {@link Enum} mapping the value to check
     * @param defPoint           The value to use as default
     * @param allowImplicitPoint Whether to interpret a {@code Number} value as implicitly defining
     *                           both fields of a {@code PointF}.
     * @return The {@code JSONObject} passed as '{@code json}', for call-chaining.
     */
    public static <P extends Enum<P>> JSONObject putDefault(JSONObject json, P e, PointF defPoint,
                                                            boolean allowImplicitPoint) {
        if (json != null) {
            JSONObject pointJson = optJSONObject(json, e);
            if (pointJson != null) {
                // Check both members; default if missing
                try {
                    if (!hasNumber(pointJson, "x", true)) {
                        pointJson.put("x", defPoint.x);
                    }
                    if (!hasNumber(pointJson, "y", true)) {
                        pointJson.put("y", defPoint.y);
                    }
                } catch (JSONException e1) {
                    throw new RuntimeException(e1);
                }
            } else {
                // Some properties allow a point to be implicitly specified with a single number,
                // which is then used for both 'x' and 'y' values.
                if (!(allowImplicitPoint && hasNumber(json, e))) {
                    put(json, e, defPoint);
                }
            }
        }
        return json;
    }

    public static <P extends Enum<P>> PointF removePointF(JSONObject json, P e) {
        final Object remove = json.remove(e.name());
        if (remove instanceof JSONObject && isPoint((JSONObject) remove)) {
            return asPointF((JSONObject) remove);
        } else if (remove instanceof PointF) {
            return (PointF) remove;
        } else if (remove instanceof Point) {
            return new PointF(((Point) remove).x, ((Point) remove).y);
        }
        return null;
    }

    /**
     * Checks whether the value mapped by enum exists, is a {@link JSONObject}, has at least one
     * field named either "x" or "y", and that if either field is present, it is a number.  If at
     * least one of the fields is present, it can be assumed that the missing field is defaulted to
     * zero.  If both are missing, it's ambiguous, at best, whether the object in question can
     * reasonably be treated as a {@link Point}.  This specification is permissive -- there can be
     * other, non-{@code Point}, fields present.  In that case, it can be considered that the object
     * <em>extends</em> {@code Point}.
     *
     * @param json {@code JSONObject} to check
     * @param e    {@link Enum} labeling the data to check
     * @return {@code True} if the mapping exists and meets the conditions above; {@code false}
     * otherwise.
     */
    public static <P extends Enum<P>> boolean hasPoint(final JSONObject json, P e) {
        Object o = opt(json, e);
        return o != null && o != JSONObject.NULL && o instanceof JSONObject &&
                isPoint((JSONObject) o);
    }

    public static boolean isPoint(JSONObject jo) {
        Object x = jo.opt("x");
        Object y = jo.opt("y");
        Log.d(Log.SUBSYSTEM.JSON, TAG, "isPoint(): x: %s, y: %s", x, y);
        if (x == null && y == null) return false;
        if (x != null && !(x instanceof Number)) return false;
        if (y != null && !(y instanceof Number)) return false;

        return true;
    }

    public static <P extends Enum<P>> Vector3f getVector3f(JSONObject json, P e) {
        JSONObject value = getJSONObject(json, e);

        // If there's no value, getJSONValue() will throw a RuntimeException
        return asVector3f(value, null);
    }

    public static <P extends Enum<P>> Vector3f optVector3f(JSONObject json, P e) {
        return optVector3f(json, e, null);
    }

    public static <P extends Enum<P>> Vector3f optVector3f(JSONObject json, P e, Vector3f fallback) {
        JSONObject value = optJSONObject(json, e);

        return asVector3f(value, fallback);
    }

    public static <P extends Enum<P>> JSONObject putDefault(JSONObject json, P e, Vector3f defVector) {
        if (json != null && !has(json, e)) {
            put(json, e, defVector);
        }
        return json;
    }

    public static <P extends Enum<P>> void put(JSONObject json, P e, Vector3f vector) {
        JSONObject defJson = new JSONObject();
        try {
            defJson.put("x", vector.x);
            defJson.put("y", vector.y);
            defJson.put("z", vector.z);
        } catch (JSONException e1) {
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
        safePut(json, e, defJson);
    }

    public static Vector3f asVector3f(JSONObject value, Vector3f fallback) {
        Vector3f v = fallback;
        if (value != null && isVector3f(value)) {
            float x = (float) value.optDouble("x", fallback != null ? fallback.x : Float.NaN);
            float y = (float) value.optDouble("y", fallback != null ? fallback.y : Float.NaN);
            float z = (float) value.optDouble("z", fallback != null ? fallback.z : Float.NaN);

            v = new Vector3f(x, y, z);
        }
        return v;
    }

    public static <P extends Enum<P>> Vector3f optVector3f(final JSONObject json, P e,
                                                           boolean emptyForNull) {
        Vector3f v = optVector3f(json, e);
        if (v == null && emptyForNull) {
            v = new Vector3f();
        }
        return v;
    }

    public static <P extends Enum<P>> boolean hasVector3f(final JSONObject json, P e) {
        String name = e.name();
        return hasVector3f(json, name);
    }

    public static boolean hasVector3f(JSONObject json, String name) {
        Object o = json.opt(name);
        return o != null && o != JSONObject.NULL && o instanceof JSONObject &&
                isVector3f((JSONObject) o);
    }

    public static boolean isVector3f(JSONObject jo) {
        Object x = jo.opt("x");
        Object y = jo.opt("y");
        Object z = jo.opt("z");
        Log.d(Log.SUBSYSTEM.JSON, TAG, "isVector3f(): x: %s, y: %s, z: %s", x, y, z);
        if (x == null && y == null && z == null) return false;
        if (x != null && !(x instanceof Number)) return false;
        if (y != null && !(y instanceof Number)) return false;
        if (z != null && !(z instanceof Number)) return false;

        return true;
    }

    public static <P extends Enum<P>> boolean has(final JSONObject json, P e) {
        return has(json, e, false);
    }

    private static <P extends Enum<P>> boolean has(JSONObject json, P e, boolean lowerCase) {
        String name = e.name();
        if (lowerCase) {
            name = name.toLowerCase();
        }
        return json.has(name);
    }

    /**
     * Check if the value at {@code key} is a {@link Boolean}.
     *
     * @param json {@link JSONObject} to inspect
     * @param key  Item in object to check
     * @return {@code True} if the item exists and is a {@code Boolean};
     * {@code false} otherwise
     */
    public static boolean hasBoolean(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, Boolean.class);
    }

    public static <P extends Enum<P>> boolean hasBoolean(final JSONObject json, P e) {
        return hasBoolean(json, e.name());
    }

    /**
     * Check if the value at {@code key} is a {@link Boolean} or can,
     * optionally, be coerced into a {@code Boolean}.
     *
     * @param json   {@link JSONObject} to inspect
     * @param key    Item in object to check
     * @param coerce If {@code true}, check if the value can be coerced to
     *               {@code Boolean}
     * @return {@code True} if the item exists and is a {@code Boolean};
     * {@code false} otherwise
     */
    public static boolean hasBoolean(final JSONObject json, final String key,
                                     final boolean coerce) {
        if (!coerce) {
            return hasBoolean(json, key);
        }

        // This could be trivially implemented as
        // `return JSON.toBoolean(json.opt(key)) != null`
        // but JSON is not public
        Object o = json.opt(key);
        if (o == null || o == JSONObject.NULL) {
            return false;
        }
        if (o instanceof Boolean) {
            return true;
        }
        if (o instanceof Integer || o instanceof Long) {
            final Long num = (Long) o;
            return num == 0 || num == 1;
        }
        if (o instanceof String) {
            final String s = (String) o;
            return s.compareToIgnoreCase("false") == 0
                    || s.compareToIgnoreCase("true") == 0;
        }
        return false;
    }

    /**
     * Check if the value at {@code key} is a {@link Double}.
     *
     * @param json {@link JSONObject} to inspect
     * @param key  Item in object to check
     * @return {@code True} if the item exists and is a {@code Double};
     * {@code false} otherwise
     */
    public static boolean hasDouble(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, Number.class);
    }

    public static <P extends Enum<P>> boolean hasDouble(final JSONObject json, P e) {
        return hasDouble(json, e.name());
    }

    /**
     * Alias for {@link #hasDouble(JSONObject, String)}.
     */
    public static boolean hasFloat(final JSONObject json, final String key) {
        return hasDouble(json, key);
    }

    public static <P extends Enum<P>> boolean hasFloat(final JSONObject json, P e) {
        return hasDouble(json, e);
    }

    /**
     * Check if the value at {@code key} is an {@link Integer} or can be coerced to an {@code int}.
     *
     * @param json {@link JSONObject} to inspect
     * @param key  Item in object to check
     * @return {@code True} if the item exists and is an {@code Integer};
     * {@code false} otherwise
     */
    public static boolean hasInt(final JSONObject json, final String key) {
        return hasInt(json, key, true);
    }

    public static boolean hasInt(final JSONObject json, final String key, boolean allowCoercion) {
        return (allowCoercion && hasInstanceOf(json, key, Number.class))
                || hasInstanceOf(json, key, Integer.class);
    }

    public static <P extends Enum<P>> boolean hasInt(final JSONObject json, P e) {
        return hasInt(json, e.name(), true);
    }

    public static <P extends Enum<P>> boolean hasInt(final JSONObject json, P e,
                                                     boolean allowCoercion) {
        return hasInt(json, e.name(), allowCoercion);
    }

    /**
     * Check if the value at {@code key} is a {@link Long}.
     *
     * @param json {@code JSONObject} to inspect
     * @param key  Item in object to check
     * @return {@code True} if the item exists and is a {@code Long};
     * {@code false} otherwise
     */
    public static boolean hasLong(final JSONObject json, final String key) {
        return hasLong(json, key, true);
    }

    public static boolean hasLong(final JSONObject json, final String key, boolean allowCoercion) {
        return (allowCoercion && hasInstanceOf(json, key, Number.class))
                || hasInstanceOf(json, key, Long.class);
    }

    public static <P extends Enum<P>> boolean hasLong(final JSONObject json, P e) {
        return hasLong(json, e.name(), true);
    }

    public static <P extends Enum<P>> boolean hasLong(final JSONObject json, P e,
                                                      boolean allowCoercion) {
        return hasLong(json, e.name(), allowCoercion);
    }

    /**
     * Check if the value at {@code key} is a {@link Number}.
     *
     * @param json {@link JSONObject} to inspect
     * @param key  Item in object to check
     * @return {@code True} if the item exists and is a {@code Number};
     * {@code false} otherwise
     */
    public static boolean hasNumber(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, Number.class);
    }

    public static <P extends Enum<P>> boolean hasNumber(final JSONObject json, P e) {
        return hasNumber(json, e.name());
    }

    /**
     * Check if the value at {@code key} is a {@link Number} or can, optionally,
     * be coerced into a {@code Number}.
     *
     * @param json   {@code JSONObject} to inspect
     * @param key    Item in object to check
     * @param coerce If {@code true}, check if the value can be coerced to a
     *               {@code Number}
     * @return {@code True} if the item exists and is a {@code Number};
     * {@code false} otherwise
     */
    public static boolean hasNumber(final JSONObject json, final String key,
                                    final boolean coerce) {
        if (!coerce) {
            return hasNumber(json, key);
        }
        final Object o = json.opt(key);
        if (o == null || o == JSONObject.NULL) {
            return false;
        }
        if (o instanceof Number) {
            return true;
        }
        if (o instanceof Boolean) {
            return true;
        }
        if (o instanceof String) {
            final String s = (String) o;
            try {
                Double.valueOf(s);
                return true;
            } catch (NumberFormatException e) {
                Log.e(Log.SUBSYSTEM.JSON, TAG, e,
                        "hasNumber(): failed to coerce value at '%s' (%s)", key, o);
            }
        }
        return false;
    }

    /**
     * Check if the specified {@link JSONObject} has a {@code String} at
     * {@code key}.
     *
     * @param json {@code JSONObject} to inspect
     * @param key  Item in object to check
     * @return {@code True} if the item exists and is a {@code String};
     * {@code false} otherwise
     */
    public static boolean hasString(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, String.class);
    }

    public static <P extends Enum<P>> boolean hasString(final JSONObject json, P e) {
        return hasString(json, e.name());
    }

    /**
     * Check if the specified {@link JSONObject} has an {@link Enum} at {@code key}.
     * <p>
     * If the item is not naturally an {@code Enum}, checks to see if it is a {@link String} with
     * one of the {@code Enum's} {@linkplain Enum#valueOf(Class, String) values}.
     *
     * @param json {@code JSONObject} to inspect
     * @param key  Item in object to check
     * @return {@code True} if the item exists and is an {@code Enum};
     * {@code false} otherwise
     */
    public static <P extends Enum<P>> boolean hasEnum(final JSONObject json,
                                                      final String key, final Class<? extends Enum> enumType) {
        if (hasInstanceOf(json, key, enumType)) {
            return true;
        }

        final Object o = json.opt(key);
        if (o == null || o == JSONObject.NULL) {
            return false;
        }

        if (o instanceof String) {
            final String s = (String) o;
            try {
                Enum.valueOf(enumType, s);
                return true;
            } catch (IllegalArgumentException e) {
                Log.e(Log.SUBSYSTEM.JSON, TAG, e,
                        "hasEnum(): failed to coerce value at '%s' to %s (%s)",
                        key, enumType.getSimpleName(), o);
            }
        }
        return false;
    }

    public static <P extends Enum<P>> boolean hasEnum(final JSONObject json, P e,
                                                      Class<? extends Enum> enumType) {
        return hasEnum(json, e.name(), enumType);
    }

    /**
     * Check if the specified {@link JSONObject} has a {@code JSONArray} at
     * {@code key}.
     *
     * @param json {@code JSONObject} to inspect
     * @param key  Item in object to check
     * @return {@code True} if the item exists and is a {@code JSONArray};
     * {@code false} otherwise
     */
    public static boolean hasJSONArray(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, JSONArray.class);
    }

    public static <P extends Enum<P>> boolean hasJSONArray(final JSONObject json, P e) {
        return hasJSONArray(json, e.name());
    }

    /**
     * Check if the specified {@link JSONObject} has a {@code JSONObject} at
     * {@code key}.
     *
     * @param json {@code JSONObject} to inspect
     * @param key  Item in object to check
     * @return {@code True} if the item exists and is a {@code JSONObject};
     * {@code false} otherwise
     */
    public static boolean hasJSONObject(final JSONObject json, final String key) {
        return hasInstanceOf(json, key, JSONObject.class);
    }

    public static <P extends Enum<P>> boolean hasJSONObject(final JSONObject json, P e) {
        return hasJSONObject(json, e.name());
    }

    /**
     * Check if the {@link JSONObject} has an item at {@code key} that is an
     * instance of {@code type}.
     *
     * @param json {@code JSONObject} to inspect
     * @param key  Item in object to check
     * @param type Type to check the item against
     * @return {@code True} if the item exists and is of the specified
     * {@code type}; {@code false} otherwise
     */
    public static boolean hasInstanceOf(final JSONObject json, final String key,
                                        Class<?> type) {
        Object o = json.opt(key);
        return isInstanceOf(o, type);
    }

    public static <P extends Enum<P>> boolean hasInstanceOf(final JSONObject json, P e, Class<?> type) {
        return hasInstanceOf(json, e.name(), type);
    }

    public static boolean isInstanceOf(Object o, Class<?> type) {
        return o != null && o != JSONObject.NULL && type.isInstance(o);
    }

    /**
     * Load a JSON file from the application's "asset" directory.
     *
     * @param context Valid {@link Context}
     * @param asset   Name of the JSON file
     * @return New instance of {@link JSONObject}
     */
    public static JSONObject loadJSONAsset(Context context, final String asset) {
        if (asset == null) {
            return new JSONObject();
        }
        return getJsonObject(org.gearvrf.widgetlib.main.Utility.readTextFile(context, asset));
    }

    /**
     * Load a JSON file from one of the public directories defined by {@link Environment}.
     *
     * @param publicDirectory One of the {@code DIRECTORY_*} constants defined by {@code Environment}.
     * @param file            Relative path to file in the public directory.
     * @return New instance of {@link JSONObject}
     */
    public static JSONObject loadPublicJSONFile(final String publicDirectory, final String file) {
        final File dir = Environment.getExternalStoragePublicDirectory(publicDirectory);
        return loadJSONFile(dir, file);
    }

    public static JSONObject loadJSONFile(Context context, final String file) {
        return loadJSONFile(context.getFilesDir(), file);
    }

    public static JSONObject loadJSONFile(Context context, final String directory, final String file) {
        Log.d(Log.SUBSYSTEM.JSON, TAG, "loadJSONFile(): Context.getDir(): %s",
                context.getDir(Environment.DIRECTORY_DOCUMENTS, Context.MODE_PRIVATE));
        File dir = new File(context.getFilesDir(), directory);
        return loadJSONFile(dir, file);
    }

    public static JSONObject loadJSONDocument(Context context, final String file) {
        return loadJSONFile(context, Environment.DIRECTORY_DOCUMENTS, file);
    }

    @NonNull
    private static JSONObject loadJSONFile(File dir, String file) {
        String rawJson = null;
        if (dir.exists()) {
            final File f = new File(dir, file);
            if (f.exists()) {
                rawJson = readTextFile(f);
                Log.d(Log.SUBSYSTEM.JSON, TAG, "loadJSONFile(): %s", f.getPath());
            } else {
                Log.w(TAG, "loadJSONFile(): file %s doesn't exists", f.getPath());
            }
        } else {
            Log.w(Log.SUBSYSTEM.JSON,
                    TAG, "loadJSONFile(): directory %s doesn't exists", dir.getPath());
        }

        return getJsonObject(rawJson);
    }

    /**
     * Load a JSON file from {@link Environment#DIRECTORY_DOCUMENTS}.
     *
     * @param file Relative path to file in "Documents" directory.
     * @return New instance of {@link JSONObject}
     */
    public static JSONObject loadPublicJSONDocument(final String file) {
        return loadPublicJSONFile(Environment.DIRECTORY_DOCUMENTS, file);
    }

    /**
     * Load a JSON file from a private application directory as defined by {@link Environment}.
     *
     * @param directory One of the {@code DIRECTORY_*} constants defined by {@code Environment}.
     * @param file      Relative path to file in the public directory.
     * @return New instance of {@link JSONObject}
     */
    public static JSONObject loadExternalJSONFile(Context context, final String directory,
                                                  final String file) {
        final File dir = context.getExternalFilesDir(directory);
        return loadJSONFile(dir, file);
    }

    /**
     * Load a JSON file from the application's private {@link Environment#DIRECTORY_DOCUMENTS}.
     *
     * @param file Relative path to file in "Documents" directory.
     * @return New instance of {@link JSONObject}
     */
    public static JSONObject loadExternalJSONDocument(Context context, final String file) {
        return loadExternalJSONFile(context, Environment.DIRECTORY_DOCUMENTS, file);
    }

    /**
     * @return The full path to the application's private {@link Environment#DIRECTORY_DOCUMENTS} dir.
     */
    public static File getExternalJSONDocumentDirectory(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
    }

    @NonNull
    private static JSONObject getJsonObject(String rawJson) {
        Log.v(Log.SUBSYSTEM.JSON, TAG, "getJsonObject(): raw JSON: %s", rawJson);
        if (rawJson == null) {
            return new JSONObject();
        }
        try {
            return new JSONObject(rawJson);
        } catch (JSONException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Create a deep copy of {@code src} in a new {@link JSONArray}.
     * <p>
     * Equivalent to calling {@link #copy(JSONArray, boolean) copy(src, true)}.
     *
     * @param src {@code JSONArray} to copy
     * @return A new {@code JSONArray} copied from {@code src}
     */
    public static JSONArray copy(final JSONArray src) {
        final JSONArray dest = new JSONArray();
        final int len = src.length();
        for (int i = 0; i < len; ++i) {
            dest.put(src.opt(i));
        }
        return dest;
    }

    /**
     * Copies {@code src} into a new {@link JSONArray}.
     *
     * @param src  {@code JSONArray} to copy
     * @param deep {@code True} to perform a deep copy, {@code false} to perform
     *             a shallow copy
     * @return A new {@code JSONArray} copied from {@code src}
     */
    public static JSONArray copy(final JSONArray src, final boolean deep) {
        final JSONArray dest = new JSONArray();
        final int len = src.length();
        for (int i = 0; i < len; ++i) {
            final Object value = src.opt(i);
            if (deep) {
                if (value instanceof JSONObject) {
                    dest.put(copy((JSONObject) value));
                } else if (value instanceof JSONArray) {
                    dest.put(copy((JSONArray) value));
                } else {
                    dest.put(value);
                }
            } else {
                dest.put(value);
            }
        }
        return dest;
    }

    /**
     * Create a deep copy of {@code src} in a new {@link JSONObject}.
     * <p>
     * Equivalent to calling {@link #copy(JSONObject, boolean) copy(src, true)}.
     *
     * @param src {@code JSONObject} to copy
     * @return A new {@code JSONObject} copied from {@code src}
     */
    public static JSONObject copy(final JSONObject src) {
        return copy(src, true);
    }

    /**
     * Copies {@code src} into a new {@link JSONObject}.
     *
     * @param src  {@code JSONObject} to copy
     * @param deep {@code True} to perform a deep copy, {@code false} to perform
     *             a shallow copy
     * @return A new {@code JSONObject} copied from {@code src}
     */
    public static JSONObject copy(final JSONObject src, final boolean deep) {
        final JSONObject dest = new JSONObject();
        Iterator<String> keys = src.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            final Object value = src.opt(key);
            if (deep) {
                if (value instanceof JSONObject) {
                    safePut(dest, key, copy((JSONObject) value, deep));
                } else if (value instanceof JSONArray) {
                    safePut(dest, key, copy((JSONArray) value, deep));
                } else {
                    safePut(dest, key, value);
                }
            } else {
                safePut(dest, key, value);
            }
        }
        return dest;
    }

    /**
     * Merges values from {@code src} into {@code dest}. {@link JSONObject} and
     * {@link JSONArray} values are {@linkplain #copy(JSONObject) deep copied}.
     * Additional values in {@code src} are appended to {@code dest}.
     * <p>
     * Values at matching indices are overwritten in {@code dest}; if
     * {@code src} is longer than {@dest}, this turns {@dest} into a copy of
     * {@src}.
     *
     * @param src  {@code JSONArray} to merge from
     * @param dest {@code JSONArray} to merge to
     * @return The modified {@code dest} array.
     */
    public static JSONArray merge(final JSONArray src, final JSONArray dest) {
        return merge(src, dest, true);
    }

    /**
     * Merges values from {@code src} into {@code dest}. {@link JSONObject} and
     * {@link JSONArray} values are {@linkplain #copy(JSONObject) deep copied}.
     * Null values at matching indices are overwritten in {@code dest}.
     * Additional values in {@code src} are appended to {@code dest}.
     * <p>
     * Optionally, non-null values at matching indices may be overwritten in
     * {@code dest}; if enabled and {@code src} is longer than {@dest}, this
     * turns {@dest} into a copy of {@src}.
     *
     * @param src       {@code JSONArray} to merge from
     * @param dest      {@code JSONArray} to merge to
     * @param overwrite If {@code true}, overwrites non-null values in {@code dest}
     * @return The modified {@code dest} array.
     */
    public static JSONArray merge(JSONArray src, JSONArray dest,
                                  boolean overwrite) {
        return merge(src, dest, null, overwrite);
    }

    public static JSONArray merge(JSONArray src, JSONArray dest, String name, boolean overwrite) {
        return merge(src, dest, name, overwrite, true);
    }

    /**
     * Merges values from {@code src} into {@code dest}. Null values at matching
     * indices are overwritten in {@code dest}. Additional values in {@code src}
     * are appended to {@code dest}.
     * <p>
     * Optionally, non-null values at matching indices may be overwritten in
     * {@code dest}; if enabled and {@code src} is longer than {@dest}, this
     * turns {@dest} into a copy of {@src}.
     * <p>
     * Optionally, {@link JSONObject} and {@link JSONArray} values are
     * {@linkplain #copy(JSONObject) deep copied}.
     *
     * @param src       {@code JSONArray} to merge from
     * @param dest      {@code JSONArray} to merge to
     * @param overwrite If {@code true}, overwrites non-null values in {@code dest}
     * @param deep      If {@code true}, makes deep copies of any {@code JSONObject}
     *                  and {@code JSONArray} values
     * @return The modified {@code dest} array.
     */
    public static JSONArray merge(JSONArray src, JSONArray dest,
                                  final boolean overwrite, boolean deep) {
        return merge(src, dest, null, overwrite, deep);
    }

    public static JSONArray merge(JSONArray src, JSONArray dest, String name,
                                  final boolean overwrite, boolean deep) {
        Log.d(Log.SUBSYSTEM.JSON, TAG, "merge(%s), array: src: %s, dest: %s", name, src, dest);
        final int srcLen = src.length();
        final int destLen = dest.length();
        int i = 0;
        try {
            for (; i < srcLen && i < destLen; ++i) {
                final Object destVal = dest.get(i);
                Object value = src.get(i);
                if (destVal instanceof JSONObject && value instanceof JSONObject) {
                    Log.d(Log.SUBSYSTEM.JSON, TAG, "merge(%s), array: merging objects at %d", name, i);
                    merge((JSONObject) value, (JSONObject) destVal, name, overwrite);
                } else if (destVal instanceof JSONArray && value instanceof JSONArray) {
                    Log.d(Log.SUBSYSTEM.JSON, TAG, "merge(%s), array: merging arrays at %d", name, i);
                    merge((JSONArray) value, (JSONArray) destVal, name, overwrite, deep);
                } else if (destVal == null || overwrite) {
                    if (deep) {
                        if (value instanceof JSONObject) {
                            value = copy((JSONObject) value);
                        } else if (value instanceof JSONArray) {
                            value = copy((JSONArray) value);
                        }
                    }
                    dest.put(i, value);
                } else {
                    Log.w(Log.SUBSYSTEM.JSON, TAG, "merge(%s), array: mismatched value types at %d, can't merge; src %s, dest %s",
                            name, i, value.getClass().getSimpleName(), destVal.getClass().getSimpleName());
                }
            }
            for (; i < srcLen; ++i) {
                dest.put(src.get(i));
            }
        } catch (JSONException e) {
            Log.e(Log.SUBSYSTEM.JSON, TAG, e, "merge(): This shouldn't be able to happen! (at %d)",
                    i);
        }
        Log.d(Log.SUBSYSTEM.JSON, TAG, "merge(%s), array: result: %s", name, dest);
        return dest;
    }

    /**
     * Merges values from {@code src} into a copy of {@code dest}.
     * See {@link #merge(JSONArray, JSONArray)}.
     *
     * @param src  {@code JSONArray} to merge from
     * @param dest {@code JSONArray} to merge to
     * @return The modified {@code dest} array.
     */
    public static JSONArray merge(final JSONArray src,
                                  final UnmodifiableJSONArray dest) {
        return merge(src, copy(dest));
    }

    /**
     * Merges values from {@code src} into a copy of {@code dest}.
     * See {@link #merge(JSONArray, JSONArray, boolean)}.
     *
     * @param src       {@code JSONArray} to merge from
     * @param dest      {@code JSONArray} to merge to
     * @param overwrite If {@code true}, overwrites non-null values in {@code dest}
     * @return The modified {@code dest} array.
     */
    public static JSONArray merge(JSONArray src, UnmodifiableJSONArray dest,
                                  boolean overwrite) {
        return merge(src, copy(dest), overwrite);
    }

    /**
     * Merges values from {@code src} into a copy of {@code dest}.
     * See {@link #merge(JSONArray, JSONArray, boolean, boolean)}.
     *
     * @param src       {@code JSONArray} to merge from
     * @param dest      {@code JSONArray} to merge to
     * @param overwrite If {@code true}, overwrites non-null values in {@code dest}
     * @param deep      If {@code true}, makes deep copies of any {@code JSONObject}
     *                  and {@code JSONArray} values
     * @return The modified {@code dest} array.
     */
    public static JSONArray merge(JSONArray src, UnmodifiableJSONArray dest,
                                  boolean overwrite, boolean deep) {
        return merge(src, copy(dest), overwrite, deep);
    }

    /**
     * Recursively merges values from {@code src} into {@code dest}, overwriting
     * values in {@code dest} when matching keys are present in {@code src}.
     * Deep copies unmerged {@link JSONObject JSONObjects} and {@link JSONArray
     * JSONArrays}.
     * <p>
     * Equivalent to {@link #merge(JSONObject, JSONObject, boolean) merge(src,
     * dest, true)}.
     *
     * @param src  {@code JSONObject} to merge from
     * @param dest {@code JSONObject} to merge to
     * @return The modified {@code dest} object
     */
    public static JSONObject merge(JSONObject src, JSONObject dest) {
        return merge(src, dest, null);
    }

    public static JSONObject merge(JSONObject src, JSONObject dest, String name) {
        return merge(src, dest, name, true);
    }

    /**
     * Recursively merges values from {@code src} into {@code dest}, optionally
     * overwriting values of matching keys.
     * <p>
     * If {@code overwrite} is {@code true}, any matching keys in {@code dest}
     * will have their values overwritten by values from {@code src}. If the
     * values of the matching keys are both {@link JSONObject JSONObjects} or
     * {@link JSONArray JSONArrays}, those objects will be merged; otherwise,
     * the value in {@code dest} will be overwritten.
     * <p>
     * Any {@code JSONObject} or {@code JSONArray} values that are not merged
     * will be {@linkplain #copy(JSONObject, boolean) deep copied}.
     *
     * @param src       {@code JSONObject} to merge from
     * @param dest      {@code JSONObject} to merge to
     * @param overwrite If {@code true}, overwrites matching keys in {@code dest}
     * @return The modified {@code dest} object
     */
    public static JSONObject merge(JSONObject src, JSONObject dest, String name,
                                   final boolean overwrite) {
        Log.d(Log.SUBSYSTEM.JSON, TAG, "merge(%s), object: src: %s, dest: %s", name, src, dest);
        final Iterator<String> keys = src.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            final Object value = src.opt(key);
            if (!dest.has(key)) {
                Log.d(Log.SUBSYSTEM.JSON, TAG, "merge(%s), object: no field '%s' in dest; putting %s", name, key, value);
                safePut(dest, key, value);
            } else if (value instanceof JSONObject) {
                Log.d(Log.SUBSYSTEM.JSON, TAG, "merge(%s), object: object value for '%s', submerging %s", name, key, value);
                mergeSubObject(dest, key, (JSONObject) value, name, overwrite);
            } else if (value instanceof JSONArray) {
                Log.d(Log.SUBSYSTEM.JSON, TAG, "merge(%s), object: array value for '%s', submerging %s", name, key, value);
                mergeSubArray(dest, key, (JSONArray) value, name, overwrite);
            } else if (overwrite) {
                safePut(dest, key, value);
            }
        }
        Log.d(Log.SUBSYSTEM.JSON, TAG, "merge(%s), object: result: %s", name, dest);
        return dest;
    }

    public static JSONObject merge(JSONObject src, JSONObject dest, final boolean overwrite) {
        return merge(src, dest, null, overwrite);
    }

    /**
     * Recursively merges values from {@code src} into a copy of {@code dest}.
     *
     * @param src  {@code JSONObject} to merge from
     * @param dest {@code JSONObject} to merge to
     * @return A copy of {@code dest} with {@code src} merged in
     * @see #merge(JSONObject, JSONObject)
     */
    public static JSONObject merge(JSONObject src, UnmodifiableJSONObject dest) {
        return merge(src, copy(dest));
    }

    public static JSONObject merge(JSONObject src, UnmodifiableJSONObject dest, String name) {
        return merge(src, copy(dest), name);
    }

    /**
     * Recursively merges values from {@code src} into a copy of {@code dest},
     * optionally overwriting values of matching keys.
     *
     * @param src       {@code JSONObject} to merge from
     * @param dest      {@code JSONObject} to merge to
     * @param overwrite If {@code true}, overwrites matching keys in {@code dest}
     * @return A copy of {@code dest} with {@code src} merged in
     * @see #merge(JSONObject, JSONObject, boolean)
     */
    public static JSONObject merge(JSONObject src, UnmodifiableJSONObject dest,
                                   boolean overwrite) {
        return merge(src, copy(dest), overwrite);
    }

    private JSONHelpers() {

    }

    private static void mergeSubArray(JSONObject dest, final String key,
                                      JSONArray value, String name, boolean overwrite) {
        final JSONArray subArray = dest.optJSONArray(key);
        if (subArray != null) {
            Log.d(Log.SUBSYSTEM.JSON, TAG, "mergeSubArray(%s): merging %s into %s", name, value, subArray);
            merge(value, subArray, name, overwrite);
        } else {
            safePut(dest, key, copy(value));
        }
    }

    private static void mergeSubObject(JSONObject dest, final String key,
                                       JSONObject value, String name, final boolean overwrite) {
        final JSONObject subObject = dest.optJSONObject(key);
        if (subObject != null) {
            Log.d(Log.SUBSYSTEM.JSON, TAG, "mergeSubObject(%s): merging %s into %s", name, value, subObject);
            merge(value, subObject, name, overwrite);
        } else {
            safePut(dest, key, copy(value));
        }
    }

    /**
     * A convenience wrapper for {@linkplain JSONObject#put(String, Object)
     * put()} when {@code key} and {@code value} are both known to be "safe"
     * (i.e., neither should cause the {@code put()} to throw
     * {@link JSONException}). Cuts down on unnecessary {@code try/catch} blocks
     * littering the code and keeps the call stack clean of
     * {@code JSONException} throw declarations.
     *
     * @param dest  {@link JSONObject} to call {@code put()} on
     * @param key   The {@code key} parameter for {@code put()}
     * @param value The {@code value} parameter for {@code put()}
     * @throws RuntimeException If either {@code key} or {@code value} turn out to
     *                          <em>not</em> be safe.
     */
    private static void safePut(JSONObject dest, final String key,
                                final Object value) {
        try {
            dest.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException("This should not be able to happen!", e);
        }
    }

    private static <P extends Enum<P>> void safePut(JSONObject dest, P e, Object value) {
        safePut(dest, e, value, false);
    }

    private static <P extends Enum<P>> void safePut(JSONObject dest, P e, Object value, boolean lowerCase) {
        String name = e.name();
        if (lowerCase) {
            name = name.toLowerCase();
        }
        safePut(dest, name, value);
    }


    /**
     * Attempts to get a color formatted as an integer with ARGB ordering.  If the specified field
     * doesn't exist, returns {@code defColor}.
     *
     * @param json {@link JSONObject} to get the color from
     * @param elementName Name of the color element
     * @param defColor Color to return if the element doesn't exist
     * @return An ARGB formatted integer
     */
    public static int getJSONColor(final JSONObject json, String elementName, int defColor) {
        try {
            return getJSONColor(json, elementName);
        } catch (JSONException e) {
            return defColor;
        }
    }

    /**
     * Gets a color formatted as an integer with ARGB ordering.
     *
     * @param json {@link JSONObject} to get the color from
     * @param elementName Name of the color element
     * @return An ARGB formatted integer
     * @throws JSONException
     */
    public static int getJSONColor(final JSONObject json, String elementName) throws JSONException {
        Object raw = json.get(elementName);
        return convertJSONColor(raw);
    }

    public static <P extends Enum<P>> int getJSONColor(final JSONObject json, P e) throws JSONException {
        Object raw = JSONHelpers.get(json, e);
        return convertJSONColor(raw);
    }

    public static <P extends Enum<P>> int getJSONColor(final JSONObject json, P e, int defColor) {
        try {
            return getJSONColor(json, e);
        } catch (JSONException e1) {
            return defColor;
        } catch (RuntimeException e2) {
            if (e2.getCause() instanceof  JSONException) {
                return defColor;
            }
            throw e2;
        }
    }

    private static final int ALPHA_MASK = 0xf000;
    private static final int R_MASK = 0x0f00;
    private static final int ALPHA_R_MASK = ALPHA_MASK | R_MASK;
    private static final int G_MASK = 0x00f0;
    private static final int R_G_MASK = R_MASK | G_MASK;
    private static final int B_MASK = 0x000f;
    private static final int G_B_MASK = G_MASK | B_MASK;

    private static int convertJSONColor(Object raw) throws JSONException {
        final int r;
        final int g;
        final int b;
        final int a;

        if (raw instanceof String) {
            final String s = (String) raw;

            // CSS style color specification
            if (s.startsWith("#")) {
                Matcher m = sColorStringPattern.matcher(s);
                if (!m.find()) {
                    throw new JSONException("String format of color element not valid: " + s);
                }
                String cstr = m.group(1);
                final int length = cstr.length();
                long color = Long.parseLong(cstr, 16);
                // Convert the short CSS form to the long form
                if (length == 3 || length == 4) {
                    color = ((color & ALPHA_MASK) << 16) | ((color & ALPHA_R_MASK) << 12) |
                            ((color & R_G_MASK) << 8) | ((color & G_B_MASK) << 4) |
                            ((color & B_MASK));
                }
                Log.d(Log.SUBSYSTEM.JSON, TAG, "getJSONColor(): string matching '%s' (%s): %s", s, cstr, raw);
                return (int)color;
            }

            try {
                COLOR_NAMES colorName = COLOR_NAMES.valueOf(s.toUpperCase(Locale.getDefault()));
                return colorName.color;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.e(TAG, e, "convertJSONColor()");
                throw new JSONException("Not a valid color name: " + s);
            }
        }

        if (raw instanceof Integer) {
            int c = (Integer) raw;
            return c;
        } else if (raw instanceof JSONArray) {
            final JSONArray jsonArray = (JSONArray) raw;

            r = jsonArray.getInt(0);
            g = jsonArray.getInt(1);
            b = jsonArray.getInt(2);
            if (jsonArray.length() > 3) {
                a = jsonArray.getInt(3);
            } else {
                a = 0xFF;
            }
        } else if (raw instanceof JSONObject) {
            final JSONObject jsonObject = (JSONObject) raw;
            r = jsonObject.getInt("r");
            g = jsonObject.getInt("g");
            b = jsonObject.getInt("b");
            a = jsonObject.optInt("a", 0xFF);
        } else {
            throw new JSONException("Object format of color element not valid!");
        }

        Log.d(Log.SUBSYSTEM.JSON, TAG, "getJSONColor(): r: %d, g: %d, b: %d, a: %d", r, g, b, a);
        return Color.argb(a, r, g, b);
    }

    public static <P extends Enum<P>> float[] getJSONColorGl(final JSONObject json, P e) {
        try {
            return getJSONColorGl(json, e.name());
        } catch (JSONException e1) {
            e1.printStackTrace();
            Log.e(TAG, e1, "getJSONColorGl()");
            throw new RuntimeException(e1.getLocalizedMessage(), e1);
        }
    }

    /**
     * Gets a color formatted as an array of float in [r, g, b, a] order, with each component value
     * ranging from 0.0 to 1.0 -- the OpenGL format.
     *
     * @param json {@link JSONObject} to get the color from
     * @param elementName Name of the color element
     * @return An [r, g, b, a] formatted array of float
     * @throws JSONException
     */
    public static float[] getJSONColorGl(final JSONObject json, String elementName) throws JSONException {
        final float r;
        final float g;
        final float b;
        final float a;

        Object raw = json.get(elementName);
        if (raw instanceof String) {
            final String s = (String) raw;
            Matcher m = sColorStringPattern.matcher(s);
            if (!m.find()) {
                throw new JSONException("String format of color element '"
                        + elementName + "' not valid: " + s);
            }
            final String cstr = m.group(1);
            raw = Integer.parseInt(cstr, 16);
            Log.d(Log.SUBSYSTEM.JSON, TAG, "getJSONColor(): string matching '%s' (%s): 0x%x", s, cstr, raw);
        }

        if (raw instanceof Integer) {
            int c = (Integer) raw;
            if (c > 0xFFFFFF) {
                a = Color.alpha(c);
            } else {
                a = 0;
            }
            r = Colors.byteToGl(Color.red(c));
            g = Colors.byteToGl(Color.green(c));
            b = Colors.byteToGl(Color.blue(c));
        } else if (raw instanceof JSONArray) {
            final JSONArray jsonArray = (JSONArray) raw;
            r = (float) jsonArray.getDouble(0);
            g = (float) jsonArray.getDouble(1);
            b = (float) jsonArray.getDouble(2);
            if (jsonArray.length() > 3) {
                a = (float) jsonArray.getDouble(3);
            } else {
                a = 0;
            }
        } else if (raw instanceof JSONObject) {
            final JSONObject jsonObject = (JSONObject) raw;
            r = (float) jsonObject.getDouble("r");
            g = (float) jsonObject.getDouble("g");
            b = (float) jsonObject.getDouble("b");
            a = (float) jsonObject.optDouble("a", 0);
        } else {
            throw new JSONException("Format of color element '" + elementName
                    + "' not valid!");
        }

        Log.d(Log.SUBSYSTEM.JSON, TAG, "getJSONColorGl(): r: %.2f, g: %.2f, b: %.2f, a: %.2f", r, g, b, a);
        return new float[] { r, g, b, a };
    }

    private static final Pattern sColorStringPattern = Pattern.compile("^#((?:[0-9a-fA-F]{3,4})|(?:[0-9a-fA-F]{6}(?:[0-9a-fA-F]{2})?))$");
    private enum COLOR_NAMES {
        BLACK(Color.BLACK),
        BLUE(Color.BLUE),
        CYAN(Color.CYAN),
        DKGRAY(Color.DKGRAY),
        GRAY(Color.GRAY),
        GREEN(Color.GREEN),
        LTGRAY(Color.LTGRAY),
        MAGENTA(Color.MAGENTA),
        RED(Color.RED),
        TRANSPARENT(Color.TRANSPARENT),
        WHITE(Color.WHITE),
        YELLOW(Color.YELLOW);

        COLOR_NAMES(int color) {
            this.color = color;
        }

        public final int color;
    }


    private static final String TAG = JSONHelpers.class.getSimpleName();
}
