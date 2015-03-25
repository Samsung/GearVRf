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

/**
 * Finds the scene objects you are pointing to.
 * 
 * For a {@linkplain GVRSceneObject scene object} to be pickable, it must have a
 * {@link GVREyePointeeHolder}
 * {@link GVRSceneObject#attachEyePointeeHolder(GVREyePointeeHolder) attached}
 * and {@linkplain GVREyePointeeHolder#setEnable(boolean) enabled.}
 * 
 * <p>
 * This picker "casts" a ray into the screen graph, and returns each enabled
 * {@link GVREyePointeeHolder} that the ray hits, sorted by distance from the
 * camera. Use {@link GVREyePointeeHolder#getOwnerObject()} to map the eye
 * pointee holder to a scene object.
 */
public class GVRPicker {
    private GVRPicker() {
    }

    /**
     * Casts a ray into the scene graph, and returns the objects it intersects.
     * 
     * The ray is defined by its origin {@code [ox, oy, oz]} and its direction
     * {@code [dx, dy, dz]}.
     * 
     * <p>
     * The ray origin may be [0, 0, 0] and the direction components should be
     * normalized from -1 to 1: Note that the y direction runs from -1 at the
     * bottom to 1 at the top. To construct a picking ray originating at the
     * user's head and pointing into the scene along the camera lookat vector,
     * pass in 0, 0, 0 for the origin and 0, 0, -1 for the direction.
     * 
     * @param scene
     *            The {@link GVRScene} with all the objects to be tested.
     * 
     * @param ox
     *            The x coordinate of the ray origin.
     * 
     * @param oy
     *            The y coordinate of the ray origin.
     * 
     * @param oz
     *            The z coordinate of the ray origin.
     * 
     * @param dx
     *            The x vector of the ray direction.
     * 
     * @param dy
     *            The y vector of the ray direction.
     * 
     * @param dz
     *            The z vector of the ray direction.
     * 
     * @return The {@linkplain GVREyePointeeHolder eye pointee holders}
     *         penetrated by the ray, sorted by distance from the camera rig.
     *         Use {@link GVREyePointeeHolder#getOwnerObject()} to get the
     *         corresponding scene objects.
     * 
     */
    public static final GVREyePointeeHolder[] pickScene(GVRScene scene,
            float ox, float oy, float oz, float dx, float dy, float dz) {
        long[] ptrs = NativePicker.pickScene(scene.getPtr(), ox, oy, oz, dx,
                dy, dz);
        GVREyePointeeHolder[] eyePointeeHolders = new GVREyePointeeHolder[ptrs.length];
        for (int i = 0; i < ptrs.length; ++i) {
            eyePointeeHolders[i] = GVREyePointeeHolder.factory(
                    scene.getGVRContext(), ptrs[i]);
        }
        return eyePointeeHolders;
    }

    /**
     * Tests the {@link GVRSceneObject}s contained within scene against the
     * camera rig's lookat vector.
     * 
     * @param scene
     *            The {@link GVRScene} with all the objects to be tested.
     * 
     * @return the {@link GVREyePointeeHolder}s which are penetrated by the
     *         picking ray. The holders are sorted by distance from the camera
     *         rig.
     * 
     */
    public static final GVREyePointeeHolder[] pickScene(GVRScene scene) {
        return pickScene(scene, 0, 0, 0, 0, 0, -1.0f);
    }

    /**
     * Tests the {@link GVRSceneObject} against the camera rig's lookat vector.
     * 
     * @param sceneObject
     *            The {@link GVRSceneObject} to be tested.
     * 
     * @param cameraRig
     *            The {@link GVRCameraRig} to use for ray testing.
     * 
     * @return the distance from the camera rig. It returns positive infinity if
     *         the cameraRig is not pointing to the sceneObject.
     * 
     */
    public static final float pickSceneObject(GVRSceneObject sceneObject,
            GVRCameraRig cameraRig) {
        return NativePicker.pickSceneObject(sceneObject.getPtr(),
                cameraRig.getPtr());
    }
}

final class NativePicker {
    public static native long[] pickScene(long scene, float ox, float oy,
            float oz, float dx, float dy, float dz);

    public static native float pickSceneObject(long sceneObject, long cameraRig);
}
