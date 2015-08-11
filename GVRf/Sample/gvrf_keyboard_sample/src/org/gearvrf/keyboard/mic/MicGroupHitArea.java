
package org.gearvrf.keyboard.mic;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.mic.model.MicItem;
import org.gearvrf.keyboard.util.SceneObjectNames;

public class MicGroupHitArea extends GVRSceneObject {

    MicItem mHitArea;

    public MicGroupHitArea(GVRContext gvrContext) {
        super(gvrContext);
        setName(SceneObjectNames.MIC_GROUP_HIT_AREA);

        mHitArea = new MicItem(gvrContext, R.raw.empty);
        this.addChildObject(mHitArea);
        enableHitArea(gvrContext, mHitArea);

    }

    public GVRSceneObject getHitAreaObject() {

        return mHitArea;
    }

    private void enableHitArea(GVRContext gvrContext, MicItem hitArea) {
        attachDefaultEyePointee(hitArea);
    }

    private void attachDefaultEyePointee(GVRSceneObject sceneObject) {
        sceneObject.attachEyePointeeHolder();
    }

}
