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
