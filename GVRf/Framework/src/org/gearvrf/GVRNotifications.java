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

import android.opengl.GLSurfaceView;

/**
 * Allows non-GL threads to synchronize with {@link GVRScript#onStep()}.
 * 
 * You might have code that you want to run every frame, but that does not need
 * to run in the GL thread. Using this class gives you access to timing signals,
 * without slowing the GL thread (which would increase the chance of missing
 * frames).
 */
public final class GVRNotifications {

    private final static Object[] beforeStep = new Object[0];
    private final static Object[] afterStep = new Object[0];

    private GVRNotifications() {
    }

    /** Wake any threads that are blocked in {@link #waitBeforeStep()} */
    static void notifyBeforeStep() {
        synchronized (beforeStep) {
            beforeStep.notifyAll();
        }
    }

    /**
     * Wait for the next before-step event.
     * 
     * This method will block until the GL thread receives the next
     * {@link GLSurfaceView.Renderer#onDrawFrame(javax.microedition.khronos.opengles.GL10)
     * onDrawFrame()} call. Since your thread(s) will wake up shortly before
     * {@link GVRScript#onStep()}, you should only use this method if you need
     * the tightest possible synchronization with the GL thread. That is, you
     * should use {@link #waitAfterStep()} if you can: This method can not
     * guarantee immediate wake-up ... and, if you do get immediate wake-up, you
     * will be competing with the GL thread.
     */
    public static void waitBeforeStep() {
        synchronized (beforeStep) {
            try {
                beforeStep.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** Wake any threads that are blocked in {@link #waitAfterStep()} */
    static void notifyAfterStep() {
        synchronized (afterStep) {
            afterStep.notifyAll();
        }
    }

    /**
     * Wait for the next after-step event.
     * 
     * This method will block until the GL thread is returning from the next
     * {@link GLSurfaceView.Renderer#onDrawFrame(javax.microedition.khronos.opengles.GL10)
     * onDrawFrame()} call: waking non-GL threads here guarantees the least
     * possible competition for CPU time.
     */
    public static void waitAfterStep() {
        synchronized (afterStep) {
            try {
                afterStep.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
