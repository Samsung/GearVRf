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

package org.gearvrf.resonanceaudio;

import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;

/**
 * Represents a listener in the scene. It records the sound around it and plays through the
 * speakers. It is allowed to have only one listener in the scene.
 */
public class GVRAudioListener extends GVRComponent {
    private static final String TAG = GVRAudioListener.class.getSimpleName();

    private static long TYPE_AUDIO = newComponentType(GVRAudioListener.class);

    /**
     * Constructs a new GVRAudioListener.
     *
     * @param gvrContext GVR context.
     * @param scene Scene of the audio listener.
     */
    public GVRAudioListener(GVRContext gvrContext, GVRScene scene) {
        super(gvrContext, 0L);

        mType = getComponentType();
    }

    public static long getComponentType() { return TYPE_AUDIO; }

    protected static long newComponentType(Class<? extends GVRAudioListener> clazz) {
        long hash = (long) clazz.hashCode() << 32;
        long t = System.currentTimeMillis() & 0xfffffff;
        long result = hash | t;
        return result;
    }

    void addSource(GVRAudioSource audioSource) {
    }

    void removeSource(GVRAudioSource audioSource) {
    }

    @Override
    public void onEnable() {
        super.onEnable();

        resume();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        pause();
    }

    private void pause() {
    }

    private void resume() {

    }

    void loadSoundFile(GVRAudioSource audioSource) {
    }

    void unloadSoundFile(GVRAudioSource audioSource) {
    }

    void playSound(GVRAudioSource audioSource, boolean looping) {
    }

    void pauseSound(GVRAudioSource audioSource) {
    }

    void resumeSound(GVRAudioSource audioSource) {
    }

    void stopSound(GVRAudioSource audioSource) {
    }

    void setSoundVolume(GVRAudioSource audioSource, float volume) {
    }

    boolean isSoundPlaying(GVRAudioSource audioSource) {
        return true;
    }
}
