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

package org.gearvrf.scene_objects;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMaterial.GVRShaderType;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.view.Surface;

/**
 * A {@linkplain GVRSceneObject scene object} that shows video, using the
 * Android {@link MediaPlayer}.
 */
public class GVRVideoSceneObject extends GVRSceneObject {
    private GVRVideo mVideo;

    /** Video type constants, for use with {@link GVRVideoSceneObject} */
    public abstract class GVRVideoType {
        public static final int MONO = 0;
        public static final int HORIZONTAL_STEREO = 1;
        public static final int VERTICAL_STEREO = 2;
    };

    /**
     * Play a video on a {@linkplain GVRSceneObject scene object} with an
     * arbitrarily complex geometry, using the Android {@link MediaPlayer}
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param mesh
     *            a {@link GVRMesh} - see
     *            {@link GVRContext#loadMesh(org.gearvrf.GVRAndroidResource)}
     *            and {@link GVRContext#createQuad(float, float)}
     * @param mediaPlayer
     *            an Android {@link MediaPlayer}
     * @param texture
     *            a {@link GVRExternalTexture} to link with {@link MediaPlayer}
     * @param videoType
     *            One of the {@linkplain GVRVideoType video type constants}
     * @throws IllegalArgumentException
     *             on an invalid {@code videoType} parameter
     */
    public GVRVideoSceneObject(final GVRContext gvrContext, GVRMesh mesh,
                               final MediaPlayer mediaPlayer, final GVRExternalTexture texture,
                               int videoType) {
        super(gvrContext, mesh);
        GVRMaterialShaderId materialType;

        switch (videoType) {
            case GVRVideoType.MONO:
                materialType = GVRShaderType.OES.ID;
                break;
            case GVRVideoType.HORIZONTAL_STEREO:
                materialType = GVRShaderType.OESHorizontalStereo.ID;
                break;
            case GVRVideoType.VERTICAL_STEREO:
                materialType = GVRShaderType.OESVerticalStereo.ID;
                break;
            default:
                try {
                    texture.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                throw new IllegalArgumentException();
        }
        GVRMaterial material = new GVRMaterial(gvrContext, materialType);
        material.setMainTexture(texture);
        getRenderData().setMaterial(material);

        gvrContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                // Because texture.getId() is called, this needs to run in GL thread
                mVideo = new GVRVideo(gvrContext, mediaPlayer, texture);
                gvrContext.registerDrawFrameListener(mVideo);
            }
        });
    }

    /**
     * Play a video on a {@linkplain GVRSceneObject scene object} with an
     * arbitrarily complex geometry, using the Android {@link MediaPlayer}
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param mesh
     *            a {@link GVRMesh} - see
     *            {@link GVRContext#loadMesh(org.gearvrf.GVRAndroidResource)}
     *            and {@link GVRContext#createQuad(float, float)}
     * @param mediaPlayer
     *            an Android {@link MediaPlayer}
     * @param videoType
     *            One of the {@linkplain GVRVideoType video type constants}
     * @throws IllegalArgumentException
     *             on an invalid {@code videoType} parameter
     */
    public GVRVideoSceneObject(final GVRContext gvrContext, GVRMesh mesh,
            final MediaPlayer mediaPlayer, int videoType) {
        this(gvrContext, mesh, mediaPlayer, new GVRExternalTexture(gvrContext), videoType);
    }

    /**
     * Play a video on a 2D, rectangular {@linkplain GVRSceneObject scene
     * object,} using the Android {@link MediaPlayer}
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param width
     *            the rectangle's width
     * @param height
     *            the rectangle's height
     * @param mediaPlayer
     *            an Android {@link MediaPlayer}
     * @param videoType
     *            One of the {@linkplain GVRVideoType video type constants}
     * @throws IllegalArgumentException
     *             on an invalid {@code videoType} parameter
     */
    public GVRVideoSceneObject(GVRContext gvrContext, float width,
            float height, MediaPlayer mediaPlayer, int videoType) {
        this(gvrContext, gvrContext.createQuad(width, height), mediaPlayer,
                videoType);
    }

    /**
     * Poll the {@link MediaPlayer} once per frame.
     * 
     * <p>
     * This call does not directly affect the {@link MediaPlayer}. In
     * particular, activation is not the same as calling
     * {@link MediaPlayer#start()}.
     */
    public void activate() {
        if (mVideo == null) {
            return;
        }

        mVideo.activate();
    }

    /**
     * Stop polling the {@link MediaPlayer}.
     * 
     * <p>
     * This call does not directly affect the {@link MediaPlayer}. In
     * particular, deactivation is not the same as calling
     * {@link MediaPlayer#pause()}.
     */
    public void deactivate() {
        if (mVideo == null) {
            return;
        }

        mVideo.deactivate();
    }

    /**
     * Returns the current {@link MediaPlayer} status.
     * 
     * See {@link #activate()} and {@link #deactivate()}: polling activation is
     * not correlated with the {@code MediaPlayer} state.
     * 
     * @return Whether or not we polling the {@code MediaPlayer} every frame.
     */
    public boolean isActive() {
        if (mVideo == null) {
            return false;
        }

        return mVideo.isActive();
    }

    /**
     * Returns the current {@link MediaPlayer}, if any
     * 
     * @return current {@link MediaPlayer}
     */
    public MediaPlayer getMediaPlayer() {
        if (mVideo == null) {
            return null;
        }

        return mVideo.getMediaPlayer();
    }

    /**
     * Sets the current {@link MediaPlayer}
     * 
     * @param mediaPlayer
     *            An Android {@link MediaPlayer}
     */
    public void setMediaPlayer(final MediaPlayer mediaPlayer) {
        if (mVideo == null) {
            getGVRContext().runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    mVideo.setMediaPlayer(mediaPlayer);
                }});
        } else {
            mVideo.setMediaPlayer(mediaPlayer);
        }
    }

    /**
     * Reset and {@link MediaPlayer#release() release()} the current
     * {@link MediaPlayer}, if any
     */
    public void release() {
        if (mVideo == null) {
            return;
        }

        mVideo.release();
    }

    /**
     * Returns the current time stamp, in nanoseconds. This comes from
     * {@link SurfaceTexture#getTimestamp()}: you should read the Android
     * documentation on that before you use this value.
     *
     * @return current time stamp, in nanoseconds. 0 if the video is not ready.
     */
    public long getTimeStamp() {
        if (mVideo == null) {
            return 0; // time stamp not available yet
        }

        return mVideo.getTimeStamp();
    }

    private static class GVRVideo implements GVRDrawFrameListener {

        private final GVRContext mContext;
        private SurfaceTexture mSurfaceTexture = null;
        private WeakReference<MediaPlayer> mMediaPlayerRef = null;
        private boolean mActive = true;

        /**
         * Constructs a GVRVideo with a {@link MediaPlayer} and a
         * {@link GVRExternalTexture} to be used
         * 
         * @param mediaPlayer
         *            the {@link MediaPlayer} type object to be used in the
         *            class
         * @param texture
         *            the {@link GVRExternalTexture} type object to be used in
         *            the class
         */
        public GVRVideo(GVRContext gvrContext, MediaPlayer mediaPlayer, GVRExternalTexture texture) {
            mContext = gvrContext;
            mSurfaceTexture = new SurfaceTexture(texture.getId());
            if (mediaPlayer != null) {
                setMediaPlayer(mediaPlayer);
            }
        }

        /**
         * On top of the various {@link MediaPlayer} states, this wrapper may be
         * 'active' or 'inactive'. When the wrapper is active, it updates the
         * screen each time {@link GVRDrawFrameListener#onDrawFrame(float)} is
         * called; when the wrapper is inactive, {@link MediaPlayer} changes do
         * not show on the screen.
         * 
         * <p>
         * Note that calling {@link #activate()} does not call
         * {@link MediaPlayer#start()}, and calling {@link #deactivate()} does
         * not call {@link MediaPlayer#pause()}.
         * 
         * @return Whether this wrapper is actively polling its
         *         {@link MediaPlayer}
         */
        public boolean isActive() {
            return mActive;
        }

        /**
         * Tell the wrapper to poll its {@link MediaPlayer} each time
         * {@link GVRDrawFrameListener#onDrawFrame(float)} is called.
         * 
         * <p>
         * Note that activation is not the same as calling
         * {@link MediaPlayer#start()}.
         */
        public void activate() {
            mActive = true;
        }

        /**
         * Tell the wrapper to stop polling its {@link MediaPlayer} each time
         * {@link GVRDrawFrameListener#onDrawFrame(float)} is called.
         * 
         * <p>
         * Note that deactivation is not the same as calling
         * {@link MediaPlayer#pause()}.
         */
        public void deactivate() {
            mActive = false;
        }

        /**
         * Returns the current {@link MediaPlayer}, if any
         * 
         * @return the current {@link MediaPlayer}
         */
        public MediaPlayer getMediaPlayer() {
            return mMediaPlayerRef.get();
        }

        /**
         * Set the {@link MediaPlayer} used to show video
         * 
         * @param mediaPlayer
         *            An Android {@link MediaPlayer}
         */
        public void setMediaPlayer(MediaPlayer mediaPlayer) {
            release(); // any current MediaPlayer

            mMediaPlayerRef = new WeakReference<MediaPlayer>(mediaPlayer);
            Surface surface = new Surface(mSurfaceTexture);
            mediaPlayer.setSurface(surface);
            surface.release();
        }

        /**
         * Returns the current time stamp, in nanoseconds. This comes from
         * {@link SurfaceTexture#getTimestamp()}: you should read the Android
         * documentation on that before you use this value.
         * 
         * @return current time stamp, in nanoseconds
         */
        public long getTimeStamp() {
            return mSurfaceTexture.getTimestamp();
        }

        /**
         * Reset and {@link MediaPlayer#release() release()} the
         * {@link MediaPlayer}
         */
        public void release() {
            if (mMediaPlayerRef != null) {
                MediaPlayer mediaPlayer = mMediaPlayerRef.get();
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
            }
        }

        @Override
        public void onDrawFrame(float drawTime) {
            MediaPlayer mediaPlayer = mMediaPlayerRef.get();
            if (mediaPlayer != null && mActive) {
                mSurfaceTexture.updateTexImage();
            }

            if (mediaPlayer == null) {
                mContext.unregisterDrawFrameListener(this);
            }
        }
    }

}
