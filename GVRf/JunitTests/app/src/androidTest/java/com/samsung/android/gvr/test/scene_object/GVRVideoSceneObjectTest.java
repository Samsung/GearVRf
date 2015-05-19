package com.samsung.android.gvr.test.scene_object;

import android.media.MediaPlayer;

import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.scene_objects.GVRVideoSceneObject;

public class GVRVideoSceneObjectTest extends ActivityInstrumentationGVRf {


    public void testCreateGVRVideoSceneObject() {

        MediaPlayer mediaPlayer = new MediaPlayer();
        assertNotNull(new GVRVideoSceneObject(TestDefaultGVRViewManager.mGVRContext, 100f, 100f, mediaPlayer, GVRVideoSceneObject.GVRVideoType.HORIZONTAL_STEREO));
    }

    public void testCreateNameVideoSceneObject() {

        MediaPlayer mediaPlayer = new MediaPlayer();
        GVRVideoSceneObject videoSceneObject = new GVRVideoSceneObject(TestDefaultGVRViewManager.mGVRContext, 100f, 100f, mediaPlayer, GVRVideoSceneObject.GVRVideoType.HORIZONTAL_STEREO);
        videoSceneObject.setName("Test");
        assertEquals(videoSceneObject.getName(), "Test");
    }

    public void testStartVideo() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        GVRVideoSceneObject videoSceneObject = new GVRVideoSceneObject(TestDefaultGVRViewManager.mGVRContext, 100f, 100f, mediaPlayer, GVRVideoSceneObject.GVRVideoType.HORIZONTAL_STEREO);
        videoSceneObject.getMediaPlayer().start();
    }

    public void testStopVideo() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        GVRVideoSceneObject videoSceneObject = new GVRVideoSceneObject(TestDefaultGVRViewManager.mGVRContext, 100f, 100f, mediaPlayer, GVRVideoSceneObject.GVRVideoType.HORIZONTAL_STEREO);
        videoSceneObject.getMediaPlayer().start();
        videoSceneObject.getMediaPlayer().stop();
    }

    public void testPauseVideo() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        GVRVideoSceneObject videoSceneObject = new GVRVideoSceneObject(TestDefaultGVRViewManager.mGVRContext, 100f, 100f, mediaPlayer, GVRVideoSceneObject.GVRVideoType.HORIZONTAL_STEREO);
        videoSceneObject.getMediaPlayer().start();
        videoSceneObject.getMediaPlayer().pause();
    }

    public void testGetGVRVideoSceneObjectTransform() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        GVRVideoSceneObject videoSceneObject = new GVRVideoSceneObject(TestDefaultGVRViewManager.mGVRContext, 100f, 100f, mediaPlayer, GVRVideoSceneObject.GVRVideoType.HORIZONTAL_STEREO);
        assertNotNull(videoSceneObject.getTransform());
    }

    public void testGetGVRVideoSceneObjectTimeStamp() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        GVRVideoSceneObject videoSceneObject = new GVRVideoSceneObject(TestDefaultGVRViewManager.mGVRContext, 100f, 100f, mediaPlayer, GVRVideoSceneObject.GVRVideoType.HORIZONTAL_STEREO);
        assertNotNull(videoSceneObject.getTimeStamp());
    }

    public void testGetGVRVideoSceneIsActive() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        GVRVideoSceneObject videoSceneObject = new GVRVideoSceneObject(TestDefaultGVRViewManager.mGVRContext, 100f, 100f, mediaPlayer, GVRVideoSceneObject.GVRVideoType.HORIZONTAL_STEREO);
        assertTrue(videoSceneObject.isActive());
    }

    public void testGetGVRVideoSceneCameraRig() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        GVRVideoSceneObject videoSceneObject = new GVRVideoSceneObject(TestDefaultGVRViewManager.mGVRContext, 100f, 100f, mediaPlayer, GVRVideoSceneObject.GVRVideoType.VERTICAL_STEREO);
        GVRCameraRig cameraRig = new GVRCameraRig(TestDefaultGVRViewManager.mGVRContext);
        videoSceneObject.attachCameraRig(cameraRig);
        videoSceneObject.getMediaPlayer().start();
        assertNotNull(videoSceneObject.getCameraRig());
    }

    public void testDeactivateVideo() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        GVRVideoSceneObject videoSceneObject = new GVRVideoSceneObject(TestDefaultGVRViewManager.mGVRContext, 100f, 100f, mediaPlayer, GVRVideoSceneObject.GVRVideoType.MONO);
        videoSceneObject.deactivate();
        assertFalse(videoSceneObject.isActive());
    }

    public void testReleaseVideo() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        GVRVideoSceneObject videoSceneObject = new GVRVideoSceneObject(TestDefaultGVRViewManager.mGVRContext, 100f, 100f, mediaPlayer, GVRVideoSceneObject.GVRVideoType.HORIZONTAL_STEREO);
        videoSceneObject.getMediaPlayer().start();
        videoSceneObject.release();
    }

    public void testSetVideoTypeInvalid() {
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            new GVRVideoSceneObject(TestDefaultGVRViewManager.mGVRContext, 100f, 100f, mediaPlayer, 320);

        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "IllegalArgumentException for videoType. Types supported: " +
                    "GVRVideoSceneObject.GVRVideoType.MONO, " +
                    "GVRVideoSceneObject.GVRVideoType.HORIZONTAL_STEREO, " +
                    "GVRVideoSceneObject.GVRVideoType.VERTICAL_STEREO");
        }
    }
}
