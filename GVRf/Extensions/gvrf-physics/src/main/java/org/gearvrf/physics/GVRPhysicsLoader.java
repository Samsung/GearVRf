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

import android.util.ArrayMap;
import android.util.Log;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCollider;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRComponentGroup;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GVRPhysicsLoader {
    static private final String TAG = GVRPhysicsLoader.class.getSimpleName();

    static {
        System.loadLibrary("gvrf-physics");
    }

    /**
     * Loads a physics settings file.
     *
     * @param gvrContext The context of the app.
     * @param fileName Physics settings file name.
     * @param scene The scene containing the objects to attach physics components.
     */
    public static void loadPhysicsFile(GVRContext gvrContext, String fileName, GVRScene scene) throws IOException
    {
        loadPhysicsFile(gvrContext, fileName, false, scene);
    }

    /**
     * Loads a physics settings file.
     *
     * Use this if you want the up-axis information from physics file to be ignored.
     *
     * @param gvrContext The context of the app.
     * @param fileName Physics settings file name.
     * @param ignoreUpAxis Set to true if up-axis information from file must be ignored.
     * @param scene The scene containing the objects to attach physics components.
     */
    public static void loadPhysicsFile(GVRContext gvrContext, String fileName, boolean ignoreUpAxis, GVRScene scene) throws IOException
    {
        byte[] inputData = null;
        try {
            inputData = toByteArray(toAndroidResource(gvrContext, fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (inputData == null || inputData.length == 0) {
            throw new IOException("Fail to load bullet file " + fileName);
        }

        long loader = NativePhysics3DLoader.ctor(inputData, inputData.length, ignoreUpAxis);

        if (loader == 0) {
            throw new IOException("Fail to parse bullet file " + fileName);
        }

        GVRSceneObject sceneRoot = scene.getRoot();
        ArrayMap<Long, GVRSceneObject> rbObjects = new ArrayMap<>();

        long nativeRigidBody;
        while ((nativeRigidBody = NativePhysics3DLoader.getNextRigidBody(loader)) != 0) {
            String name = NativePhysics3DLoader.getRigidBodyName(loader, nativeRigidBody);
            GVRSceneObject sceneObject = sceneRoot.getSceneObjectByName(name);
            if (sceneObject == null) {
                Log.w(TAG, "Didn't find scene object for rigid body '" + name + "'");
                continue;
            }

            if (sceneObject.getComponent(GVRCollider.getComponentType()) == null) {
                GVRMeshCollider collider = new GVRMeshCollider(gvrContext, true);
                // Collider for picking.
                sceneObject.attachComponent(collider);
            }

            if (sceneObject.getParent() != sceneRoot) {
                // Rigid bodies must be at scene root.
                float[] modelmtx = sceneObject.getTransform().getModelMatrix();
                sceneObject.getParent().removeChildObject(sceneObject);
                sceneObject.getTransform().setModelMatrix(modelmtx);
                sceneRoot.addChildObject(sceneObject);
            }

            GVRRigidBody rigidBody = new GVRRigidBody(gvrContext, nativeRigidBody);
            sceneObject.attachComponent(rigidBody);
            rbObjects.put(nativeRigidBody, sceneObject);
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
                Log.w(TAG, "Ignoring constraint with missing rigid body.");
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
                GVRComponentGroup<GVRConstraint> group;
                group = (GVRComponentGroup)sceneObject.getComponent(GVRConstraint.getComponentType());
                if (group == null) {
                    group = new GVRComponentGroup<>(gvrContext, GVRConstraint.getComponentType());
                    sceneObject.attachComponent(group);
                }

                group.addChildComponent(constraint);
                constraint.setOwnerObject(sceneObject);
            }
        }

        NativePhysics3DLoader.delete(loader);
    }

    private static byte[] toByteArray(GVRAndroidResource resource) throws IOException {
        resource.openStream();
        InputStream is = resource.getStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int read; (read = is.read(buffer, 0, buffer.length)) != -1; ) {
            baos.write(buffer, 0, read);
        }
        baos.flush();
        resource.closeStream();
        return  baos.toByteArray();
    }

    private static GVRAndroidResource toAndroidResource(GVRContext context, String fileName) throws IOException {
        GVRResourceVolume resVol = new GVRResourceVolume(context, fileName);

        final int i = fileName.lastIndexOf("/");
        if (i > 0) {
            fileName = fileName.substring(i + 1);
        }

        return resVol.openResource(fileName);
    }
}

class NativePhysics3DLoader {
    static native long ctor(byte[] bytes, int len, boolean ignoreUpAxis);

    static native long delete(long loader);

    static native long getNextRigidBody(long loader);

    static native String getRigidBodyName(long loader, long rigid_body);

    static native long getNextConstraint(long loader);

    static native long getConstraintBodyA(long loader, long constraint);

    static native long getConstraintBodyB(long loader, long constraint);
}
