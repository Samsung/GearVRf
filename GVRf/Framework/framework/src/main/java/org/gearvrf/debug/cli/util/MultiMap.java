/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli.util;

import java.util.Collection;
import java.util.Set;

/**
 * This is an extension to Java Collections framework.
 * MultiMap is a map which can contain multiple values under the same key.
 * @author ASG
 */
public interface MultiMap<K, V> {

    void put(K key, V value);

    void putAll(MultiMap<K, V> map);

    Collection<V> get(K key);

    Set<K> keySet();

    void remove(K key, V value);

    void removeAll(K key);

    /**
     * @return total size of all value collections in the MultiMap.
     */
    int size();
    
}
