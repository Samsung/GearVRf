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

package org.gearvrf.immersivepedia.videoComponent;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.immersivepedia.R;
import org.gearvrf.immersivepedia.focus.FocusableSceneObject;
import org.gearvrf.immersivepedia.focus.OnClickListener;

public class Seekbar extends FocusableSceneObject {

    public static final float WIDTH = 4.2f;
    private static final float HEIGHT = 0.2f;

    private GVRSceneObject playedSide;
    private FocusableSceneObject seekbarHover;

    private GVRContext gvrContext;

    public Seekbar(GVRContext gvrContext, float width, float height, GVRTexture texture) {
        super(gvrContext, width, height, texture);
        this.gvrContext = gvrContext;

        addChildObject(createPlaySide());
        addChildObject(createSeekbarHover());

        createEyePointee();
    }

    private GVRSceneObject createSeekbarHover() {

        seekbarHover = new FocusableSceneObject(gvrContext, gvrContext.createQuad(WIDTH, HEIGHT),
                gvrContext.loadTexture(new GVRAndroidResource(
                        gvrContext.getActivity(), R.drawable.timelime_hover_mask)));
        seekbarHover.getTransform().setPositionZ(.1f);

        return seekbarHover;
    }

    private void createEyePointee() {
        attachEyePointeeHolder();
        seekbarHover.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {

                VideoComponent videoComponent = (VideoComponent) getParent();
                videoComponent.forwardOrRewindVideo();

            }
        });
    }

    private GVRSceneObject createPlaySide() {
        playedSide = new GVRSceneObject(gvrContext, gvrContext.createQuad(1.0f, HEIGHT / 2), gvrContext.loadTexture(new GVRAndroidResource(
                gvrContext.getActivity(), R.drawable.timeline_watched)));

        playedSide.getRenderData().setRenderingOrder(GVRRenderingOrder.TRANSPARENT + 2);
        playedSide.getRenderData().setOffset(true);
        playedSide.getRenderData().setOffsetFactor(-2.0f);
        playedSide.getRenderData().setOffsetUnits(-2.0f);
        playedSide.getTransform().setPositionZ(.1f);
        return playedSide;
    }

    public void setTime(int current, int duration) {

        float ratio = (float) current / (float) duration;
        float left = -WIDTH * 0.5f;
        float center = ratio * WIDTH + left;

        playedSide.getTransform().setPositionX((left + center) * 0.5f);
        playedSide.getTransform().setScaleX(center - left);
    }

    public void turnOffGUISeekbar() {
        seekbarHover.detachEyePointeeHolder();
        new GVROpacityAnimation(this, .1f, 0).start(gvrContext.getAnimationEngine());
        new GVROpacityAnimation(seekbarHover, .1f, 0).start(gvrContext.getAnimationEngine());
        new GVROpacityAnimation(playedSide, .1f, 0).start(gvrContext.getAnimationEngine());
    }

    public void turnOnGUISeekbar() {
        seekbarHover.attachEyePointeeHolder();
        new GVROpacityAnimation(this, .1f, 1).start(gvrContext.getAnimationEngine());
        new GVROpacityAnimation(seekbarHover, .1f, 1).start(gvrContext.getAnimationEngine());
        new GVROpacityAnimation(playedSide, .1f, 1).start(gvrContext.getAnimationEngine());
    }

}
