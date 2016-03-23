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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

/**
 * Text file utilities.
 * 
 * <p>
 * Please note that the {@code org.gearvrf.utility} package contains low-level
 * utility code used in multiple GVRF packages. We can't keep you from using
 * this code in a GVRF application, but we <em>can</em> urge you not to:
 * everything in this package is minimally documented, internal code. We are not
 * making any promises about the behavior or performance of any code in this
 * package; this package is not part of the GVRF API, and future releases may
 * change or remove classes or methods without providing for backward
 * compatibility.
 */
public abstract class TextFile {

    /**
     * Read a text file into a single string
     * 
     * @param filename
     *            The file to read
     * @return The contents, or null on error.
     */
    public static String readTextFile(String filename) {
        return readTextFile(new File(filename));
    }

    /**
     * Read a text file into a single string
     * 
     * @param file
     *            The file to read
     * @return The contents, or null on error.
     */
    public static String readTextFile(File file) {
        try {
            return readTextFile(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Read a text file resource into a single string
     * 
     * @param context
     *            A non-null Android Context
     * @param resourceId
     *            An Android resource id
     * @return The contents, or null on error.
     */
    public static String readTextFile(Context context, int resourceId) {
        InputStream inputStream = context.getResources().openRawResource(
                resourceId);
        return readTextFile(inputStream);
    }

    /**
     * Read a text stream into a single string.
     * 
     * @param inputStream
     *            Stream containing text. Will be closed on exit.
     * @return The contents, or null on error.
     */
    public static String readTextFile(InputStream inputStream) {
        InputStreamReader streamReader = new InputStreamReader(inputStream);
        return readTextFile(streamReader);
    }

    private static String readTextFile(InputStreamReader reader) {
        return readTextFile(new BufferedReader(reader));
    }

    private static String readTextFile(BufferedReader reader) {
        StringBuilder text = new StringBuilder();

        try {
            char[] buffer = new char[8192]; // default BufferedReader size
            int read;
            while ((read = reader.read(buffer)) >= 0) {
                if (read > 0) {
                    text.append(buffer, 0, read);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return text.toString();
    }
}
