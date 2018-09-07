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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import org.gearvrf.utility.FileNameUtils;

import android.content.res.Resources;
import android.os.Environment;

/**
 * Models a file system which supports stream I/O.
 */
public class GVRResourceVolume {
    private static final String TAG = GVRResourceVolume.class.getSimpleName();

    public enum VolumeType {
        ANDROID_ASSETS ("assets", "/"),
        ANDROID_RESOURCE ("res", "/"),
        ANDROID_SDCARD ("sdcard", "/"),
        LINUX_FILESYSTEM ("linux", "/"),
        NETWORK ("url", "/"),
        INPUT_STREAM ("stream", "/");

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
    protected String fileName;
    protected boolean enableUrlLocalCache = false;
    protected InputStream volumeInputStream;

    /**
     * Constructor. Creates a {@link GVRResourceVolume} object based on a volume type.
     * @param gvrContext The GVR Context.
     * @param volume The volume type.
     */
    public GVRResourceVolume(GVRContext gvrContext, VolumeType volume) {
        this(gvrContext, volume, null);
    }

    /**
     * Constructor. Creates a {@link GVRResourceVolume} object based on a resource.
     *
     * This form of the constructor gets the volume type and the volume path
     * from the filename in the resource. This form of the constructor maintains the
     * resource filename and can provide it later via @{link #getFileName() }.
     *
     * If a volume is opened using a GVRAndroidResource that references a file
     * using an Android resource ID, other files that are referenced by that
     * volume (textures, MTL files) are assumed to be in res/raw.
     *
     * @param context The GVR Context.
     * @param resource GVRAndroidResource residing on the volume. This resource is added
     *                 to the internal resource map of this volume so it can be opened
     *                 based on only the filename using GVRResourceVolume.openResource.
     * @see #getFileName()
     */
    public GVRResourceVolume(GVRContext context, GVRAndroidResource resource)
    {
        String filename = resource.getResourcePath();
        String fname = filename.toLowerCase();
        gvrContext = context;
        volumeType = GVRResourceVolume.VolumeType.ANDROID_ASSETS;
        defaultPath = FileNameUtils.getParentDirectory(filename);
        switch (resource.getResourceType())
        {
            case ANDROID_RESOURCE:
            volumeType = VolumeType.ANDROID_RESOURCE;

            default:
            break;

            case LINUX_FILESYSTEM:
            volumeType = VolumeType.LINUX_FILESYSTEM;
            if (fname.startsWith("sd:"))
            {
                if (defaultPath != null)
                {
                    defaultPath = defaultPath.substring(3);
                }
                volumeType = GVRResourceVolume.VolumeType.ANDROID_SDCARD;
            }
            break;

            case NETWORK:
            volumeType = VolumeType.NETWORK;
            if (fname.startsWith("http:") || fname.startsWith("https:"))
            {
                defaultPath = FileNameUtils.getURLParentDirectory(filename);
            }
            break;

            case INPUT_STREAM:
            volumeType = VolumeType.INPUT_STREAM;
            try
            {
                volumeInputStream = resource.getStream();
            }
            catch (IOException ex)
            {
                volumeInputStream = null;
            }
            break;
        }
        if (defaultPath != null)
        {
            fileName = filename.substring(defaultPath.length() + 1);
        }
        else
        {
            fileName = filename;
        }
        addResource(resource);
    }

    /**
     * Constructor. Creates a {@link GVRResourceVolume} object based on a filename.
     *
     * This form of the constructor maintains the filename and can provide it
     * later via @{link #getFileName() }.
     * @param filename A filename, relative to the root of the volume.
     *            If the filename starts with "sd:" the file is assumed to reside on the SD Card.
     *            If the filename starts with "http:" or "https:" it is assumed to be a URL.
     *            Otherwise the file is assumed to be relative to the "assets" directory.
     * @param context The GVR Context.
     * @see #getFileName()
     */
    public GVRResourceVolume(GVRContext context, String filename)
    {
        String fname = filename.toLowerCase();
        gvrContext = context;
        volumeType = GVRResourceVolume.VolumeType.ANDROID_ASSETS;
        defaultPath = FileNameUtils.getParentDirectory(filename);
        if (fname.startsWith("sd:"))
        {
            if (defaultPath != null)
            {
                defaultPath = defaultPath.substring(3);
            }
            volumeType = GVRResourceVolume.VolumeType.ANDROID_SDCARD;
        }
        else if (fname.startsWith("http:") || fname.startsWith("https:"))
        {
            volumeType = GVRResourceVolume.VolumeType.NETWORK;
            defaultPath = FileNameUtils.getURLParentDirectory(filename);
        }
        else
        {
            defaultPath = FileNameUtils.getParentDirectory(filename);
        }
        if (defaultPath != null)
        {
            fileName = filename.substring(defaultPath.length() + 1);
        }
        else
        {
            fileName = filename;
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

    private ConcurrentHashMap<String, GVRAndroidResource> resourceMap = new ConcurrentHashMap<String, GVRAndroidResource>();

    /**
     * Adds an android resource to the volume's resource map.
     * This function is useful if you have created the GVRAndroidResource from a resource ID
     * but would like to read it via it's filename. If you add the resource first,
     * you can call openResource() on it's filename.
     *
     * @param resource
     *            Android resource to open.
     *
     * @throws IOException
     */
    public GVRAndroidResource addResource(GVRAndroidResource resource)
    {
        String fileName = resource.getResourcePath();
        GVRAndroidResource resourceValue = resourceMap.get(fileName);
        if (resourceValue != null)
        {
            return resourceValue;
        }
        // Only put the resourceKey into the map for the first time, later put
        // will simply return the first resource
        resourceValue = resourceMap.putIfAbsent(fileName, resource);
        return resourceValue == null ? resource : resourceValue;
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
        // In this case, the path is interpreted as relative to defaultPath,
        // which is the root of the filesystem.
        if (filePath.startsWith(File.separator)) {
            filePath = filePath.substring(File.separator.length());
        }

        filePath = adaptFilePath(filePath);
        String path;
        int resourceId;

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

            case ANDROID_RESOURCE:
            path = FileNameUtils.getBaseName(filePath);
            resourceId = gvrContext.getContext().getResources().getIdentifier(path, "raw", gvrContext.getContext().getPackageName());
            if (resourceId == 0) {
                throw new FileNotFoundException(filePath + " resource not found");
            }
            resourceKey = new GVRAndroidResource(gvrContext, resourceId);
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

        case INPUT_STREAM:
            resourceKey = new GVRAndroidResource(getFullPath(defaultPath, filePath), volumeInputStream);
            break;

        case NETWORK:
            resourceKey = new GVRAndroidResource(gvrContext,
                    getFullURL(defaultPath, filePath), enableUrlLocalCache);
            break;

        default:
            throw new IOException(
                    String.format("Unrecognized volumeType %s", volumeType));
        }
        return addResource(resourceKey);
    }

    /***
     * Gets the filename from the initial file path specified in the constructor.
     * This filename is only available if the GVRResourceVolume was constructed
     * with the GVRResourceVolume(GVRContext, String) constructor.
     * The filename returned does not include the parent directory.
     */
    String getFileName() { return fileName; }



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

    public String getFullPath() { return getFullPath(defaultPath, getFileName()); }

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

