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

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import android.content.res.AssetManager;

import org.gearvrf.GVRImportSettings;

/**
 * {@link GVRImporter} provides methods for importing 3D models and making them
 * available through instances of {@link GVRAssimpImporter}.
 * <p>
 * Supports importing models from an application's resources (both
 * {@code assets} and {@code res/raw}) and from directories on the device's SD
 * card that the application has permission to read.
 */
class GVRImporter {
    private GVRImporter() {
    }

    /**
     * Imports a 3D model from the specified file in the application's
     * {@code asset} directory.
     * 
     * @param gvrContext
     *            Context to import file from.
     * @param filename
     *            Name of the file to import.
     * @return An instance of {@link GVRAssimpImporter} or {@code null} if the
     *         file does not exist (or cannot be read)
     */
    static GVRAssimpImporter readFileFromAssets(GVRContext gvrContext,
            String filename, EnumSet<GVRImportSettings> settings) { 
        long nativeValue = NativeImporter.readFileFromAssets(gvrContext
                .getContext().getAssets(), filename, GVRImportSettings.getAssimpImportFlags(settings));
        return nativeValue == 0 ? null : new GVRAssimpImporter(gvrContext,
                nativeValue);
    }

    static GVRAssimpImporter readFileFromResources(GVRContext gvrContext,
            int resourceId, EnumSet<GVRImportSettings> settings) {
        return readFileFromResources(gvrContext, new GVRAndroidResource(
                gvrContext, resourceId), settings);
    }

    /** @since 1.6.2 */
    static GVRAssimpImporter readFileFromResources(GVRContext gvrContext,
            GVRAndroidResource resource, EnumSet<GVRImportSettings> settings) {
        try {
            byte[] bytes;
            InputStream stream = resource.getStream();
            try {
                bytes = new byte[stream.available()];
                stream.read(bytes);
            } finally {
                resource.closeStream();
            }
            String resourceFilename = resource.getResourceFilename();
            if (resourceFilename == null) {
                resourceFilename = ""; // Passing null causes JNI exception.
            }
            long nativeValue = NativeImporter.readFromByteArray(bytes,
                    resourceFilename, GVRImportSettings.getAssimpImportFlags(settings));
            return new GVRAssimpImporter(gvrContext, nativeValue);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Imports a 3D model from a file on the device's SD card. The application
     * must have read permission for the directory containing the file.
     * 
     * Does not check that file exists and is readable by this process: the only
     * public caller does that check.
     * 
     * @param gvrContext
     *            Context to import file from.
     * @param filename
     *            Name of the file to import.
     * @return An instance of {@link GVRAssimpImporter}.
     */
    static GVRAssimpImporter readFileFromSDCard(GVRContext gvrContext,
            String filename, EnumSet<GVRImportSettings> settings) {
        long nativeValue = NativeImporter.readFileFromSDCard(filename, GVRImportSettings.getAssimpImportFlags(settings));
        return new GVRAssimpImporter(gvrContext, nativeValue);
    }
}

class NativeImporter {
    static native long readFileFromAssets(AssetManager assetManager,
            String filename, int settings);

    static native long readFileFromSDCard(String filename, int settings);

    static native long readFromByteArray(byte[] bytes, String filename, int settings);
}
