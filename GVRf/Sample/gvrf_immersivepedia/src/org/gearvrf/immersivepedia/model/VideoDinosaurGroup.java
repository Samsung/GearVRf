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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.immersivepedia.R;
import org.gearvrf.immersivepedia.dinosaur.Dinosaur;
import org.gearvrf.immersivepedia.dinosaur.DinosaurFactory;
import org.gearvrf.immersivepedia.focus.OnGestureListener;
import org.gearvrf.immersivepedia.props.Totem;
import org.gearvrf.immersivepedia.props.TotemEventListener;
import org.gearvrf.immersivepedia.util.AudioClip;
import org.gearvrf.immersivepedia.videoComponent.VideoComponent;

import java.io.IOException;

public class VideoDinosaurGroup extends GVRSceneObject implements TotemEventListener, OnGestureListener {

    private VideoComponent videoComponent;
    private Dinosaur trex;
    private GVRContext gvrContext;
    private GVRScene scene;

    public VideoDinosaurGroup(GVRContext gvrContext, GVRScene scene) throws IOException {
        super(gvrContext);

        this.gvrContext = gvrContext;
        this.scene = scene;

        createDinosaur();
        createTotem();
    }

    private void createVideoComponent() {

        videoComponent = new VideoComponent(getGVRContext(), VideoComponent.WIDTH, VideoComponent.HEIGHT);
        videoComponent.getTransform().setPosition(0f, 0f, 0f);
        videoComponent.getTransform().setPosition(0f, 2.0f, -3.0f);
        videoComponent.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.TREX_ANGLE_AROUND_CAMERA, 0f, 1f, 0f, 0f, 0f, 0f);
        scene.addSceneObject(videoComponent);

    }

    private void createDinosaur() throws IOException {

        trex = DinosaurFactory.getInstance(getGVRContext()).getTRex();
        trex.attachEyePointeeHolder();
        trex.setOnGestureListener(this);
        trex.getTransform().setRotationByAxis(-90, 1, 0, 0);
        addChildObject(trex);

    }

    private void createTotem() {
        Totem totem = new Totem(this.gvrContext,
                this.gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.totem_tex_diffuse)));

        totem.getTransform().setPosition(0f, 0f, 0f);
        totem.setTotemEventListener(this);
        scene.addSceneObject(totem);
        totem.getTransform().setPosition(.3f, 0f, -5.0f);
        totem.getTransform().rotateByAxis(180.0f, 0f, 1f, 0f);
        totem.getTransform().setScale(1f, 1f, 1f);
        totem.setText(gvrContext.getActivity().getResources().getString(R.string.video_totem));
        totem.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.TREX_ANGLE_AROUND_CAMERA - 35.0f, 0f, 1f, 0f, 0f, 0f, 0f);
    }

    public void onStep() {
        if (videoComponent != null && videoComponent.isPlaying()) {
            videoComponent.getSeekbar().setTime(videoComponent.getCurrentPosition(),
                    videoComponent.getDuration());
        }
    }

    @Override
    public void onFinishLoadingTotem(Totem totem) {
        gvrContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                createVideoComponent();
                videoComponent.showVideo();
            }
        });
    }

    @Override
    public boolean shouldTotemAppear(Totem totem) {
        if (videoComponent != null) {
            return !videoComponent.isActive();
        } else {
            return true;
        }
    }

    public boolean isOpen() {
        if (videoComponent != null)
            return videoComponent.isActive();
        return false;
    }

    public void closeAction() {
        videoComponent.getButtonbar().closeAction();
    }

    @Override
    public void onSwipeUp() {
    }

    @Override
    public void onSwipeDown() {
    }

    @Override
    public void onSwipeForward() {
        AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
        new GVRRotationByAxisAnimation(trex, 4f, 45, 0, 1, 0).start(gvrContext.getAnimationEngine());
    }

    @Override
    public void onSwipeBack() {
        AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
        new GVRRotationByAxisAnimation(trex, 4f, -45, 0, 1, 0).start(gvrContext.getAnimationEngine());
    }

    @Override
    public void onSwipeIgnore() {
    }

}
