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

/**
 * Represents a spatial sound source in the scene.
 */
public class GVRAudioSource extends GVRComponent {
    static private long TYPE_AUDIO = newComponentType(GVRAudioSource.class);

    /**
     * Constructs a new sound source.
     *
     * @param gvrContext GVR context.
     */
    public GVRAudioSource(GVRContext gvrContext) {
        super(gvrContext, 0);

        mType = getComponentType();
    }

    static public long getComponentType() { return TYPE_AUDIO; }

    static protected long newComponentType(Class<? extends GVRAudioSource> clazz)
    {
        long hash = (long) clazz.hashCode() << 32;
        long t = ((long) System.currentTimeMillis() & 0xfffffff);
        long result = hash | t;
        return result;
    }

    /**
     * Preloads a sound file.
     *
     * @param soundFile path/name of the file to be played.
     */
    public void load(String soundFile) {
    }

    /**
     * Unload the sound file.
     */
    public void unload() {
    }

    public boolean isPlaying() {
        return true;
    }

    /**
     * Starts the playback of a preloaded sound.
     */
    public void play() {
    }

    /**
     * Starts the playback of a preloaded sound.
     *
     * @param looped enables looped playback
     */
    public void play(boolean looped) {
    }

    /**
     * Starts the playback of a sound.
     *
     * @param looped enables looped playback
     */
    public void play(String soundFile, boolean looped) {
    }

    /**
     * Pauses the playback of a sound.
     */
    public void pause() {
    }

    /**
     * Resumes the playback of a sound.
     */
    public void resume() {
    }

    /**
     * Stops the playback of a sound and destroys the corresponding Sound Object or Soundfield.
     */
    public void stop() {
    }

    /**
     * Changes the volume of an existing sound.
     * @param volume volume value. Should range from 0 (mute) to 1 (max)
     */
    public void setVolume(float volume) {
    }

    /**
     * Returns the volume value.
     *
     * @return 0 to mute and 1 to max.
     */
    public float getVolume() {
        return 1.0f;
    }
}
