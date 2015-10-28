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

package org.gearvrf.immersivepedia.props;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;
import org.gearvrf.GVRTexture;
import org.gearvrf.immersivepedia.R;
import org.gearvrf.immersivepedia.focus.FocusListener;
import org.gearvrf.immersivepedia.focus.FocusableSceneObject;
import org.gearvrf.immersivepedia.focus.OnClickListener;
import org.gearvrf.immersivepedia.loadComponent.LoadComponent;
import org.gearvrf.immersivepedia.loadComponent.LoadComponentListener;
import org.gearvrf.immersivepedia.util.AudioClip;
import org.gearvrf.immersivepedia.util.MathUtils;
import org.gearvrf.immersivepedia.util.PlayPauseButton;
import org.gearvrf.immersivepedia.util.RenderingOrderApplication;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

public class Totem extends FocusableSceneObject implements FocusListener {

    private static final float TEXT_HEIGHT = 2f;
    private static final float TEXT_WIDTH = 5f;
    private LoadComponent loadComponent = null;
    private GVRContext gvrContext = null;
    private TotemEventListener totemEventListener;
    private PlayPauseButton icon;
    private int streamIDTotem;

    public Totem(GVRContext gvrContext, GVRTexture t) {
        super(gvrContext, gvrContext.loadMesh(new GVRAndroidResource(gvrContext.getActivity(),
                R.raw.totem_standup_mesh)), t);

        prepareTotem(gvrContext);

        this.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                if (loadComponent != null && loadComponent.isLoading()) {
                    loadComponent.finishLoadComponent();
                    AudioClip.getInstance(getGVRContext().getContext()).pauseSound(streamIDTotem);
                }
            }
        });

    }

    private void prepareTotem(GVRContext gvrContext) {
        this.gvrContext = gvrContext;
        this.getRenderData().setCullFace(GVRCullFaceEnum.None);
        getRenderData().setRenderingOrder(RenderingOrderApplication.TOTEM);
        attachEyePointeeHolder();
        this.focusListener = this;
    }

    @Override
    public void gainedFocus(FocusableSceneObject object) {
        if (this.totemEventListener != null
                && this.totemEventListener.shouldTotemAppear(this) == true) {
            createLoadComponent();
            streamIDTotem = AudioClip.getInstance(getGVRContext().getContext()).playLoop(AudioClip.getUILoadingSoundID(), 1.0f, 1.0f);
        }
    }

    @Override
    public void lostFocus(FocusableSceneObject object) {
        if (this.loadComponent != null) {
            this.loadComponent.disableListener();
            this.removeChildObject(this.loadComponent);
            AudioClip.getInstance(getGVRContext().getContext()).stopSound(streamIDTotem);
        }
    }

    @Override
    public void inFocus(FocusableSceneObject object) {

    }

    private void createLoadComponent() {

        loadComponent = new LoadComponent(gvrContext, new LoadComponentListener() {

            @Override
            public void onFinishLoadComponent() {
                Totem.this.onFinishLoadComponent();

            }
        });
        this.addChildObject(loadComponent);

        loadComponent.setFloatTexture();
        loadComponent.getTransform().setPosition(0f, 1f, -0.11f);
        loadComponent.getTransform().rotateByAxis(180f, 0f, 1f, 0f);
        loadComponent.getTransform().setScale(1f, 1f, 1f);

    }

    private void onFinishLoadComponent() {

        if (this.totemEventListener != null) {
            this.totemEventListener.onFinishLoadingTotem(this);
            removeChildObject(loadComponent);
        }
    }

    public void setText(String text) {
        GVRTextViewSceneObject textTitle = new GVRTextViewSceneObject(gvrContext, gvrContext.getActivity(), TEXT_WIDTH, TEXT_HEIGHT,
                MathUtils.getViewContainerMeasurement(
                        getGVRContext(), TEXT_WIDTH), MathUtils.getViewContainerMeasurement(getGVRContext(), TEXT_HEIGHT), text);
        textTitle.setTextSize(22);
        textTitle.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        textTitle.getTransform().setPosition(0f, .6f, -0.1f);
        textTitle.getTransform().rotateByAxis(-180, 0, 1, 0);
        addChildObject(textTitle);
    }

    public void setIcon(int iconPath) {
        icon = new PlayPauseButton(gvrContext, .3f, .3f,
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext, iconPath)));
        icon.getTransform().setPosition(0f, 1f, -0.11f);
        icon.getTransform().rotateByAxis(-180, 0, 1, 0);
        icon.attachEyePointeeHolder();
        addChildObject(icon);
    }

    public PlayPauseButton getIcon() {
        return icon;
    }

    public void setTotemEventListener(TotemEventListener listener) {
        this.totemEventListener = listener;
    }

}
