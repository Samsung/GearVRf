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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Utilities to parse file paths.
 */
public class FileNameUtils {
    private static final String sPatternUrl = "^[a-zA-Z]+://.*";

    /**
     * Gets the base filename without the extension but includes
     * the path if {@code fileName} includes path.
     *
     * @param fileName The file name to parse.
     * @return The base name of the file.
     */
    public static String getBaseName(String fileName) {
        if (fileName == null)
            return null;

        String[] tokens = split(fileName);
        return tokens[0];
    }

    /**
     * Gets the filename extension (characters after the last '.' character).
     * @param fileName The file name to parse.
     * @return The extension of the file.
     */
    public static String getExtension(String fileName) {
        if (fileName == null)
            return null;

        String[] tokens = split(fileName);
        if (tokens.length >= 2)
            return tokens[tokens.length - 1];

        return null;
    }

    /**
     * Gets the filename from a path or URL.
     * @param path or url.
     * @return the file name.
     */
    public static String getFilename(String path) throws IllegalArgumentException {
        if (Pattern.matches(sPatternUrl, path))
            return getURLFilename(path);

        return new File(path).getName();
    }

    public static String getURLFilename(String urlString) throws IllegalArgumentException {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw Exceptions.IllegalArgument("Malformed URL: %s", url);
        }

        return url.getPath().substring(url.getPath().lastIndexOf("/") + 1);
    }

    /**
     * Returns the directory of the file.
     *
     * @param filePath Path of the file.
     * @return The directory string or {@code null} if
     * there is no parent directory.
     * @throws IllegalArgumentException
     */
    public static String getParentDirectory(String filePath) throws IllegalArgumentException {
        if (Pattern.matches(sPatternUrl, filePath))
            return getURLParentDirectory(filePath);

        return new File(filePath).getParent();
    }

    /**
     * Returns the directory of the URL.
     *
     * @param filePath Path of the file.
     * @return The directory string.
     * @throws IllegalArgumentException
     */
    public static String getURLParentDirectory(String urlString) throws IllegalArgumentException {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw Exceptions.IllegalArgument("Malformed URL: %s", url);
        }

        String path = url.getPath();
        int lastSlashIndex = path.lastIndexOf("/");
        String directory = lastSlashIndex == -1 ? "" : path.substring(0, lastSlashIndex);
        return String.format("%s://%s%s%s%s", url.getProtocol(),
                url.getUserInfo() == null ? "" : url.getUserInfo() + "@",
                url.getHost(),
                url.getPort() == -1 ? "" : ":" + Integer.toString(url.getPort()),
                directory);
    }

    // http://stackoverflow.com/questions/4545937/java-splitting-the-filename-into-a-base-and-extension
    private static String[] split(String fileName) {
        String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
        return tokens;
    }
}
