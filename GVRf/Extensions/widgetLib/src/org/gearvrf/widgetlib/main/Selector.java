package org.gearvrf.widgetlib.main;

/**
 * A selection (or filter) function. Useful for defining selection methods that can be used with
 * various objects.}.
 *
 * @param <T> The type that the {@code Selector} operates on.
 */
public interface Selector<T> {
    /**
     * The selection method.
     * @param t Object to perform selection on.
     * @return {@True} if the object {@code t} was selected, {@code false} if it was rejected.
     */
    boolean select(T t);
}
