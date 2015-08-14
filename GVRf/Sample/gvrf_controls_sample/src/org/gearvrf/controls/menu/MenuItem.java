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

import android.graphics.Color;
import android.graphics.Paint.Align;

import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRPositionAnimation;
import org.gearvrf.controls.R;
import org.gearvrf.controls.focus.ControlSceneObject;
import org.gearvrf.controls.focus.FocusListener;
import org.gearvrf.controls.util.GVRTextBitmapFactory;
import org.gearvrf.controls.util.Text;

class MenuItem extends GVRSceneObject implements FocusListener {

    private static float[] originalPosition = null;

    private static final int TEXT_TEXTURE_WIDTH = 100;
    private static final int TEXT_TEXTURE_HEIGHT = 100;
    private static final int TEXT_FONT_SIZE = 20;
    private static final int TEXT_MAX_LENGHT = 255;

    private static final float X_OFFSET = -4.0f;
    private static final float Y_OFFSET = 0f;
    private static final float Z_OFFSET = 0f;
    private static final float SPACING = 2.0f;

    private static final float Z_HOVER_ANIMATION_OFFSET = 0.2f;
    private static final float Z_HOVER_ANIMATION_TIME = 0.5f;

    private GVRSceneObject motion;
    private GVRSceneObject color;
    private GVRSceneObject scale;
    private GVRSceneObject rotation;
    private GVRContext gvrContext;
    private static final float WIDTH = 2.0f;
    private static final float HEIGHT = 0.6f;

    MenuItem(GVRContext gvrContext) {
        super(gvrContext);
        this.gvrContext = gvrContext;
        createMenuItems();

    }

    private void createMenuItems() {

        motion = getMenuItem(new Text(this.gvrContext.getContext().getResources()
                .getString(R.string.motion), Align.CENTER, TEXT_FONT_SIZE, Color.BLACK,
                Color.WHITE,
                TEXT_MAX_LENGHT));

        color = getMenuItem(new Text(this.gvrContext.getContext().getResources()
                .getString(R.string.color), Align.CENTER, TEXT_FONT_SIZE, Color.BLACK,
                Color.RED, TEXT_MAX_LENGHT));

        scale = getMenuItem(new Text(this.gvrContext.getContext().getResources()
                .getString(R.string.scale), Align.CENTER, TEXT_FONT_SIZE, Color.BLACK, Color.WHITE,
                TEXT_MAX_LENGHT));

        rotation = getMenuItem(new Text(this.gvrContext.getContext().getResources()
                .getString(R.string.rotatation), Align.CENTER, TEXT_FONT_SIZE, Color.BLACK,
                Color.WHITE,
                TEXT_MAX_LENGHT));

        float itemPositionX = 0f;
        float itemPositionY = 0f;
        float itemPositionZ = 0f;

        motion.getTransform().setPosition(itemPositionX + X_OFFSET, itemPositionY + Y_OFFSET,
                itemPositionZ + Z_OFFSET);

        color.getTransform().setPosition(motion.getTransform().getPositionX() + SPACING,
                itemPositionY, itemPositionZ);

        scale.getTransform().setPosition(color.getTransform().getPositionX() + SPACING,
                itemPositionY, itemPositionZ);

        rotation.getTransform().setPosition(scale.getTransform().getPositionX() + SPACING,
                itemPositionY, itemPositionZ);

        addChildObject(motion);
        addChildObject(color);
        addChildObject(scale);
        addChildObject(rotation);

    }

    private GVRSceneObject getMenuItem(Text text) {

        GVRMaterial material = new GVRMaterial(gvrContext);
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        GVRMesh mesh = getGVRContext().createQuad(WIDTH, HEIGHT);

        renderData.setMesh(mesh);
        renderData.setMaterial(material);
        ControlSceneObject sceneObject = new ControlSceneObject(gvrContext);
        sceneObject.focusListener = this;

        sceneObject.attachRenderData(renderData);
        sceneObject.getRenderData().setRenderingOrder(1110);
        GVRBitmapTexture bitmap = new GVRBitmapTexture(gvrContext,
                GVRTextBitmapFactory.create(gvrContext.getContext(), TEXT_TEXTURE_WIDTH,
                        TEXT_TEXTURE_HEIGHT, text, 0));

        sceneObject.getRenderData().getMaterial().setMainTexture(bitmap);
        sceneObject.attachEyePointeeHolder();

        return sceneObject;
    }

    private void setOriginalPosition() {
        originalPosition = new float[3];
        originalPosition[0] = this.getTransform().getPositionX();
        originalPosition[1] = this.getTransform().getPositionY();
        originalPosition[2] = this.getTransform().getPositionZ();
    }

    private void bringMenuItemToFront(GVRSceneObject item) {
        if (originalPosition == null) {
            setOriginalPosition();
        }

        new GVRPositionAnimation(item.getTransform(),
                Z_HOVER_ANIMATION_TIME, item.getTransform().getPositionX(), item.getTransform()
                        .getPositionY(),
                originalPosition[2] + Z_HOVER_ANIMATION_OFFSET)
                .start(getGVRContext().getAnimationEngine());

    }

    private void sendMenuItemToBack(GVRSceneObject item) {

        if (originalPosition == null) {
            setOriginalPosition();
        }

        new GVRPositionAnimation(item.getTransform(),
                Z_HOVER_ANIMATION_TIME, item.getTransform().getPositionX(), item.getTransform()
                        .getPositionY(),
                originalPosition[2])
                .start(getGVRContext().getAnimationEngine());

    }

    @Override
    public void gainedFocus(GVRSceneObject object) {
        bringMenuItemToFront(object);
    }

    @Override
    public void lostFocus(GVRSceneObject object) {
        sendMenuItemToBack(object);

    }

    @Override
    public void inFocus(GVRSceneObject object) {
        // TODO Auto-generated method stub

    }
}
