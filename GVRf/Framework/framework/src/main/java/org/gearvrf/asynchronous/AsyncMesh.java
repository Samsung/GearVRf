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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAndroidResource.CancelableCallback;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.asynchronous.Throttler.AsyncLoader;
import org.gearvrf.asynchronous.Throttler.AsyncLoaderFactory;
import org.gearvrf.asynchronous.Throttler.GlConverter;
import org.gearvrf.utility.Log;

/**
 * Async resource loading: meshes.
 * 
 * @since 1.6.2
 */
class AsyncMesh {

    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(AsyncMesh.class);

    /*
     * The API
     */

    void loadMesh(GVRContext gvrContext,
            CancelableCallback<GVRMesh> callback, GVRAndroidResource resource,
            int priority) {
        AsyncManager.get().getScheduler().registerCallback(gvrContext, MESH_CLASS, callback, resource,
                priority);
    }

    /*
     * Singleton
     */
    

    private static final Class<GVRMesh> MESH_CLASS = GVRMesh.class;

    private static AsyncMesh sInstance = new AsyncMesh();

    /**
     * Gets the {@link AsyncMesh} singleton for loading bitmap textures.
     * @return The {@link AsyncMesh} singleton.
     */
    public static AsyncMesh get() {
        return sInstance;
    }

    private AsyncMesh() {
        AsyncManager.get().registerDatatype(MESH_CLASS,
                new AsyncLoaderFactory<GVRMesh, GVRMesh>() {
                    @Override
                    AsyncLoader<GVRMesh, GVRMesh> threadProc(
                            GVRContext gvrContext,
                            GVRAndroidResource request,
                            CancelableCallback<GVRMesh> callback,
                            int priority) {
                        return new AsyncLoadMesh(gvrContext, request, callback, priority);
                    }
                });
    }

    /*
     * The implementation
     */

    private static class AsyncLoadMesh extends AsyncLoader<GVRMesh, GVRMesh> {
        static final GlConverter<GVRMesh, GVRMesh> sConverter = new GlConverter<GVRMesh, GVRMesh>() {

            @Override
            public GVRMesh convert(GVRContext gvrContext, GVRMesh mesh) {
                return mesh;
            }
        };

        AsyncLoadMesh(GVRContext gvrContext, GVRAndroidResource request,
                CancelableCallback<GVRMesh> callback, int priority) {
            super(gvrContext, sConverter, request, callback);
        }

        @Override
        protected GVRMesh loadResource() throws InterruptedException {
            return gvrContext.loadMesh(resource);
        }
    }
}
