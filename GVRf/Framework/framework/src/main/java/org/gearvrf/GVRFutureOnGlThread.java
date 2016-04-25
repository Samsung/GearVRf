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

package org.gearvrf;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This is a special RunnableFuture implementation ONLY FOR the case that when
 * in a non-GL thread you want to push a asynchronous call with return value to
 * GL thread, ex. trying to update bitmap of a texture in input event thread.
 * 
 * Please be aware that this is not a common Future implementation. It is not
 * recommended to use this class for common Future use like
 * Thread.start(GVRFutureOnGlThread).
 * 
 * The most common use cases should be with GVRContext.runOnGlThread. You can
 * submit the call request through GVRContext.runOnGlThread(GVRFutureOnGlThread)
 * and get the result with get() method. Just be careful that it's best to call
 * GVRContext.runOnGlThread(GVRFutureOnGlThread) when
 * GVRContext.isCurrentThreadGLThread() returns false since you can directly
 * call GVRFutureOnGlThread.run() while in GL thread.
 * 
 */

public class GVRFutureOnGlThread<T> implements RunnableFuture<T> {
    private Callable<T> mCallable;
    private T t;
    private boolean mIsDone;
    private boolean mIsStarted;
    private boolean mIsCancelled;
    private final Object[] lock = new Object[0];

    /**
     * Construct a call request.
     * 
     * @param callable
     *            The call you want to make happen in GL thread.
     */
    public GVRFutureOnGlThread(Callable<T> callable) {
        mCallable = callable;
    }

    /**
     * Where the call actually happens. If you submit the call through
     * GVRContext.runOnGlThread(). This will be automatically called in GL
     * thread. You can also directly call this function while in GL thread.
     */
    @Override
    public void run() {
        synchronized (lock) {
            if (mIsDone || mIsCancelled) {
                return;
            }
            mIsStarted = true;
        }

        try {
            t = mCallable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        synchronized (lock) {
            mIsDone = true;
            lock.notifyAll();
        }

    }

    /**
     * The result we get after run() successfully executed.
     * 
     * @return The result from the Callable we put in.
     * @throws InterruptedException 
     * @throws CancellationException
     *             If the task was successfully cancelled.
     */
    @Override
    public T get() throws InterruptedException {
        try {
            get(0, null);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return t;
    }

    /**
     * Normally, a Future.cancel(true) means you can force interrupt the thread
     * which is running the request.But GVRFutureOnGlThread will probably run on
     * GL thread. An Interruption on GL thread means forced stop on the app. So
     * currently we only support the cancellation before the task get executed.
     * 
     * @return if the cancellation request is successful.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (lock) {
            if (!mIsStarted) {
                mIsCancelled = true;
                lock.notifyAll();
            }
        }
        return mIsCancelled;
    }

    /**
     * To see if the task is successfully cancelled or not.
     * 
     * @return If current task is cancelled.
     */
    @Override
    public boolean isCancelled() {
        return mIsCancelled;
    }

    /**
     * To see if current task is done.
     * 
     * @return If current task is done.
     */
    @Override
    public boolean isDone() {
        return mIsDone;
    }

    /**
     * To get the result we get from run(). With timeout request.
     * 
     * @param timeout
     *            The timeout number.
     * @param unit
     *            The time unit bound to timeout number.
     * @return The result from the Callable we put in.
     * 
     * @throws InterruptedException
     *             When this thread is interrupted while waiting.
     * @throws CancellationException
     *             When the callable task got successfully cancelled.
     * @throws TimeOutExcetipn
     *             When the execution of the callable call() out of timeout
     *             limitation.
     */
    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException,
            TimeoutException {
        synchronized (lock) {
            if (mIsCancelled) {
                throw new CancellationException(
                        "The task on GVRFutureOnGlThread has already been cancelled.");
            }
            if (mIsDone) {
                return t;
            }
            if(unit == null){
                lock.wait();
            }else{
                lock.wait(unit.convert(timeout, TimeUnit.MILLISECONDS));
            }
            if (mIsCancelled) {
                throw new CancellationException(
                        "The task on GVRFutureOnGlThread has already been cancelled.");
            }
            if (!mIsDone) {
                throw new TimeoutException("Request time out for"
                        + unit.convert(timeout, TimeUnit.MILLISECONDS) + "ms");
            }
            return t;
        }
    }
}
