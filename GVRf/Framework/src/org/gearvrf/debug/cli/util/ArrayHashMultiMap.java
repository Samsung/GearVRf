/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ASG
 */
public final class ArrayHashMultiMap<K, V> implements MultiMap<K, V> {

    private Map<K, List<V>> listMap;

    public ArrayHashMultiMap() {
        listMap = new HashMap<K, List<V>>();
    }

    public ArrayHashMultiMap(MultiMap<K, V> map) {
        this();
        putAll(map);
    }

    public void put(K key, V value) {
        List<V> values = listMap.get(key);
        if (values == null) {
            values = new ArrayList<V>();
            listMap.put(key, values);
        }
        values.add(value);
    }

    public Collection<V> get(K key) {
        List<V> result = listMap.get(key);
        if (result == null) {
            result = new ArrayList<V>();
        }
        return result;
    }

    public Set<K> keySet() {
        return listMap.keySet();
    }

    public void remove(K key, V value) {
        List<V> values = listMap.get(key);
        if (values != null) {
            values.remove(value);
            if (values.isEmpty()) {
                listMap.remove(key);
            }
        }
    }

    public void removeAll(K key) {
        listMap.remove(key);
    }

    public int size() {
        int sum = 0;
        for (K key : listMap.keySet()) {
            sum += listMap.get(key).size();
        }
        return sum;
    }

    public void putAll(MultiMap<K, V> map) {
        for (K key : map.keySet()) {
            for (V val : map.get(key)) {
                put(key, val);
            }
        }
    }

    @Override
    public String toString() {
        return listMap.toString();
    }



}
