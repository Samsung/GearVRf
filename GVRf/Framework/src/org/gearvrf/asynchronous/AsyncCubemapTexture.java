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

package org.gearvrf.asynchronous;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAndroidResource.CancelableCallback;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCubemapTexture;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.asynchronous.Throttler.AsyncLoader;
import org.gearvrf.asynchronous.Throttler.AsyncLoaderFactory;
import org.gearvrf.asynchronous.Throttler.GlConverter;
import org.gearvrf.utility.FileNameUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Async resource loading: cube map textures.
 * 
 * Since ZipInputStream does not support mark() and reset(), we directly use
 * BitmapFactory .decodeStream() in loadResource().
 * 
 * @since 1.6.9
 */
abstract class AsyncCubemapTexture {

    /*
     * The API
     */

    static void loadTexture(GVRContext gvrContext,
            CancelableCallback<GVRTexture> callback,
            GVRAndroidResource resource, int priority, Map<String, Integer> map) {
        faceIndexMap = map;
        Throttler.registerCallback(gvrContext, TEXTURE_CLASS, callback,
                resource, priority);
    }

    /*
     * Static constants
     */

    // private static final String TAG = Log.tag(AsyncCubemapTexture.class);

    private static final Class<? extends GVRHybridObject> TEXTURE_CLASS = GVRCubemapTexture.class;

    /*
     * Asynchronous loader for uncompressed cubemap
     */

    private static class AsyncLoadCubemapTextureResource extends
            AsyncLoader<GVRCubemapTexture, Bitmap[]> {

        private static final GlConverter<GVRCubemapTexture, Bitmap[]> sConverter = new GlConverter<GVRCubemapTexture, Bitmap[]>() {

            @Override
            public GVRCubemapTexture convert(GVRContext gvrContext,
                    Bitmap[] bitmapArray) {
                return new GVRCubemapTexture(gvrContext, bitmapArray);
            }
        };

        protected AsyncLoadCubemapTextureResource(GVRContext gvrContext,
                GVRAndroidResource request,
                CancelableCallback<GVRHybridObject> callback, int priority) {
            super(gvrContext, sConverter, request, callback);
        }

        @Override
        protected Bitmap[] loadResource() {
            Bitmap[] bitmapArray = new Bitmap[6];
            ZipInputStream zipInputStream = new ZipInputStream(
                    resource.getStream());

            try {
                ZipEntry zipEntry = null;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String imageName = zipEntry.getName();
                    String imageBaseName = FileNameUtils.getBaseName(imageName);
                    Integer imageIndex = faceIndexMap.get(imageBaseName);
                    if (imageIndex == null) {
                        throw new IllegalArgumentException("Name of image ("
                                + imageName + ") is not set!");
                    }
                    bitmapArray[imageIndex] = BitmapFactory
                            .decodeStream(zipInputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            resource.closeStream();
            return bitmapArray;
        }
    }

    static {
        Throttler.registerDatatype(TEXTURE_CLASS,
                new AsyncLoaderFactory<GVRCubemapTexture, Bitmap[]>() {

                    @Override
                    AsyncLoadCubemapTextureResource threadProc(
                            GVRContext gvrContext, GVRAndroidResource request,
                            CancelableCallback<GVRHybridObject> callback,
                            int priority) {
                        return new AsyncLoadCubemapTextureResource(gvrContext,
                                request, callback, priority);
                    }
                });
    }

    private static Map<String, Integer> faceIndexMap;
}
