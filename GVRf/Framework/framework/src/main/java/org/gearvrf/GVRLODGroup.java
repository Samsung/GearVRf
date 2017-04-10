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

import org.gearvrf.utility.Log;
import org.joml.Vector4f;

import java.util.LinkedList;


/**
 * Example:
 * <pre>
 * root = new GVRSceneObject(..);
 *
 * sphereHighDensity = new GVRSceneObject(..);
 * sphereMediumDensity = new GVRSceneObject(..);
 * sphereLowDensity = new GVRSceneObject(..);
 *
 * GVRLODGroup lodGroup = new GVRLODGroup(gvrContext);
 * lodGroup.addRange(0, sphereHighDensity);
 * lodGroup.addRange(5, sphereMediumDensity);
 * lodGroup.addRange(9, sphereLowDensity);
 * root.attachComponent(lodGroup);
 * </pre>
 */
public final class GVRLODGroup extends GVRBehavior {
    static private long TYPE_LODGROUP = newComponentType(GVRLODGroup.class);

    public GVRLODGroup(GVRContext gvrContext) {
        super(gvrContext, 0);
        mType = getComponentType();
    }

    static public long getComponentType() {
        return TYPE_LODGROUP;
    }

    private final Vector4f mCenter = new Vector4f();
    private final Vector4f mVector = new Vector4f();
    private final LinkedList<Object[]> mRanges = new LinkedList<>();

    /**
     * Add a range to this LOD group. Specify the scene object that should be displayed in this
     * range. Add the LOG group as a component to the parent scene object. The scene objects
     * associated with each range will automatically be added as children to the parent.
     * @param range show the scene object if the camera distance is greater than this value
     * @param sceneObject scene object that should be rendered when in this range
     * @throws IllegalArgumentException if range is negative or sceneObject null
     */
    public synchronized void addRange(final float range, final GVRSceneObject sceneObject)
    {
        if (null == sceneObject) {
            throw new IllegalArgumentException("sceneObject must be specified!");
        }
        if (range < 0) {
            throw new IllegalArgumentException("range cannot be negative");
        }

        final int size = mRanges.size();
        final float rangePow2 = range*range;
        final Object[] newElement = new Object[] {rangePow2, sceneObject};

        for (int i = 0; i < size; ++i) {
            final Object[] el = mRanges.get(i);
            final Float r = (Float)el[0];
            if (r > rangePow2) {
                mRanges.add(i, newElement);
                break;
            }
        }

        if (mRanges.size() == size) {
            mRanges.add(newElement);
        }

        final GVRSceneObject owner = getOwnerObject();
        if (null != owner) {
            owner.addChildObject(sceneObject);
        }
    }

    /**
     * Do not call directly.
     * @deprecated
     */
    public void onDrawFrame(float frameTime) {
        final GVRSceneObject owner = getOwnerObject();
        if (owner == null) {
            return;
        }

        final int size = mRanges.size();
        final GVRTransform t = getGVRContext().getMainScene().getMainCameraRig().getCenterCamera().getTransform();

        for (final Object[] range : mRanges) {
            ((GVRSceneObject)range[1]).setEnable(false);
        }

        for (int i = size - 1; i >= 0; --i) {
            final Object[] range = mRanges.get(i);
            final GVRSceneObject child = (GVRSceneObject) range[1];
            if (child.getParent() != owner) {
                Log.w(TAG, "the scene object for distance greater than " + range[0] + " is not a child of the owner; skipping it");
                continue;
            }

            final float[] values = child.getBoundingVolumeRawValues();
            mCenter.set(values[0], values[1], values[2], 1.0f);
            mVector.set(t.getPositionX(), t.getPositionY(), getTransform().getPositionZ(), 1.0f);

            mVector.sub(mCenter);
            mVector.negate();

            float distance = mVector.dot(mVector);

            if (distance >= (Float) range[0]) {
                child.setEnable(true);
                break;
            }
        }
    }

    @Override
    public synchronized void onAttach(GVRSceneObject newOwner) {
        super.onAttach(newOwner);

        for (final Object[] el : mRanges) {
            newOwner.addChildObject((GVRSceneObject)el[1]);
        }
    }

    @Override
    public synchronized void onDetach(GVRSceneObject oldOwner) {
        super.onDetach(oldOwner);

        for (final Object[] el : mRanges) {
            oldOwner.removeChildObject((GVRSceneObject)el[1]);
        }
    }

    private static final String TAG = "GVRLODGroup";
}