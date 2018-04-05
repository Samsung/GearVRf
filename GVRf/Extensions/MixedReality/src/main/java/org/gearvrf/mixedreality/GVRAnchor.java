package org.gearvrf.mixedreality;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

/**
 * Created by diego.a on 04/04/18.
 */

public class GVRAnchor extends GVRBehavior {
    GVRSceneObject mOwner;
    float[] modelMatrix = new float[16];

    public GVRAnchor(GVRContext gvrContext) {
        super(gvrContext);
    }

    public void setModelMatrix(float[] modelMatrix) {
        this.modelMatrix = modelMatrix;
    }

    public float[] getModelMatrix() {
        return modelMatrix;
    }

    @Override
    public void onAttach(GVRSceneObject newOwner) {
        super.onAttach(newOwner);
        mOwner = newOwner;
    }

    @Override
    public void onDrawFrame(float frameTime) {
        super.onDrawFrame(frameTime);
        if (mOwner != null) {
            mOwner.getTransform().setModelMatrix(modelMatrix);
        }

    }
}
