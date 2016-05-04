/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ASG
 */
public class EmptyMultiMap<K, V> implements MultiMap<K, V> {

    public void put(K key, V value) {
        throw new UnsupportedOperationException("You can't modify EmptyMultyMap: it's always empty!");
    }

    public Collection<V> get(K key) {
        return new ArrayList<V>();
    }

    public Set<K> keySet() {
        return new HashSet<K>();
    }

    public void remove(K key, V value) {
        throw new UnsupportedOperationException("You can't modify EmptyMultyMap: it's always empty!");
    }

    public void removeAll(K key) {
        throw new UnsupportedOperationException("You can't modify EmptyMultyMap: it's always empty!");
    }

    public int size() {
        return 0;
    }

    public void putAll(MultiMap<K, V> map) {
        throw new UnsupportedOperationException("You can't modify EmptyMultyMap: it's always empty!");
    }

    @Override
    public String toString() {
        return "{}";
    }



}
