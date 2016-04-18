/* Copyright 2016 Samsung Electronics Co., LTD
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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRAndroidResource.CancelableCallback;
import org.gearvrf.asynchronous.Throttler.AsyncLoader;
import org.gearvrf.asynchronous.Throttler.AsyncLoaderFactory;
import org.gearvrf.asynchronous.Throttler.GlConverter;

import org.gearvrf.utility.Log;

/**
 * Async resource loading: compressed textures.
 * 
 */

class AsyncCompressedTexture {

    /*
     * The API
     */
    static void loadTexture(GVRContext gvrContext,
            CancelableCallback<GVRCompressedTexture> callback,
            GVRAndroidResource resource, int priority) {
        AsyncManager.get().getScheduler().registerCallback(gvrContext,
                TEXTURE_CLASS, callback, resource, priority);
    }

    /*
     * Singleton
     */

    private static final Class<GVRCompressedTexture> TEXTURE_CLASS = GVRCompressedTexture.class;
    
    private static AsyncCompressedTexture sInstance = new AsyncCompressedTexture();

    /**
     * Gets the {@link AsynCompressedTexture} singleton for loading compressed textures.
     * 
     * @return The {@link AsynCompressedTexture} singleton.
     */
    public static AsyncCompressedTexture get() {
        return sInstance;
    }

    private AsyncCompressedTexture() {
        AsyncManager.get().registerDatatype(TEXTURE_CLASS,
                new AsyncLoaderFactory<GVRCompressedTexture, CompressedTexture>() {
                    @Override
                    AsyncLoader<GVRCompressedTexture, CompressedTexture> threadProc(
                            GVRContext gvrContext, GVRAndroidResource request,
                            CancelableCallback<GVRCompressedTexture> callback,
                            int priority) {
                        return new AsyncLoadTextureResource(gvrContext, request,
                                callback, priority);
                    }
                });
    }

    /*
     * Asynchronous loader
     */

    private static class AsyncLoadTextureResource
            extends AsyncLoader<GVRCompressedTexture, CompressedTexture> {

        private static final GlConverter<GVRCompressedTexture, CompressedTexture> sConverter = new GlConverter<GVRCompressedTexture, CompressedTexture>() {

            @Override
            public GVRCompressedTexture convert(GVRContext gvrContext,
                    CompressedTexture compressedTexture) {
                return compressedTexture == null ? null
                        : compressedTexture.toTexture(gvrContext,
                                GVRCompressedTexture.BALANCED);
            }
        };

        protected AsyncLoadTextureResource(GVRContext gvrContext,
                GVRAndroidResource request,
                CancelableCallback<GVRCompressedTexture> callback,
                int priority) {
            super(gvrContext, sConverter, request, callback);
        }

        @Override
        protected CompressedTexture loadResource() {
            GVRCompressedTextureLoader loader = resource.getCompressedLoader();
            CompressedTexture compressedTexture = null;
            try {
                compressedTexture = CompressedTexture
                        .parse(resource.getStream(), false, loader);
                Log.d("ASYNC", "parse compressed texture %s", resource);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                resource.closeStream();
            }
            return compressedTexture;
        }
    }
}
