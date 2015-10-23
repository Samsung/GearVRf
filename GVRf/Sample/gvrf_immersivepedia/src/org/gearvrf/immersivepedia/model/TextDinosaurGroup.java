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

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.immersivepedia.R;
import org.gearvrf.immersivepedia.dinosaur.Dinosaur;
import org.gearvrf.immersivepedia.dinosaur.DinosaurFactory;
import org.gearvrf.immersivepedia.focus.OnGestureListener;
import org.gearvrf.immersivepedia.props.Totem;
import org.gearvrf.immersivepedia.props.TotemEventListener;
import org.gearvrf.immersivepedia.util.AudioClip;
import org.gearvrf.immersivepedia.util.MathUtils;
import org.gearvrf.immersivepedia.util.RenderingOrderApplication;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;

import java.io.IOException;

public class TextDinosaurGroup extends GVRSceneObject implements TotemEventListener, OnGestureListener {

    private final float DESCRIPTION_HEIGHT = 3f;
    private final float DESCRIPTION_WIDTH = 16f;
    private final float TITLE_HEIGHT = 1f;
    private final float TITLE_WIDTH = 6f;
    private Dinosaur ankylosaurus;
    private GVRContext gvrContext;
    private GVRScene scene;
    private static final float TEXT_ANIMATION_TIME = 0.2f;
    private boolean isOpen;
    private GVRTextViewSceneObject title;
    private GVRTextViewSceneObject description;

    public TextDinosaurGroup(GVRContext gvrContext, GVRScene scene) throws IOException {
        super(gvrContext);

        this.gvrContext = gvrContext;
        this.scene = scene;
        createTotem();
        createDinosaur();
    }

    private void createDinosaur() throws IOException {

        ankylosaurus = DinosaurFactory.getInstance(getGVRContext()).getAnkylosaurus();
        ankylosaurus.getTransform().setRotationByAxis(-90, 1, 0, 0);
        ankylosaurus.attachEyePointeeHolder();
        ankylosaurus.setOnGestureListener(this);
        addChildObject(ankylosaurus);

    }

    private void createTotem() {
        Totem totem = new Totem(this.gvrContext,
                this.gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.totem_tex_diffuse)));

        totem.getTransform().setPosition(0f, 0f, 0f);
        totem.setTotemEventListener(this);
        scene.addSceneObject(totem);
        totem.getTransform().setPosition(-.3f, 0f, -5.0f);
        totem.getTransform().rotateByAxis(180.0f, 0f, 1f, 0f);
        totem.getTransform().setScale(1f, 1f, 1f);
        totem.setText(gvrContext.getActivity().getResources().getString(R.string.text_totem));
        totem.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.ANKYLOSAURUS_ANGLE_AROUND_CAMERA - 35.0f, 0f, 1f, 0f, 0f, 0f, 0f);
    }

    @Override
    public void onFinishLoadingTotem(Totem totem) {
        gvrContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUITextAppearSoundID(), 1.0f, 1.0f);
                show();
            }
        });
    }

    @Override
    public boolean shouldTotemAppear(Totem totem) {
        return !isOpen;
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
        new GVRRotationByAxisAnimation(ankylosaurus, 4f, 45, 0, 1, 0).start(gvrContext.getAnimationEngine());
    }

    @Override
    public void onSwipeBack() {
        AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
        new GVRRotationByAxisAnimation(ankylosaurus, 4f, -45, 0, 1, 0).start(gvrContext.getAnimationEngine());
    }

    @Override
    public void onSwipeIgnore() {
    }

    public void closeAction() {
        AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUITextDisappearSoundID(), 1.0f, 1.0f);
        new GVROpacityAnimation(description, TEXT_ANIMATION_TIME, 0f).start(getGVRContext().getAnimationEngine());
        new GVROpacityAnimation(title, TEXT_ANIMATION_TIME, 0f).start(getGVRContext().getAnimationEngine());
        isOpen = false;
    }

    private void show() {
        if (title == null) {
            createDinosaurTitle();
        }
        if (description == null) {
            createDinosaurDescription();
        }
        new GVROpacityAnimation(description, TEXT_ANIMATION_TIME, 1f).start(getGVRContext().getAnimationEngine());
        new GVROpacityAnimation(title, TEXT_ANIMATION_TIME, 1f).start(getGVRContext().getAnimationEngine());
        isOpen = true;
    }

    private void createDinosaurTitle() {
        String stringTitle = getGVRContext().getContext().getString(R.string.ankylosaurus_title);
        Drawable background = gvrContext.getActivity().getDrawable(R.drawable.title_background);
        title = new GVRTextViewSceneObject(gvrContext, gvrContext.getActivity(), TITLE_WIDTH, TITLE_HEIGHT, MathUtils.getViewContainerMeasurement(getGVRContext(), TITLE_WIDTH), MathUtils.getViewContainerMeasurement(
                getGVRContext(), TITLE_HEIGHT), stringTitle);
        title.setRefreshFrequency(IntervalFrequency.HIGH);
        title.setTextColor(Color.BLACK);
        title.setBackGround(background);
        title.getTransform().setScale(0.3f, 0.3f, 0.3f);
        title.setTextSize(51);
        title.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        title.getTransform().setPosition(-2f, 2.6f, 3f);
        addChildObject(title);
        title.getRenderData().getMaterial().setOpacity(0);
    }

    private void createDinosaurDescription() {
        description = new GVRTextViewSceneObject(getGVRContext(), gvrContext.getActivity()
                , DESCRIPTION_WIDTH, DESCRIPTION_HEIGHT, MathUtils.getViewContainerMeasurement(getGVRContext(), DESCRIPTION_WIDTH), MathUtils.getViewContainerMeasurement(getGVRContext(), DESCRIPTION_HEIGHT), getGVRContext().getContext().getString(
                        R.string.ankylosaurus_text));
        Drawable background = gvrContext.getActivity().getDrawable(R.drawable.white_texture);
        description.setGravity(Gravity.LEFT);
        description.setTextColor(Color.BLACK);
        description.getTransform().setPositionY(2f);
        description.getRenderData().setRenderingOrder(RenderingOrderApplication.TEXT_BACKGROUND);
        description.setTextSize(49);
        description.setBackGround(background);
        description.getTransform().setScale(0.3f, 0.3f, 0.3f);
        description.getTransform().setPosition(-.3f, 1.7f, 3f);
        addChildObject(description);
        description.getRenderData().getMaterial().setOpacity(0);
    }

    public boolean isOpen() {
        return isOpen;
    }

}
