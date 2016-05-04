package org.gearvrf.utility;

/**
 * Wrapper class to pass any data type by reference.
 *
 * @param <T> the type parameter.
 */
public class Cell<T> {
    T value;

    public Cell(T t) {
        value = t;
    }

    public void set(T t) {
        value = t;
    }

    public T get() {
        return value;
    }
}