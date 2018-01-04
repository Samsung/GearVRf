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
import org.gearvrf.GVRSceneObject;

/**
 * Represents a spatial sound source in the scene.
 */
public class GVRAudioSource extends GVRComponent {
    static private long TYPE_AUDIO = newComponentType(GVRAudioSource.class);

    private String mSoundFile;
    private volatile int mSourceId;
    private GVRAudioListener mAudioListener;

    private float mVolume;

    /**
     * Constructs a new sound source.
     *
     * @param gvrContext GVR context.
     */
    public GVRAudioSource(GVRContext gvrContext) {
        super(gvrContext, 0);

        mType = getComponentType();

        mAudioListener = null;
        mSoundFile = null;
        mSourceId = -1;

        mVolume = 1f;
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
        if (mAudioListener == null) {
            throw new UnsupportedOperationException("Audio source not connected to audio listener");
        }

        mSoundFile = soundFile;
        mAudioListener.loadSoundFile(this);
    }

    /**
     * Unload the sound file.
     */
    public void unload() {
        if (mAudioListener == null) {
            throw new UnsupportedOperationException("Audio source not connected to audio listener");
        }

        mAudioListener.unloadSoundFile(this);
        mSoundFile = null;
        mSourceId = -1;
    }

    public String getSoundFile() {
        return mSoundFile;
    }

    int getSourceId() {
        return mSourceId;
    }

    void setSourceId(int sourceId) {
        mSourceId = sourceId;
    }

    @Override
    public void setOwnerObject(GVRSceneObject owner) {
        GVRAudioListener newListener = null;

        if (owner != null && owner.getParent() != null) {
            newListener = (GVRAudioListener)owner.getParent().getComponent(GVRAudioListener.getComponentType());
        }

        switchListener(newListener);

        this.owner = owner;
    }

    private void switchListener(GVRAudioListener newListener) {
        if (mAudioListener != newListener) {
            if (mAudioListener != null) {
                stop();
                mAudioListener.removeSource(this);
            }

            if (newListener != null) {
                newListener.addSource(this);
            }
        }

        mAudioListener = newListener;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        stop();
    }

    @Override
    public void onNewOwnersParent(GVRSceneObject newOwnersParent) {
        GVRAudioListener newListener = (GVRAudioListener)newOwnersParent.getComponent(GVRAudioListener.getComponentType());
        switchListener(newListener);
    }

    @Override
    public void onRemoveOwnersParent(GVRSceneObject oldOwnersParent) {
        switchListener(null);
    }

    public boolean isPlaying() {
        if (mAudioListener == null) {
            throw new UnsupportedOperationException("Audio source not connected to audio listener");
        }

        return mAudioListener.isSoundPlaying(this);
    }

    /**
     * Starts the playback of a preloaded sound.
     */
    public void play() {
        play(false);
    }

    /**
     * Starts the playback of a preloaded sound.
     *
     * @param looped enables looped playback
     */
    public void play(boolean looped) {
        if (isEnabled()) {
            onPlay(looped);
        }
    }

    /**
     * Starts the playback of a sound.
     *
     * @param looped enables looped playback
     */
    public void play(String soundFile, boolean looped) {
        mSoundFile = soundFile;
        play(looped);
    }

    protected void onPlay(boolean looped) {
        mAudioListener.playSound(this, looped);
    }

    /**
     * Pauses the playback of a sound.
     */
    public void pause() {
        mAudioListener.pauseSound(this);
    }

    /**
     * Resumes the playback of a sound.
     */
    public void resume() {
        if (isEnabled()) {
            onResume();
        }
    }

    protected void onResume() {
        mAudioListener.resumeSound(this);
    }

    /**
     * Stops the playback of a sound and destroys the corresponding Sound Object or Soundfield.
     */
    public void stop() {
        mAudioListener.stopSound(this);
    }

    /**
     * Changes the volume of an existing sound.
     * @param volume volume value. Should range from 0 (mute) to 1 (max)
     */
    public void setVolume(float volume) {
        if (mAudioListener == null) {
            throw new UnsupportedOperationException("Audio source not connected to audio listener");
        }

        // Save this in case this audio source is not being played yet
        mVolume = volume;

        // This will actually work only if the sound file is being played
        mAudioListener.setSoundVolume(this, volume);
    }

    /**
     * Returns the volume value.
     *
     * @return 0 to mute and 1 to max.
     */
    public float getVolume() {
        return mVolume;
    }
}
