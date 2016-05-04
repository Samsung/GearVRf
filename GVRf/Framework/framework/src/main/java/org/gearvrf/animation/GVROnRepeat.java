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

/** Optional on-finish callback, with options for controlling repetition */
public interface GVROnRepeat extends GVROnFinish {
    /**
     * Optional callback, that lets you stop an animation when your app reaches
     * a desired state.
     * 
     * When you {@linkplain GVRAnimation#setOnFinish(GVROnFinish) set} a
     * {@code GVROnRepeat} callback, {@code iteration} will be called after each
     * iteration: returning {@code false} will stop the animation.
     * 
     * <p>
     * The repeat count {@linkplain GVRAnimation#DEFAULT_REPEAT_COUNT defaults
     * to 2,} but setting a {@code GVROnRepeat} callback also sets the repeat
     * count to a negative number, so that the animation is under your
     * callback's control. Do note that {@link GVRAnimation#setRepeatCount(int)
     * setting a positive repeat count} <em>after</em> setting the
     * {@code GVROnRepeat} callback will override this: {@code iteration} is not
     * called when the repeat count is {@literal >=} 0.
     * 
     * @param animation
     *            The animation that just finished, so you can use the same
     *            callback with multiple animations.
     * @param count
     *            The number of repetitions since the animation started
     * @return {@code true} to allow the animation to repeat. {@code false} will
     *         stop the animation: the engine will
     *         {@linkplain GVRAnimationEngine#stop(GVRAnimation) unregister the
     *         animation} <em>and</em> call
     *         {@link GVROnFinish#finished(GVRAnimation)}.
     */
    boolean iteration(GVRAnimation animation, int count);
}