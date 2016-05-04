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

import java.util.HashMap;
import java.util.Map;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.asynchronous.Throttler.AsyncLoaderFactory;

public class AsyncManager {
    private static AsyncManager sInstance = new AsyncManager();

    /**
     * Gets the asynchronous manager.
     * @return The asynchronous manager.
     */
    public static AsyncManager get() {
        return sInstance;
    }

    /**
     * Gets the current scheduler;
     * @return The current scheduler object.
     */
    public Scheduler getScheduler() {
        return mScheduler;
    }

    /**
     * Sets the resource loading scheduler.
     *
     * @param scheduler
     *         The scheduler object.
     */
    public void setScheduler(Scheduler scheduler) {
        mScheduler = scheduler;
    }

    /**
     * Loaders call this method to register themselves. This method can be called by
     * loaders provided by the application.
     *
     * @param textureClass
     *         The class the loader is responsible for loading.
     *
     * @param asyncLoaderFactory
     *         The factory object.
     */
    public void registerDatatype(Class<? extends GVRHybridObject> textureClass,
            AsyncLoaderFactory<? extends GVRHybridObject, ?> asyncLoaderFactory) {
        mFactories.put(textureClass, asyncLoaderFactory);
    }

    Map<Class<? extends GVRHybridObject>, AsyncLoaderFactory<? extends GVRHybridObject, ?>> getFactories() {
        return mFactories;
    }

    // The resource loading scheduler
    private Scheduler mScheduler;

    // Factories
    private Map<Class<? extends GVRHybridObject>, AsyncLoaderFactory<? extends GVRHybridObject, ?>> mFactories;

    private AsyncManager() {
        // Make the static field available before this constructor returns
        sInstance = this;

        mFactories = new HashMap<Class<? extends GVRHybridObject>, AsyncLoaderFactory<? extends GVRHybridObject, ?>>(); 

        // Setup default scheduler to Throttler
        mScheduler = Throttler.get();
    }
}
