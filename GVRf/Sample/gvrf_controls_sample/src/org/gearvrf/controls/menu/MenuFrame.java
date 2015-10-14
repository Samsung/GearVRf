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

package org.gearvrf.controls.menu;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.controls.R;
import org.gearvrf.controls.util.RenderingOrder;

public class MenuFrame extends GVRSceneObject {
    
    private static final float ANIMATION_TIME = 0.4f;
    private static final float ANIMATION_FRAME_RESIZE = .8f;
    private static final float FRAME_POSITION_Z = -.02f;
    
    private static final float FRAME_EXPAND_SCALE_X = 160f;
    private static final float PIVOT_OFFSET_Y = .8f;

    private GVRSceneObject mMenuFrame;
    public static boolean isOpen = false;

    private GVRScaleAnimation scaleCollapse;
    private GVRRelativeMotionAnimation rmCollapse;
    private GVRScaleAnimation scaleExpand;
    private GVRRelativeMotionAnimation rmExpand;

    private GVRSceneObject pivot = null;

    public MenuFrame(GVRContext gvrContext) {
        super(gvrContext);

        pivot = new GVRSceneObject(gvrContext);
        pivot.getTransform().setPosition(0, PIVOT_OFFSET_Y,FRAME_POSITION_Z);
        
        GVRMesh mesh = getGVRContext().createQuad(3.57f, 0.01f);
        GVRTexture texture = getGVRContext().loadTexture(new GVRAndroidResource(this.getGVRContext(), R.drawable.background_frame));

        mMenuFrame = new GVRSceneObject(getGVRContext(), mesh, texture);
        mMenuFrame.getRenderData().getMaterial().setOpacity(0);
        mMenuFrame.getRenderData().setRenderingOrder(RenderingOrder.MENU_FRAME_BG);

        getTransform().setPosition(0f, MenuBox.FRAME_INITITAL_POSITION_Y, FRAME_POSITION_Z);

        pivot.addChildObject(mMenuFrame);

        addChildObject(pivot);
    }

    public void expandFrame(final MenuHeader menuHeader) {

        if (!isOpen) {

            stopAnimations();

            mMenuFrame.getRenderData().getMaterial().setOpacity(0.5f);

            scaleExpand = new GVRScaleAnimation(pivot, ANIMATION_TIME, 1, FRAME_EXPAND_SCALE_X, 1);
            scaleExpand.setRepeatMode(GVRRepeatMode.ONCE);
            scaleExpand.start(this.getGVRContext().getAnimationEngine());

            rmExpand = new GVRRelativeMotionAnimation(this, ANIMATION_TIME, 0, -ANIMATION_FRAME_RESIZE, 0);
            rmExpand.setRepeatMode(GVRRepeatMode.ONCE);
            rmExpand.start(this.getGVRContext().getAnimationEngine()).setOnFinish(new GVROnFinish() {

                @Override
                public void finished(GVRAnimation arg0) {
                    menuHeader.show();
                }
            });

            isOpen = true;
           
        } else {
            menuHeader.show();
        }
    }

    private void stopAnimations() {

        if (scaleExpand != null) {
            this.getGVRContext().getAnimationEngine().stop(scaleExpand);
        }

        if (rmExpand != null) {
            this.getGVRContext().getAnimationEngine().stop(rmExpand);
        }

        if (scaleCollapse != null) {
            this.getGVRContext().getAnimationEngine().stop(scaleCollapse);
        }

        if (rmCollapse != null) {
            this.getGVRContext().getAnimationEngine().stop(rmCollapse);
        }
    }

    public void collapseFrame() {

        if (isOpen) {

            stopAnimations();

            scaleCollapse = new GVRScaleAnimation(pivot, ANIMATION_TIME, 1, 0, 1);
            scaleCollapse.setRepeatMode(GVRRepeatMode.ONCE);
            scaleCollapse.start(getGVRContext().getAnimationEngine());

            rmCollapse = new GVRRelativeMotionAnimation(this, ANIMATION_TIME, 0, ANIMATION_FRAME_RESIZE, 0);
            rmCollapse.setRepeatMode(GVRRepeatMode.ONCE);
            rmCollapse.setOnFinish(new GVROnFinish() {

                @Override
                public void finished(GVRAnimation arg0) {

                    mMenuFrame.getRenderData().getMaterial().setOpacity(0);
                    getTransform().setPosition(0f, MenuBox.FRAME_INITITAL_POSITION_Y, FRAME_POSITION_Z);
                }
            });

            rmCollapse.start(getGVRContext().getAnimationEngine());
           
            isOpen = false;
        }
    }

    public boolean isOpen() {
        return isOpen;
    }
}
