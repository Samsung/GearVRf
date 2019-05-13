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

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.view.Surface;

import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRExternalImage;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTexture;

/**
 * A {@linkplain GVRSceneObject scene object} that shows video, using the
 * Android {@link MediaPlayer}.
 */
public class GVRVideoSceneObject extends GVRSceneObject {
    private volatile GVRVideo mVideo;
    private GVRVideoSceneObjectPlayer gvrVideoSceneObjectPlayer = null;

    /** Video type constants, for use with {@link GVRVideoSceneObject} */
    public abstract class GVRVideoType {
        public static final int MONO = 0;
        public static final int HORIZONTAL_STEREO = 1;
        public static final int VERTICAL_STEREO = 2;
    };

    private static class ActivityEventsListener extends GVREventListeners.ActivityEvents {
        private boolean wasPlaying;
        private GVRVideoSceneObjectPlayer mPlayer;

        private ActivityEventsListener(GVRVideoSceneObjectPlayer player) {
            mPlayer = player;
        }

        @Override
        public void onPause() {
            wasPlaying = mPlayer.isPlaying();
            mPlayer.pause();
        }

        @Override
        public void onResume() {
            if (wasPlaying) {
                mPlayer.start();
            }
        }
    };
    private ActivityEventsListener mActivityEventsListener;

    /**
     * Play a video on a {@linkplain GVRSceneObject scene object} with an
     * arbitrarily complex geometry, using the Android {@link MediaPlayer}
     *
     * @param gvrContext
     *            current {@link GVRContext}
     * @param mesh
     *            a {@link GVRMesh} - see
     *            {@link GVRAssetLoader#loadMesh(org.gearvrf.GVRAndroidResource)}
     *            and {@link GVRContext#createQuad(float, float)}
     * @param mediaPlayer
     *            an Android {@link MediaPlayer}
     * @param image
     *            a {@link GVRExternalImage} to link with {@link MediaPlayer}
     * @param videoType
     *            One of the {@linkplain GVRVideoType video type constants}
     * @throws IllegalArgumentException
     *             on an invalid {@code videoType} parameter
     */
    public GVRVideoSceneObject(final GVRContext gvrContext, GVRMesh mesh,
                               final MediaPlayer mediaPlayer, final GVRExternalImage image,
                               int videoType) {
        this(gvrContext, mesh, makePlayerInstance(mediaPlayer), new GVRTexture(image), videoType);
    }

    /**
     * Play a video on a {@linkplain GVRSceneObject scene object} with an
     * arbitrarily complex geometry, using the Android {@link MediaPlayer}
     *
     * @param gvrContext
     *            current {@link GVRContext}
     * @param mesh
     *            a {@link GVRMesh} - see
     *            {@link GVRAssetLoader#loadMesh(org.gearvrf.GVRAndroidResource)}
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
        this(gvrContext, mesh, makePlayerInstance(mediaPlayer), videoType);
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
        this(gvrContext, width, height, makePlayerInstance(mediaPlayer), videoType);
    }

    /**
     * Play a video on a {@linkplain GVRSceneObject scene object} with an
     * arbitrarily complex geometry, using the Android {@link MediaPlayer}
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param mesh
     *            a {@link GVRMesh} - see
     *            {@link GVRAssetLoader#loadMesh(org.gearvrf.GVRAndroidResource)}
     *            and {@link GVRContext#createQuad(float, float)}
     * @param mediaPlayer
     *            a wrapper for a media player
     * @param texture
     *            a {@link GVRTexture} to link with {@link MediaPlayer}
     * @param videoType
     *            One of the {@linkplain GVRVideoType video type constants}
     * @throws IllegalArgumentException
     *             on an invalid {@code videoType} parameter
     */
    public GVRVideoSceneObject(final GVRContext gvrContext, GVRMesh mesh,
                               final GVRVideoSceneObjectPlayer mediaPlayer, final GVRTexture texture,
                               int videoType) {
        super(gvrContext, mesh);
        GVRShaderId materialType;

        gvrVideoSceneObjectPlayer = mediaPlayer;
        mActivityEventsListener = new ActivityEventsListener(gvrVideoSceneObjectPlayer);
        gvrContext.getApplication().getEventReceiver().addListener(mActivityEventsListener);

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
                throw new IllegalArgumentException();
        }
        GVRMaterial material = new GVRMaterial(gvrContext, materialType);
        material.setMainTexture(texture);
        getRenderData().setMaterial(material);

        gvrContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                // Because texture.id() is called, this needs to run in GL thread
                mVideo = new GVRVideo(gvrContext, mediaPlayer, texture);
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
     *            {@link GVRAssetLoader#loadMesh(org.gearvrf.GVRAndroidResource)}
     *            and {@link GVRContext#createQuad(float, float)}
     * @param mediaPlayer
     *            a wrapper for a media player
     * @param videoType
     *            One of the {@linkplain GVRVideoType video type constants}
     * @throws IllegalArgumentException
     *             on an invalid {@code videoType} parameter
     */
    public GVRVideoSceneObject(final GVRContext gvrContext, GVRMesh mesh,
            final GVRVideoSceneObjectPlayer mediaPlayer, int videoType) {
        this(gvrContext, mesh, mediaPlayer, new GVRTexture(new GVRExternalImage(gvrContext)), videoType);
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
     *            a wrapper for a video player
     * @param videoType
     *            One of the {@linkplain GVRVideoType video type constants}
     * @throws IllegalArgumentException
     *             on an invalid {@code videoType} parameter
     */
    public GVRVideoSceneObject(GVRContext gvrContext, float width,
            float height, GVRVideoSceneObjectPlayer mediaPlayer, int videoType) {
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
    public GVRVideoSceneObjectPlayer getMediaPlayer() {
        return gvrVideoSceneObjectPlayer;
    }

    /**
     * Sets the current {@link MediaPlayer}
     * 
     * @param mediaPlayer
     *            An Android {@link MediaPlayer}
     */
    public void setMediaPlayer(final GVRVideoSceneObjectPlayer mediaPlayer) {
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
        getGVRContext().getApplication().getEventReceiver().removeListener(mActivityEventsListener);
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

    @Override
    protected void finalize() throws Throwable {
        try {
            getGVRContext().getApplication().getEventReceiver().removeListener(mActivityEventsListener);
            if (null != mVideo) {
                mVideo.release();
            }
        } finally {
            super.finalize();
        }
    }

    private static class GVRVideo {

        private final GVRContext mContext;
        private SurfaceTexture mSurfaceTexture = null;
        private GVRVideoSceneObjectPlayer mMediaPlayer;
        private boolean mActive = true;

        /**
         * Constructs a GVRVideo with a {@link MediaPlayer} and a
         * {@link GVRTexture} to be used
         * 
         * @param mediaPlayer
         *            the {@link MediaPlayer} type object to be used in the
         *            class
         * @param texture
         *            the {@link GVRTexture} type object to be used in
         *            the class
         */
        public GVRVideo(GVRContext gvrContext, GVRVideoSceneObjectPlayer mediaPlayer, GVRTexture texture) {
            mContext = gvrContext;
            mSurfaceTexture = new SurfaceTexture(texture.getId());
            if (mediaPlayer != null) {
                setMediaPlayer(mediaPlayer);
            }

            mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                Runnable onFrameAvailableGLCallback = new Runnable() {
                    @Override
                    public void run() {
                        mSurfaceTexture.updateTexImage();
                    }
                };

                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    mContext.runOnGlThread(onFrameAvailableGLCallback);
                }
            });
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
        public GVRVideoSceneObjectPlayer getMediaPlayer() {
            return mMediaPlayer;
        }

        /**
         * Set the {@link MediaPlayer} used to show video
         * 
         * @param mediaPlayer
         *            An Android {@link MediaPlayer}
         */
        public void setMediaPlayer(GVRVideoSceneObjectPlayer mediaPlayer) {
            release(); // any current MediaPlayer

            mMediaPlayer = mediaPlayer;
            Surface surface = new Surface(mSurfaceTexture);
            mediaPlayer.setSurface(surface);

            if (mediaPlayer.canReleaseSurfaceImmediately()) {
                surface.release();
            }
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
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    }

    /**
     * Creates a player wrapper for the Android MediaPlayer.
     */
    public static GVRVideoSceneObjectPlayer<MediaPlayer> makePlayerInstance(final MediaPlayer mediaPlayer) {
        return new GVRVideoSceneObjectPlayer<MediaPlayer>() {
            @Override
            public MediaPlayer getPlayer() {
                return mediaPlayer;
            }

            @Override
            public void setSurface(Surface surface) {
                mediaPlayer.setSurface(surface);
            }

            @Override
            public void release() {
                mediaPlayer.release();
            }

            @Override
            public boolean canReleaseSurfaceImmediately() {
                return true;
            }

            @Override
            public void pause() {
                try {
                    mediaPlayer.pause();
                } catch (final IllegalStateException exc) {
                    //intentionally ignored; might have been released already or never got to be
                    //initialized
                }
            }

            @Override
            public void start() {
                mediaPlayer.start();
            }

            @Override
            public boolean isPlaying() {
                return mediaPlayer.isPlaying();
            }
        };
    }
}
