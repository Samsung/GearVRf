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

package org.gearvrf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.utility.Log;
import org.gearvrf.debug.GVRConsole;

/** The scene graph */
public class GVRScene extends GVRHybridObject {
    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(GVRScene.class);

    private final List<GVRSceneObject> mSceneObjects = new ArrayList<GVRSceneObject>();
    private GVRCameraRig mMainCameraRig;

    /**
     * Constructs a scene with a camera rig holding left & right cameras in it.
     * 
     * @param gvrContext
     *            {@link GVRContext} the app is using.
     */
    public GVRScene(GVRContext gvrContext) {
        super(gvrContext, NativeScene.ctor());

        GVRCamera leftCamera = new GVRPerspectiveCamera(gvrContext);
        leftCamera.setRenderMask(GVRRenderMaskBit.Left);
        GVRSceneObject leftCameraObject = new GVRSceneObject(gvrContext);
        leftCameraObject.attachCamera(leftCamera);

        GVRCamera rightCamera = new GVRPerspectiveCamera(gvrContext);
        rightCamera.setRenderMask(GVRRenderMaskBit.Right);
        GVRSceneObject rightCameraObject = new GVRSceneObject(gvrContext);
        rightCameraObject.attachCamera(rightCamera);

        GVRSceneObject cameraRigObject = new GVRSceneObject(gvrContext);
        GVRCameraRig cameraRig = new GVRCameraRig(gvrContext);
        cameraRig.attachLeftCamera(leftCamera);
        cameraRig.attachRightCamera(rightCamera);
        cameraRigObject.attachCameraRig(cameraRig);

        addSceneObject(cameraRigObject);
        cameraRigObject.addChildObject(leftCameraObject);
        cameraRigObject.addChildObject(rightCameraObject);
        setMainCameraRig(cameraRig);
    }

    private GVRScene(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    /**
     * Add an {@linkplain GVRSceneObject scene object}
     * 
     * @param sceneObject
     *            The {@linkplain GVRSceneObject scene object} to add.
     */
    public void addSceneObject(GVRSceneObject sceneObject) {
        mSceneObjects.add(sceneObject);
        NativeScene.addSceneObject(getNative(), sceneObject.getNative());
    }

    /**
     * Remove a {@linkplain GVRSceneObject scene object}
     * 
     * @param sceneObject
     *            The {@linkplain GVRSceneObject scene object} to remove.
     */
    public void removeSceneObject(GVRSceneObject sceneObject) {
        mSceneObjects.remove(sceneObject);
        NativeScene.removeSceneObject(getNative(), sceneObject.getNative());
    }

    /**
     * The top-level scene objects.
     * 
     * @return A read-only list containing all the 'root' scene objects (those
     *         that were added directly to the scene).
     * 
     * @since 2.0.0
     */
    public List<GVRSceneObject> getSceneObjects() {
        return Collections.unmodifiableList(mSceneObjects);
    }

    /**
     * @return The {@link GVRCameraRig camera rig} used for rendering the scene
     *         on the screen.
     */
    public GVRCameraRig getMainCameraRig() {
        return mMainCameraRig;
    }

    /**
     * Set the {@link GVRCameraRig camera rig} used for rendering the scene on
     * the screen.
     * 
     * @param cameraRig
     *            The {@link GVRCameraRig camera rig} to render with.
     */
    public void setMainCameraRig(GVRCameraRig cameraRig) {
        mMainCameraRig = cameraRig;
        NativeScene.setMainCameraRig(getNative(), cameraRig.getNative());
    }

    /**
     * @return The flattened hierarchy of {@link GVRSceneObject objects} as an
     *         array.
     */
    public GVRSceneObject[] getWholeSceneObjects() {
        List<GVRSceneObject> list = new ArrayList<GVRSceneObject>(mSceneObjects);
        for (GVRSceneObject child : mSceneObjects) {
            addChildren(list, child);
        }
        return list.toArray(new GVRSceneObject[list.size()]);
    }

    private void addChildren(List<GVRSceneObject> list,
            GVRSceneObject sceneObject) {
        for (GVRSceneObject child : sceneObject.rawGetChildren()) {
            list.add(child);
            addChildren(list, child);
        }
    }

    /**
     * Sets the frustum culling for the {@link GVRScene}.
     */
    public void setFrustumCulling(boolean flag) {
        NativeScene.setFrustumCulling(getNative(), flag);
    }

    /**
     * Sets the occlusion query for the {@link GVRScene}.
     */
    public void setOcclusionQuery(boolean flag) {
        NativeScene.setOcclusionQuery(getNative(), flag);
    }

    private GVRConsole mStatsConsole = null;
    private boolean mStatsEnabled = false;
    private boolean pendingStats = false;

    /**
     * Returns whether displaying of stats is enabled for this scene.
     * 
     * @return whether displaying of stats is enabled for this scene.
     */
    public boolean getStatsEnabled() {
        return mStatsEnabled;
    }

    /**
     * Set whether to enable display of stats for this scene.
     *
     * @param enabled
     *     Flag to indicate whether to enable display of stats.
     */
    public void setStatsEnabled(boolean enabled) {
        pendingStats = enabled;
    }

    void updateStatsEnabled() {
        if(mStatsEnabled == pendingStats) {
            return;
        }

        mStatsEnabled = pendingStats;
        NativeScene.setStatsEnabled(getNative(), mStatsEnabled);

        if(mStatsEnabled && mStatsConsole == null) {
            mStatsConsole = new GVRConsole(getGVRContext(), GVRConsole.EyeMode.BOTH_EYES);
            mStatsConsole.setCanvasWidthHeight(512, 512);
            mStatsConsole.setXOffset(125.0f);
            mStatsConsole.setYOffset(125.0f);
        }

        if(mStatsEnabled && mStatsConsole != null) {
            mStatsConsole.setEyeMode(GVRConsole.EyeMode.BOTH_EYES);
        } else if(!mStatsEnabled && mStatsConsole != null) {
            mStatsConsole.setEyeMode(GVRConsole.EyeMode.NEITHER_EYE);
        }
    }

    void resetStats() {
        updateStatsEnabled();
        if(mStatsEnabled) {
            mStatsConsole.clear();
            NativeScene.resetStats(getNative());
        }
    }

    void updateStats() {
        if(mStatsEnabled) {
            int numberDrawCalls = NativeScene.getNumberDrawCalls(getNative());
            int numberTriangles = NativeScene.getNumberTriangles(getNative());
            float drawTime = NativeScene.getDrawTime(getNative());

            mStatsConsole.writeLine("Draw Calls: %d", numberDrawCalls);
            mStatsConsole.writeLine(" Triangles: %d", numberTriangles);
            mStatsConsole.writeLine("Draw Time: %3.1fms", drawTime);
        }
    }
}

class NativeScene {
    static native long ctor();

    static native void addSceneObject(long scene, long sceneObject);

    static native void removeSceneObject(long scene, long sceneObject);

    public static native void setFrustumCulling(long scene, boolean flag);

    public static native void setOcclusionQuery(long scene, boolean flag);

    static native void setMainCameraRig(long scene, long cameraRig);

    public static native void resetStats(long scene);
    public static native int getNumberDrawCalls(long scene);
    public static native int getNumberTriangles(long scene);
    public static native float getDrawTime(long scene);
    public static native void setStatsEnabled(long scene, boolean flag);
}
