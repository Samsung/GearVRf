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

package org.gearvrf.immersivepedia.model;

import java.io.IOException;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.immersivepedia.R;
import org.gearvrf.immersivepedia.dinosaur.DinosaurFactory;
import org.gearvrf.immersivepedia.focus.FocusListener;
import org.gearvrf.immersivepedia.focus.FocusableSceneObject;
import org.gearvrf.immersivepedia.focus.OnClickListener;
import org.gearvrf.immersivepedia.focus.OnGestureListener;
import org.gearvrf.immersivepedia.focus.SwipeIndicator;
import org.gearvrf.immersivepedia.props.Totem;
import org.gearvrf.immersivepedia.util.AudioClip;
import org.gearvrf.immersivepedia.util.PlayPauseButton;

public class RotateDinosaurGroup extends GVRSceneObject implements OnGestureListener {

    private FocusableSceneObject styrocosaurus;
    private GVRContext gvrContext;
    private GVRScene scene;
    private Totem totem;
    private boolean isPlaying;
    private GVRAnimation animation;
    private SwipeIndicator swipeIndicator;
    private int streamID;

    public RotateDinosaurGroup(GVRContext gvrContext, GVRScene scene) throws IOException {
        super(gvrContext);

        this.gvrContext = gvrContext;
        this.scene = scene;

        createDinosaur();
        createTotem();
        createDinoAnimation();
        createSwipeIndicator();

    }

    private void createSwipeIndicator() {
        swipeIndicator = new SwipeIndicator(gvrContext, styrocosaurus);
        swipeIndicator.getTransform().setPosition(0, 1.5f, -3f);
        swipeIndicator.init();
        addChildObject(swipeIndicator);
    }

    private void createTotem() {

        totem = new Totem(this.gvrContext, this.gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.totem_tex_diffuse)));
        totem.setTotemEventListener(null);
        scene.addSceneObject(totem);
        totem.getTransform().setPosition(-.3f, 0f, -5.0f);
        totem.getTransform().rotateByAxis(180.0f, 0f, 1f, 0f);
        totem.getTransform().setScale(1f, 1f, 1f);
        totem.setText(gvrContext.getActivity().getResources().getString(R.string.rotate_totem));
        totem.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.STYRACOSAURUS_ANGLE_AROUND_CAMERA - 35.0f, 0f, 1f, 0f, 0f, 0f, 0f);
        totem.setIcon(R.drawable.play);
    }

    private void createDinoAnimation() {
        final PlayPauseButton playPause = totem.getIcon();
        playPause.attachEyePointeeHolder();
        playPause.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {

                if (animation == null) {
                    startAnimation();
                    streamID = AudioClip.getInstance(getGVRContext().getContext()).playLoop(AudioClip.getUiLoopRotateSoundID(), 1.0f, 1.0f);
                } else {
                    stopAnimation();
                    AudioClip.getInstance(getGVRContext().getContext()).pauseSound(streamID);
                }
            }

        });

        playPause.focusListener = new FocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {
                if (isPlaying) {
                    renderTextureButton(PlayPauseButton.PAUSE_NORMAL, playPause);
                } else {
                    renderTextureButton(PlayPauseButton.PLAY_NORMAL, playPause);
                }
            }

            @Override
            public void inFocus(FocusableSceneObject object) {
                if (isPlaying) {
                    renderTextureButton(PlayPauseButton.PAUSE_HOVER, playPause);
                } else {
                    renderTextureButton(PlayPauseButton.PLAY_HOVER, playPause);
                }
            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {

            }
        };

    }

    private void startAnimation() {
        animation = new GVRRotationByAxisAnimation(styrocosaurus, 25, 360, 0, 1, 0).start(gvrContext.getAnimationEngine());
        animation.setRepeatMode(1);
        animation.setRepeatCount(-1);
        isPlaying = true;
        swipeIndicator.setStop(true);

    }

    private void stopAnimation() {
        gvrContext.getAnimationEngine().stop(animation);
        animation = null;
        isPlaying = false;
    }

    private void createDinosaur() throws IOException {
        styrocosaurus = DinosaurFactory.getInstance(gvrContext).getStyracosaurus();
        styrocosaurus.getTransform().rotateByAxisWithPivot(-90, 1, 0, 0, 0, 0, 0);
        styrocosaurus.getTransform().setPosition(0, 0, -8);
        styrocosaurus.setOnGestureListener(this);
        addChildObject(styrocosaurus);
    }

    public void renderTextureButton(String textureID, GVRSceneObject sceneObject) {
        sceneObject.getRenderData().getMaterial()
                .setMainTexture(sceneObject.getRenderData().getMaterial().getTexture(textureID));
    }

    @Override
    public void onSwipeUp() {
    }

    @Override
    public void onSwipeDown() {
    }

    @Override
    public void onSwipeForward() {
        if (!isPlaying) {
            AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
            new GVRRotationByAxisAnimation(styrocosaurus, 4f, 45, 0, 1, 0).start(gvrContext.getAnimationEngine());
            swipeIndicator.setStop(true);
        }
    }

    @Override
    public void onSwipeBack() {
        if (!isPlaying) {
            AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
            new GVRRotationByAxisAnimation(styrocosaurus, 4f, -45, 0, 1, 0).start(gvrContext.getAnimationEngine());
            swipeIndicator.setStop(true);
        }
    }

    @Override
    public void onSwipeIgnore() {
    }

}
