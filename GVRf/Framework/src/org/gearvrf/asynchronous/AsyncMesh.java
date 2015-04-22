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
import org.gearvrf.GVRHybridObject;
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
abstract class AsyncMesh {

    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(AsyncMesh.class);

    /*
     * The API
     */

    static void loadMesh(GVRContext gvrContext, CancelableCallback<GVRMesh> callback,
            GVRAndroidResource resource, int priority) {
        Throttler.registerCallback(gvrContext, MESH_CLASS, callback, resource,
                priority);
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
                CancelableCallback<GVRHybridObject> callback, int priority) {
            super(gvrContext, sConverter, request, callback);
        }

        @Override
        protected GVRMesh loadResource() throws InterruptedException {
            return gvrContext.loadMesh(resource);
        }
    }

    private static final Class<? extends GVRHybridObject> MESH_CLASS = GVRMesh.class;

    static {
        Throttler.registerDatatype(MESH_CLASS,
                new AsyncLoaderFactory<GVRMesh, GVRMesh>() {

                    @Override
                    AsyncLoader<GVRMesh, GVRMesh> threadProc(
                            GVRContext gvrContext, GVRAndroidResource request,
                            CancelableCallback<GVRHybridObject> callback,
                            int priority) {
                        return new AsyncLoadMesh(gvrContext, request, callback,
                                priority);
                    }
                });
    }

}
