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

package org.gearvrf.controls.menu.rotation;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.controls.MainScript;
import org.gearvrf.controls.R;
import org.gearvrf.controls.anim.StarPreviewInfo;

public class RotationGroup extends GVRSceneObject {
    private static final float SCALE_FACTOR = 0.7f;
    GVRSceneObject star;
    GVRSceneObject base;
    private GVRSceneObject place;

    public RotationGroup(GVRContext gvrContext) {
        super(gvrContext);

        place = new GVRSceneObject(gvrContext);

        addChildObject(place);
        createStar();
        createBase();
        place.getTransform().setScale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
        place.addChildObject(star);
        place.addChildObject(base);
        
        StarPreviewInfo.putStarReference(place);
    }

    private void createBase() {

        GVRAndroidResource baseTextRes = new GVRAndroidResource(getGVRContext(),
                R.drawable.direction_rotation);

        base = new GVRSceneObject(getGVRContext(), 1, 1, getGVRContext().loadTexture(baseTextRes));

        base.getTransform().rotateByAxis(90, 0, 0, 1);
        base.getTransform().rotateByAxis(-90, 1, 0, 0);
    }

    private void createStar() {
        
        GVRAndroidResource starMeshRes = new GVRAndroidResource(getGVRContext(), R.raw.star);
        GVRAndroidResource starTextRes = new GVRAndroidResource(getGVRContext(),
                R.drawable.star_diffuse);

        star = new GVRSceneObject(getGVRContext(), starMeshRes, starTextRes);
        star.getTransform().setPositionY(0.5f);
    }

    public void rotate(final float angleFactor) {
        
        GVRRotationByAxisAnimation rotationAnimation = new GVRRotationByAxisAnimation(place, 0.1f, angleFactor, 0, 1, 0);
        
        rotationAnimation.setOnFinish(new GVROnFinish() {
            
            @Override
            public void finished(GVRAnimation arg0) {
                
                MainScript.enableAnimationStar();
                
                if(angleFactor > 0){
                    
                    StarPreviewInfo.changeRotationFactor(StarPreviewInfo.Direction.left);
                } else {
                    
                    StarPreviewInfo.changeRotationFactor(StarPreviewInfo.Direction.right);
                }
            }
        });
        
        rotationAnimation.start(getGVRContext().getAnimationEngine());
    }
}