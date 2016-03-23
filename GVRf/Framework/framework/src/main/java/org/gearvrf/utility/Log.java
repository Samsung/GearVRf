/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.utility;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

/** {@link android.util.Log} with String.format() pattern */
public abstract class Log {

    /*
     * Log wrappers
     */

    /** {@link android.util.Log#d(String, String)}, with String.format() pattern */
    public static void d(String TAG, String pattern, Object... parameters) {
        android.util.Log.d(TAG, format(pattern, parameters));
    }

    /** {@link android.util.Log#e(String, String)}, with String.format() pattern */
    public static void e(String TAG, String pattern, Object... parameters) {
        android.util.Log.e(TAG, format(pattern, parameters));
    }

    /** {@link android.util.Log#i(String, String)}, with String.format() pattern */
    public static void i(String TAG, String pattern, Object... parameters) {
        android.util.Log.i(TAG, format(pattern, parameters));
    }

    /** {@link android.util.Log#v(String, String)}, with String.format() pattern */
    public static void v(String TAG, String pattern, Object... parameters) {
        android.util.Log.v(TAG, format(pattern, parameters));
    }

    /** {@link android.util.Log#w(String, String)}, with String.format() pattern */
    public static void w(String TAG, String pattern, Object... parameters) {
        android.util.Log.w(TAG, format(pattern, parameters));
    }

    private static String format(String pattern, Object... parameters) {
        return parameters == null || parameters.length == 0 ? pattern : //
                String.format(pattern, parameters);
    }

    /**
     * Log TAG creator
     */

    /**
     * Constructs debug {@code TAG} strings using a {@link Class}, so that
     * rename refactorings will keep the {@code TAG} up-to-date. Also handles
     * constructing fully-qualified {@code TAG} strings for nested classes.
     * <p>
     * Note that this involves a small amount of runtime overhead at start-up,
     * so if your code is particularly performance-sensitive, you may want to
     * stick to a manually constructed {@code TAG} string.
     * 
     * <p>
     * How you use it:
     * 
     * <pre>
     * class MyClass {
     *     ...
     *     private static final String TAG = Utility.tag(MyClass.class);
     * }
     * </pre>
     * 
     * @param clazz
     *            The {@link Class} of the class to build a {@code TAG} string
     *            for
     * @return Fully-qualified {@code TAG} string
     */
    public static String tag(Class<?> clazz) {
        String result = clazz.getSimpleName();
        for (Class<?> outer = clazz.getEnclosingClass(); outer != null; outer = outer
                .getEnclosingClass()) {
            result = outer.getSimpleName() + "." + result;
        }
        return result;
    }

    /**
     * Generates a string with {@code length} spaces.
     *
     * @param length length of the string
     * @return the string
     */
    public static String getSpaces(int length) {
        char[] charArray = new char[length];
        Arrays.fill(charArray, ' ');
        String str = new String(charArray);
        return str;
    }

    /**
     * Log long string using verbose tag
     * 
     * @param TAG The tag.
     * @param longString The long string.
     */
    public static void logLong(String TAG, String longString) {
        InputStream is = new ByteArrayInputStream( longString.getBytes() );
        @SuppressWarnings("resource")
        Scanner scan = new Scanner(is);
        while (scan.hasNextLine()) {
            Log.v(TAG, scan.nextLine());
        }
    }
}
