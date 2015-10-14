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

package org.gearvrf.immersivepedia.focus;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.immersivepedia.GazeController;
import org.gearvrf.immersivepedia.input.TouchPadInput;
import org.gearvrf.immersivepedia.util.VRTouchPadGestureDetector.SwipeDirection;

public class FocusableSceneObject extends GVRSceneObject {

    private boolean focus = false;
    public FocusListener focusListener = null;
    public String tag = null;
    public boolean showInteractiveCursor = true;
    private OnClickListener onClickListener;
    private OnGestureListener onGestureListener;
    private int focusCount = 0;

    public FocusableSceneObject(GVRContext gvrContext) {
        super(gvrContext);
        FocusableController.interactiveObjects.add(this);
    }

    public FocusableSceneObject(GVRContext gvrContext, GVRMesh gvrMesh, GVRTexture gvrTexture) {
        super(gvrContext, gvrMesh, gvrTexture);
        FocusableController.interactiveObjects.add(this);
    }

    public FocusableSceneObject(GVRContext gvrContext, float width, float height, GVRTexture t) {
        super(gvrContext, width, height, t);
        FocusableController.interactiveObjects.add(this);
    }

    public void dispatchGainedFocus() {
        if (this.focusListener != null) {
            this.focusListener.gainedFocus(this);
        }
        if (showInteractiveCursor) {
            GazeController.enableInteractiveCursor();
        }
    }

    public void dispatchLostFocus() {
        if (this.focusListener != null) {
            focusListener.lostFocus(this);
            focusCount = 0;
        }
        if (showInteractiveCursor) {
            GazeController.disableInteractiveCursor();
        }
    }

    public void setFocus(boolean state) {
        if (state == true && focus == false && focusCount > 1) {
            focus = true;
            this.dispatchGainedFocus();
            return;
        }

        if (state == false && focus == true) {
            focus = false;
            this.dispatchLostFocus();
            return;
        }
    }

    public void dispatchInFocus() {
        if (this.focusListener != null) {
            if (focusCount > 1)
                this.focusListener.inFocus(this);
            if (focusCount <= 2)
                focusCount++;
        }
        if (showInteractiveCursor) {
            GazeController.enableInteractiveCursor();
        }
    }

    public void dispatchInClick() {
        if (this.onClickListener != null)
            this.onClickListener.onClick();
    }

    public boolean hasFocus() {
        return focus;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnGestureListener(OnGestureListener onGestureListener) {
        this.onGestureListener = onGestureListener;
    }

    public boolean isFocus() {
        return focus;
    }

    public void dispatchInGesture(SwipeDirection swipeDirection) {
        if (this.onGestureListener != null) {

            if (TouchPadInput.getCurrent().swipeDirection == SwipeDirection.Ignore)
                onGestureListener.onSwipeIgnore();
            else if (TouchPadInput.getCurrent().swipeDirection == SwipeDirection.Forward)
                onGestureListener.onSwipeForward();
            else if (TouchPadInput.getCurrent().swipeDirection == SwipeDirection.Backward)
                onGestureListener.onSwipeBack();
            else if (TouchPadInput.getCurrent().swipeDirection == SwipeDirection.Up)
                onGestureListener.onSwipeUp();
            else
                onGestureListener.onSwipeDown();

        }
    }
}
