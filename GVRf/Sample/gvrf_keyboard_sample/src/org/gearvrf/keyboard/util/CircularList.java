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

import java.util.List;

public class CircularList<T> extends ListWrapper<T> {
    private List<T> list;

    public CircularList(List<T> list) {
        super(list);
        this.list = list;
    }

    public List<T> getList() {
        return list;
    }

    public T get(int index) {
        return wrapped.get(index);
    }

    public T getNext(int currentIndex) {
        return wrapped.get(getNextPosition(currentIndex));
    }

    public T getPrevious(int currentIndex) {
        return wrapped.get(getPreviousPosition(currentIndex));
    }

    public int getNextPosition(int currentIndex) {

        int position = (currentIndex + 1) % wrapped.size();

        return position;
    }

    public int getPreviousPosition(int currentIndex) {

        int position = (currentIndex - 1) % wrapped.size();

        if (position <= -1) {
            position = wrapped.size() + position;
        }

        return position;
    }

}
