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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A recycle bin is a place you can put buffers and other data structures that
 * you would just as soon not allocate again if you don't have to. It's not
 * keyed like a cache, though.
 * 
 * <p>
 * This class is a recycle bin generator: regular reference bins, soft reference
 * bins, and weak reference bins.
 * 
 * <p>
 * Regular references are sometimes called hard references. The garbage
 * collector looks at them, and keeps their referent in memory.
 * 
 * <p>
 * A soft reference is a special class that tells the garbage collector that it
 * can free the referent, if it has to, but you'd really rather it didn't. If
 * the garbage collector does free the referent, it will update the soft
 * reference, so you can always tell whether your soft cached item is still in
 * memory.
 * 
 * <p>
 * A weak reference is another special class. The garbage collector will free
 * its referent in the next garbage collection, just as if there were no
 * reference at all, but it will update the soft reference, so you can always
 * tell whether your weak cached item is still in memory.
 * 
 * @param <T>
 *            Item to bin.
 */
public abstract class RecycleBin<T> {

    /**
     * Get and remove an item from the bin, or return null.
     * 
     * @return Cached item, or null
     */
    public abstract T get();

    /**
     * Add an item to the bin. Bin makes no attempt to reset values.
     * 
     * @param item
     *            Data structure you could reuse
     */
    public abstract void put(T item);

    /** Returns a wrapper that synchronizes get and put operations */
    public RecycleBin<T> synchronize() {
        return new SynchronizedRecycleBin<T>(this);
    }

    /**
     * A hard reference bin. Being in this bin will keep an X from being freed.
     * 
     * @param <X>
     *            Type of data to bin
     * @return A hard RecycleBin<X>
     */
    public static <X> RecycleBin<X> hard() {
        return new RegularReferenceBin<X>();
    }

    /**
     * A soft reference bin. Being in this bin is a hint to the garbage
     * collector that we would really prefer that these X were not freed.
     * 
     * @param <X>
     *            Type of data to bin
     * @return A soft RecycleBin<X>
     */
    public static <X> RecycleBin<X> soft() {
        return new SpecialReferenceBin.Soft<X>();
    }

    /**
     * A weak reference bin. Being in this bin will not keep an X from being
     * freed at the next garbage collection.
     * 
     * @param <X>
     *            Type of data to bin
     * @return A weak RecycleBin<X>
     */
    public static <X> RecycleBin<X> weak() {
        return new SpecialReferenceBin.Weak<X>();
    }

    private static class RegularReferenceBin<T> extends RecycleBin<T> {

        private List<T> bin = new ArrayList<T>();

        @Override
        public T get() {
            int size = bin.size();
            if (size <= 0) {
                return null;
            } else {
                T item = bin.get(size - 1);
                bin.remove(size - 1);
                return item;
            }
        }

        @Override
        public void put(T item) {
            bin.add(item);
        }
    }

    private static abstract class SpecialReferenceBin<T> extends RecycleBin<T> {

        protected Set<Reference<T>> bin = new HashSet<Reference<T>>();

        @Override
        public T get() {
            T result = null;
            Reference<T> resultReference = null;
            List<Reference<T>> dereferenced = new ArrayList<Reference<T>>(
                    bin.size());

            for (Reference<T> reference : bin) {
                T referent = reference.get();
                if (referent != null) {
                    result = referent;
                    resultReference = reference;
                    break;
                } else {
                    dereferenced.add(reference);
                }
            }

            bin.removeAll(dereferenced);
            bin.remove(resultReference);
            return result;
        }

        private static class Soft<T> extends SpecialReferenceBin<T> {

            @Override
            public void put(T item) {
                bin.add(new SoftReference<T>(item));
            }

        }

        private static class Weak<T> extends SpecialReferenceBin<T> {

            @Override
            public void put(T item) {
                bin.add(new WeakReference<T>(item));
            }

        }

    }

    private static class SynchronizedRecycleBin<T> extends RecycleBin<T> {
        private final RecycleBin<T> bin;

        public SynchronizedRecycleBin(RecycleBin<T> bin) {
            this.bin = bin;
        }

        @Override
        public synchronized T get() {
            return bin.get();
        }

        @Override
        public synchronized void put(T item) {
            bin.put(item);

        }
    }
}
