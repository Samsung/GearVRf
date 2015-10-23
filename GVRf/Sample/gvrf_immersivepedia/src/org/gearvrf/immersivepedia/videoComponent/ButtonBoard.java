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
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.immersivepedia.R;
import org.gearvrf.immersivepedia.focus.FocusListener;
import org.gearvrf.immersivepedia.focus.FocusableSceneObject;
import org.gearvrf.immersivepedia.focus.OnClickListener;
import org.gearvrf.immersivepedia.util.PlayPauseButton;
import org.gearvrf.immersivepedia.util.RenderingOrderApplication;

public class ButtonBoard extends GVRSceneObject {

    private final float PLAY_PAUSE_X_POSITION = 0.1f;
    private final float PLAY_PAUSE_Y_POSITION = -1.8f;
    private final float PLAY_PAUSE_Z_POSITION = 0.01f;

    private static final float WIDTH = .3f;
    private static final float HEIGHT = .3f;

    private PlayPauseButton playPauseButton;
    private VideoComponent videoComponent;

    private GVRContext gvrContext;

    public ButtonBoard(GVRContext gvrContext, float width, float height, GVRTexture texture,
            VideoComponent videoComponent) {
        super(gvrContext, width, height, texture);

        this.videoComponent = videoComponent;
        this.gvrContext = gvrContext;

        getRenderData().setRenderingOrder(RenderingOrderApplication.BUTTON_BOARD);

        createButtonPlayAndPause();
    }

    private void createButtonPlayAndPause() {
        playPauseButton = new PlayPauseButton(gvrContext, WIDTH, HEIGHT,
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.play)));
        playPauseButton.getRenderData().setRenderingOrder(RenderingOrderApplication.BUTTON_BOARD + 1);
        playPauseButton.getTransform().setPosition(PLAY_PAUSE_X_POSITION, PLAY_PAUSE_Y_POSITION, PLAY_PAUSE_Z_POSITION);
        playPauseButton.attachEyePointeeHolder();
        renderTextureButton(PlayPauseButton.PAUSE_NORMAL, playPauseButton);
        playPauseButton.focusListener = new FocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {

                if (videoComponent.isPlaying()) {
                    renderTextureButton(PlayPauseButton.PAUSE_NORMAL, playPauseButton);
                } else {
                    renderTextureButton(PlayPauseButton.PLAY_NORMAL, playPauseButton);
                }
            }

            @Override
            public void inFocus(FocusableSceneObject object) {

                if (videoComponent.isPlaying()) {
                    renderTextureButton(PlayPauseButton.PAUSE_HOVER, playPauseButton);
                } else {
                    renderTextureButton(PlayPauseButton.PLAY_HOVER, playPauseButton);
                }

            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {

            }
        };

        playPauseButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {

                if (playPauseButton.isFocus()) {

                    if (videoComponent.isPlaying()) {
                        videoComponent.pauseVideo();
                    } else {
                        videoComponent.playVideo();
                    }

                }

            }
        });
        addChildObject(playPauseButton);
    }

    public void turnOffGUIButton() {
        new GVROpacityAnimation(playPauseButton, .1f, 0).start(gvrContext.getAnimationEngine());
    }

    public void turnOnGUIButton() {
        new GVROpacityAnimation(playPauseButton, .1f, 1).start(gvrContext.getAnimationEngine());
    }

    public void turnOffGUIButtonUpdatingTexture() {
        new GVROpacityAnimation(playPauseButton, .1f, 0).start(gvrContext.getAnimationEngine()).setOnFinish(new GVROnFinish() {

            @Override
            public void finished(GVRAnimation animation) {
                renderTextureButton(PlayPauseButton.PAUSE_NORMAL, playPauseButton);
            }
        });
    }

    public void turnOnGUIButtonUpdatingTexture() {
        turnOnGUIButton();
        renderTextureButton(PlayPauseButton.PLAY_NORMAL, playPauseButton);
    }

    public void renderTextureButton(String textureID, GVRSceneObject sceneObject) {

        sceneObject.getRenderData().getMaterial()
                .setMainTexture(sceneObject.getRenderData().getMaterial().getTexture(textureID));
    }

    public void closeAction() {
        videoComponent.hideVideo();
    }

}
