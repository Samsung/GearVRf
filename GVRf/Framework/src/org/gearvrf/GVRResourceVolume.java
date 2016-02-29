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

package org.gearvrf;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Environment;

/**
 * Models a file system which supports stream I/O.
 */
public class GVRResourceVolume {
    private static final String TAG = GVRResourceVolume.class.getSimpleName();

    public enum VolumeType {
        ANDROID_ASSETS ("assets", "/"),
        ANDROID_SDCARD ("sdcard", "/"),
        LINUX_FILESYSTEM ("linux", "/"),
        NETWORK ("url", "/");

        private String name;
        private String separator;

        VolumeType(String name, String separator) {
            this.name = name;
            this.separator = separator;
        }

        public String getName() {
            return name;
        }

        public String getSeparator() {
            return separator;
        }

        // Gets a volume type from a string. For example, when loading
        // a script from a bundle file, the volume type attribute needs
        // to be converted to a VolumeType.
        public static VolumeType fromString(String name) {
            for (VolumeType type : VolumeType.values()) {
                if (type.getName().equalsIgnoreCase(name)) // case insensitive
                    return type;
            }

            return null;
        }
    }

    protected GVRContext gvrContext;
    protected VolumeType volumeType;
    protected String defaultPath;
    protected boolean enableUrlLocalCache = false;

    public GVRResourceVolume(GVRContext gvrContext, VolumeType volume) {
        this(gvrContext, volume, null);
    }

    public GVRResourceVolume(GVRContext gvrContext, VolumeType volumeType, String defaultPath) {
        this.gvrContext = gvrContext;
        this.volumeType = volumeType;
        this.defaultPath = defaultPath;
    }

    /* package */ GVRResourceVolume(GVRContext gvrContext,
            VolumeType volumeType, String defaultPath, boolean cacheEnabled) {
        this(gvrContext, volumeType, defaultPath);
        this.enableUrlLocalCache = cacheEnabled;
    }

    /**
     * Opens a file from the volume. The filePath is relative to the
     * defaultPath.
     *
     * @param filePath
     *            File path of the resource to open.
     *
     * @throws IOException
     */
    public GVRAndroidResource openResource(String filePath) throws IOException {
        // Error tolerance: Remove initial '/' introduced by file::///filename
        // In this case, the path is interpreted as relative to defaultPath, which
        // is the root of the filesystem.
        if (filePath.startsWith(File.separator)) {
            filePath = filePath.substring(File.separator.length());
        }

        filePath = adaptFilePath(filePath);

        switch (volumeType) {
        case ANDROID_ASSETS:
            return new GVRAndroidResource(gvrContext, getFullPath(defaultPath, filePath));

        case LINUX_FILESYSTEM:
            return new GVRAndroidResource(getFullPath(defaultPath, filePath));

        case ANDROID_SDCARD:
            String linuxPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            return new GVRAndroidResource(getFullPath(linuxPath, defaultPath, filePath));

        case NETWORK:
            return new GVRAndroidResource(gvrContext,
                    getFullURL(defaultPath, filePath), enableUrlLocalCache);

        default:
            throw new IOException(String.format("Unrecognized volumeType %s", volumeType));
        }
    }

    /**
     * Adapt a file path to the current file system.
     * @param filePath The input file path string.
     * @return File path compatible with the file system of this {@link GVRResourceVolume}.
     */
    protected String adaptFilePath(String filePath) {
        // Convert windows file path to target FS
        String targetPath = filePath.replaceAll("\\\\", volumeType.getSeparator());

        return targetPath;
    }

    private URL getFullURL(String defaultPath, String filePath) throws MalformedURLException {
        return new URL(defaultPath + "/" + filePath);
    }

    /**
     * Changes the default path.
     *
     * @param defaultPath The path to change to.
     */
    public void changeDefaultPath(String defaultPath) {
        defaultPath = new String(defaultPath);
    }

    protected String getFullPath(String... path) {
        StringBuilder fullPath = new StringBuilder();

        boolean first = true;
        for (String fileName : path) {
            if (fileName == null || fileName.isEmpty())
                continue;

            if (!first) {
                fullPath.append(File.separator);
            } else {
                first = false;
            }

            fullPath.append(fileName);
        }

        return fullPath.toString();
    }
}
