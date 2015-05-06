package org.gearvrf;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This is a wrapper to convert T to Future<T> so that we do not need many
 * versions of constructor for {@link GVRSceneObject} for different combinations
 * of {@link GVRMesh} or Future<{@link GVRMesh}>, {@link GVRTexture} or Future<
 * {@link GVRTexture}>.
 * 
 * @param <V>
 *            Internal type to be wrapped. It can be either mesh or texture.
 */
public class FutureWrapper<V> implements Future<V> {

    private final V value_;

    public FutureWrapper(V value) {
        value_ = value;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return value_;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        return value_;
    }
}
