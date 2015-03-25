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

import org.gearvrf.GVRRenderData.GVRRenderMaskBit;

/** The scene graph */
public class GVRScene extends GVRHybridObject {
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
        GVRCamera rightCamera = new GVRPerspectiveCamera(gvrContext);
        rightCamera.setRenderMask(GVRRenderMaskBit.Right);
        GVRSceneObject leftCameraObject = new GVRSceneObject(gvrContext);
        leftCameraObject.attachCamera(leftCamera);
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

    static GVRScene factory(GVRContext gvrContext, long ptr) {
        GVRHybridObject wrapper = wrapper(ptr);
        return wrapper == null ? new GVRScene(gvrContext, ptr)
                : (GVRScene) wrapper;
    }

    @Override
    protected final boolean registerWrapper() {
        return true;
    }

    /**
     * Add an {@linkplain GVRSceneObject scene object} 
     * 
     * @param sceneObject
     *            The {@linkplain GVRSceneObject scene object} to add.
     */
    public void addSceneObject(GVRSceneObject sceneObject) {
        NativeScene.addSceneObject(getPtr(), sceneObject.getPtr());
    }

    /**
     * Remove a {@linkplain GVRSceneObject scene object} 
     * 
     * @param sceneObject
     *            The {@linkplain GVRSceneObject scene object} to remove.
     */
    public void removeSceneObject(GVRSceneObject sceneObject) {
        NativeScene.removeSceneObject(getPtr(), sceneObject.getPtr());
    }

    /**
     * @return The {@link GVRCameraRig camera rig} used for rendering the scene
     *         on the screen.
     */
    public GVRCameraRig getMainCameraRig() {
        long ptr = NativeScene.getMainCameraRig(getPtr());
        return ptr == 0 ? null : GVRCameraRig.factory(getGVRContext(), ptr);
    }

    /**
     * Set the {@link GVRCameraRig camera rig} used for rendering the scene on
     * the screen.
     * 
     * @param cameraRig
     *            The {@link GVRCameraRig camera rig} to render with.
     */
    public void setMainCameraRig(GVRCameraRig cameraRig) {
        NativeScene.setMainCameraRig(getPtr(), cameraRig.getPtr());
    }

    /**
     * @return The flattened hierarchy of {@link GVRSceneObject objects} as an
     *         array.
     */
    public GVRSceneObject[] getWholeSceneObjects() {
        long[] ptrs = NativeScene.getWholeSceneObjects(getPtr());
        GVRSceneObject[] sceneObjects = new GVRSceneObject[ptrs.length];
        for (int i = 0; i < ptrs.length; ++i) {
            sceneObjects[i] = GVRSceneObject.factory(getGVRContext(), ptrs[i]);
        }
        return sceneObjects;
    }
}

class NativeScene {
    public static native long ctor();

    public static native void addSceneObject(long scene, long sceneObject);

    public static native void removeSceneObject(long scene, long sceneObject);

    public static native long getMainCameraRig(long scene);

    public static native void setMainCameraRig(long scene, long cameraRig);

    public static native long[] getWholeSceneObjects(long scene);
}
