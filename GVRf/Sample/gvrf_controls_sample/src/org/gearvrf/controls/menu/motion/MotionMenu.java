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

package org.gearvrf.controls.menu.motion;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.controls.R;
import org.gearvrf.controls.anim.AnimationsTime;
import org.gearvrf.controls.focus.ControlSceneObject;
import org.gearvrf.controls.menu.GridSceneObjects;
import org.gearvrf.controls.menu.ItemSelectedListener;
import org.gearvrf.controls.menu.MenuWindow;
import org.gearvrf.controls.menu.RadioButtonSceneObject;
import org.gearvrf.controls.menu.RadioGrupoSceneObject;
import org.gearvrf.controls.model.Apple;
import org.gearvrf.controls.model.Apple.Motion;

public class MotionMenu extends MenuWindow {

    private final float PREVIEW_POSITION_X = -.92f;
    private final float PREVIEW_POSITION_Y = -0.63f;
    private final float PREVIEW_POSITION_Z = 0.2f;
    private final float GRID_POSITION_X = 0.33f;
    private final float GRID_POSITION_Y = -0.38f;
    private final float GRID_POSITION_Z = 0.f;
    
    private MenuPreview previewArea;
    private GridSceneObjects mGrid;
    private RadioGrupoSceneObject radioGroup;
    
    public MotionMenu(GVRContext gvrContext) {
        super(gvrContext);

        createPreviewBox();

        attachGrid();
        
        attachRadioGroup();
    }

    private void attachGrid() {
        
        ParserMotionItem parse = new ParserMotionItem(getGVRContext());
    
        mGrid = new GridSceneObjects(getGVRContext(), parse.getList(),R.array.grid,
                new ItemSelectedListener() {

                    @Override
                    public void selected(ControlSceneObject object) {

                        Motion motion = ((MotionButton) object).getMotion();
                        Apple.motion = motion;
                        previewArea.changeInterpolatorTo(Apple.defineInterpolator(motion));
                        previewArea.changeColorTo(Apple.getColor(getGVRContext()));
                    }
                });
        
        mGrid.getTransform().setPosition(GRID_POSITION_X, GRID_POSITION_Y,
                GRID_POSITION_Z);
    }

    private void createPreviewBox() {
      
        previewArea = new MenuPreview(getGVRContext(), getGVRContext().createQuad(1.2f, 1),
                getGVRContext().loadTexture( new GVRAndroidResource(this.getGVRContext(), R.raw.empty)));
       
        previewArea.getTransform().setPosition(PREVIEW_POSITION_X, PREVIEW_POSITION_Y,
                PREVIEW_POSITION_Z);

        previewArea.getRenderData().getMaterial().setOpacity(0);

        addChildObject(previewArea);
    }
    
    private void attachRadioGroup() {

        radioGroup =  new RadioGrupoSceneObject(getGVRContext(), new ItemSelectedListener() {
            
            @Override
            public void selected(ControlSceneObject object) {
                
                RadioButtonSceneObject button = (RadioButtonSceneObject)object;
                AnimationsTime.setDropTime(button.getSecond());
                
                previewArea.animationsTime();
            }
        }, 1, 3, 5);
        
        radioGroup.getTransform().setPosition(-1.37f, -1.24f, PREVIEW_POSITION_Z);
        
        addChildObject(radioGroup);
    }

    @Override
    public void show() {
        
        radioGroup.show();

        removeChildObject(mGrid);
        removeChildObject(previewArea);

        addChildObject(mGrid);
        addChildObject(previewArea);
        previewArea.show();

        GVROpacityAnimation opacitypreviewArea = new GVROpacityAnimation(previewArea, 1f, 1);
        opacitypreviewArea.setRepeatMode(GVRRepeatMode.ONCE);
        opacitypreviewArea.start(getGVRContext().getAnimationEngine());
    }

    @Override
    public void hide() {
        
        radioGroup.hide();

        removeChildObject(mGrid);

        previewArea.hide();
        removeChildObject(previewArea);

        GVROpacityAnimation opacitypreviewArea = new GVROpacityAnimation(previewArea, 0.5f, 0);
        opacitypreviewArea.setRepeatMode(GVRRepeatMode.ONCE);
        opacitypreviewArea.start(getGVRContext().getAnimationEngine());
    }
}
