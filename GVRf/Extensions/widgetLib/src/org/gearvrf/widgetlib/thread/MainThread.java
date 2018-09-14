package org.gearvrf.widgetlib.thread;

import java.util.concurrent.CountDownLatch;

import org.gearvrf.GVRContext;

import org.gearvrf.widgetlib.log.Log;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

/**
 *  MainThread needs to be a final class since the constructor starts
 *  a new thread.
 */
public final class MainThread {
    /**
     * Creates and starts the main thread and its message handling Looper.
     * <p>
     * <b><span style="color:red">NOTE:</span></b> This method blocks until the
     * thread is started and the handler is ready to receive messages.
     * <p>
     * @throws InterruptedException
     *             if the thread wasn't successfully started.
     */
    public MainThread(GVRContext gvrContext) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        mainThread = new Thread("MainThread") {
            public void run() {
                threadId = Thread.currentThread().getId();
                Looper.prepare();
                handler = new Handler();
                latch.countDown();
                Looper.loop();
                terminated = true;
                sanityCheck(null);
            }
        };
        mainThread.start();
        latch.await();
    }

    /**
     * Shuts down the main thread's looper using {@code Looper#quitSafely()}.
     */
    public void quit() {
        if (!sanityCheck("quit")) {
            return;
        }
        handler.getLooper().quitSafely();
    }

    /**
     * Query whether the current running thread is the main thread
     *
     * @return Returns {@code true} if the thread this method is called from is
     *         the main thread.
     */
    public boolean isMainThread() {
        return Thread.currentThread().equals(mainThread);
    }

    /**
     * Remove any pending posts of {@link Runnable} {@code r} that are in the
     * message queue.
     *
     * @param r
     *            {@code Runnable} to remove from queue.
     */
    public void removeCallbacks(Runnable r) {
        assert handler != null;
        if (!sanityCheck("removeCallbacks " + r)) {
            return;
        }
        handler.removeCallbacks(r);
    }

    /**
     * Executes a {@link Runnable} on the main thread. If this method is called
     * from the main thread, the {@code Runnable} will be executed immediately;
     * otherwise, it will be {@linkplain Handler#post(Runnable) posted} to the
     * {@link Looper Looper's} message queue.
     *
     * @param r
     *            The {@link Runnable} to run on the main thread.
     * @return Returns {@code true} if the Runnable was either immediately
     *         executed or successfully placed in the Looper's message queue.
     */
    public boolean runOnMainThread(final Runnable r) {
        assert handler != null;
        if (!sanityCheck("runOnMainThread " + r)) {
            return false;
        }

        Runnable wrapper =  new Runnable() {
            public void run() {
                FPSCounter.timeCheck("runOnMainThread <START> " + r);
                r.run();
                FPSCounter.timeCheck("runOnMainThread <END> " + r);
            }
        };

        if (isMainThread()) {
            wrapper.run();
            return true;
        } else {
            return runOnMainThreadNext(wrapper);
        }
    }

    /**
     * Queues a Runnable to be run on the main thread on the next iteration of
     * the messaging loop. This is handy when code running on the main thread
     * needs to run something else on the main thread, but only after the
     * current code has finished executing.
     *
     * @param r
     *            The {@link Runnable} to run on the main thread.
     * @return Returns {@code true} if the Runnable was successfully placed in
     *         the Looper's message queue.
     */
    public boolean runOnMainThreadNext(Runnable r) {
        assert handler != null;

        if (!sanityCheck("runOnMainThreadNext " + r)) {
            return false;
        }
        return handler.post(r);
    }

    /**
     * Queues a Runnable to be run on the main thread at the time specified by
     * {@code uptimeMillis}. The time base is {@link SystemClock#uptimeMillis()
     * uptimeMillis()} . A pause in application execution may add an additional
     * delay.
     *
     * @param r
     *            The Runnable to run on the main thread.
     * @param uptimeMillis
     *            The absolute time at which the Runnable should be run, in
     *            milliseconds.
     * @return Returns {@code true} if the Runnable was successfully placed in
     *         the Looper's message queue.
     */
    public boolean runOnMainThreadAtTime(Runnable r, long uptimeMillis) {
        assert handler != null;

        if (!sanityCheck("runOnMainThreadAtTime " + r)) {
            return false;
        }
        return handler.postAtTime(r, uptimeMillis);
    }

    /**
     * Queues a Runnable to be run on the main thread after the time specified
     * by {@code delayMillis} has elapsed. A pause in application execution may
     * add an additional delay.
     *
     * @param r
     *            The Runnable to run on the main thread.
     * @param delayMillis
     *            The delay, in milliseconds, until the Runnable is run.
     * @return Returns {@code true} if the Runnable was successfully placed in
     *         the Looper's message queue.
     */
    public boolean runOnMainThreadDelayed(Runnable r, long delayMillis) {
        assert handler != null;

        if (!sanityCheck("runOnMainThreadDelayed " + r + " delayMillis = " + delayMillis)) {
            return false;
        }
        return handler.postDelayed(r, delayMillis);
    }

    /**
     * Gets Handler
     * @return
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * Checks if the logic is running on main thread. If this is not a case - throw
     * {@link RuntimeException} exception
     * @param label
     */
    public void assertIsMainThread(final String label) {
        final boolean mainThread = isMainThread();
        Log.d(TAG, "%s: is main thread: %b", label, mainThread);
        if (!mainThread) {
            throw new RuntimeException(
                    "Main thread code run on another thread: "
                            + Thread.currentThread().getName());
        }
    }

    private boolean sanityCheck(String taskName) {
        if (terminated) {
            Log.d(TAG, "MainThread " + threadId + " already terminated" +
                    (taskName == null ? "" : ", rejecting task " + taskName));
            return false;
        }
        return true;
    }

    private Thread mainThread;
    private Handler handler;
    private boolean terminated;
    private long    threadId;

    private static final String TAG = MainThread.class.getSimpleName();
}
