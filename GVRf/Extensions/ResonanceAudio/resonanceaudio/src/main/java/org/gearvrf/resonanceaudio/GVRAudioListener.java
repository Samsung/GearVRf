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
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.joml.Quaternionf;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a listener in the scene. It records the sound around it and plays through the
 * speakers. It is allowed to have only one listener in the scene.
 */
public class GVRAudioListener extends GVRComponent {
    private static final String TAG = GVRAudioListener.class.getSimpleName();

    private static long TYPE_AUDIO = newComponentType(GVRAudioListener.class);

    private final List<GVRAudioSource> mAudioSources;

    private final GvrAudioEngine mAudioEngine;

    private final GVRScene mScene;

    public GVRAudioListener(GVRContext gvrContext, GVRScene scene) {
        super(gvrContext, 0L);

        mType = getComponentType();

        mScene = scene;
        mScene.getRoot().attachComponent(this);

        mAudioSources = new ArrayList<>();

        mAudioEngine = new GvrAudioEngine(gvrContext.getActivity(), GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
        mAudioEngine.pause();

        AudioEngineEventsHandler.getInstance().startEventsHandler(gvrContext);
    }

    public static long getComponentType() { return TYPE_AUDIO; }

    protected static long newComponentType(Class<? extends GVRAudioListener> clazz) {
        long hash = (long) clazz.hashCode() << 32;
        long t = System.currentTimeMillis() & 0xfffffff;
        long result = hash | t;
        return result;
    }

    void addSource(GVRAudioSource audioSource) {
        mAudioSources.add(audioSource);
    }

    void removeSource(GVRAudioSource audioSource) {
        mAudioSources.remove(audioSource);
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
        mAudioEngine.pause();
    }

    private void resume() {
        if (isEnabled() && mScene == getGVRContext().getMainScene()) {
            mAudioEngine.resume();
        }
    }

    void loadSoundFile(GVRAudioSource audioSource) {
        mAudioEngine.preloadSoundFile(audioSource.getSoundFile());
    }

    void unloadSoundFile(GVRAudioSource audioSource) {
        mAudioEngine.unloadSoundFile(audioSource.getSoundFile());
    }

    void playSound(GVRAudioSource audioSource, boolean looping) {
        if (audioSource.getOwnerObject().getParent() != mScene.getRoot()) {
            throw new UnsupportedOperationException("Audio source must be at the same root as audio listener");
        }

        int sourceId = mAudioEngine.createSoundObject(audioSource.getSoundFile());
        audioSource.setSourceId(sourceId);
        // TODO: Check GvrAudioEngine.INVALID_ID

        mAudioEngine.setSoundVolume(sourceId, audioSource.getVolume());

        updateSoundPosition(audioSource);
        mAudioEngine.playSound(audioSource.getSourceId(), looping);
    }

    void pauseSound(GVRAudioSource audioSource) {
        mAudioEngine.pauseSound(audioSource.getSourceId());
    }

    void resumeSound(GVRAudioSource audioSource) {
        if (audioSource.getOwnerObject().getParent() != mScene.getRoot()) {
            throw new UnsupportedOperationException("Audio source must be at the same root as audio listener");
        }

        updateSoundPosition(audioSource);
        mAudioEngine.resumeSound(audioSource.getSourceId());
    }

    void stopSound(GVRAudioSource audioSource) {
        mAudioEngine.stopSound(audioSource.getSourceId());
    }

    void setSoundVolume(GVRAudioSource audioSource, float volume) {
        mAudioEngine.setSoundVolume(audioSource.getSourceId(), volume);
    }

    boolean isSoundPlaying(GVRAudioSource audioSource) {
        if (audioSource.getSourceId() == GvrAudioEngine.INVALID_ID) {
            return false;
        }

        return mAudioEngine.isSoundPlaying(audioSource.getSourceId());
    }

    private void updateSoundPosition(GVRAudioSource audioSource) {
        GVRTransform t = audioSource.getTransform();

        //TODO: Should move it to audio thread!?
        mAudioEngine.setSoundObjectPosition(audioSource.getSourceId(),
                t.getPositionX(), t.getPositionY(), t.getPositionZ());
    }

    private void updateHeadTransform() {
        GVRTransform owner = mScene.getMainCameraRig().getOwnerObject().getTransform();
        Quaternionf rotation = new Quaternionf(owner.getRotationX(),
                owner.getRotationY(), owner.getRotationZ(), owner.getRotationW());

        GVRTransform head = mScene.getMainCameraRig().getHeadTransform();
        rotation.mul(head.getRotationX(),
                head.getRotationY(), head.getRotationZ(), head.getRotationW());

        mAudioEngine.setHeadPosition(owner.getPositionX(),
                owner.getPositionY(), owner.getPositionZ());
        mAudioEngine.setHeadRotation(rotation.x, rotation.y, rotation.z, rotation.w);
    }

    private void update() {
        if (!isEnabled()) {
            return;
        }

        updateHeadTransform();

        for (GVRAudioSource audioSource: mAudioSources) {
            updateSoundPosition(audioSource);
        }

        // TODO: maybe the following method should not be called from rendering thread
        mAudioEngine.update();
    }

    @Override
    public void setOwnerObject(GVRSceneObject owner) {
        // This component will only be attached to scene root object
        if (owner != null && owner != mScene.getRoot()) {
            throw new UnsupportedOperationException("Audio listener can not change owner");
        }

        super.setOwnerObject(owner);
    }

    private static class AudioEngineEventsHandler extends GVREventListeners.ActivityEvents
            implements GVRDrawFrameListener {
        private static WeakReference<AudioEngineEventsHandler> mInstance = new WeakReference<AudioEngineEventsHandler>(null);

        private GVRContext mContext;
        private GVRAudioListener mCurrentListener;
        private GVRScene mCurrentScene;

        public static synchronized AudioEngineEventsHandler getInstance() {
            if (mInstance.get() == null) {
                mInstance = new WeakReference<AudioEngineEventsHandler>(new AudioEngineEventsHandler());
            }

            return mInstance.get();
        }

        private AudioEngineEventsHandler() {}

        private void startEventsHandler(GVRContext gvrContext) {
            if (mContext == null) {
                mContext = gvrContext;
                mCurrentListener = null;
                mCurrentScene = null;

                mContext.registerDrawFrameListener(this);
                mContext.getActivity().getEventReceiver().addListener(this);
            }
        }

        private void stopEventsHandler() {
            if (mContext != null) {
                mContext.getActivity().getEventReceiver().removeListener(this);
                mContext.unregisterDrawFrameListener(this);
                mContext = null;
            }
        }

        @Override
        public void onDrawFrame(float v) {
            // TODO: Optimize this.
            if (mContext.getMainScene() != mCurrentScene) {
                // GVRf doesn't provide a way to detect scene exchanges.
                onMainSceneChanged(mContext.getMainScene());
            }

            if (mCurrentListener != null) {
                mCurrentListener.update();
            }
        }

        @Override
        public void onPause() {
            super.onPause();

            // Will pause current listener and the resume will occur at next onDrawFrame call.
            onMainSceneChanged( null);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            GVRActivity activity = mContext.getActivity();

            if (activity.isFinishing()) {
                stopEventsHandler();
            }
        }

        private void onMainSceneChanged(GVRScene newScene) {
            if (mCurrentListener != null) {
                mCurrentListener.pause();
            }

            mCurrentScene = newScene;

            if (newScene != null) {
                resumeAudioListenerFromScene(newScene);
            }
        }

        private  void resumeAudioListenerFromScene(GVRScene scene) {
            mCurrentListener = (GVRAudioListener)scene.
                    getRoot().getComponent(GVRAudioListener.getComponentType());

            if (mCurrentListener != null) {
                mCurrentListener.resume();
            }
        }
    }
}
