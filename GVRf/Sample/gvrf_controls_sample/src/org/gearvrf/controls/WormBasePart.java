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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.controls.anim.AnimationsTime;
import org.gearvrf.controls.animation.GVRColorSwapAnimation;
import org.gearvrf.controls.shaders.ColorSwapShader;
import org.gearvrf.controls.util.ColorControls.Color;
import org.gearvrf.controls.util.RenderingOrder;

public class WormBasePart extends GVRSceneObject {

    private Color color;
    private final float WORM_INITIAL_Z = -3;
    private final float WORM_INITIAL_Y = -0.83f;
    private GVRSceneObject segment;

    public WormBasePart(GVRContext gvrContext, int meshResId, int textureResId, Color color) {
        super(gvrContext);
        this.color = color;

        getTransform().setPosition(0, WORM_INITIAL_Y, WORM_INITIAL_Z);
        getTransform().setScale(0.4f, 0.4f, 0.4f);

        build(meshResId, textureResId);
    }

    private void build(int meshResId, int textureResId) {

        GVRContext gvrContext = getGVRContext();

        GVRMesh mesh = gvrContext.loadMesh(
                new GVRAndroidResource(gvrContext, meshResId));

        GVRTexture texture = gvrContext.loadTexture(
                new GVRAndroidResource(gvrContext, textureResId));

        segment = new GVRSceneObject(gvrContext, mesh, texture, new ColorSwapShader(
                getGVRContext()).getShaderId());

        segment.getRenderData().setRenderingOrder(RenderingOrder.WORM);

        applyShader(gvrContext, segment, color);
        addChildObject(segment);
    }

    private void applyShader(GVRContext gvrContext, GVRSceneObject wormPiece, Color color) {

        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.wormy_diffuse_light));

        wormPiece.getRenderData().getMaterial()
                .setTexture(ColorSwapShader.TEXTURE_GRAYSCALE, texture);

        texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.wormy_diffuse_2));

        wormPiece.getRenderData().getMaterial()
                .setTexture(ColorSwapShader.TEXTURE_DETAILS, texture);
        wormPiece
                .getRenderData()
                .getMaterial()
                .setVec4(ColorSwapShader.COLOR, color.getRed(), color.getGreen(), color.getBlue(),
                        1);
    }

    public void animChangeColor(Color color) {

        float[] colorArray = new float[3];
        colorArray[0] = color.getRed();
        colorArray[1] = color.getGreen();
        colorArray[2] = color.getBlue();

        new GVRColorSwapAnimation(segment, AnimationsTime.getChangeColorTime(), colorArray)
                .start(getGVRContext().getAnimationEngine());
    }

    public void resetColor(Color color) {

        segment.getRenderData().getMaterial().setVec4(
                ColorSwapShader.COLOR, color.getRed(), color.getGreen(), color.getBlue(), 1);
    }
}
