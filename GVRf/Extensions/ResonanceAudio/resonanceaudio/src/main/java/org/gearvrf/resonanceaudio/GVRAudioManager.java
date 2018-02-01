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

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRTransform;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a listener in the scene. It records the sound around it and plays through the
 * speakers. It is allowed to have only one listener in the scene.
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
            mContext.getActivity().getEventReceiver().addListener(this);
            mAudioEngine.resume();
        }
        else
        {
            mContext.unregisterDrawFrameListener(this);
            mContext.getActivity().getEventReceiver().removeListener(this);
            mAudioEngine.pause();
        }
    }

    public boolean isEnabled()
    {
        return mEnabled;
    }

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

    public void removeSource(GVRAudioSource audioSource)
    {
        synchronized (mAudioSources)
        {
            audioSource.setListener(null);
            mAudioSources.remove(audioSource);
        }
    }

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
        GVRActivity activity = mContext.getActivity();
        if (activity.isFinishing())
        {
            setEnable(false);
        }
    }
}
