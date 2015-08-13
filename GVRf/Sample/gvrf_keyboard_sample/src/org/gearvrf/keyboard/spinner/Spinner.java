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

package org.gearvrf.keyboard.spinner;

import android.util.Log;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.keyboard.model.CharList;
import org.gearvrf.keyboard.textField.TextFieldItem;
import org.gearvrf.keyboard.util.Util;

public class Spinner extends GVRSceneObject {

    private static final float Z_DISTANCE = 3 * 0.15f;
    private static final float ANIMATION_TIME = .1f;
    private SpinnerSkeleton spinnerSkeleton;
    private SpinnerRoulette spinnerRoulette;
    private boolean active;
    private boolean isShuttingDown;

    public boolean isActive() {
        return active;
    }

    public Spinner(GVRContext gvrContext, int initialCharacterPosition, int mode) {
        super(gvrContext);

        spinnerRoulette = new SpinnerRoulette(gvrContext, initialCharacterPosition, mode);
        spinnerSkeleton = new SpinnerSkeleton(gvrContext);

        addChildObject(spinnerRoulette);
        addChildObject(spinnerSkeleton);
    }

    public SpinnerSkeleton getSpinnerSkeleton() {
        return spinnerSkeleton;
    }

    public SpinnerRoulette getSpinnerRoulette() {
        return spinnerRoulette;
    }

    public synchronized void off() {
        spinnerRoulette.setSoudOn(false);
        isShuttingDown = true;
        spinnerRoulette.stopAnimations();

        for (int i = 0; i < spinnerRoulette.getSpinnerAdapter().getSpinnerItems().size(); i++) {
            new GVROpacityAnimation(spinnerRoulette.getSpinnerAdapter().getSpinnerItems().get(i),
                    ANIMATION_TIME, 0).start(
                    this.getGVRContext().getAnimationEngine());

        }

        new GVROpacityAnimation(spinnerSkeleton.getSpinnerBox(), ANIMATION_TIME, 0).start(this
                .getGVRContext().getAnimationEngine());

        new GVROpacityAnimation(spinnerSkeleton.getSpinnerShadow(), ANIMATION_TIME, 0).start(
                this.getGVRContext().getAnimationEngine()).setOnFinish(
                new GVROnFinish() {

                    @Override
                    public void finished(GVRAnimation arg0) {
                        active = false;
                        spinnerRoulette.cleanRotation();
                        isShuttingDown = false;
                    }
                });

    }

    public boolean isShuttingDown() {
        return isShuttingDown;
    }

    public void setShuttingDown(boolean isShuttingDown) {
        this.isShuttingDown = isShuttingDown;
    }

    public synchronized void on(int initialCharacterPosition, int mode, int position) {
        spinnerRoulette.setSoudOn(true);
        spinnerRoulette.setInitialCharacterPosition(initialCharacterPosition);
        spinnerRoulette.setPosition(position);
        spinnerRoulette.getSpinnerAdapter().setCharacterList(
                CharList.getInstance(getGVRContext()).getListCircular(mode));

        spinnerRoulette.setDefaultCharactersInSpinner();

        new GVROpacityAnimation(spinnerSkeleton.getSpinnerBox(), ANIMATION_TIME, 1).start(this
                .getGVRContext().getAnimationEngine());

        new GVROpacityAnimation(spinnerSkeleton.getSpinnerShadow(), ANIMATION_TIME, 1).start(this
                .getGVRContext().getAnimationEngine());

        for (int i = 0; i < spinnerRoulette.getSpinnerAdapter().getSpinnerItems().size(); i++) {
            new GVROpacityAnimation(spinnerRoulette.getSpinnerAdapter().getSpinnerItems().get(i),
                    ANIMATION_TIME, 1).start(
                    this.getGVRContext().getAnimationEngine());
            active = true;
        }
    }

    public boolean isHitArea() {

        GVREyePointeeHolder[] eyePointeeHolders = GVRPicker.pickScene(this.getGVRContext()
                .getMainScene());

        if (eyePointeeHolders.length > 0) {

            for (GVREyePointeeHolder eyePointeeHolder : eyePointeeHolders) {

                if (eyePointeeHolder.hashCode() == spinnerSkeleton.getSpinnerBox()
                        .getEyePointeeHolder().hashCode()) {

                    return false;
                }
            }
        }

        return true;
    }

    public void move(TextFieldItem currentChar) {

        float x = currentChar.getTransform().getPositionX();
        float y = currentChar.getTransform().getPositionY();
        float z = currentChar.getTransform().getPositionZ() + Z_DISTANCE;

        getTransform().setPosition(x, y, z);

        lookAt(currentChar);

    }

    private void lookAt(GVRSceneObject currentChar) {
        GVRCameraRig camera = this.getGVRContext().getMainScene().getMainCameraRig();
        Vector3D vectorCamera = new Vector3D(camera.getTransform().getPositionX(), camera
                .getTransform().getPositionY(), camera.getTransform()
                .getPositionZ());
        Vector3D vectorKeyboard = new Vector3D(this.getParent().getParent().getTransform()
                .getPositionX(), this.getParent().getParent()
                .getTransform().getPositionY(), this.getParent().getParent().getTransform()
                .getPositionZ());

        float newX = currentChar.getTransform().getPositionX()
                + currentChar.getParent().getTransform().getPositionX();
        float newY = 0;
        float newZ = (float) Vector3D.distance(vectorKeyboard, vectorCamera);

        Log.d("lookatspinner", "newX " + newX);
        Log.d("lookatspinner", "newY " + newY);
        Log.d("lookatspinner", "newZ " + newZ);

        Vector3D emulatedSpinner = new Vector3D(newX, newY, newZ * -1);

        Vector3D emulateCam = new Vector3D(0, 0, 0);

        float angle = Util.getYRotationAngle(emulatedSpinner, emulateCam);

        Log.d("lookatspinner", "angle " + angle);

        // angle =(float) (angle*1.1);

        Log.d("lookatspinner", "angle new" + angle);

        getTransform().setRotationByAxis(angle, 0, 1, 0);

    }

}
