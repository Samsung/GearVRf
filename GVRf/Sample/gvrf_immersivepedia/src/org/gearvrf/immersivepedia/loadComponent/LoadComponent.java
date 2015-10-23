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

package org.gearvrf.immersivepedia.loadComponent;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.immersivepedia.R;
import org.gearvrf.immersivepedia.focus.FocusListener;
import org.gearvrf.immersivepedia.focus.FocusableSceneObject;
import org.gearvrf.immersivepedia.shader.CutoutShader;
import org.gearvrf.immersivepedia.util.RenderingOrderApplication;

public class LoadComponent extends GVRSceneObject implements FocusListener {

    private static final int CUTOUT_VALUE = 1;
    private static final float LOADING_SPEED = 0.01f;
    private GVRTexture circleAlphaTexture;
    private GVRTexture circleTexture;
    private GVRTexture plusTexture;

    private GVRSceneObject circleAlpha;
    private GVRSceneObject plus;
    private FocusableSceneObject circle;
    private GVRContext gvrContext;

    private float valueFloatTexture;

    private GVRDrawFrameListener drawFrameListener;
    private LoadComponentListener componentListener;
    private boolean isLoading = false;

    public LoadComponent(GVRContext gvrContext, LoadComponentListener componentListener) {
        super(gvrContext);
        this.componentListener = componentListener;
        this.gvrContext = gvrContext;
        this.gvrContext.runOnGlThread(new Runnable() {

            @Override
            public void run() {

                loadTexture();
                createLoadComponent();
            }
        });
    }

    private void createLoadComponent() {
        circleAlpha = new GVRSceneObject(gvrContext, gvrContext.createQuad(.5f, .5f),
                circleAlphaTexture);
        plus = new GVRSceneObject(gvrContext, gvrContext.createQuad(.5f, .5f), plusTexture);
        circle = new FocusableSceneObject(gvrContext, gvrContext.createQuad(.5f, .5f),
                circleTexture);

        plus.getRenderData().getMaterial().setMainTexture(plusTexture);
        plus.getRenderData().setRenderingOrder(RenderingOrderApplication.LOADING_COMPONENT);

        circle.getRenderData().getMaterial().setMainTexture(circleTexture);
        circle.getRenderData().setRenderingOrder(RenderingOrderApplication.LOADING_COMPONENT);
        circle.focusListener = this;

        circleAlpha.getRenderData().getMaterial()
                .setShaderType(new CutoutShader(gvrContext).getShaderId());
        circleAlpha.getRenderData().getMaterial()
                .setTexture(CutoutShader.TEXTURE_KEY, circleAlphaTexture);
        circleAlpha.getRenderData().setRenderingOrder(RenderingOrderApplication.LOADING_COMPONENT);
        circleAlpha.getRenderData().getMaterial().setMainTexture(circleAlphaTexture);

        addChildObject(circleAlpha);
        addChildObject(plus);
        addChildObject(circle);
    }

    private void loadTexture() {
        circleAlphaTexture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.loading_two__colors));
        circleTexture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.loading));
        plusTexture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.plus));
    }

    public void setFloatTexture() {

        drawFrameListener = new GVRDrawFrameListener() {

            @Override
            public void onDrawFrame(float frameTime) {
                isLoading = true;
                valueFloatTexture += LOADING_SPEED;

                if (valueFloatTexture <= CUTOUT_VALUE) {
                    circleAlpha.getRenderData().getMaterial()
                            .setFloat(CutoutShader.CUTOUT, valueFloatTexture);

                } else {
                    startSoundLoadComponent();
                }

            }
        };

        gvrContext.registerDrawFrameListener(drawFrameListener);

    }

    private void startSoundLoadComponent() {
        finishLoadComponent();
    }

    public void finishLoadComponent() {
        gvrContext.unregisterDrawFrameListener(drawFrameListener);
        isLoading = false;
        gvrContext.getMainScene().removeSceneObject(this);

        componentListener.onFinishLoadComponent();
    }

    public void disableListener() {
        gvrContext.unregisterDrawFrameListener(drawFrameListener);
    }

    public void removeLoadComponent() {
        gvrContext.unregisterDrawFrameListener(drawFrameListener);
        gvrContext.getMainScene().removeSceneObject(this);
    }

    @Override
    public void gainedFocus(FocusableSceneObject object) {

    }

    @Override
    public void lostFocus(FocusableSceneObject object) {
        finishLoadComponent();

    }

    @Override
    public void inFocus(FocusableSceneObject object) {

    }

    public boolean isLoading() {
        return isLoading;
    }

}
