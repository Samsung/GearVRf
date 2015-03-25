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


package org.gearvrf.video;

import android.annotation.SuppressLint;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSceneObject;

public class Seekbar extends GVRSceneObject {
    private static final float WIDTH = 8.0f;
    private static final float HEIGHT = 0.4f;
    private static final float DEPTH = 8.0f;
    private static final float Y = -0.2f;
    private GVRSceneObject mPlayedSide = null;
    private GVRSceneObject mLeftSide = null;
    private GVRSceneObject mPointer = null;
    private GVRSceneObject mGlow = null;
    private GVRSceneObject mCurrentTime = null;
    private GVRSceneObject mDuration = null;

    public Seekbar(GVRContext gvrContext) {
        super(gvrContext);
        getTransform().setPosition(-0.1f, Y, -DEPTH);

        mPlayedSide = new GVRSceneObject(gvrContext, gvrContext.createQuad(
                1.0f, 0.1f), gvrContext.loadTexture("seekbar/dark-gray.png"));
        mPlayedSide.getRenderData().setRenderingOrder(
                GVRRenderingOrder.TRANSPARENT + 2);
        mPlayedSide.getRenderData().setOffset(true);
        mPlayedSide.getRenderData().setOffsetFactor(-2.0f);
        mPlayedSide.getRenderData().setOffsetUnits(-2.0f);

        mLeftSide = new GVRSceneObject(gvrContext, gvrContext.createQuad(1.0f,
                0.1f), gvrContext.loadTexture("seekbar/light-gray.png"));
        mLeftSide.getRenderData().setRenderingOrder(
                GVRRenderingOrder.TRANSPARENT + 2);
        mLeftSide.getRenderData().setOffset(true);
        mLeftSide.getRenderData().setOffsetFactor(-2.0f);
        mLeftSide.getRenderData().setOffsetUnits(-2.0f);

        mPointer = new GVRSceneObject(gvrContext, gvrContext.createQuad(0.08f,
                0.3f), gvrContext.loadTexture("seekbar/dark-gray-circle.png"));
        mPointer.getRenderData().setRenderingOrder(
                GVRRenderingOrder.TRANSPARENT + 3);
        mPointer.getRenderData().setOffset(true);
        mPointer.getRenderData().setOffsetFactor(-3.0f);
        mPointer.getRenderData().setOffsetUnits(-3.0f);

        mGlow = new GVRSceneObject(gvrContext,
                gvrContext.createQuad(8.8f, 0.5f),
                gvrContext.loadTexture("seekbar/seekbar-glow.png"));
        mGlow.getRenderData().setRenderingOrder(
                GVRRenderingOrder.TRANSPARENT + 1);
        mGlow.getRenderData().setOffset(true);
        mGlow.getRenderData().setOffsetFactor(-1.0f);
        mGlow.getRenderData().setOffsetUnits(-1.0f);

        mCurrentTime = new GVRSceneObject(gvrContext, gvrContext.createQuad(
                2.4f, 0.3f), TextFactory.create(gvrContext, "1111"));
        mCurrentTime.getTransform().setPosition(-3.2f, -0.3f, 0.0f);
        mCurrentTime.getRenderData().setRenderingOrder(
                GVRRenderingOrder.TRANSPARENT + 2);
        mCurrentTime.getRenderData().setOffset(true);
        mCurrentTime.getRenderData().setOffsetFactor(-2.0f);
        mCurrentTime.getRenderData().setOffsetUnits(-2.0f);

        mDuration = new GVRSceneObject(gvrContext, gvrContext.createQuad(2.4f,
                0.3f), TextFactory.create(gvrContext, "2222"));
        mDuration.getTransform().setPosition(3.2f, -0.3f, 0.0f);
        mDuration.getRenderData().setRenderingOrder(
                GVRRenderingOrder.TRANSPARENT + 2);
        mDuration.getRenderData().setOffset(true);
        mDuration.getRenderData().setOffsetFactor(-2.0f);
        mDuration.getRenderData().setOffsetUnits(-2.0f);

        addChildObject(mPlayedSide);
        addChildObject(mLeftSide);
        addChildObject(mPointer);
        addChildObject(mGlow);
        addChildObject(mCurrentTime);
        addChildObject(mDuration);
    }

    public Float getRatio(float[] lookAt) {
        float x = lookAt[0];
        float y = lookAt[1];
        float z = lookAt[2];

        x *= -DEPTH / z;
        y *= -DEPTH / z;

        if (x > -WIDTH * 0.5f && x < WIDTH * 0.5f && y > Y - HEIGHT * 0.5f
                && y < Y + HEIGHT * 0.5f) {
            return x / WIDTH + 0.5f;
        } else {
            return null;
        }
    }

    @SuppressLint("DefaultLocale")
    public void setTime(GVRContext gvrContext, int current, int duration) {
        float ratio = (float) current / (float) duration;
        float left = -WIDTH * 0.5f;
        float center = ratio * WIDTH + left;
        float right = WIDTH * 0.5f;
        mPlayedSide.getTransform().setPositionX((left + center) * 0.5f);
        mPlayedSide.getTransform().setScaleX(center - left);
        mLeftSide.getTransform().setPositionX((center + right) * 0.5f);
        mLeftSide.getTransform().setScaleX(right - center);
        mPointer.getTransform().setPositionX(center);

        /*
         * ms to s
         */
        current /= 1000;
        duration /= 1000;

        String currentText = String.format("%02d:%02d:%02d", current / 3600,
                (current % 3600) / 60, current % 60);
        String durationText = String.format("%02d:%02d:%02d", duration / 3600,
                (duration % 3600) / 60, duration % 60);
        mCurrentTime.getRenderData().getMaterial()
                .setMainTexture(TextFactory.create(gvrContext, currentText));
        mDuration.getRenderData().getMaterial()
                .setMainTexture(TextFactory.create(gvrContext, durationText));
    }

    public void glow() {
        mGlow.getRenderData().setRenderMask(
                GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
    }

    public void unglow() {
        mGlow.getRenderData().setRenderMask(0);
    }

    public void setRenderMask(int renderMask) {
        mPlayedSide.getRenderData().setRenderMask(renderMask);
        mLeftSide.getRenderData().setRenderMask(renderMask);
        mPointer.getRenderData().setRenderMask(renderMask);
        mCurrentTime.getRenderData().setRenderMask(renderMask);
        mDuration.getRenderData().setRenderMask(renderMask);
    }
}
