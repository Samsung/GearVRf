/*
 * Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf.io.cursor3d;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

/**
 * This class manages access to the {@link GVRSceneObject} controlled by the {@link Cursor}.
 */
class CursorSceneObject {
    private static final String TAG = CursorSceneObject.class.getSimpleName();
    // The main scene object that represents a {@link Cursor} object in GVRf
    private GVRSceneObject cursor;
    // In order to protect the main cursor scene object from being altered or modified by the
    // library or an external application we add this object as a child of the main scene object.
    // Any modifications to this object does not affect the main cursor object while transforms
    // applied to the main scene objects are also applied to this object.
    private GVRSceneObject externalSceneObject;
    // For cursor objects with a hierarchy we save a reference to the scene object with the
    // mesh for a faster lookup for collision detection.
    private GVRSceneObject meshSceneObject;
    private GVRSceneObject mainObject;
    private int id;
    private Object lock = new Object();

    CursorSceneObject(GVRContext context, int id) {
        mainObject = new GVRSceneObject(context);
        cursor = new GVRSceneObject(context);
        externalSceneObject = new GVRSceneObject(context);
        mainObject.addChildObject(externalSceneObject);
        mainObject.addChildObject(cursor);
        meshSceneObject = cursor;
        this.id = id;
    }

    float getPositionX() {
        return mainObject.getTransform().getPositionX();
    }

    float getPositionY() {
        return mainObject.getTransform().getPositionY();
    }

    float getPositionZ() {
        return mainObject.getTransform().getPositionZ();
    }

    GVRSceneObject getMainSceneObject() {
        return mainObject;
    }

    GVRSceneObject getExternalSceneObject() {
        return externalSceneObject;
    }

    public int getId() {
        return id;
    }

    // find the scene object with a mesh, return the first one.
    private GVRSceneObject getMeshObject(GVRSceneObject object) {
        GVRRenderData renderData = object.getRenderData();
        if (renderData != null && renderData.getMesh() != null) {
            return object;
        }

        GVRSceneObject returnObject;
        for (GVRSceneObject child : object.children()) {
            returnObject = getMeshObject(child);
            if (returnObject != null) {
                return returnObject;
            }
        }
        return null;
    }

    boolean isColliding(GVRSceneObject sceneObject) {

        // make sure that the render data does not change when
        // a collision test is in progress.
        synchronized (lock) {
            if (sceneObject.hasMesh() == false) {
                return false;
            }

            if (meshSceneObject == null || meshSceneObject.hasMesh() == false) {
                return false;
            }

            return meshSceneObject.isColliding(sceneObject);
        }
    }

    void addChildObject(GVRSceneObject sceneObject) {
        cursor.addChildObject(sceneObject);
    }

    void set(GVRSceneObject sceneObject){
        meshSceneObject = getMeshObject(sceneObject);
    }

    void reset(){
        meshSceneObject = null;
    }

    void removeChildObject(GVRSceneObject sceneObject) {
        cursor.removeChildObject(sceneObject);
    }

    void setScale(float scale) {
        mainObject.getTransform().setScale(scale, scale, scale);
    }

    void setModelMatrix(float[] matrix) {
        cursor.getTransform().setModelMatrix(matrix);
    }
}

