package org.gearvrf.widgetlib.thread;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ConcurrentObjectPool} with some specializations to support {@linkplain List lists}. Hook
 * methods are provided to enable per-item processing when acquiring and releasing {@code Lists}.
 */
public class ConcurrentListPool<T> extends ConcurrentObjectPool<List<T>> {
    public ConcurrentListPool(String name) {
        super(name);
    }

    /**
     * Acquire a {@link List} from the pool, copying items from another {@code List} into it.
     * <p>
     *     <em>NOTE:</em> Although the pool itself is thread-safe, no guarantees are made for
     *     {@code Lists} passed to this method.  Thread-safety for those {@code Lists} is the
     *     responsibility of the caller.
     * </p>
     *
     * @param rhs {@code List} to copy into the newly acquired {@code List}.
     * @return A new {@code List} from the pool with {@code rhs} items copied into it.
     */
    public List<T> acquire(List<T> rhs) {
        List<T> result = acquire();
        for (T t : rhs) {
            result.add(acquireItem(t));
        }
        return result;
    }

    @Override
    public void release(List<T> list) {
        for (T t : list) {
            releaseItem(t);
        }
        list.clear();
        super.release(list);
    }

    @Override
    protected List<T> create() {
        return new ArrayList<>();
    }

    /**
     * Hook method for processing {@link List} items being copied into a newly
     * {@linkplain #acquire(List) acquired} {@code List}. This is called for each item immediately
     * before it is added to the new {@code List}.
     *
     * @param item An object being copied into a newly {@linkplain #acquire(List) acquired}
     *             {@code List}.
     * @return An object to be added to the new {@code List}. Can be the item passed in or a
     * different item.
     */
    protected T acquireItem(T item) {
        return item;
    }

    /**
     * Hook method for processing items in a {@link List} being {@linkplain #release(List) released}.
     * This is called for each item immediately before the {@code List} is
     * {@linkplain List#clear() cleared} and {@linkplain ConcurrentObjectPool#release(Object)
     * released} back into the pool.
     *
     * @param item An object in a releasing {@code List}.
     */
    protected void releaseItem(T item) {

    }
}
