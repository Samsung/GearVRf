
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
