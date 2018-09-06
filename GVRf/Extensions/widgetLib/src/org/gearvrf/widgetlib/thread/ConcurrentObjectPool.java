package org.gearvrf.widgetlib.thread;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.gearvrf.widgetlib.log.Log;

/**
 * A low-feature, thread-safe object pool. Override {@link #create()} to
 * allocate new objects as necessary.
 *
 * @param <T>
 *            The type of object held by the pool.
 */
public abstract class ConcurrentObjectPool<T> {
    // @formatter:off
    /*
     * Potential enhancements, should they be necessary:
     * - Time-based object expiration
     * - Hook for object clean-up on expiration
     * - Hook for object validation on acquire()
     */
    // @formatter:on
    public ConcurrentObjectPool(final String name) {
        this.name = name;
    }

    /**
     * Get an object from the pool. Allocates a new instance (via
     * {@link #create()}) if the pool is empty.
     *
     * @return An instance of type {@code T}.
     */
    public T acquire() {
        T t = pool.poll();
        if (t == null) {
            t = create();
            ++count;
        }
        ++requests;
        // log();
        return t;
    }

    /**
     * Return an object to the pool.
     *
     * @param t
     *            An instance of type {@code T}
     */
    public void release(T t) {
        pool.add(t);
    }

    abstract protected T create();

    @SuppressWarnings("unused")
    private void log() {
        Log.d(TAG, "%s: count: %d, requests: %d", name, count, requests);
    }

    private ConcurrentLinkedQueue<T> pool = new ConcurrentLinkedQueue<T>();
    private long count;
    private long requests;
    private final String name;

    private static final String TAG = ConcurrentObjectPool.class
            .getSimpleName();
}
