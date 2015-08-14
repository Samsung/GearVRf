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

package org.gearvrf.controls;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;


public class ChainElement extends GVRSceneObject {

    public GVRSceneObject target;

    public ChainElement(GVRContext gvrContext, GVRSceneObject target) {
        super(gvrContext);
        this.target = target;
    }

    public void follow() {

        float currentX = this.getTransform().getPositionX();
        float currentY = this.getTransform().getPositionY();
        float currentZ = this.getTransform().getPositionZ();

        float nextX = target.getTransform().getPositionX() -
                currentX;

        float nextY = target.getTransform().getPositionY() -
                currentY;

        float nextZ = target.getTransform().getPositionZ() -
                currentZ;

        getTransform().setPosition(
                currentX + nextX, currentY + nextY, currentZ + nextZ);

    }

}
