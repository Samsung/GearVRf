package org.gearvrf.asynchronous;

import java.io.IOException;
import java.util.Map;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAndroidResource.CancelableCallback;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.asynchronous.Throttler.AsyncLoader;
import org.gearvrf.asynchronous.Throttler.AsyncLoaderFactory;
import org.gearvrf.utility.Log;

/**
 * This is an implementation of the resource loading scheduler, which
 * blocks until each resource is loaded. It is mainly used as an example
 * of implementing a scheduler and for debugging purposes.
 */
public class SyncScheduler implements Scheduler {
    private static final String TAG = Log.tag(SyncScheduler.class);

    /*
     * Singleton
     */

    private static SyncScheduler mInstance;

    public static SyncScheduler get() {
        if (mInstance != null) {
            return mInstance;
        }

        synchronized (SyncScheduler.class) {
            mInstance = new SyncScheduler();
        }

        return mInstance;
    }

    private SyncScheduler() {
    }

    /*
     * Scheduler
     */

    @Override
    public <OUTPUT extends GVRHybridObject, INTER> void registerCallback(GVRContext gvrContext,
            Class<OUTPUT> outClass,
            CancelableCallback<OUTPUT> callback, GVRAndroidResource request,
            int priority) {
        @SuppressWarnings("unchecked")
        AsyncLoaderFactory<OUTPUT, INTER> factory = (AsyncLoaderFactory<OUTPUT, INTER>) getFactories().get(outClass);
        if (factory == null) {
            callback.failed(new IOException("Cannot find loader factory"), request);
            return;
        }

        AsyncLoader<OUTPUT, INTER> loader =
                factory.threadProc(gvrContext, request, callback, priority);

        // Run the loader synchronously
        loader.run();
    }

    private Map<Class<? extends GVRHybridObject>, AsyncLoaderFactory<? extends GVRHybridObject, ?>> getFactories() {
        return AsyncManager.get().getFactories();
    }
}
