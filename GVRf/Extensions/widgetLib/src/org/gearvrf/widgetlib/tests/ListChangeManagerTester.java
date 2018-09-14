package org.gearvrf.widgetlib.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.database.DataSetObserver;
import android.util.LongSparseArray;

import org.gearvrf.widgetlib.adapter.ListChangeManager;
import org.gearvrf.widgetlib.adapter.ListChangeManager.Action;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.adapter.Adapter;
import org.gearvrf.widgetlib.adapter.BaseAdapter;
import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.Widget;

public class ListChangeManagerTester {

    static final String TAG = ListChangeManagerTester.class.getSimpleName();

    static final int MAX_APPS           = 6;
    static final int MAX_ID             = 9;
    static final int TEST_ITERATIONS    = 1000;

    public void test() {
        Log.d(TAG, "test starting");

        case1();

        for(int i = 0; i < TEST_ITERATIONS; i++) {
            caseRandom();
        }

        Log.d(TAG, "test finished");
    }

    private void case1() {
        List<Long> list = new ArrayList<Long>();
        setList(list, new long[]{ 2, 9, 4, 5 });
        execute( mListChangeManager.getUpdateActions(list, 0) );

        setList(list, new long[] { 5, 8, 9 });
        updateAdapter(list, 1);
    }

    private void caseRandom() {
        final int numApps = rand(MAX_APPS) + 1;
        ArrayList<Long> list = new ArrayList<Long>();
        LongSparseArray<Integer> map = new LongSparseArray<Integer>(); // ensure unique app IDs

        int startpos = 0;
        if (mCurrentList.size() > 1) {
            startpos = rand(mCurrentList.size() / 2);
            for(int i = 0; i < startpos; i++) {
                long id = mCurrentList.get(i);
                map.put(id, i);
                list.add(id);
            }
        }

        while(list.size() < numApps) {
            long id = rand(MAX_ID) + 1;
            if (map.get(id, -1) == -1) {
                map.put(id, list.size());
                list.add(id);
            }
        }

        updateAdapter(list.subList(startpos, list.size()), startpos);
    }

    private void updateAdapter(List<Long> list, int startpos) {
        logList(startpos, list, "==> ", "@" + startpos);
        List<Action> actions = mListChangeManager.getUpdateActions(list, startpos);
        execute(actions);
        logList(0, mCurrentList, "<== ", null);

        compareList(list, mCurrentList, startpos);
    }

    private static void compareList(List<Long> sublist, List<Long> list, int startpos) {
        if (sublist != null && list != null && sublist.size() + startpos == list.size()) {
            for(int i = 0; i < sublist.size(); i++) {
                long a = sublist.get(i);
                long b = list.get(i + startpos);
                if (a != b) {
                    throw new RuntimeException("test failed");
                }
            }
            return;
        }
        throw new RuntimeException("test failed");
    }

    private void execute(List<Action> actions) {
        for(Action action : actions) {
            long id = action.id;
            int position;
            switch(action.action) {
            case DELETE:
                position = action.pos1;
                Log.d(TAG, "  ID " + id + " deleted @" + position);

                if (position < 0 || position >= mCurrentList.size() || id != mCurrentList.get(position)) {
                    throw new RuntimeException("internal error");
                }
                mCurrentList.remove(position);
                break;
            case ADD:
                position = action.pos2;
                Log.d(TAG, "  ID " + id + " added @" + position);

                if (position < 0 || position > mCurrentList.size()) {
                    throw new RuntimeException("internal error");
                }
                mCurrentList.add(position, id);
                break;
            case MOVE:
                int fromPos = action.pos1;
                int toPos = action.pos2;
                Log.d(TAG, "  ID " + id + " moved from " + fromPos + " to " + toPos);

                if (fromPos < 0 || fromPos >= mCurrentList.size() || mCurrentList.get(fromPos) != id ||
                    toPos < 0 || toPos >= mCurrentList.size()){
                    throw new RuntimeException("internal error");
                }
                mCurrentList.remove(fromPos);
                mCurrentList.add(toPos, id);
            }
        }
    }

    private void logList(int startpos, List<Long> list, String prefix, String postfix) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(prefix);
        }

        boolean firstTime = true;
        if (startpos > 0) {
            for(int i = 0; i < startpos; i++) {
                if (!firstTime) {
                    sb.append("|");
                }
                sb.append(mCurrentList.get(i));
                firstTime = false;
            }
            sb.append("|");
            firstTime = true;
        }

        for(long n : list) {
            if (!firstTime) {
                sb.append(",");
            }
            sb.append(n);
            firstTime = false;
        }
        if (postfix != null) {
            sb.append(" (" + postfix + ")");
        }
        Log.d(TAG, "%s", sb);
    }

    private int rand(int max) {
        int value = Math.abs(mRandom.nextInt());
        if (max > 0) {
            value %= max;
        }
        return value;
    }

    private static void setList(List<Long> list, long[] values) {
        list.clear();
        for(long value : values) {
            list.add(value);
        }
    }

    Adapter mAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mCurrentList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return mCurrentList.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public Widget getView(int position, Widget convertView,
                GroupWidget parent) {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean hasUniformViewSize() {
            return true;
        }

        @Override
        public float getUniformWidth() {
            return 0;
        }

        @Override
        public float getUniformHeight() {
            return 0;
        }

        @Override
        public float getUniformDepth() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return getCount() < 1;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
        }

        @Override
        public void unregisterAllDataSetObservers() {
        }
    };

    ListChangeManager mListChangeManager = new ListChangeManager(mAdapter);
    Random mRandom = new Random();
    List<Long> mCurrentList = new ArrayList<Long>();
}
