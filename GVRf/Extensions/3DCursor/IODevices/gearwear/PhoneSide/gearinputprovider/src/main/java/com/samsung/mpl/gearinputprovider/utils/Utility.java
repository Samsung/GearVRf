/*
 * Copyright (C) 2014 Samsung Electronics. All Rights Reserved.
 * Source code is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the
 * License.
 *
 * IMPORTANT LICENSE NOTE:
 * The IMAGES AND RESOURCES are licensed under the Creative Commons BY-NC-SA 3.0
 * License (http://creativecommons.org/licenses/by-nc-sa/3.0/).
 * The source code is allows commercial re-use, but IMAGES and RESOURCES forbids it.
 */

package com.samsung.mpl.gearinputprovider.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

/**
 * Convenience methods
 */
public class Utility {

    private static boolean vLogsEnabled = false;

    public static void setVLogsEnabled(boolean enable) {
        vLogsEnabled = enable;
    }

    public static void logV(String tag, String message) {
        if(vLogsEnabled) {
            Log.v(tag, message);
        }
    }

    public static void logV(String tag, String format, Object... args) {
        if(vLogsEnabled) {
            Log.v(tag, buildFormattedMessage(format, args));
        }
    }

    public static void logD(String tag, String message) {
        Log.d(tag, message);
    }

    public static void logD(String tag, String format, Object... args) {
        Log.d(tag, buildFormattedMessage(format, args));
    }

    public static void logI(String tag, String message) {
        Log.i(tag, message);
    }

    public static void logI(String tag, String format, Object... args) {
        Log.i(tag, buildFormattedMessage(format, args));
    }

    public static void logW(String tag, String message) {
        Log.w(tag, message);
    }

    public static void logW(String tag, String format, Object... args) {
        Log.w(tag, buildFormattedMessage(format, args));
    }

    public static void logE(String tag, String message) {
        Log.e(tag, message);
    }

    public static void logE(String tag, String format, Object... args) {
        Log.e(tag, buildFormattedMessage(format, args));
    }

    public static void logE(String tag, Throwable tr, String message) {
        Log.e(tag, message, tr);
    }

    public static void logE(String tag, Throwable tr, String format, Object... args) {
        Log.e(tag, buildFormattedMessage(format, args), tr);
    }

    public static String buildFormattedMessage(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }

    public static void showShortToast(Context context, CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void showShortToast(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void showLongToast(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
    }
}
