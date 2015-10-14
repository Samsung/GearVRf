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

package org.gearvrf.controls.model;

import android.content.res.Resources;
import android.content.res.TypedArray;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRInterpolator;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.controls.MainScript;
import org.gearvrf.controls.R;
import org.gearvrf.controls.WormShadow;
import org.gearvrf.controls.anim.AnimationsTime;
import org.gearvrf.controls.interpolators.Bounce;
import org.gearvrf.controls.interpolators.CircularIn;
import org.gearvrf.controls.interpolators.CircularOut;
import org.gearvrf.controls.interpolators.ExpoIn;
import org.gearvrf.controls.interpolators.ExpoOut;
import org.gearvrf.controls.interpolators.QuadIn;
import org.gearvrf.controls.interpolators.QuadOut;
import org.gearvrf.controls.shaders.ColorSwapShader;
import org.gearvrf.controls.util.Constants;
import org.gearvrf.controls.util.RenderingOrder;
import org.gearvrf.controls.util.Util;

import java.util.ArrayList;

public class Apple extends GVRSceneObject {

    // public final float ANIMATION_DURATION =
    // AnimationsTime.getDropTime();//2.5f;
    public final float OPACITY_ANIMATION_DURATION = 2;
    public final float Y_ANIMATION_DELTA = -5;
    private final float APPLE_SCALE = 0.75f;
    private final static float MAX_APPLES_DISTANCE = 1.5f;
    private final static float CAMERA_DIRECTION_THREASHOLD = 0.75f;
    public static ArrayList<Apple> appleList = new ArrayList<Apple>();
    public Star star;
    private WormShadow shadow;

    public static int currentMotion = 0;

    public enum Motion {
        Linear, Bouncing, CircularIn, CircularOut, ExpoIn, ExpoOut, QuadIn, QuadOut
    };

    public static Motion motion = Motion.Linear;

    public Apple(GVRContext gvrContext) {
        super(gvrContext);
        this.getTransform().setScale(APPLE_SCALE, APPLE_SCALE, APPLE_SCALE);
        setAppleRenderData(gvrContext);
        setAppleShaderParameters(gvrContext);
        star = new Star(gvrContext);
        shadow = new WormShadow(gvrContext, 0.27f, 0.27f, RenderingOrder.APPLE_SHADOW);
        shadow.getTransform().setScale(2, 2, 2);
        gvrContext.getMainScene().addSceneObject(star);
    }

    public void setAppleRenderData(GVRContext gvrContext) {
        GVRMesh mesh = gvrContext.loadMesh(new GVRAndroidResource(gvrContext,
                R.raw.apple));

        ColorSwapShader shader = new ColorSwapShader(gvrContext);
        GVRMaterial material = new GVRMaterial(gvrContext, shader.getShaderId());
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        renderData.setMesh(mesh);
        renderData.setMaterial(material);
        this.attachRenderData(renderData);

        getRenderData().setRenderingOrder(RenderingOrder.APPLE);
    }

    public void setAppleShaderParameters(GVRContext gvrContext) {
        GVRTexture grayScaleTexture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.apple_diffuse));
        GVRTexture detailsTexture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.apple_details));

        this.getRenderData().getMaterial().setOpacity(0);
        this.getRenderData().getMaterial()
                .setTexture(ColorSwapShader.TEXTURE_GRAYSCALE, grayScaleTexture);
        this.getRenderData().getMaterial()
                .setTexture(ColorSwapShader.TEXTURE_DETAILS, detailsTexture);
        updateAppleColor();
    }

    public static void addApple(Apple apple) {
        appleList.add(apple);
    }

    public static float[] getColor(GVRContext gvrContext) {

        Resources res = gvrContext.getContext().getResources();
        TypedArray colorArray = res.obtainTypedArray(R.array.colors);
        TypedArray colorTypeValues;
        float[] appleColor = new float[3];
        colorTypeValues = res.obtainTypedArray(colorArray.getResourceId(Apple.currentMotion, 0));

        appleColor[0] = colorTypeValues.getFloat(0, 0);
        appleColor[1] = colorTypeValues.getFloat(1, 0);
        appleColor[2] = colorTypeValues.getFloat(2, 0);

        return Util.normalizeColor(appleColor);

    }

    public static GVRInterpolator defineInterpolator(Motion motion) {

        GVRInterpolator interpolator = null;
        switch (motion) {

            case Bouncing:
                interpolator = new Bounce();

                break;
            case CircularIn:
                interpolator = new CircularIn();

                break;
            case CircularOut:
                interpolator = new CircularOut();

                break;
            case ExpoIn:
                interpolator = new ExpoIn();

                break;
            case ExpoOut:
                interpolator = new ExpoOut();

                break;
            case QuadIn:
                interpolator = new QuadIn();

                break;
            case QuadOut:
                interpolator = new QuadOut();

                break;
            default:
                interpolator = null;

                break;
        }
        currentMotion = motion.ordinal();
        return interpolator;
    }

    public void updateAppleColor() {

        float[] color = getColor(this.getGVRContext());
        this.getRenderData().getMaterial()
                .setVec4(ColorSwapShader.COLOR, color[0], color[1], color[2], 1);

    }

    public void playAnimation(GVRContext gvrContext) {

        GVRAnimation anim = new GVRRelativeMotionAnimation(this, AnimationsTime.getDropTime(), 0,
                -Constants.APPLE_INICIAL_YPOS - 1, 0);
        anim.setInterpolator(defineInterpolator(motion));
        anim.start(gvrContext.getAnimationEngine());
        playShadowAnimation();
        playOpacityAnimation(gvrContext);
    }

    private void playShadowAnimation() {
        new GVRScaleAnimation(shadow, AnimationsTime.getDropTime(), 2f).setInterpolator(defineInterpolator(motion)).start(
                getGVRContext().getAnimationEngine());
    }

    public void playOpacityAnimation(GVRContext gvrContext) {

        GVRAnimation anim = new GVROpacityAnimation(this, OPACITY_ANIMATION_DURATION, 1);
        anim.start(gvrContext.getAnimationEngine());

    }

    public void resetPosition(GVRContext gvrContext) {

        updateAppleColor();
        star.playMoveAnimation(gvrContext, this);
        setApplePositionInsideFrustum(gvrContext);
        this.getTransform().setPositionY(Constants.APPLE_INICIAL_YPOS);
        playAnimation(gvrContext);

    }

    public boolean checkValidPosition(Vector3D pos) {

        Vector3D wormPos = new
                Vector3D(MainScript.worm.wormParent.getTransform().getPositionX(),
                        MainScript.worm.wormParent
                                .getTransform().getPositionY(), MainScript.worm.wormParent
                                .getTransform()
                                .getPositionZ());
        if (Vector3D.distance(pos, wormPos) < MAX_APPLES_DISTANCE)
            return false;
        for (Apple a : appleList) {

            if (a == this)
                continue;
            Vector3D iteratedApple = new
                    Vector3D(a.getTransform().getPositionX(), 0, a.getTransform()
                            .getPositionZ());
            float distance = (float) Vector3D.distance(pos, iteratedApple);

            if (distance < MAX_APPLES_DISTANCE) {

                return false;
            }

        }
        return true;
    }

    public void setAppleRandomPosition(
            GVRContext context) {

        float angle = (float) Math.random() * 360;
        float distance = (float) (Math.random()
                * (Constants.MAX_APPLE_DISTANCE - (Constants.MAX_APPLE_DISTANCE - Constants.MIN_APPLE_DISTANCE))
                + Constants.MIN_APPLE_DISTANCE);
        this.getTransform().setPositionZ(distance);
        this.getTransform().rotateByAxisWithPivot(angle, 0, 1, 0, 0, 0, 0);

        Vector3D instanceApple = new Vector3D(this.getTransform().getPositionX(), this
                .getTransform().getPositionY(), this.getTransform().getPositionZ());
        if (!checkValidPosition(instanceApple)) {
            setAppleRandomPosition(context);

        }
        else {
            if (!appleList.contains(this)) {
                addApple(this);
                shadow.getTransform().setPosition((float) instanceApple.getX(), -0.9999f, (float) instanceApple.getZ());
                getGVRContext().getMainScene().addSceneObject(shadow);
            }
        }
    }

    public void setApplePositionInsideFrustum(
            GVRContext context) {
        float angle = (float) Math.random() * 80 - 40;
        float distance = (float) (Math.random()
                * (Constants.MAX_WORM_MOVE_DISTANCE - (Constants.MAX_WORM_MOVE_DISTANCE - Constants.MIN_WORM_MOVE_DISTANCE))
                + Constants.MIN_WORM_MOVE_DISTANCE);

        Vector3D instanceApple = setNewApplePosition(context, angle, distance);
        if (instanceApple == null)
            return;
        if (!checkValidPosition(instanceApple)) {

            setApplePositionInsideFrustum(context);

        }

    }

    private Vector3D setNewApplePosition(GVRContext context, float angle, float distance) {
        float[] vecDistance = context.getMainScene().getMainCameraRig().getLookAt();

        if (vecDistance[1] > CAMERA_DIRECTION_THREASHOLD
                || vecDistance[1] < -CAMERA_DIRECTION_THREASHOLD) {
            setAppleRandomPosition(context);
            return null;
        }

        vecDistance[0] *= distance;
        vecDistance[2] *= distance;
        Vector3D instanceApple = new Vector3D(vecDistance[0], 0, vecDistance[2]);
        instanceApple.normalize();
        this.getTransform().setPositionX((float) instanceApple.getX());
        this.getTransform().setPositionZ((float) instanceApple.getZ());
        this.getTransform().rotateByAxisWithPivot(angle, 0, 1, 0, 0, 0, 0);
        instanceApple = new Vector3D(this.getTransform().getPositionX(), this
                .getTransform().getPositionY(), this.getTransform().getPositionZ());
        shadow.getTransform().setPosition((float) instanceApple.getX(), -0.9999f, (float) instanceApple.getZ());
        shadow.getTransform().setScale(1, 1, 1);
        return instanceApple;
    }
}
