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

package org.gearvrf.animation;

/** Optional on-finish callback */
public interface GVROnFinish {

    /**
     * Optional callback: called when the animation is complete, and has stopped
     * running.
     * 
     * <p>
     * With the default, {@linkplain GVRRepeatMode#ONCE run-once} animation,
     * this is pretty straight forward: the animation will run to completion and
     * {@code finished()} will be called. Note that repeat mode {@code ONCE}
     * overrides any {@linkplain GVRAnimation#setRepeatCount(int) repeat count.}
     * 
     * <p>
     * The repetitive types {@link GVRRepeatMode#REPEATED REPEATED} and
     * {@link GVRRepeatMode#PINGPONG PINGPONG} <em>do</em> pay attention to the
     * repeat count. With a positive repeat count, the animation will run for
     * the specified number of times, and then call {@code finished()}. Note
     * that the {@link GVRAnimation#DEFAULT_REPEAT_COUNT} is 2.
     * 
     * <p>
     * If the repeat count is negative, the animation will run until you stop
     * it. There are three ways you can do this:
     * 
     * <ul>
     * <li>You can call {@link GVRAnimationEngine#stop(GVRAnimation)}. This will
     * stop the animation immediately, regardless of the current state of the
     * animated property, so this is generally appropriate only for pop-up
     * objects like Please Wait spinners that are about to disappear. Calling
     * {@code stop()} will <em>not</em> trigger the {@code finished()} callback.
     * 
     * <li>You can call {@link GVRAnimation#setRepeatCount(int)
     * setRepeatCount(0)} to schedule a graceful termination, at the end of the
     * current animation cycle: this will also call {@code finished()}.
     * 
     * <li>If the callback is also a {@link GVROnRepeat}, the
     * {@link GVROnRepeat#iteration(GVRAnimation, int)} method will be called at
     * the end of each animation cycle. (This is the best way to terminate a
     * ping pong animation, as you can be sure that you are leaving the animated
     * property in its start state.) When {@code iteration} returns
     * {@code false}, the animation will be stopped, and {@code finish} will be
     * called.
     * 
     * </ul>
     * 
     * @param animation
     *            The animation that just finished, so you can use the same
     *            callback with multiple animations.
     */
    void finished(GVRAnimation animation);

}
