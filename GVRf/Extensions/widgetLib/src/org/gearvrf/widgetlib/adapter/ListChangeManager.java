package org.gearvrf.widgetlib.adapter;

import java.util.ArrayList;
import java.util.List;

import android.util.LongSparseArray;

public class ListChangeManager {

    public enum ACTION_TYPE { ADD, DELETE, MOVE };

    public static class Action {
        public ACTION_TYPE  action;
        public long    id;
        public int     pos1;
        public int     pos2;

        Action(ACTION_TYPE action, long id, int pos1, int pos2) {
            this.action = action;
            this.id     = id;
            this.pos1   = pos1;
            this.pos2   = pos2;
        }
    }

    public ListChangeManager(Adapter adapter) {
        if (!adapter.hasStableIds()) {
            throw new RuntimeException("Adapter must have stable IDs");
        }
        mAdapter = adapter;
    }

    /**
     * getUpdateActions() is called inside RingList.onChanged(). i.e., when its data set
     * has changed and items needs to be reconstructed in a efficient way.
     *
     * Given the current state (the list of item IDs), this function returns a list of
     * actions to be performed sequentially in order to reach the desired new state.
     *
     * @param itemIDs: list of IDs starting from firstItemPos
     * @param firstItemPos: position of the first item.
     * @return a list of actions to be performed sequentially
     */
    public List<Action> getUpdateActions(List<Long> itemIDs, int firstItemPos) {
        List<Long> oldIDs = new ArrayList<Long>();
        final int count = mAdapter.getCount();
        for(int index = 0; index < count; index++) {
            long id = mAdapter.getItemId(index);
            oldIDs.add(Long.valueOf(id));
        }

        LongSparseArray<Integer> newMap = new LongSparseArray<Integer>();
        listToMap(itemIDs, newMap, firstItemPos);

        LongSparseArray<Integer> oldMap = new LongSparseArray<Integer>();

        final List<Action> actions = new ArrayList<Action>();

        synchronized(mSyncObject) {
            if (oldIDs != null) {
                listToMap(oldIDs, oldMap, 0);

                for(int pos = oldIDs.size() - 1; pos >= firstItemPos; pos--) {
                    Long id = oldIDs.get(pos);
                    int newPos = newMap.get(id, -1);
                    if (newPos == -1) {
                        oldIDs.remove(pos);
                        oldMap.delete(id);
                        updateMap(oldIDs, oldMap, pos, -1);
                        actions.add(new Action(ACTION_TYPE.DELETE, id, pos, -1));
                    }
                }
            }

            if (itemIDs != null) {
                for(int index = 0; index < itemIDs.size(); index++) {
                    final int pos = firstItemPos + index;
                    final Long id  = itemIDs.get(index);

                    int oldPos = oldMap.get(id, -1);
                    if (oldPos != -1) {
                        if (oldPos != pos) {
                            oldIDs.remove(oldPos);
                            oldIDs.add(pos, id);
                            int min = Math.min(oldPos, pos);
                            int max = Math.max(oldPos, pos);
                            updateMap(oldIDs, oldMap, min, max + 1);
                            actions.add(new Action(ACTION_TYPE.MOVE, id, oldPos, pos));
                        }
                    } else {
                        if (oldIDs != null) {
                            oldIDs.add(pos, id);
                            updateMap(oldIDs, oldMap, pos, -1);
                        }
                        actions.add(new Action(ACTION_TYPE.ADD, id, -1, pos));
                    }
                }
            }
        }

        return actions;
    }

    private static void updateMap(List<Long> list, LongSparseArray<Integer> map, int pos, int end) {
        if (end == -1) {
            end = list.size();
        }
        for(; pos < end; pos++) {
            Long id = list.get(pos);
            map.put(id, pos);
        }
    }

    private static void listToMap(List<Long> list, LongSparseArray<Integer> map, int firstItemPos) {
        if (list != null) {
            for(int index = 0; index < list.size(); index++) {
                Long id = list.get(index);
                map.put(id, index + firstItemPos);
            }
        }
    }

    private final Adapter   mAdapter;
    private final Object    mSyncObject = new Object();
    static final String     TAG = ListChangeManager.class.getSimpleName();
}
