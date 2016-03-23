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

import static org.gearvrf.utility.Threads.VERBOSE_SCHEDULING;
import static org.gearvrf.utility.Threads.threadId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAndroidResource.Callback;
import org.gearvrf.GVRAndroidResource.CancelableCallback;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMesh;
import org.gearvrf.utility.Exceptions;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.RuntimeAssertion;
import org.gearvrf.utility.Threads;
import org.gearvrf.utility.Threads.Cancelable;
import org.gearvrf.utility.Threads.ThreadLimiter;
import org.gearvrf.utility.Threads.ThreadPolicyProvider;

import android.graphics.Bitmap;
import android.util.SparseArray;

/**
 * Asynchronous, heterogeneous resource loading with integrated thread
 * throttling.
 * 
 * @since 1.6.2
 */
class Throttler implements Scheduler {
    /*
     * The API
     */

    @Override
    public <OUTPUT extends GVRHybridObject, INTER> void registerCallback(
            GVRContext gvrContext, Class<OUTPUT> outClass,
            CancelableCallback<OUTPUT> callback, GVRAndroidResource request,
            int priority) {
        requests.registerCallback(gvrContext, outClass, callback, request,
                priority);
    }

    /*
     * Static constants
     */

    private static final String TAG = Log.tag(Throttler.class);

    protected static final boolean RUNTIME_ASSERTIONS = Threads.RUNTIME_ASSERTIONS;
    protected static final boolean CHECK_ARGUMENTS = Threads.RUNTIME_ASSERTIONS;

    /*
     * static fields
     */

    private static final int CORE_COUNT = Runtime.getRuntime()
            .availableProcessors();

    /**
     * Max threads doing resource loads at any one time.
     * 
     * Set to CORE_COUNT-1 on the theory that we can't peg the CPU with decodes;
     * even though the GL thread is higher priority than our decode threads, it
     * could still have to wait for a long-running decode to time-out and be
     * suspended.
     */
    private static final int DECODE_THREAD_LIMIT = Math.max(CORE_COUNT - 1, 1);
    
    /*
     * Singleton
     */

    private static Throttler mInstance = new Throttler();

    public static Throttler get() {
        return mInstance;
    }

    private Throttler() {
    }

    /*
     * Extension points
     */

    /**
     * The GL part of async resource loading.
     * 
     * Two main things happen once an async resource load has started running.
     * The expensive part happens on a CPU background thread (<i>i.e.</i> not
     * the GUI thread and not the GL thread): we convert the stream to a Java
     * object. This may or may not be the GVRF type that we want: image loading
     * generates a {@link Bitmap} while mesh loading goes straight to a
     * {@link GVRMesh}. When we have an intermediate data type, we need to do a
     * (cheap) conversion on the GL thread before we can pass the GVRF type to
     * the app's callback.
     * 
     * <p>
     * This interface represents that conversion, if any. (When
     * {@code OUTPUT == INPUT}, the {@code convert()} function just returns its
     * {@code input} parameter.)
     * 
     * @param <OUTPUT>
     *            The GVRF type that we will pass to the app's callback
     * @param <INTERMEDIATE>
     *            The possibly different type that we got from the background
     *            thread
     */
    interface GlConverter<OUTPUT extends GVRHybridObject, INTERMEDIATE> {
        OUTPUT convert(GVRContext gvrContext, INTERMEDIATE input);
    }

    /**
     * This is the base class for the background resource loaders.
     * 
     * An {@link AsyncLoader} is-a {@link Runnable}, which runs on a background
     * thread. The {@link #run()} method calls {@link #loadResource()} which
     * does the actual work of reading the {@link GVRAndroidResource} stream and
     * converting it to the {@code INTERMEDIATE} type. If the load succeeds,
     * {@code run()} pushes a {@code Runnable} to the GL thread, which does any
     * needed conversions (like {@code Bitmap} to {@code GVRTGexture}) and then
     * calls the app's
     * {@link Callback#loaded(GVRHybridObject, GVRAndroidResource) loaded()}
     * callback from the GL thread. If the load throws an exception or returns
     * {@code null}, {@code run()} calls the app's
     * {@link Callback#failed(Throwable, GVRAndroidResource) failed()} callback,
     * from the background thread.
     * 
     * <p>
     * Descendants must implement {@link #loadResource()}.
     * 
     * @param <OUTPUT>
     *            The GVRF type, delivered to the app's
     *            {@link Callback#loaded(GVRHybridObject, GVRAndroidResource)
     *            loaded()} callback
     * @param <INTERMEDIATE>
     *            The type generated on the background thread by the
     *            {@link #loadResource()} method
     */
    static abstract class AsyncLoader<OUTPUT extends GVRHybridObject, INTERMEDIATE>
            implements Cancelable {

        protected final GVRContext gvrContext;
        protected final GVRAndroidResource resource;
        protected final GlConverter<OUTPUT, INTERMEDIATE> converter;
        protected final CancelableCallback<OUTPUT> callback;

        protected AsyncLoader(GVRContext gvrContext,
                GlConverter<OUTPUT, INTERMEDIATE> converter,
                GVRAndroidResource request,
                CancelableCallback<OUTPUT> callback) {
            this.gvrContext = gvrContext;
            this.converter = converter;
            this.resource = request;
            this.callback = callback;
        }

        @Override
        public void run() {
            INTERMEDIATE async = null;
            try {
                async = loadResource(); // load resource, on background thread
            } catch (Throwable t) {
                t.printStackTrace();
                async = null;
                callback.failed(t, resource);
            } finally {
                if (async != null) {
                    final INTERMEDIATE loadedResource = async;
                    try {
                        OUTPUT gvrfResource = converter.convert(gvrContext,
                                loadedResource);
                        callback.loaded(gvrfResource, resource);
                    } catch (Throwable t) {
                        // Catch converter errors
                        callback.failed(t, resource);
                    }
                } else {
                    // loadResource() returned null
                    callback.failed(null, resource);
                }
            }
        }

        @Override
        public boolean stillWanted() {
            return callback.stillWanted(resource);
        }

        /**
         * Reads {@link #resource}; returns a Java data type, which may need
         * conversion before being passed to the app's
         * {@link Callback#loaded(GVRHybridObject, GVRAndroidResource) loaded()}
         * callback
         * 
         * @return An Android or GVRF resource type
         * @throws InterruptedException
         */
        protected abstract INTERMEDIATE loadResource()
                throws InterruptedException;
    }

    /**
     * Generates actual {@link AsyncLoader} instances.
     * 
     * 'Type experts' ({@link AsyncBitmapTexture} and {@link AsyncMesh} call
     * {@link Throttler#registerDatatype(Class, AsyncLoaderFactory)} to
     * associate an {@link AsyncLoaderFactory} with a target {@code .class}
     * constant.
     * {@link Throttler#registerCallback(GVRContext, Class, CancelableCallback, GVRAndroidResource, int)}
     * uses the {@code .class} constant to find the right
     * {@link AsyncLoaderFactory} when it's time to actually run a request; it
     * creates an {@link AsyncLoaderFactory} and runs it on a
     * {@link Threads#spawn(Runnable) background thread.}
     * 
     * @param <OUTPUT>
     *            The GVRF type, delivered to the app's
     *            {@link Callback#loaded(GVRHybridObject, GVRAndroidResource)
     *            loaded()} callback
     * @param <INTERMEDIATE>
     *            The type generated on the background thread by the
     *            {@link AsyncLoader#loadResource()} method
     */
    static abstract class AsyncLoaderFactory<OUTPUT extends GVRHybridObject, INTERMEDIATE> {
        /** Create an AsyncLoader of the right type */
        abstract AsyncLoader<OUTPUT, INTERMEDIATE> threadProc(
                GVRContext gvrContext, GVRAndroidResource request,
                CancelableCallback<OUTPUT> cancelableCallback, int priority);
    }

    /*
     * Pending requests
     */

    // TODO I don't THINK we need to reset PendingRequests on restart, but I may
    // be wrong ....
    private final PendingRequests requests = new PendingRequests();

    /**
     * This is the 'heart' of the throttler.
     * 
     * It prevents redundant loads by maintaining a set of pending requests,
     * each of which has a list of callbacks. If a request comes in for a
     * pending load, the callback is simply added to the list. When a resource
     * is loaded, the object is passed to each callback on the list.
     * 
     * TODO No longer *needs* to be a nested class ... not clear the change is
     * worth the effort, though.
     */
    private static class PendingRequests {

        private static final String TAG = Log.tag(PendingRequests.class);

        private final Map<GVRAndroidResource, PendingRequest<? extends GVRHybridObject, ?>> pendingRequests =
                new ConcurrentHashMap<GVRAndroidResource, PendingRequest<? extends GVRHybridObject, ?>>();

        private Map<Class<? extends GVRHybridObject>, AsyncLoaderFactory<? extends GVRHybridObject, ?>> getFactories() {
            return AsyncManager.get().getFactories();
        }

        private final ThreadLimiter<PriorityCancelable> deviceThreadLimiter = new ThreadLimiter<PriorityCancelable>(
                DECODE_THREAD_LIMIT,
                new PriorityCancelingLifoThreadPolicyProvider(),
                /* Don't exceed DECODE_THREAD_LIMIT when a download gets wedged */
                Integer.MAX_VALUE);

        <OUTPUT extends GVRHybridObject, INTER> void registerCallback(GVRContext gvrContext,
                Class<OUTPUT> outClass,
                CancelableCallback<OUTPUT> callback,
                GVRAndroidResource request, int priority) {
            if (VERBOSE_SCHEDULING) {
                Log.d(TAG, "registerCallback(%s, %s)", request, callback);
            }
            if (RUNTIME_ASSERTIONS) {
                if (request == null) {
                    throw Exceptions
                            .IllegalArgument("request must not be null");
                }
                if (callback == null) {
                    throw Exceptions
                            .IllegalArgument("callback must not be null");
                }
            }

            ThreadLimiter<PriorityCancelable> threadLimiter = deviceThreadLimiter;

            synchronized (pendingRequests) {
                PendingRequest<OUTPUT, INTER> pending = (PendingRequest<OUTPUT, INTER>) pendingRequests
                        .get(request);

                if (pending != null) {
                    // There is already a request for this resource: add
                    // callback, and reschedule
                    pending.addCallback(callback, priority);
                    if (VERBOSE_SCHEDULING) {
                        Log.d(TAG, "Thread %d: rescheduling %s for request %s",
                                threadId(), pending, request);
                    }
                    threadLimiter.reschedule(pending);
                } else {
                    // There is no current request for this resource. Create a
                    // new PendingRequest, using a threadFactory to create the
                    // appropriate AsyncLoader.
                    request.openStream();

                    pending = new PendingRequest<OUTPUT, INTER>(gvrContext,
                            request, callback, priority, outClass);

                    pendingRequests.put(request, pending);

                    if (VERBOSE_SCHEDULING) {
                        Log.d(TAG, "Thread %d: spawning %s for request %s",
                                threadId(), pending, request);
                    }
                    threadLimiter.spawn(pending);
                }
            }
        }

        private class PendingRequest<OUTPUT extends GVRHybridObject, INTER> implements
                CancelableCallback<OUTPUT>, PriorityCancelable {

            private final String TAG = Log.tag(PendingRequest.class);

            private final int EMPTY_LIST = GVRContext.LOWEST_PRIORITY - 1;

            private final GVRAndroidResource request;
            private final List<CancelableCallback<OUTPUT>> callbacks = new ArrayList<CancelableCallback<OUTPUT>>(1);
            private final Cancelable cancelable;
            private int priority = EMPTY_LIST;
            private int highestPriority = priority;

            public PendingRequest(GVRContext gvrContext,
                    GVRAndroidResource request,
                    CancelableCallback<OUTPUT> callback,
                    int priority, Class<OUTPUT> outClass) {
                this.request = request;
                addCallback(callback, priority);
                updatePriority();

                @SuppressWarnings("unchecked")
                AsyncLoaderFactory<OUTPUT, INTER> factory =
                        (AsyncLoaderFactory<OUTPUT, INTER>) getFactories().get(outClass);

                cancelable = factory.threadProc(
                        gvrContext, request, PendingRequest.this, priority);
            }

            public void addCallback(CancelableCallback<OUTPUT> callback,
                    int priority) {
                callbacks.add(callback);
                if (priority > this.highestPriority) {
                    this.highestPriority = priority;
                }
            }

            @Override
            public void loaded(final OUTPUT gvrResource,
                    final GVRAndroidResource androidResource) {
                if (VERBOSE_SCHEDULING) {
                    Log.d(TAG, "%s loaded(%s, %s), thread %d: request %s",
                            this, gvrResource, androidResource, threadId(),
                            request);
                }

                // gvrResource may be null, if we caught an exception in
                // AsyncLoadImage.run)
                synchronized (pendingRequests) {
                    // TODO Auto-generated method stub
                    if (gvrResource != null) {
                        List<CancelableCallback<OUTPUT>> listeners = new ArrayList<CancelableCallback<OUTPUT>>();
                        /*
                         * Copy the list of listeners then clear it, so any
                         * requests that come in after we leave the sync block
                         * but before return will not spawn a new thread. We
                         * only delete the pendingRequests entry once we've
                         * notified everybody.
                         */
                        listeners.clear();
                        if (callbacks.isEmpty()) {
                            if (VERBOSE_SCHEDULING) {
                                Log.d(TAG,
                                        "ready(), thread %d: no callbacks for request %s",
                                        threadId(), request);
                            }
                        }
                        listeners.addAll(callbacks);
                        callbacks.clear();

                        for (CancelableCallback<OUTPUT> callback : listeners) {
                            /*
                             * Each callback in its own exception frame,
                             * 
                             * to minimize the damage.
                             */
                            try {
                                // Inform handler the resource has been
                                // loaded.
                                callback.loaded(gvrResource, androidResource);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (VERBOSE_SCHEDULING) {
                        Log.d(TAG,
                                "ready(), thread %d: clearing pending request for request %s",
                                threadId(), request);
                    }

                    @SuppressWarnings("unchecked")
                    PendingRequest<OUTPUT, INTER> removed = (PendingRequest<OUTPUT, INTER>) pendingRequests
                            .remove(request);

                    if (RUNTIME_ASSERTIONS) {
                        if (removed != PendingRequest.this) {
                            throw new RuntimeAssertion(
                                    "pendingRequests.remove(%s, parameters) removed %s, not %s",
                                    request, removed, this);
                        }
                    }
                }
            }

            @Override
            public void failed(Throwable t, GVRAndroidResource androidResource) {
                throw Exceptions
                        .RuntimeAssertion("Not expecting this to be called");
            }

            @Override
            public boolean stillWanted(GVRAndroidResource request) {
                for (CancelableCallback<OUTPUT> callback : callbacks) {
                    if (callback.stillWanted(request)) {
                        return true;
                    }
                }

                // else
                return false;
            }

            // PriorityCancelable

            @Override
            public void run() {
                cancelable.run();
            }

            @Override
            public boolean stillWanted() {
                List<CancelableCallback<OUTPUT>> canceled = new ArrayList<CancelableCallback<OUTPUT>>(
                        callbacks.size());
                for (CancelableCallback<OUTPUT> callback : callbacks) {
                    if (callback.stillWanted(request) != true) {
                        canceled.add(callback);
                    }
                }
                callbacks.removeAll(canceled);

                boolean cancel = callbacks.size() == 0;

                if (cancel) {
                    if (VERBOSE_SCHEDULING) {
                        Log.d(TAG, "Canceling %s, request %s", this, request);
                    }
                    @SuppressWarnings("unchecked")
                    PendingRequest<OUTPUT, INTER> removed = (PendingRequest<OUTPUT, INTER>) pendingRequests
                            .remove(request);
                    if (removed != this) {
                        throw new RuntimeAssertion("removed = %s, this = %s",
                                removed, this);
                    }
                }

                return cancel != true;

            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public void updatePriority() {
                priority = highestPriority;
            }
        }
    }

    private interface PriorityCancelable extends Cancelable {

        /**
         * The value that {@link #getPriority()} returns may be random until
         * {@link #updatePriority()} is called; once {@link #updatePriority()}
         * has been called the value that {@link #getPriority()} returns should
         * not change until {@link #updatePriority()} is called again.
         * 
         * This allows {@link ThreadPolicyProvider#reschedule(Cancelable)} to
         * detect that a group's priority has changed because another request
         * has been added.
         */
        void updatePriority();

        /**
         * Every request has a priority: the larger the number, the higher
         * priority. That is, {@link Throttler#LOWEST_PRIORITY} is a negative
         * number, while {@link Throttler#HIGHEST_PRIORITY} is a positive
         * number.
         */
        int getPriority();
    }

    private static class PriorityGroup implements Comparable<PriorityGroup> {
        private int priority;
        final List<PriorityCancelable> content = new ArrayList<PriorityCancelable>();

        public PriorityGroup(int priority) {
            this.priority = priority;
        }

        @Override
        public int compareTo(PriorityGroup another) {
            // Reverse order: Integer.MAX_VALUE is a higher priority than
            // Integer.MIN_VALUE
            return another.priority - priority;
        }
    }

    private static class PriorityCancelingLifoThreadPolicyProvider implements
            ThreadPolicyProvider<PriorityCancelable> {

        private final PriorityQueue<PriorityGroup> queue = new PriorityQueue<PriorityGroup>();
        private final SparseArray<PriorityGroup> groups = new SparseArray<PriorityGroup>();

        private void addGroup(int priority, PriorityGroup newGroup) {
            queue.add(newGroup);
            groups.put(priority, newGroup);
        }

        private void removeGroup(PriorityGroup group) {
            queue.remove(group);
            groups.delete(group.priority);
        }

        @Override
        public void put(PriorityCancelable procedure) {
            int priority = procedure.getPriority();

            PriorityGroup group = groups.get(priority);
            if (group != null) {
                group.content.add(procedure);
                return;
            }

            PriorityGroup newGroup = new PriorityGroup(priority);
            newGroup.content.add(procedure);
            addGroup(priority, newGroup);
        }

        @Override
        public boolean isEmpty() {
            while (queue.size() > 0) {
                PriorityGroup first = queue.peek();

                List<Cancelable> cancel = new ArrayList<Cancelable>();
                for (Cancelable cancelable : first.content) {
                    // Find cancelable requests
                    if (cancelable.stillWanted() != true) {
                        cancel.add(cancelable);
                    }
                }
                first.content.removeAll(cancel);

                if (first.content.size() > 0) {
                    return false;
                }

                // remove empty group
                removeGroup(first);
            }

            return true;
        }

        @Override
        public Runnable get() {
            PriorityGroup first = queue.peek();
            if (first == null) {
                return null;
            }

            List<PriorityCancelable> content = first.content;
            Cancelable threadProc = content.remove(content.size() - 1);
            if (content.isEmpty()) {
                removeGroup(first);
            }
            return threadProc;
        }

        @Override
        public void reschedule(PriorityCancelable threadProc) {
            int priority = threadProc.getPriority();
            threadProc.updatePriority();
            int newPriority = threadProc.getPriority();

            PriorityGroup group = groups.get(priority);
            if (group != null) {
                if (group.content.remove(threadProc)) {
                    if (priority == newPriority) {

                        group.content.add(threadProc);
                    } else {
                        put(threadProc);
                    }
                    if (group.content.isEmpty()) {
                        removeGroup(group);
                    }
                } else if (VERBOSE_SCHEDULING) {
                    Log.d(TAG,
                            "reschedule() didn't find %s - it must be running (or have already run)",
                            threadProc);
                }
                return;
            }
        }
    }
}
