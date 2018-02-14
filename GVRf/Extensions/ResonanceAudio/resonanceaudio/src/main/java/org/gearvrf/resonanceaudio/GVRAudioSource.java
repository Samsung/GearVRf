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

import com.google.vr.sdk.audio.GvrAudioEngine;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.utility.Log;

/**
 * Represents a spatial sound source in the scene.
 * An audio source is attached to a scene object and derives
 * its position from the transform of the scene objects.
 * An audio source must be added to the {@link GVRAudioManager}
 * in order to play it.
 */
public class GVRAudioSource extends GVRBehavior
{
    static private long TYPE_AUDIO = newComponentType(GVRAudioSource.class);

    private String mSoundFile;
    private volatile int mSourceId;
    private GVRAudioManager mAudioListener;
    private float mVolume;
    private boolean mLoop = false;

    /**
     * Constructs a new sound source.
     *
     * @param gvrContext GVR context.
     */
    public GVRAudioSource(GVRContext gvrContext)
    {
        super(gvrContext);
        mHasFrameCallback = false;
        mType = getComponentType();
        mAudioListener = null;
        mSoundFile = null;
        mSourceId = GvrAudioEngine.INVALID_ID;
        mVolume = 1f;
    }

    static public long getComponentType() { return TYPE_AUDIO; }

    private boolean canPlay()
    {
        return (isEnabled() && (mAudioListener != null) && (getOwnerObject() != null));
    }

    /**
     * Internal function to associate the audio source with
     * a specific GVRAudioManager
     * @param listener GVRAudioManager to use for this source
     */
    void setListener(GVRAudioManager listener)
    {
        if ((listener == null) && isPlaying())
        {
            stop();
        }
        mAudioListener = listener;
    }

    /**
     * Preloads a sound file.
     *
     * @param soundFile path/name of the file to be played.
     */
    public void load(String soundFile)
    {
        if (mSoundFile != null)
        {
            unload();
        }
        mSoundFile = soundFile;
        if (mAudioListener != null)
        {
            mAudioListener.getAudioEngine().preloadSoundFile(soundFile);
            Log.d("SOUND", "loaded audio file %s", getSoundFile());
        }
    }

    /**
     * Unloads the sound file for this source, if any.
     */
    public void unload()
    {
        if (mAudioListener != null)
        {
            Log.d("SOUND", "unloading audio source %d %s", getSourceId(), getSoundFile());
            mAudioListener.getAudioEngine().unloadSoundFile(getSoundFile());
        }
        mSoundFile = null;
        mSourceId = GvrAudioEngine.INVALID_ID;
    }

    /**
     * Gets the name of the sound file for this source.
     * @return sound file name or null if none
     */
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
    public void onDetach(GVRSceneObject owner)
    {
        super.onDetach(owner);
        if (mAudioListener != null)
        {
            stop();
            mAudioListener.removeSource(this);
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        stop();
    }

    public boolean isPlaying()
    {
        if ((mAudioListener != null) &&
            (getSourceId() != GvrAudioEngine.INVALID_ID))
        {
            return mAudioListener.getAudioEngine().isSoundPlaying(getSourceId());
        }
        return false;
    }

    /**
     * Starts the playback of a preloaded sound.
     */
    public void play() {
        play(mLoop);
    }

    /**
     * Starts the playback of a preloaded sound.
     *
     * @param looped enables looped playback
     */
    public void play(boolean looped)
    {
        mLoop = looped;
        if (canPlay())
        {
            onPlay();
        }
    }

    /**
     * Starts the playback of a sound.
     *
     * @param looped enables looped playback
     */
    public void play(String soundFile, boolean looped)
    {
        mSoundFile = soundFile;
        play();
    }

    protected void onPlay()
    {
        String fileName = getSoundFile();
        if ((fileName == null) || (fileName.equals("")))
        {
            return;
        }
        GvrAudioEngine audioEngine = mAudioListener.getAudioEngine();
        int sourceId = getSourceId();
        if (sourceId == GvrAudioEngine.INVALID_ID)
        {
            sourceId = audioEngine.createSoundObject(fileName);
        }
        if (sourceId != GvrAudioEngine.INVALID_ID)
        {
            setSourceId(sourceId);
            audioEngine.setSoundVolume(sourceId, getVolume());
            updatePosition(audioEngine);
            audioEngine.playSound(sourceId, mLoop);
            Log.d("SOUND", "playing audio source %d %s", sourceId, fileName);
        }
    }

    void updatePosition(GvrAudioEngine audioEngine)
    {
        int sourceId = getSourceId();
        if (sourceId != GvrAudioEngine.INVALID_ID)
        {
            GVRTransform t = getTransform();
            audioEngine.setSoundObjectPosition(sourceId, t.getPositionX(), t.getPositionY(), t.getPositionZ());
        }
    }

    /**
     * Pauses the playback of a sound.
     */
    public void pause()
    {
        if (mAudioListener != null)
        {
            int sourceId = getSourceId();
            if (sourceId != GvrAudioEngine.INVALID_ID)
            {
                mAudioListener.getAudioEngine().pauseSound(sourceId);
            }
        }
    }

    /**
     * Resumes the playback of a sound.
     */
    public void resume()
    {
        if (canPlay())
        {
            onResume();
        }
    }

    protected void onResume()
    {
        GvrAudioEngine audioEngine = mAudioListener.getAudioEngine();
        int sourceId = getSourceId();
        if (sourceId != GvrAudioEngine.INVALID_ID)
        {
            audioEngine.setSoundVolume(sourceId, mVolume);
            updatePosition(audioEngine);
            audioEngine.resumeSound(sourceId);
        }
    }

    /**
     * Stops the playback of a sound and destroys the corresponding Sound Object or Soundfield.
     */
    public void stop()
    {
        if (mAudioListener != null)
        {
            Log.d("SOUND", "stopping audio source %d %s", getSourceId(), getSoundFile());
            mAudioListener.getAudioEngine().stopSound(getSourceId());
        }
    }

    /**
     * Changes the volume of an existing sound.
     * @param volume volume value. Should range from 0 (mute) to 1 (max)
     */
    public void setVolume(float volume)
    {
        // Save this in case this audio source is not being played yet
        mVolume = volume;
        if (isPlaying() && (getSourceId() != GvrAudioEngine.INVALID_ID))
        {
            // This will actually work only if the sound file is being played
            mAudioListener.getAudioEngine().setSoundVolume(getSourceId(), getVolume());
        }
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
