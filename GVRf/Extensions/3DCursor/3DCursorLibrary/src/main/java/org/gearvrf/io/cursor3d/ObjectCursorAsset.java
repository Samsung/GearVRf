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

import android.util.SparseArray;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Log;

import java.io.IOException;

/**
 * Use this behavior to uniquely set an object to the {@link Cursor}.
 */
class ObjectCursorAsset extends CursorAsset {
    private static final String TAG = ObjectCursorAsset.class.getSimpleName();
    private final String assetName;
    private SparseArray<GVRModelSceneObject> objects;
    private GVRAnimationEngine animationEngine;
    private int LOOP_REPEAT_COUNT = -1;

    ObjectCursorAsset(GVRContext context, CursorType type, Action action, String assetName) {
        super(context, type, action);
        this.assetName = assetName;
        objects = new SparseArray<GVRModelSceneObject>();
        animationEngine = context.getAnimationEngine();
    }

    @Override
    void set(Cursor cursor) {
        super.set(cursor);
        GVRModelSceneObject modelSceneObject = objects.get(cursor.getId());

        if (modelSceneObject == null) {
            Log.e(TAG, "Model not found, should not happen");
            return;
        }
        modelSceneObject.setEnable(true);

        for (GVRAnimation animation : modelSceneObject.getAnimations()) {
            animation.setRepeatMode(GVRRepeatMode.REPEATED);
            animation.setRepeatCount(LOOP_REPEAT_COUNT);
            animation.start(animationEngine);
        }
    }

    private GVRModelSceneObject loadModelSceneObject() {
        GVRModelSceneObject modelSceneObject = null;
        try {
            modelSceneObject = context.getAssetLoader().loadModel(assetName);
        } catch (IOException e) {
            //should not happen
            Log.e(TAG, "Could not load model", e);
        }
        return modelSceneObject;
    }

    @Override
    void reset(Cursor cursor) {
        super.reset(cursor);

        GVRModelSceneObject modelSceneObject = objects.get(cursor.getId());

        modelSceneObject.setEnable(false);
        for (GVRAnimation animation : modelSceneObject.getAnimations()) {
            if (animation.isFinished() == false) {
                animation.setRepeatMode(GVRRepeatMode.ONCE);
                animation.setRepeatCount(0);
                animationEngine.stop(animation);
            }
        }
    }

    @Override
    void load(Cursor cursor) {
        Integer key = cursor.getId();
        GVRModelSceneObject modelSceneObject = objects.get(key);

        if (modelSceneObject == null) {
            modelSceneObject = loadModelSceneObject();
            modelSceneObject.setName( getAction().toString() + key.toString());
            objects.put(key, modelSceneObject);
        }
        cursor.addChildObject(modelSceneObject);
        modelSceneObject.setEnable(false);
    }

    @Override
    void unload(Cursor cursor) {
        GVRSceneObject assetSceneObject = objects.get(cursor.getId());
        cursor.removeChildObject(assetSceneObject);
        objects.remove(cursor.getId());
    }
}
