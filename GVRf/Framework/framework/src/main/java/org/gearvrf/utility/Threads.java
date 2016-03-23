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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Threading utilities: thread pool, thread limiter, and some miscellany. */
public abstract class Threads {
    private static final String TAG = Log.tag(Threads.class);

    /** Lots of info to debug scheduling issues */
    public static final boolean VERBOSE_SCHEDULING = false;

    public static final boolean RUNTIME_ASSERTIONS = true;

    /**
     * {@link #spawn(Runnable)} priority: A normal background thread, running at
     * a lower priority than the GUI.
     */
    private static final int BACKGROUND_THREAD_PRIORITY = (Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2;

    /**
     * {@link #spawnLow(Runnable)} priority: Less than
     * BACKGROUND_THREAD_PRIORITY but more than IDLE_THREAD_PRIORITY
     */
    private static final int LOW_BACKGROUND_THREAD_PRIORITY = BACKGROUND_THREAD_PRIORITY - 1;

    /** {@link #spawnIdle(Runnable) priority: Lowest priority background task */
    private static final int IDLE_THREAD_PRIORITY = Thread.MIN_PRIORITY;

    /**
     * {@link #spawnHigh(Runnable)} priority: Higher than a normal
     * BACKGROUND_THREAD_PRIORITY, but still lower priority than the GUI.
     */
    private static final int HIGH_BACKGROUND_THREAD_PRIORITY = BACKGROUND_THREAD_PRIORITY + 1;

    /**
     * This is the default timeout (in milliseconds) when you create a
     * {@link ThreadLimiter#ThreadLimiter(int, ThreadPolicyProvider)
     * ThreadLimiter} without an
     * {@link ThreadLimiter#ThreadLimiter(int, ThreadPolicyProvider, int)
     * explicit slow-thread-timeout value.}
     */
    private static final int DEFAULT_SLOW_THREAD_TIMEOUT = 10000;

    /*
     * Thread pool
     */

    /**
     * Execute a Runnable on a thread pool thread
     *
     * @param priority
     *            The thread priority. Be careful with this! <blockquote>
     *            <b>Note:</b> Use Thread.MIN_PRIORITY..Thread.MAX_PRIORITY
     *            (1..10), <b>not</b> the Process/Linux -20..19 range!
     *            </blockquote>
     * @param threadProc
     *            The code to run. It doesn't matter if this code never returns
     *            - by using spawn (and, hence the thread pool) there is at
     *            least a chance that you will be reusing a thread, thus saving
     *            teardown/startup costs.
     *
     * @return A Future<?> that lets you wait for thread completion, if
     *         necessary
     */
    private static Future<?> spawn(final int priority, final Runnable threadProc) {
        return threadPool.submit(new Runnable() {

            @Override
            public void run() {
                Thread current = Thread.currentThread();
                int defaultPriority = current.getPriority();

                try {
                    current.setPriority(priority);

                    /*
                     * We yield to give the foreground process a chance to run.
                     * This also means that the new priority takes effect RIGHT
                     * AWAY, not after the next blocking call or quantum
                     * timeout.
                     */
                    Thread.yield();

                    try {
                        threadProc.run();
                    } catch (Exception e) {
                        logException(TAG, e);
                    }
                } finally {
                    current.setPriority(defaultPriority);
                }

            }

        });
    }

    /**
     * Execute a Runnable on a thread pool thread, at low (but not MIN_PRIORITY)
     * priority. (We have a hierarchy of spawn calls: <b>spawn()</b> starts a
     * background task that runs at a lower priority than the GUI; spawnLow()
     * starts a task that runs at a lower priority than spawn(); while
     * spawnIdle() starts a task that runs at the lowest possible priority.
     * Additionally, spawnHigh() - which should be used very sparingly - starts
     * a background task that runs at a higher priority than other background
     * tasks, though still lower than the GUI.)
     *
     * @param threadProc
     *            The code to run. It doesn't matter if this code never returns
     *            - by using spawn (and, hence the thread pool) there is at
     *            least a chance that you will be reusing a thread, thus saving
     *            teardown/startup costs.
     *
     * @return A Future<?> that lets you wait for thread completion, if
     *         necessary
     */
    public static Future<?> spawn(final Runnable threadProc) {
        return spawn(BACKGROUND_THREAD_PRIORITY, threadProc);
    }

    /**
     * Execute a Runnable on a thread pool thread, at lower priority than
     * spawn() but higher priority than spawnIdle(). (We have a hierarchy of
     * spawn calls: spawn() starts a background task that runs at a lower
     * priority than the GUI; <b>spawnLow()</b> starts a task that runs at a
     * lower priority than spawn(); while spawnIdle() starts a task that runs at
     * the lowest possible priority. Additionally, spawnHigh() - which should be
     * used very sparingly - starts a background task that runs at a higher
     * priority than other background tasks, though still lower than the GUI.)
     *
     * @param threadProc
     *            The code to run. It doesn't matter if this code never returns
     *            - by using spawn (and, hence the thread pool) there is at
     *            least a chance that you will be reusing a thread, thus saving
     *            teardown/startup costs.
     *
     * @return A Future<?> that lets you wait for thread completion, if
     *         necessary
     */
    public static Future<?> spawnLow(final Runnable threadProc) {
        return spawn(LOW_BACKGROUND_THREAD_PRIORITY, threadProc);
    }

    /**
     * Execute a Runnable on a thread pool thread, at the lowest possible
     * priority. (We have a hierarchy of spawn calls: spawn() starts a
     * background task that runs at a lower priority than the GUI; spawnLow()
     * starts a task that runs at a lower priority than spawn(); while
     * <b>spawnIdle()</b> starts a task that runs at the lowest possible
     * priority. Additionally, spawnHigh() - which should be used very sparingly
     * - starts a background task that runs at a higher priority than other
     * background tasks, though still lower than the GUI.)
     *
     * @param threadProc
     *            The code to run. It doesn't matter if this code never returns
     *            - by using spawn (and, hence the thread pool) there is at
     *            least a chance that you will be reusing a thread, thus saving
     *            teardown/startup costs.
     *
     * @return A Future<?> that lets you wait for thread completion, if
     *         necessary
     */
    public static Future<?> spawnIdle(final Runnable threadProc) {
        return spawn(IDLE_THREAD_PRIORITY, threadProc);
    }

    /**
     * Execute a Runnable on a thread pool thread, at
     * HIGH_BACKGROUND_THREAD_PRIORITY, which is higher than other background
     * threads but still lower than the GUI thread. This is the top of the
     * spawn() hierarchy, and should be used with extreme caution: If every
     * background thread uses HIGH_BACKGROUND_THREAD_PRIORITY, it becomes sort
     * of meaningless. In moderation, though, this can be useful: For example,
     * on a cache miss, the Image.FileCache loads a Bitmap from disk, then
     * spawns a high priority background thread to do the actual save. The
     * initial ("load") thread continues before the new ("save") thread starts,
     * which allows it to deliver the loaded Bitmap to the GUI, without waiting
     * to write the downsized Bitmap to cache. Meanwhile, the new save thread
     * has higher priority than other load threads, so the new Bitmap doesn't
     * stay in memory for a long time.
     *
     * @param threadProc
     *            The code to run. It doesn't matter if this code never returns
     *            - by using spawn (and, hence the thread pool) there is at
     *            least a chance that you will be reusing a thread, thus saving
     *            teardown/startup costs.
     *
     * @return A Future<?> that lets you wait for thread completion, if
     *         necessary
     */
    public static Future<?> spawnHigh(final Runnable threadProc) {
        return spawn(HIGH_BACKGROUND_THREAD_PRIORITY, threadProc);
    }

    /**
     * Execute a <code>Callable</code> on a thread pool thread, at default
     * priority. This allows you to start a background thread, and later do a
     * <code><i>join()</i></code> (<i>i.e.</i>, block until the thread is done)
     * to get the thread's output.
     *
     * @param <T>
     *            The type-parameter of the Callable and the returned Future
     * @param callable
     *            The thread proc
     * @return A <code>Future&lt;T&gt;</code> that can be queried for the
     *         thread's result
     */
    public static <T> Future<T> spawn(Callable<T> callable) {
        return threadPool.submit(callable);
    }

    private static ExecutorService threadPool;

    /**
     * By default, the spawn() methods use their own
     * {@link Executors#newCachedThreadPool()}. This method allows you to use a
     * different thread pool, if the cached thread pool provides the wrong
     * semantics or if you want to use a single thread pool for the whole app.
     *
     * <p>
     * Note that calling this method will have <em>no effect</em> on any threads
     * started on the existing thread pool; they will run to completion in a
     * totally normal manner.
     */
    public static void setThreadPool(ExecutorService newThreadPool) {
        if (RUNTIME_ASSERTIONS) {
            if (newThreadPool == null) {
                throw Exceptions
                        .IllegalArgument("newThreadPool may not be null");
            }
        }

        threadPool = newThreadPool;
    }

    /**
     * The spawn() methods use a {@link Executors#newCachedThreadPool()}; this
     * method provides access to that thread pool, so that other parts of the
     * app can use the pool without having to call one of the spwan() methods.
     *
     * <p>
     * Note that any prior call to {@link #setThreadPool(ExecutorService)} will
     * affect the result of this method!
     */
    public static ExecutorService getThreadPool() {
        return threadPool;
    }

    /*
     * Thread limiters
     */

    /**
     * If you have a queue of Runnable-s, some may be stale by the time you can
     * get to them
     */
    public interface Cancelable extends Runnable {
        /** Should we still run() this? */
        boolean stillWanted();
    }

    /**
     * Manages pool of pending threads, deciding which should run next.
     *
     * Used in conjunction with a ThreadLimiter, which handles synchronization:
     * implementations generally don't need to pay attention to thread safety.
     */
    public interface ThreadPolicyProvider<CANCELABLE extends Cancelable> {
        /** Add a thread proc to the pool */
        void put(CANCELABLE threadProc);

        /**
         * Are there any thread procs in the pool?
         *
         * May use {@link Cancelable#stillWanted()} to trim pool.
         * */
        boolean isEmpty();

        /**
         * Returns the next thread proc. May return <code>null</code> or raise
         * exception if isEmpty() is true
         */
        Runnable get();

        /**
         * Make threadProc the next in the pool.
         *
         * <ul>
         * <li>If threadProc is already in the pool, move it to the front, so
         * get() returns it next.
         * <li>If threadProc is not in the pool, add it at the front, so get()
         * returns it next.
         * </ul>
         */
        void reschedule(CANCELABLE threadProc);
    }

    /**
     * Limits the number of (thread pool) threads running at any one time.
     *
     * A stand-alone class, as different subsystems may have different limits,
     * based on the resources their threads consume.
     *
     * Each subsystem that instantiates a thread limiter must supply an
     * implementation of {@link ThreadPolicyProvider}, or use the standard
     * {@link LifoThreadPolicyProvider}.
     */
    public static class ThreadLimiter<CANCELABLE extends Cancelable> {

        private static final String TAG = Log.tag(ThreadLimiter.class);

        private final int maxThreads;
        private final List<ThreadManager> threadManagers;
        private final ThreadPolicyProvider<CANCELABLE> policy;
        private final int slowThreadTimeout;

        /** Cheap way to find the next thread timeout */
        private final PriorityQueue<ThreadTimeouts> timeoutQueue;
        /**
         * Since most threads won't timeout, we need a way to remove
         * well-behaved threads from {@link #timeoutQueue}
         */
        private final Map<ThreadManager, ThreadTimeouts> timeoutMap;

        private final static int PUT = 0;
        private final static int RESCHEDULE = 1;

        /**
         * Create a new thread limiter, with a
         * {@link #DEFAULT_SLOW_THREAD_TIMEOUT default slow-thread timeout}.
         *
         * @param maxThreads
         *            Maximum number of threads that can run at once.
         * @param policy
         *            Manages pool of pending threads, deciding which should run
         *            next.
         */
        public ThreadLimiter(int maxThreads,
                ThreadPolicyProvider<CANCELABLE> policy) {
            this(maxThreads, policy, DEFAULT_SLOW_THREAD_TIMEOUT);
        }

        /**
         * Create a new thread limiter.
         *
         * @param maxThreads
         *            Maximum number of threads that can run at once.
         * @param policy
         *            Manages pool of pending threads, deciding which should run
         *            next.
         * @param slowThreadTimeout
         *            If we try to start a new thread and any thread has gone
         *            more than this many milliseconds since starting, we will
         *            create a new thread, and drop back down to maxThreads
         *            when/if the slow thread returns.
         */
        public ThreadLimiter(int maxThreads,
                ThreadPolicyProvider<CANCELABLE> policy, int slowThreadTimeout) {
            this.maxThreads = maxThreads;
            threadManagers = new ArrayList<ThreadManager>(maxThreads);
            for (int index = 0; index < maxThreads; ++index) {
                threadManagers.add(new ThreadManager());
            }
            this.policy = policy;
            this.slowThreadTimeout = slowThreadTimeout;

            // Use maxThreads as the initial capacity, because in many cases we
            // will never exceed that
            timeoutQueue = new PriorityQueue<ThreadTimeouts>(maxThreads);
            timeoutMap = new HashMap<ThreadManager, ThreadTimeouts>(maxThreads);
        }

        /**
         * Run a thread proc, on a thread from the system thread pool.
         *
         * Thread will run immediately, if possible. If the maximum number of
         * threads are already running, the thread proc will be added to a pool,
         * and scheduling will be determined by the ThreadPolicyProvider.
         */
        public void spawn(CANCELABLE threadProc) {
            handle(threadProc, PUT);
        }

        /**
         * Run immediately, if possible; else, schedule threadProc to run next.
         */
        public void reschedule(CANCELABLE threadProc) {
            handle(threadProc, RESCHEDULE);
        }

        private void handle(CANCELABLE threadProc, int policyOp) {
            ThreadManager threadManager = null;

            synchronized (threadManagers) {
                int size = threadManagers.size();

                if (size == 0 && policyOp == PUT) {
                    // Check whether we are in a slow-thread situation
                    long time = System.nanoTime();
                    ThreadTimeouts firstTimeout = timeoutQueue.peek();

                    // firstTimeout should never be null when size == 0!
                    if (time >= firstTimeout.timeout) {
                        // We have a thread that's run 'too long' so we'll
                        // create a new ThreadManager, which will lead us to
                        // grab another thread from the thread pool

                        // Logging slow thread recovery is not part of
                        // VERBOSE_SCHEDULING because we do want to know what
                        // things trigger it during normal development
                        log(TAG,
                                "Slow thread recovery: Creating a new ThreadManager");

                        timeoutQueue.poll(); // remove firstTimeout
                        timeoutMap.remove(firstTimeout.threadManager);
                        threadManagers.add(new ThreadManager());
                        size = threadManagers.size();
                    }
                }

                if (size > 0 && policyOp == PUT) {
                    // Grab a ThreadManager, and let it run the threadProc
                    threadManager = threadManagers.remove(size - 1);
                    if (RUNTIME_ASSERTIONS) {
                        if (size != threadManagers.size() + 1) {
                            throw new RuntimeAssertion(
                                    "WTF? We popped the last item off of threadManagers, and size() went from %d to %d",
                                    size, threadManagers.size());
                        }
                        if (threadManagers.indexOf(threadManager) >= 0) {
                            throw new RuntimeAssertion(
                                    "WTF? We just popped %s off of threadManagers - and it's still on the list!",
                                    threadManager);
                        }
                    }
                    if (VERBOSE_SCHEDULING) {
                        log(TAG,
                                "Thread %d: We have ThreadManager %s and are letting it run %s",
                                threadId(), threadManager, threadProc);
                    }
                } else {
                    // precondition: size == 0 || policyOp != PUT
                    // Update policy within synchronized block
                    switch (policyOp) {
                    case PUT:
                        // run it sometime later
                        if (VERBOSE_SCHEDULING) {
                            log(TAG,
                                    "Thread %d: Scheduling %s for later execution",
                                    threadId(), threadProc);
                        }
                        policy.put(threadProc);
                        break;

                    case RESCHEDULE:
                        // run it NEXT
                        if (VERBOSE_SCHEDULING) {
                            log(TAG,
                                    "Thread %d: Rescheduling %s for immediate execution",
                                    threadId(), threadProc);
                        }
                        policy.reschedule(threadProc);
                        break;
                    default:
                        throw new RuntimeAssertion("Unknown policy op %d",
                                policyOp);
                    }
                }
            }

            if (threadManager != null) {
                // run it now
                threadManager.setRunnable(threadProc);
                Threads.spawn(threadManager);
                if (VERBOSE_SCHEDULING) {
                    log(TAG, "spawn()ed %s to run %s", threadManager,
                            threadProc);
                }
            }
        }

        private class ThreadManager implements Runnable {

            private final String TAG = Log.tag(ThreadManager.class);

            public Runnable runnable;

            @Override
            public void run() {
                boolean workToDo = true;
                while (workToDo) {
                    if (VERBOSE_SCHEDULING) {
                        log(TAG, "Thread %d running %s", threadId(), runnable);
                    }

                    // Run the supplied Runnable
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logException(TAG, e);
                    }

                    if (VERBOSE_SCHEDULING) {
                        log(TAG, "Thread %d ran %s", threadId(), runnable);
                    }

                    // Run the NEXT Runnable from the pool, if any
                    synchronized (threadManagers) {
                        boolean slowThread = removeFromTimeouts() == false;
                        if (slowThread || policy.isEmpty()) {
                            // Thread should retire, or no more work for now
                            workToDo = false;
                            runnable = null;

                            if (RUNTIME_ASSERTIONS) {
                                if (threadManagers.indexOf(this) >= 0) {
                                    throw new RuntimeAssertion(
                                            "Thread %d trying to add %s to threadManagers, which already contains a reference to it!",
                                            threadId(), this);
                                }
                            }

                            if (threadManagers.size() < maxThreads) {
                                if (VERBOSE_SCHEDULING) {
                                    log(TAG,
                                            "Thread %d returning %s to available threadManagers pool",
                                            threadId(), this);
                                }
                                threadManagers.add(this);
                            } else {
                                log(TAG,
                                        "Slow thread recovery: threadManagers pool is full, so letting %s be garbage collected",
                                        this);
                            }
                        } else {
                            if (VERBOSE_SCHEDULING) {
                                log(TAG, "Thread %d getting a new threadProc",
                                        threadId());
                            }
                            setRunnable(policy.get());
                        }
                    }
                }
            }

            private void setRunnable(Runnable runnable) {
                this.runnable = runnable;

                // Add to timeout queue
                ThreadTimeouts timeout = new ThreadTimeouts(this,
                        slowThreadTimeout);

                synchronized (threadManagers) {
                    timeoutMap.put(this, timeout);
                    timeoutQueue.add(timeout);
                }
            }

            /**
             * Remove this thread from the timeout queue
             *
             * @return {@code true} if the thread was still in the queue;
             *         {@code false} if the thread has timed out
             */
            private boolean removeFromTimeouts() {
                ThreadTimeouts timeout = timeoutMap.remove(this);

                timeoutQueue.remove(timeout);

                // timeout will be null, if this thread has already timed out
                return timeout != null;
            }
        }

        private class ThreadTimeouts implements Comparable<ThreadTimeouts> {

            // 1000 * 1000 is easier to proof-read than 1000000
            private static final long NANOSECONDS_PER_MILLISECOND = 1000 * 1000;

            /** Units are {@link System#nanoTime()  */
            private final long timeout;
            private final ThreadManager threadManager;

            private ThreadTimeouts(ThreadManager threadManager,
                    int millisecondTimeout) {
                this.timeout = System.nanoTime() + millisecondTimeout
                        * NANOSECONDS_PER_MILLISECOND;
                this.threadManager = threadManager;
            }

            @Override
            public int compareTo(ThreadTimeouts other) {
                long difference = timeout - other.timeout;
                return (difference < 0) ? -1 : ((difference == 0) ? 0 : 1);
            }

        }
    }

    /**
     * Provides LIFO thread policy management: Most recently added thread proc
     * will run next. Appropriate for image galleries, say, where most recent
     * request is most likely to be visible.
     *
     * Never cancels a request!
     */
    public static class LifoThreadPolicyProvider implements
            ThreadPolicyProvider<Cancelable> {

        private static final String TAG = Log
                .tag(LifoThreadPolicyProvider.class);

        protected final List<Cancelable> threadProcs = new ArrayList<Cancelable>();

        @Override
        public void put(Cancelable threadProc) {
            if (VERBOSE_SCHEDULING) {
                log(TAG, "put() adding %s", threadProc);
            }
            threadProcs.add(threadProc);
        }

        @Override
        public boolean isEmpty() {
            if (VERBOSE_SCHEDULING) {
                log(TAG, "isEmpty(): %d thread procs in queue",
                        threadProcs.size());
            }
            return threadProcs.size() == 0;
        }

        @Override
        public Runnable get() {
            Runnable result = threadProcs.remove(threadProcs.size() - 1);
            if (VERBOSE_SCHEDULING) {
                log(TAG, "get() returning %s", result);
            }
            return result;
        }

        @Override
        public void reschedule(Cancelable threadProc) {
            if (threadProcs.remove(threadProc)) {
                if (VERBOSE_SCHEDULING) {
                    log(TAG, "reschedule() rescheduling %s", threadProc);
                }
                threadProcs.add(threadProc);
            } else {
                // else, threadProc has already been removed (or was never
                // added), and is running (or has run)
                if (VERBOSE_SCHEDULING) {
                    log(TAG,
                            "reschedule() didn't find %s - it must be running (or have already run)",
                            threadProc);
                }

            }
        }
    }

    /*
     * Miscellaneous thread utilities
     */

    /** Shorthand for <code>Thread.currentThread().getId()</code> */
    public static long threadId() {
        return Thread.currentThread().getId();
    }

    /**
     * This is an assertion method that can be used by a thread to confirm that
     * the thread isn't already holding lock for an object, before acquiring a
     * lock
     *
     * @param object
     *            object to test for lock
     * @param name
     *            tag associated with the lock
     */
    public static void assertUnlocked(Object object, String name) {
        if (RUNTIME_ASSERTIONS) {
            if (Thread.holdsLock(object)) {
                throw new RuntimeAssertion("Recursive lock of %s", name);
            }
        }
    }

    /*
     * Private methods
     */

    private static void logException(String tag, Throwable t) {
        Log.e(tag, "%s in thread %d", t, threadId());
        t.printStackTrace();
    }

    private static void log(String tag, String pattern, Object... parameters) {
        Log.d(tag, pattern, parameters);
    }

}
