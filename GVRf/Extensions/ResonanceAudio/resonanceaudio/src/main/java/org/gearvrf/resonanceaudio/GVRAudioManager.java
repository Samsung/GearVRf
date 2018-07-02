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

import android.app.Activity;

import com.google.vr.sdk.audio.GvrAudioEngine;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRTransform;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages audio sources for the applications.
 * Handles pausing and resuming sound and maintains
 * a list of {@link GVRAudioSource} objects which are attached
 * to scene objects. The position of each audio source is
 * obtained from the {@link GVRTransform} of the {@GVRSceneObject}
 * the audio source is attached to. The audio manager also
 * tracks the head transform so the sound will be spatially
 * correct.
 */
public class GVRAudioManager extends GVREventListeners.ActivityEvents
        implements GVRDrawFrameListener
{
    private static final String TAG = GVRAudioManager.class.getSimpleName();

    private final List<GVRAudioSource> mAudioSources;

    private final GvrAudioEngine mAudioEngine;

    private final GVRContext mContext;

    private boolean mEnabled = false;


    public GVRAudioManager(GVRContext context)
    {
        mContext = context;
        mAudioSources = new ArrayList<>();
        mAudioEngine = new GvrAudioEngine(context.getActivity(),
                                          GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
        mAudioEngine.pause();
    }

    /**
     * Enables or disables sound.
     * When sound is disabled, nothing is played but the
     * audio sources remain intact.
     * @param flag true to enable sound, false to disable.
     */
    public void setEnable(boolean flag)
    {
        if (mEnabled == flag)
        {
            return;
        }
        mEnabled = flag;
        if (flag)
        {
            mContext.registerDrawFrameListener(this);
            mContext.getApplication().getEventReceiver().addListener(this);
            mAudioEngine.resume();
        }
        else
        {
            mContext.unregisterDrawFrameListener(this);
            mContext.getApplication().getEventReceiver().removeListener(this);
            mAudioEngine.pause();
        }
    }

    /**
     * Determine if audio engine is enabled.
     * @return true if enabled, false if not
     */
    public boolean isEnabled()
    {
        return mEnabled;
    }

    /**
     * Adds an audio source to the audio manager.
     * An audio source cannot be played unless it is
     * added to the audio manager. A source cannot be
     * added twice.
     * @param audioSource audio source to add
     */
    public void addSource(GVRAudioSource audioSource)
    {
        synchronized (mAudioSources)
        {
            if (!mAudioSources.contains(audioSource))
            {
                audioSource.setListener(this);
                mAudioSources.add(audioSource);
            }
        }
    }

    /**
     * Removes an audio source from the audio manager.
     * @param audioSource audio source to remove
     */
    public void removeSource(GVRAudioSource audioSource)
    {
        synchronized (mAudioSources)
        {
            audioSource.setListener(null);
            mAudioSources.remove(audioSource);
        }
    }

    /**
     * Stops all of the audio sources playing.
     * The sources are still associated with this
     * audio manager and their sound files remain loaded.
     */
    public void stop()
    {
        synchronized (mAudioSources)
        {
            for (GVRAudioSource source : mAudioSources)
            {
                source.stop();
            }
        }
    }

    /**
     * Remove all of the audio sources from the audio manager.
     * This will stop all sound from playing.
     */
    public void clearSources()
    {
        synchronized (mAudioSources)
        {
            for (GVRAudioSource source : mAudioSources)
            {
                source.setListener(null);
            }
            mAudioSources.clear();
        }
    }

    /**
     * Gets the underlying GvrAudioEngine
     * @return GvrAudioEngine for this audio manager
     */
    GvrAudioEngine getAudioEngine()
    {
        return mAudioEngine;
    }

    private void pause()
    {
        mAudioEngine.pause();
    }

    private void resume()
    {
        mAudioEngine.resume();
    }

    private void updateHeadTransform()
    {
        GVRCameraRig rig = mContext.getMainScene().getMainCameraRig();
        GVRTransform owner = rig.getOwnerObject().getTransform();
        Quaternionf rotation = new Quaternionf(owner.getRotationX(),
                                               owner.getRotationY(), owner.getRotationZ(),
                                               owner.getRotationW());

        GVRTransform head = rig.getHeadTransform();
        rotation.mul(head.getRotationX(),
                     head.getRotationY(), head.getRotationZ(), head.getRotationW());

        mAudioEngine.setHeadPosition(owner.getPositionX(),
                                     owner.getPositionY(), owner.getPositionZ());
        mAudioEngine.setHeadRotation(rotation.x, rotation.y, rotation.z, rotation.w);
    }

    private void update()
    {
        if (isEnabled())
        {
            updateHeadTransform();
            synchronized (mAudioSources)
            {
                for (GVRAudioSource audioSource : mAudioSources)
                {
                    audioSource.updatePosition(mAudioEngine);
                }
            }
            // TODO: maybe the following method should not be called from rendering thread
            mAudioEngine.update();
        }
    }

    @Override
    public void onDrawFrame(float v)
    {
        update();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        pause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Activity activity = mContext.getActivity();
        if (activity.isFinishing())
        {
            setEnable(false);
        }
    }
}
