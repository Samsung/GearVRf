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

package org.gearvrf.physics;

import android.content.res.AssetManager;
import android.util.ArrayMap;
import android.util.Log;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;

public class GVRPhysicsLoader {
    static private final String TAG = GVRPhysicsLoader.class.getSimpleName();

    static {
        System.loadLibrary("gvrf-physics");
    }

    /**
     * Loads a physics settings file from 'assets' directory of the application.
     *
     * @param gvrContext The context of the app.
     * @param fileName Physics settings file name.
     * @param scene The scene containing the objects to attach physics components.
     */
    public static void loadPhysicsFile(GVRContext gvrContext, String fileName, GVRScene scene) {
        loadPhysicsFile(gvrContext, fileName, false, scene);
    }

    /**
     * Loads a physics settings file from 'assets' directory of the application.
     *
     * Use this if you want the up-axis information from physics file to be ignored.
     *
     * @param gvrContext The context of the app.
     * @param fileName Physics settings file name.
     * @param ignoreUpAxis Set to true if up-axis information from file must be ignored.
     * @param scene The scene containing the objects to attach physics components.
     */
    public static void loadPhysicsFile(GVRContext gvrContext, String fileName, boolean ignoreUpAxis, GVRScene scene) {
        long loader = NativePhysics3DLoader.ctor(fileName, ignoreUpAxis, gvrContext.getActivity().getAssets());

        GVRSceneObject sceneRoot = scene.getRoot();
        ArrayMap<Long, GVRSceneObject> rbObjects = new ArrayMap<>();

        long nativeRigidBody;
        while ((nativeRigidBody = NativePhysics3DLoader.getNextRigidBody(loader)) != 0) {
            String name = NativePhysics3DLoader.getRigidBodyName(loader, nativeRigidBody);
            GVRSceneObject sceneObject = sceneRoot.getSceneObjectByName(name);
            if (sceneObject == null) {
                Log.d(TAG, "Did not found scene object for rigid body '" + name + "'");
            } else {
                GVRRigidBody rigidBody = new GVRRigidBody(gvrContext, nativeRigidBody);
                sceneObject.attachComponent(rigidBody);
                rbObjects.put(nativeRigidBody, sceneObject);
            }
        }

        long nativeConstraint;
        long nativeRigidBodyB;
        while ((nativeConstraint = NativePhysics3DLoader.getNextConstraint(loader)) != 0) {
            nativeRigidBody = NativePhysics3DLoader.getConstraintBodyA(loader, nativeConstraint);
            nativeRigidBodyB = NativePhysics3DLoader.getConstraintBodyB(loader, nativeConstraint);
            GVRSceneObject sceneObject = rbObjects.get(nativeRigidBody);
            GVRSceneObject sceneObjectB = rbObjects.get(nativeRigidBodyB);

            if (sceneObject == null || sceneObjectB == null) {
                // There is no scene object to own this constraint
                Log.d(TAG, "Found constraint with missing rigid body: will ignore");
                continue;
            }

            int constraintType = Native3DConstraint.getConstraintType(nativeConstraint);
            GVRConstraint constraint = null;
            if (constraintType == GVRConstraint.fixedConstraintId) {
                constraint = new GVRFixedConstraint(gvrContext, nativeConstraint);
            } else if (constraintType == GVRConstraint.point2pointConstraintId) {
                constraint = new GVRPoint2PointConstraint(gvrContext, nativeConstraint);
            } else if (constraintType == GVRConstraint.sliderConstraintId) {
                constraint = new GVRSliderConstraint(gvrContext, nativeConstraint);
            } else if (constraintType == GVRConstraint.hingeConstraintId) {
                constraint = new GVRHingeConstraint(gvrContext, nativeConstraint);
            } else if (constraintType == GVRConstraint.coneTwistConstraintId) {
                constraint = new GVRConeTwistConstraint(gvrContext, nativeConstraint);
            } else if (constraintType == GVRConstraint.genericConstraintId) {
                constraint = new GVRGenericConstraint(gvrContext, nativeConstraint);
            }

            if (constraint != null) {
                sceneObject.attachComponent(constraint);
            }
        }

        NativePhysics3DLoader.delete(loader);
    }

}

class NativePhysics3DLoader {
    static native long ctor(String file_name, boolean ignoreUpAxis, AssetManager assetManager);

    static native long delete(long loader);

    static native long getNextRigidBody(long loader);

    static native String getRigidBodyName(long loader, long rigid_body);

    static native long getNextConstraint(long loader);

    static native long getConstraintBodyA(long loader, long constraint);

    static native long getConstraintBodyB(long loader, long constraint);
}