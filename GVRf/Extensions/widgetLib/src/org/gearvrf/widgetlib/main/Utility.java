package org.gearvrf.widgetlib.main;

import android.content.Context;
import android.content.res.Resources;

import org.gearvrf.widgetlib.log.Log;

import org.gearvrf.utility.RuntimeAssertion;
import org.joml.Vector3f;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.gearvrf.utility.Log.tag;

/**
 * Generally useful constants and static methods.
 */
public abstract class Utility {
    private static final String TAG = tag(Utility.class);

    /**
     * Float equality tests are subject to rounding error. The equal() and
     * notEqual() methods look for differences less than ETA
     */
    private static final double ETA = 1e-10d;

    private static final double ETAd = ETA;
    private static final float ETAf = (float) ETA;

    // readTextFile
    /**
     * Read a text file from assets into a single string
     *
     * @param context
     *            A non-null Android Context
     * @param asset
     *            The asset file to read
     * @return The contents or null on error.
     */
    public static String readTextFile(Context context, String asset) {
        try {
            InputStream inputStream = context.getAssets().open(asset);
            return org.gearvrf.utility.TextFile.readTextFile(inputStream);
        } catch (FileNotFoundException f) {
            Log.w(TAG, "readTextFile(): asset file '%s' doesn't exist", asset);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e, "readTextFile()");
        }
        return null;
    }


    /**
     * Are these two numbers effectively equal?
     *
     * Because of rounding error, it's usually not safe to compare the results
     * of calculations for floating point equality. This method checks if the
     * difference between these two numbers is less than ETA, the Greek letter
     * usually used to stand for 'error term' in floating point tests.
     */
    public static boolean equal(double n1, double n2) {
        return Math.abs(n1 - n2) < ETAd;
    }

    /**
     * Are these two numbers effectively equal?
     *
     * Because of rounding error, it's usually not safe to compare the results
     * of calculations for floating point equality. This method checks if the
     * difference between these two numbers is less than ETA, the Greek letter
     * usually used to stand for 'error term' in floating point tests.
     * @param n1
     * @param n2
     */
    public static boolean equal(float n1, float n2) {
        return Math.abs(n1 - n2) < ETAf;
    }


    /**
     * Are these two numbers effectively equal?
     *
     * The same logic is applied for each of the 3 vector dimensions: see  {@link #equal}
     * @param v1
     * @param v2
     */
    public static boolean equal(Vector3f v1, Vector3f v2) {
        return equal(v1.x, v2.x) && equal(v1.y, v2.y) && equal(v1.z, v2.z);
    }

    /**
     * Parses the resource String id and get back the int res id
     * @param context
     * @param id String resource id
     * @return int resource id
     */
    public static int getId(Context context, String id) {
        final String defType;
        if (id.startsWith("R.")) {
            int dot = id.indexOf('.', 2);
            defType = id.substring(2, dot);
        } else {
            defType = "drawable";
        }

        Log.d(TAG, "getId(): id: %s, extracted type: %s", id, defType);
        return getId(context, id, defType);
    }

    /**
     * Get the int resource id with specified type definition
     * @param context
     * @param id String resource id
     * @return int resource id
     */
    public static int getId(Context context, String id, String defType) {
        String type = "R." + defType + ".";
        if (id.startsWith(type)) {
            id = id.substring(type.length());
        }

        Resources r = context.getResources();
        int resId = r.getIdentifier(id, defType,
                context.getPackageName());
        if (resId > 0) {
            return resId;
        } else {
            throw new RuntimeAssertion("Specified resource '%s' could not be found", id);
        }
    }
}
