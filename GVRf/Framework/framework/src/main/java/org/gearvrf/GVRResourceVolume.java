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
import java.util.concurrent.ConcurrentHashMap;

import org.gearvrf.utility.FileNameUtils;

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

    /**
     * Constructor. Creates a {@link GVRResourceVolume} object based on a volume type.
     * @param gvrContext The GVR Context.
     * @param volume The volume type.
     */
    public GVRResourceVolume(GVRContext gvrContext, VolumeType volume) {
        this(gvrContext, volume, null);
    }

    /**
     * Constructor. Creates a {@link GVRResourceVolume} object based on a filename.
     * @param filename A filename, relative to the root of the volume.
     *            If the filename starts with "sd:" the file is assumed to reside on the SD Card.
     *            If the filename starts with "http:" or "https:" it is assumed to be a URL.
     *            Otherwise the file is assumed to be relative to the "assets" directory.
     * @param context The GVR Context.
     */
    public GVRResourceVolume(GVRContext context, String filename)
    {
        String fname = filename.toLowerCase();
        gvrContext = context;
        volumeType = GVRResourceVolume.VolumeType.ANDROID_ASSETS;
        if (fname.startsWith("sd:"))
        {
            String s = FileNameUtils.getParentDirectory(filename);
            if (s != null)
            {
                defaultPath = s.substring(3);
            }
            volumeType = GVRResourceVolume.VolumeType.ANDROID_SDCARD;
        }
        else if (fname.startsWith("http:") || fname.startsWith("https:"))
        {
            volumeType = GVRResourceVolume.VolumeType.NETWORK;
            defaultPath = FileNameUtils.getURLParentDirectory(filename);
        }
    }
    
    /**
     * Constructor. Creates a {@link GVRResourceVolume} object based on a volume type,
     * and a default path.
     *
     * @param gvrContext The GVR Context.
     * @param volumeType The volume type. See {@link VolumeType}.
     * @param defaultPath The default path which specifies the 'root' directory of the
     * volume.
     */
    public GVRResourceVolume(GVRContext gvrContext, VolumeType volumeType, String defaultPath) {
        this.gvrContext = gvrContext;
        this.volumeType = volumeType;
        this.defaultPath = defaultPath;
    }

    /**
     * Constructor. Creates a {@link GVRResourceVolume} object based on a volume type,
     * and a default path.
     *
     * @param gvrContext The GVR Context.
     * @param volumeType The volume type. See {@link VolumeType}.
     * @param defaultPath The default path which specifies the 'root' directory of the
     * volume.
     * @param cacheEnabled Set to {@code true} for enabling cache for network files.
     */
    public GVRResourceVolume(GVRContext gvrContext,
            VolumeType volumeType, String defaultPath, boolean cacheEnabled) {
        this(gvrContext, volumeType, defaultPath);
        this.enableUrlLocalCache = cacheEnabled;
    }

    private ConcurrentHashMap<GVRAndroidResource, GVRAndroidResource> resourceMap = new ConcurrentHashMap<GVRAndroidResource, GVRAndroidResource>();

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
        // In this case, the path is interpreted as relative to defaultPath,
        // which is the root of the filesystem.
        if (filePath.startsWith(File.separator)) {
            filePath = filePath.substring(File.separator.length());
        }

        filePath = adaptFilePath(filePath);
        String path;

        GVRAndroidResource resourceKey;
        switch (volumeType) {
        case ANDROID_ASSETS:
            // Resolve '..' and '.'
            path = getFullPath(defaultPath, filePath);
            path = new File(path).getCanonicalPath();
            if (path.startsWith(File.separator)) {
                path = path.substring(1);
            }
            resourceKey = new GVRAndroidResource(gvrContext, path);
            break;

        case LINUX_FILESYSTEM:
            resourceKey = new GVRAndroidResource(
                    getFullPath(defaultPath, filePath));
            break;

        case ANDROID_SDCARD:
            String linuxPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            resourceKey = new GVRAndroidResource(
                    getFullPath(linuxPath, defaultPath, filePath));
            break;

        case NETWORK:
            resourceKey = new GVRAndroidResource(gvrContext,
                    getFullURL(defaultPath, filePath), enableUrlLocalCache);
            break;

        default:
            throw new IOException(
                    String.format("Unrecognized volumeType %s", volumeType));
        }

        GVRAndroidResource resourceValue = resourceMap.get(resourceKey);
        if (resourceValue != null) {
            return resourceValue;
        }

        // Only put the resourceKey into the map for the first time, later put
        // will simply return the first resource
        resourceValue = resourceMap.putIfAbsent(resourceKey, resourceKey);
        return resourceValue == null ? resourceKey : resourceValue;
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
