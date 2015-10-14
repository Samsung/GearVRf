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

package org.gearvrf.keyboard.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class ListWrapper<T> implements List<T> {
    protected final List<T> wrapped;

    public ListWrapper(List<T> wrapped) {
        this.wrapped = wrapped;
    }

    public void add(int location, T object) {
        wrapped.add(location, object);
    }

    public boolean add(T object) {
        return wrapped.add(object);
    }

    public boolean addAll(Collection<? extends T> arg0) {
        return wrapped.addAll(arg0);
    }

    public boolean addAll(int arg0, Collection<? extends T> arg1) {
        return wrapped.addAll(arg0, arg1);
    }

    public void clear() {
        wrapped.clear();
    }

    public boolean contains(Object object) {
        return wrapped.contains(object);
    }

    public boolean containsAll(Collection<?> arg0) {
        return wrapped.containsAll(arg0);
    }

    public boolean equals(Object object) {
        return wrapped.equals(object);
    }

    public int hashCode() {
        return wrapped.hashCode();
    }

    public int indexOf(Object object) {
        return wrapped.indexOf(object);
    }

    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    public Iterator<T> iterator() {
        return wrapped.iterator();
    }

    public int lastIndexOf(Object object) {
        return wrapped.lastIndexOf(object);
    }

    public ListIterator<T> listIterator() {
        return wrapped.listIterator();
    }

    public ListIterator<T> listIterator(int location) {
        return wrapped.listIterator(location);
    }

    public T remove(int location) {
        return wrapped.remove(location);
    }

    public boolean remove(Object object) {
        return wrapped.remove(object);
    }

    public boolean removeAll(Collection<?> arg0) {
        return wrapped.removeAll(arg0);
    }

    public boolean retainAll(Collection<?> arg0) {
        return wrapped.retainAll(arg0);
    }

    public T set(int location, T object) {
        return wrapped.set(location, object);
    }

    public int size() {
        return wrapped.size();
    }

    public List<T> subList(int start, int end) {
        return wrapped.subList(start, end);
    }

    public Object[] toArray() {
        return wrapped.toArray();
    }

    public <T> T[] toArray(T[] array) {
        return wrapped.toArray(array);
    }

}
