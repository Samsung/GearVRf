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

package org.gearvrf.periodic;

import java.util.PriorityQueue;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRScript;

/**
 * Schedule {@linkplain Runnable runnables} to run on the GL thread at a future
 * time.
 * 
 * You can schedule {@linkplain #runAfter(Runnable, float) one-shot events} and
 * you can schedule periodic events, which will either
 * {@linkplain #runEvery(Runnable, float, float) run forever,} or
 * {@linkplain #runEvery(Runnable, float, float, int) run for a fixed number of
 * times,} or {@linkplain #runEvery(Runnable, float, float, KeepRunning) until
 * your callback tells them to stop.}
 * 
 * <p>
 * The periodic engine is an optional component of GVRF: You need to
 * {@linkplain GVRContext#getPeriodicEngine() get the singleton} in order to use
 * it. For example,
 * 
 * <pre>
 * 
 * Runnable pulse = new Runnable() {
 * 
 *     public void run() {
 *         new GVRScaleAnimation(uiObject, 0.5f, 2f) //
 *                 .setRepeatMode(GVRRepeatMode.PINGPONG) //
 *                 .start(mAnimationEngine);
 *     }
 * };
 * gvrContext.getPeriodicEngine().runEvery(pulse, 1f, 2f, 10);
 * </pre>
 * 
 * will grow and shrink the {@code uiObject} ten times, every other second,
 * starting in a second. This can be a good way to draw the user's attention to
 * something like a notification.
 * 
 * <p>
 * The engine maintains a priority queue of events, which it checks in a
 * {@linkplain GVRDrawFrameListener frame listener}; events run as
 * {@linkplain GVRContext#runOnGlThread(Runnable) run-once events.} Every frame,
 * GVRF runs any run-once events; then any frame listeners (including
 * animations); then your {@linkplain GVRScript#onStep() onStep() method;} and
 * then it renders the scene. This means that any periodic events that run on a
 * given frame will run before any animations. It also means that there is
 * always a one frame delay between the time an event is dequeued and the time
 * it actually runs. Scheduling an event from a point in the render pipeline
 * after the periodic engine will add an additional one frame delay. (The
 * periodic engine may run before the animation engine or after it - try not to
 * write code that depends on one running before the other.) Running at 60 fps,
 * each frame is normally 17 milliseconds apart, unless you add too many
 * callbacks or put too much code into your {@code onStep()}; Android garbage
 * collection can introduce additional delays.
 */
public class GVRPeriodicEngine {
    private static GVRPeriodicEngine sInstance = null;

    static {
        GVRContext.addResetOnRestartHandler(new Runnable() {

            @Override
            public void run() {
                sInstance = null;
            }
        });
    }

    private final GVRContext mContext;
    private final DrawFrameListener mDrawFrameListener = new DrawFrameListener();
    private final PriorityQueue<EventImplementation> mQueue = new PriorityQueue<EventImplementation>();

    protected GVRPeriodicEngine(GVRContext context) {
        mContext = context;
        context.registerDrawFrameListener(mDrawFrameListener);
    }

    /**
     * Get the (lazy-created) singleton.
     * 
     * @param context
     *            Current GVRContext
     * 
     * @return Periodic engine singleton.
     */
    public static synchronized GVRPeriodicEngine getInstance(GVRContext context) {
        if (sInstance == null) {
            sInstance = new GVRPeriodicEngine(context);
        }
        return sInstance;
    }

    /**
     * Run a task once, after a delay.
     * 
     * @param task
     *            Task to run.
     * @param delay
     *            Unit is seconds.
     * @return An interface that lets you query the status; cancel; or
     *         reschedule the event.
     */
    public PeriodicEvent runAfter(Runnable task, float delay) {
        validateDelay(delay);
        return new Event(task, delay);
    }

    /**
     * Run a task periodically and indefinitely.
     * 
     * @param task
     *            Task to run.
     * @param delay
     *            The first execution will happen in {@code delay} seconds.
     * @param period
     *            Subsequent executions will happen every {@code period} seconds
     *            after the first.
     * @return An interface that lets you query the status; cancel; or
     *         reschedule the event.
     */
    public PeriodicEvent runEvery(Runnable task, float delay, float period) {
        return runEvery(task, delay, period, null);
    }

    /**
     * Run a task periodically, for a set number of times.
     * 
     * @param task
     *            Task to run.
     * @param delay
     *            The first execution will happen in {@code delay} seconds.
     * @param period
     *            Subsequent executions will happen every {@code period} seconds
     *            after the first.
     * @param repetitions
     *            Repeat count
     * @return {@code null} if {@code repetitions < 1}; otherwise, an interface
     *         that lets you query the status; cancel; or reschedule the event.
     */
    public PeriodicEvent runEvery(Runnable task, float delay, float period,
            int repetitions) {
        if (repetitions < 1) {
            return null;
        } else if (repetitions == 1) {
            // Better to burn a handful of CPU cycles than to churn memory by
            // creating a new callback
            return runAfter(task, delay);
        } else {
            return runEvery(task, delay, period, new RunFor(repetitions));
        }
    }

    /**
     * Run a task periodically, with a callback.
     * 
     * @param task
     *            Task to run.
     * @param delay
     *            The first execution will happen in {@code delay} seconds.
     * @param period
     *            Subsequent executions will happen every {@code period} seconds
     *            after the first.
     * @param callback
     *            Callback that lets you cancel the task. {@code null} means run
     *            indefinitely.
     * @return An interface that lets you query the status; cancel; or
     *         reschedule the event.
     */
    public PeriodicEvent runEvery(Runnable task, float delay, float period,
            KeepRunning callback) {
        validateDelay(delay);
        validatePeriod(period);
        return new Event(task, delay, period, callback);
    }

    /**
     * Optional callback that you can supply to
     * {@link GVRPeriodicEngine#runEvery(Runnable, float, float, KeepRunning)}.
     * 
     * It will be called after every execution, allowing you to terminate the
     * {@linkplain PeriodicEvent periodic event.}
     */
    public interface KeepRunning {
        /**
         * Called after every execution: lets you do a run-until-state event.
         * 
         * @param event
         *            Interface returned by
         *            {@link GVRPeriodicEngine#runEvery(Runnable, float, float, KeepRunning)}
         *            - this lets you can use the same callback with multiple
         *            events.
         * @return {@code true} to keep running, {@code false} to cancel the
         *         periodic event.
         */
        boolean keepRunning(PeriodicEvent event);
    }

    /**
     * Represents a scheduled event.
     * 
     * Returned by the methods that schedule a Runnable; allows you to query
     * event status; cancel; and/or reschedule the event.
     */
    public interface PeriodicEvent {
        /**
         * No execution is pending, either because this is a one-shot event that
         * has already fired, or because this is a periodic event that has been
         * canceled.
         */
        static final float UNSCHEDULED = -Float.MAX_VALUE;

        /**
         * Number of times this event has run to completion.
         * 
         * The count is guaranteed to be accurate within a {@link KeepRunning}
         * callback.
         * 
         * @return Number of times this event has run to completion.
         */
        int getRunCount();

        /**
         * Seconds to (next) execution.
         * 
         * May be slightly negative, if execution is overdue;
         * {@link #UNSCHEDULED} indicates that no execution is pending, either
         * because this is a one-shot event that has already fired, or because
         * this is a periodic event that has been canceled.
         */
        float getCurrentWait();

        /**
         * Cancel any future executions.
         * 
         * If called from non-GL thread, there is a chance it will be called
         * during an execution: this will not affect the current execution, but
         * <em>will</em> cancel any future executions.
         */
        void cancel();

        /**
         * Reschedule, to run-once after a specified delay.
         * 
         * You can call this on a run-once or a repeating event; it doesn't
         * matter whether it is currently scheduled or it has already timed out.
         * Calling this after a run-once event has run will make it run it
         * again; calling this before a run-once event has run will change its
         * scheduled time; calling this on a repeating event will turn it into a
         * run-once event.
         * 
         * @param delay
         *            The next execution will happen in {@code delay} seconds.
         */
        void runAfter(float delay);

        /**
         * Reschedule, to run periodically and indefinitely.
         * 
         * You can call this on a run-once or a repeating event; it doesn't
         * matter whether it is currently scheduled or it has already timed out.
         * Calling this on a run-once event (before or after it has run) will
         * turn it into a repeating event; calling this on a repeating event
         * will change scheduling.
         * 
         * @param delay
         *            The next execution will happen in {@code delay} seconds.
         * @param period
         *            Subsequent executions will happen every {@code period}
         *            seconds after the first rescheduled execution.
         */
        void runEvery(float delay, float period);

        /**
         * Reschedule, to run periodically, for a set number of times.
         * 
         * You can call this on a run-once or a repeating event; it doesn't
         * matter whether it is currently scheduled or it has already timed out.
         * Calling this on a run-once event (before or after it has run) will
         * turn it into a repeating event; calling this on a repeating event
         * will change scheduling.
         * 
         * @param delay
         *            The next execution will happen in {@code delay} seconds.
         * @param period
         *            Subsequent executions will happen every {@code period}
         *            seconds after the first rescheduled execution.
         * @param repetitions
         *            Repeat count
         */

        void runEvery(float delay, float period, int repetitions);

        /**
         * Reschedule, to run periodically with a callback.
         * 
         * You can call this on a run-once or a repeating event; it doesn't
         * matter whether it is currently scheduled or it has already timed out.
         * Calling this on a run-once event (before or after it has run) will
         * turn it into a repeating event; calling this on a repeating event
         * will change scheduling.
         * 
         * @param delay
         *            The next execution will happen in {@code delay} seconds.
         * @param period
         *            Subsequent executions will happen every {@code period}
         *            seconds after the first rescheduled execution.
         * @param callback
         *            Callback that lets you cancel the task. {@code null} means
         *            run indefinitely.
         */
        void runEvery(float delay, float period, KeepRunning callback);
    }

    private interface EventImplementation extends PeriodicEvent,
            Comparable<EventImplementation>, Runnable {
        float getScheduledTime();
    }

    /**
     * The periodic engine's time base.
     * 
     * Unit is seconds.
     */
    private static float now() {
        return System.nanoTime() / 1e9f;
    }

    private class DrawFrameListener implements GVRDrawFrameListener {

        @Override
        public void onDrawFrame(float frameTime) {
            float now = GVRPeriodicEngine.now();
            synchronized (mQueue) {
                for (EventImplementation //
                first = mQueue.peek(); //
                first != null && first.getScheduledTime() <= now; //
                first = mQueue.peek() //
                ) {

                    mContext.runOnGlThread(mQueue.poll());

                }
            }
        }
    }

    private class Event implements EventImplementation {

        /*
         * Task, and run-count
         */

        private final Runnable mTask;
        private int mRunCount = 0;

        /**
         * Special flag, used to adjust {@link #mRunCount} if
         * {@link PeriodicEvent#runEvery(float, float, int)} is called from
         * within {@link #mTask}.
         */
        private boolean mRunning = false;

        /*
         * Queue management
         */

        /**
         * Set by {@link #cancel()} to assure that events canceled from non-GL
         * thread during execution are not rescheduled.
         */
        private boolean mCanceled = false;

        private void lockedEnqueue() {
            if (mCanceled != true) {
                mQueue.add(this);
            }
        }

        private void lockedDequeue() {
            mQueue.remove(this);
        }

        private void enqueue() {
            synchronized (mQueue) {
                lockedEnqueue();
            }
        }

        @SuppressWarnings("unused")
        private void dequeue() {
            synchronized (mQueue) {
                lockedDequeue();
            }
        }

        /*
         * Scheduling fields.
         * 
         * A run-once event has an mPeriod == UNSCHEDULED.
         * 
         * We allow the user to change the scheduling at any time. To assure
         * consistency, neither constructors nor the rescheduling methods set
         * these fields directly: both go through setDelay() or setRepeat().
         */

        private float mTimeBase;
        private float mScheduledTime;
        private float mPeriod;
        private KeepRunning mCallback;

        private void setDelay(float delay) {
            mTimeBase = now();
            schedule(mTimeBase + delay);
            mPeriod = UNSCHEDULED;
            mCallback = null;
        }

        private void setRepeat(float delay, float period, KeepRunning callback) {
            mTimeBase = now();
            schedule(mTimeBase + delay);
            mPeriod = period;
            mCallback = callback;
        }

        private void schedule(float time) {
            mScheduledTime = time;
            mCanceled = false;
        }

        private void deschedule() {
            mScheduledTime = UNSCHEDULED;
        }

        private void reschedule() {
            if (repeats()) {
                if (mCallback != null && mCallback.keepRunning(this) != true) {
                    return; // Do NOT reschedule
                }

                float next = mTimeBase + (((now() - mTimeBase) / mPeriod) + 1)
                        * mPeriod;
                schedule(next);
                enqueue();
            }
        }

        private boolean enqueued() {
            synchronized (mQueue) {
                for (EventImplementation event : mQueue) {
                    if (this == event) {
                        return true;
                    }
                }
            }
            // else
            return false;
        }

        private boolean scheduled() {
            return mScheduledTime != UNSCHEDULED;
        }

        private boolean repeats() {
            return mPeriod != UNSCHEDULED;
        }

        /*
         * Constructors
         */

        private Event(Runnable task, float delay) {
            mTask = task;
            setDelay(delay);

            enqueue();
        }

        private Event(Runnable task, float delay, float period,
                KeepRunning callback) {
            mTask = task;
            setRepeat(delay, period, callback);

            enqueue();
        }

        /*
         * PeriodicEvent
         */

        @Override
        public int getRunCount() {
            return mRunCount;
        }

        @Override
        public float getCurrentWait() {
            return scheduled() ? mScheduledTime - now() : UNSCHEDULED;
        }

        @Override
        public void cancel() {
            synchronized (mQueue) {
                deschedule();
                lockedDequeue();
                mCanceled = true;
            }
        }

        @Override
        public void runAfter(float delay) {
            validateDelay(delay);

            synchronized (mQueue) {
                lockedDequeue();
                setDelay(delay);
                lockedEnqueue();
            }
        }

        @Override
        public void runEvery(float delay, float period) {
            runEvery(delay, period, null);
        }

        @Override
        public void runEvery(float delay, float period, int repetitions) {
            runEvery(delay, period, new RunFor(mRunCount + (mRunning ? 1 : 0),
                    repetitions));
        }

        @Override
        public void runEvery(float delay, float period, KeepRunning callback) {
            validateDelay(delay);
            validatePeriod(period);

            synchronized (mQueue) {
                lockedDequeue();
                setRepeat(delay, period, callback);
                lockedEnqueue();
            }
        }

        /*
         * EventImplementation
         */

        @Override
        public float getScheduledTime() {
            return mScheduledTime;
        }

        /*
         * Comparable<EventImplementation>
         */

        @Override
        public int compareTo(EventImplementation other) {
            final float delta = getScheduledTime() - other.getScheduledTime();
            return delta < 0 ? -1 : (delta == 0 ? 0 : 1);
        }

        /*
         * Runnable
         */

        @Override
        public void run() {
            mRunning = true;
            mTask.run();
            mRunning = false;
            mRunCount += 1;

            /*
             * Normally, the event will NOT be enqueued at this point. But, if
             * the event has rescheduled itself (using the PeriodicEvent
             * returned when it was created) then it WILL be enqueued, and we
             * shouldn't re-enqueue it.
             */
            if (enqueued() != true) {
                deschedule();
                reschedule();
            }
        }

    }

    private static class RunFor implements KeepRunning {

        private final int mTrigger;

        private RunFor(int repetitions) {
            this(0, repetitions);
        }

        private RunFor(int currentCount, int repetitions) {
            mTrigger = currentCount + repetitions;
        }

        @Override
        public boolean keepRunning(PeriodicEvent event) {
            return event.getRunCount() < mTrigger;
        }

    }

    private static void validateDelay(float delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay must be >= 0");
        }
    }

    private static void validatePeriod(float period) {
        if (period <= 0) {
            throw new IllegalArgumentException("period must be > 0");
        }
    }
}
