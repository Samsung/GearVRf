package com.samsung.android.gvr.test.base;

import android.annotation.TargetApi;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Douglas on 2/5/15.
 */
public class BoundsValues {


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static List<Float> getFloatList() {
        List<Float> floats = new ArrayList<>();
        floats.add(Float.MIN_VALUE);
        floats.add(Float.MAX_VALUE);
        floats.add(Float.MIN_NORMAL);
        floats.add(Float.POSITIVE_INFINITY);
        floats.add(Float.POSITIVE_INFINITY);
        floats.add(Float.NaN);

        return floats;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String getString() {

        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static List<Long> getLongList() {
        List<Long> longs = new ArrayList<>();
        longs.add(Long.MIN_VALUE);
        longs.add(Long.MAX_VALUE);

        return longs;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static List<Integer> getIntegerList() {
        List<Integer> integers = new ArrayList<>();
        integers.add(Integer.MIN_VALUE);
        integers.add(Integer.MAX_VALUE);

        return integers;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static List<Double> getDoubleList() {
        List<Double> doubles = new ArrayList<>();
        doubles.add(Double.MIN_VALUE);
        doubles.add(Double.MAX_VALUE);
        doubles.add(Double.MIN_NORMAL);
        doubles.add(Double.POSITIVE_INFINITY);
        doubles.add(Double.POSITIVE_INFINITY);
        doubles.add(Double.NaN);
        return doubles;
    }

    public static Float getFloatNull() {
        return null;
    }

    public static float[] getArrayFloatEmpty() {
        return new float[]{};
    }

    public static Float[] getArrayFloatObjectEmpty() {
        return new Float[]{};
    }

    public static Long getLongNull() {
        return null;
    }

    public static Integer getIntegerNull() {
        return null;
    }
}
