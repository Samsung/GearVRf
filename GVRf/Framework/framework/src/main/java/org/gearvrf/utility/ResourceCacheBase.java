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

import org.gearvrf.GVRAndroidResource;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic cache-by-resource-description.
 *
 * Keeps system from reloading resources, so long as a previous copy is still in
 * memory.
 *
 * @since 2.0.2
 */
public class ResourceCacheBase<T> {
    private static final String TAG = Log.tag(ResourceCacheBase.class);

    private final Map<GVRAndroidResource, WeakReference<T>> cache //
            = new ConcurrentHashMap<GVRAndroidResource, WeakReference<T>>();

    /** Save a weak reference to the resource */
    public void put(GVRAndroidResource androidResource, T resource) {
        Log.d(TAG, "put resource %s to cache", androidResource);

        cache.put(androidResource, new WeakReference<T>(resource));
    }

    /** Get the cached resource, or {@code null} */
    public T get(GVRAndroidResource androidResource) {
        WeakReference<T> reference = cache.get(androidResource);
        if (reference == null) {
            // Not in map
            // Log.d(TAG, "get(%s) returning %s", androidResource, null);
            return null;
        }
        T cached = reference.get();
        if (cached == null) {
            // In map, but not in memory
            cache.remove(androidResource);
        } else {
            // No one will ever read this stream
            androidResource.closeStream();
        }
        // Log.d(TAG, "get(%s) returning %s", androidResource, cached);
        return cached;
    }
}
