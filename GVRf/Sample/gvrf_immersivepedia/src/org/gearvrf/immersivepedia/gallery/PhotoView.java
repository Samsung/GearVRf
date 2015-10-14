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

package org.gearvrf.immersivepedia.gallery;

import android.graphics.Color;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRPositionAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.immersivepedia.focus.FocusListener;
import org.gearvrf.immersivepedia.focus.FocusableSceneObject;
import org.gearvrf.immersivepedia.input.TouchPadInput;
import org.gearvrf.immersivepedia.util.AudioClip;
import org.gearvrf.immersivepedia.util.RenderingOrderApplication;
import org.gearvrf.immersivepedia.util.VRTouchPadGestureDetector.SwipeDirection;

public class PhotoView extends FocusableSceneObject implements FocusListener {

    private GVRContext gvrContext = null;

    public static final int PHOTO_VIEW_OPENED = 0x01;
    public static final int PHOTO_VIEW_CLOSED = 0x02;
    public static final int PHOTO_VIEW_LOCK = 0x03;
    public static final int PHOTO_VIEW_HIDE = 0x04;
    public static final int PHOTO_VIEW_ANIMATING = 0x05;

    public static final int PHOTO_VIEW_ANIMATION_LEFT_TO_RIGHT = 0x01;
    public static final int PHOTO_VIEW_ANIMATION_RIGHT_TO_LEFT = 0;

    private static final float PHOTO_VIEW_OPACITY_HIDE = 0f;
    private static final float PHOTO_VIEW_OPACITY_VISIBLE = 1f;

    private static final float PHOTO_VIEW_OPEN_ANIMATION_TIME = 0.5f;
    private static final float PHOTO_VIEW_CLOSE_ANIMATION_TIME = 0.4f;

    private static final float PHOTO_VIEW_SCROLL_ANIMATION_TIME = 0.4f;
    private static final float PHOTO_VIEW_FADE_ANIMATION_TIME = 0.4f;

    protected int currentState = PHOTO_VIEW_CLOSED;

    // Our images have 2560 × 1440 pixels
    protected static final float SIZE_FACTOR = 0.001f;
    protected static final float WIDTH = 2560.0f * SIZE_FACTOR;
    protected static final float HEIGHT = 1440.0f * SIZE_FACTOR;

    protected static final float PHOTO_VIEW_MAX_Y = Gallery.PHOTO_VIEW_DISTANCE_BETWEEN_PHOTOS
            - (HEIGHT / 2);
    protected static final float PHOTO_VIEW_MIN_Y = -Gallery.PHOTO_VIEW_DISTANCE_BETWEEN_PHOTOS
            - (HEIGHT / 2);

    private final float PHOTO_VIEW_FOCUS_SCALE = 2.75f;

    protected PhotoGridItem gridItem = null;
    protected PhotoGridItem centeredGridItem = null;
    protected PhotoGridItem leftGridItem = null;
    protected PhotoGridItem rightGridItem = null;
    protected PhotoEventListener PhotoItemListener = null;

    private int photoId = 0;

    protected void slideUp(boolean animated) {
        this.gridItem.y = this.gridItem.y + 2 * Gallery.PHOTO_VIEW_DISTANCE_BETWEEN_PHOTOS;
        slideToGridItem(animated);
    }

    protected void slideDown(boolean animated) {
        this.gridItem.y = this.gridItem.y - 2 * Gallery.PHOTO_VIEW_DISTANCE_BETWEEN_PHOTOS;
        slideToGridItem(animated);
    }

    protected void applyScrollPage(int page) {
        this.gridItem.y = this.gridItem.y - (Gallery.PHOTO_VIEW_DISTANCE_BETWEEN_PHOTOS) * page;
        this.getTransform().setPosition(this.gridItem.x, this.gridItem.y, this.gridItem.z);
    }

    protected void slideToGridItem(boolean animated) {

        if (this.gridItem.y < PHOTO_VIEW_MIN_Y || this.gridItem.y > PHOTO_VIEW_MAX_Y) {
            fadeIn();
        } else {
            fadeOut();
        }

        if (animated) {

            new GVRPositionAnimation(this, PHOTO_VIEW_SCROLL_ANIMATION_TIME, this.gridItem.x,
                    this.gridItem.y, this.gridItem.z).start(this.gvrContext.getAnimationEngine());

        } else {
            this.getTransform().setPosition(this.gridItem.x, this.gridItem.y, this.gridItem.z);
        }
    }

    public static PhotoView createPhotoView(GVRContext gvrContext, int resourceId) {
        return new PhotoView(gvrContext, resourceId);
    }

    public PhotoView(GVRContext gvrContext, int photo) {
        this(gvrContext, WIDTH, HEIGHT, gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                photo)));
        this.gvrContext = gvrContext;
        this.photoId = photo;

        attachEyePointeeHolder();
        this.focusListener = this;

    }

    public PhotoView(GVRContext gvrContext, float f, float g, GVRTexture t) {
        super(gvrContext, f, g, t);

    }

    private void hover() {
        AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIMenuHoverSoundID(), 1.0f, 1.0f);
        this.getRenderData().getMaterial().setColor(Color.WHITE);
    }

    private void normal() {
        this.getRenderData().getMaterial().setColor(Color.GRAY);
    }

    private void openAction() {
        AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIImageOpenSoundID(), 1.0f, 1.0f);
        currentState = PHOTO_VIEW_OPENED;
        this.getTransform().setScale(1.0f, 1.0f, 1.0f);
        this.getRenderData().getMaterial().setColor(Color.WHITE);

        PhotoView.this.PhotoItemListener.dispatchLockRequest();

        new GVRScaleAnimation(this, PHOTO_VIEW_OPEN_ANIMATION_TIME, PHOTO_VIEW_FOCUS_SCALE).start(this.gvrContext
                .getAnimationEngine());

        new GVRPositionAnimation(this, PHOTO_VIEW_OPEN_ANIMATION_TIME, centeredGridItem.x,
                centeredGridItem.y,
                centeredGridItem.z)
                .setOnFinish(new GVROnFinish() {

                    @Override
                    public void finished(GVRAnimation arg0) {
                        if (PhotoView.this.PhotoItemListener != null) {
                            PhotoView.this.PhotoItemListener.dispatchUnlockRequest();
                            PhotoView.this.PhotoItemListener
                                    .itemSelectedAnimationFinished(PhotoView.this);
                        }

                    }
                }).start(this.gvrContext.getAnimationEngine());

    }

    protected void openActionWithoutAnimation() {
        AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIImageOpenSoundID(), 1.0f, 1.0f);
        currentState = PHOTO_VIEW_OPENED;
        this.getRenderData().getMaterial().setOpacity(PHOTO_VIEW_OPACITY_VISIBLE);
        this.getTransform().setScale(PHOTO_VIEW_FOCUS_SCALE, PHOTO_VIEW_FOCUS_SCALE, PHOTO_VIEW_FOCUS_SCALE);
        this.getRenderData().getMaterial().setColor(Color.WHITE);
        this.getTransform().setPosition(this.centeredGridItem.x, this.centeredGridItem.y,
                this.centeredGridItem.z);

    }

    protected void openActionWithAnimation(int animationId) {
        AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIImageOpenSoundID(), 1.0f, 1.0f);
        currentState = PHOTO_VIEW_ANIMATING;
        this.getRenderData().getMaterial().setOpacity(PHOTO_VIEW_OPACITY_HIDE);

        this.getTransform().setScale(PHOTO_VIEW_FOCUS_SCALE, PHOTO_VIEW_FOCUS_SCALE, PHOTO_VIEW_FOCUS_SCALE);
        this.getRenderData().getMaterial().setColor(Color.WHITE);

        float x = leftGridItem.x;
        float y = leftGridItem.y;
        float z = leftGridItem.z;

        float offsetZ = 0;

        if (animationId == PHOTO_VIEW_ANIMATION_LEFT_TO_RIGHT) {
            x = rightGridItem.x;
            y = rightGridItem.y;
            z = rightGridItem.z;
        }

        this.getTransform().setPosition(x, y, z);

        new GVRPositionAnimation(this, PHOTO_VIEW_FADE_ANIMATION_TIME, this.centeredGridItem.x,
                this.centeredGridItem.y, this.centeredGridItem.z).setOnFinish(new GVROnFinish() {

            @Override
            public void finished(GVRAnimation arg0) {
                PhotoView.this.currentState = PHOTO_VIEW_OPENED;

            }
        }).start(this.gvrContext.getAnimationEngine());

        new GVROpacityAnimation(this, PHOTO_VIEW_FADE_ANIMATION_TIME, PHOTO_VIEW_OPACITY_VISIBLE)
                .start(this.gvrContext
                        .getAnimationEngine());

    }

    protected void changeTexture(int resource) {
        if (this.photoId == resource)
            return;
        this.getRenderData().getMaterial()
                .setMainTexture(gvrContext.loadTexture(new GVRAndroidResource(this.gvrContext,
                        resource)));
        this.photoId = resource;
    }

    private void closeActionWithoutAnimation() {
        AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIImageCloseSoundID(), 1.0f, 1.0f);
        currentState = PHOTO_VIEW_CLOSED;
        this.getTransform().setScale(1.0f, 1.0f, 1.0f);
        this.getTransform().setPosition(gridItem.x, gridItem.y, gridItem.z);
        this.applyOpacityConstrants();
    }

    private void closeActionWithFadeAnimation(int animationId) {
        AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIImageCloseSoundID(), 1.0f, 1.0f);
        currentState = PHOTO_VIEW_ANIMATING;

        float x = rightGridItem.x;
        float y = rightGridItem.y;
        float z = rightGridItem.z;

        float offsetZ = 0.2f;

        if (animationId == PHOTO_VIEW_ANIMATION_LEFT_TO_RIGHT) {
            x = leftGridItem.x;
            y = leftGridItem.y;
            z = leftGridItem.z;
        }

        this.getTransform().setPosition(this.centeredGridItem.x, this.centeredGridItem.y,
                this.centeredGridItem.z - offsetZ);

        new GVRPositionAnimation(this, PHOTO_VIEW_FADE_ANIMATION_TIME, x, y, z - offsetZ)
                .start(this.gvrContext.getAnimationEngine());

        new GVROpacityAnimation(this, PHOTO_VIEW_FADE_ANIMATION_TIME, PHOTO_VIEW_OPACITY_HIDE)
                .setOnFinish(new GVROnFinish() {

                    @Override
                    public void finished(GVRAnimation arg0) {
                        PhotoView.this.getTransform().setScale(1.0f, 1.0f, 1.0f);
                        PhotoView.this.getTransform().setPosition(gridItem.x, gridItem.y,
                                gridItem.z);
                        PhotoView.this.applyOpacityConstrants();
                        PhotoView.this.currentState = PHOTO_VIEW_CLOSED;

                    }
                }).start(this.gvrContext.getAnimationEngine());

    }

    public void closeAction() {
        AudioClip.getInstance(getGVRContext().getContext()).playSound(AudioClip.getUIImageCloseSoundID(), 1.0f, 1.0f);
        currentState = PHOTO_VIEW_CLOSED;

        if (this.PhotoItemListener != null) {
            this.PhotoItemListener.dispatchLockRequest();
        }

        new GVRScaleAnimation(this, PHOTO_VIEW_OPEN_ANIMATION_TIME, 1.0f).start(this.gvrContext
                .getAnimationEngine());

        new GVRPositionAnimation(this, PHOTO_VIEW_CLOSE_ANIMATION_TIME, gridItem.x, gridItem.y,
                gridItem.z)
                .setOnFinish(new GVROnFinish() {

                    @Override
                    public void finished(GVRAnimation arg0) {
                        PhotoView.this.applyOpacityConstrants();
                        if (PhotoView.this.PhotoItemListener != null) {
                            PhotoView.this.PhotoItemListener.dispatchUnlockRequest();
                            PhotoView.this.PhotoItemListener
                                    .itemUnselectedAnimationFinished(PhotoView.this);
                        }

                    }
                }).start(this.gvrContext.getAnimationEngine());

    }

    @Override
    public void gainedFocus(FocusableSceneObject object) {

        if ((currentState == PHOTO_VIEW_CLOSED) && (this.PhotoItemListener != null)
                && (this.PhotoItemListener.shouldHoverItem(this) == true))
            this.hover();
    }

    @Override
    public void lostFocus(FocusableSceneObject object) {
        if ((currentState == PHOTO_VIEW_CLOSED) && (this.PhotoItemListener != null)
                && (this.PhotoItemListener.shouldHoverItem(this) == true))
            this.normal();
    }

    @Override
    public void inFocus(FocusableSceneObject object) {

        if (this.PhotoItemListener != null) {
            if (this.PhotoItemListener.isLocked())
                return;
        }

        if (TouchPadInput.getCurrent().buttonState.isUp()
                && (TouchPadInput.getCurrent().swipeDirection == SwipeDirection.Ignore)
                && (this.currentState == PHOTO_VIEW_CLOSED && this.getRenderData().getMaterial()
                        .getOpacity() > (PHOTO_VIEW_OPACITY_VISIBLE / 2.0f))) {
            if (this.PhotoItemListener != null
                    && this.PhotoItemListener.shouldOpenItem(this) == true) {
                this.PhotoItemListener.itemSelected(this);
                this.openAction();
            }

        }
        else if (TouchPadInput.getCurrent().buttonState.isUp()
                && (TouchPadInput.getCurrent().swipeDirection == SwipeDirection.Ignore)
                && (this.currentState == PHOTO_VIEW_OPENED)) {
            if (this.PhotoItemListener != null
                    && this.PhotoItemListener.shouldCloseItem(this) == true) {
                this.PhotoItemListener.itemUnselected(this);
                this.closeAction();
            }
        }

        if (TouchPadInput.getCurrent().swipeDirection == SwipeDirection.Forward
                && (this.currentState == PHOTO_VIEW_OPENED)) {
            this.closeActionWithFadeAnimation(PHOTO_VIEW_ANIMATION_LEFT_TO_RIGHT);
            if (this.PhotoItemListener != null)
                this.PhotoItemListener.itemSwipedToRight(this);
        }
        if (TouchPadInput.getCurrent().swipeDirection == SwipeDirection.Backward
                && (this.currentState == PHOTO_VIEW_OPENED)) {
            this.closeActionWithFadeAnimation(PHOTO_VIEW_ANIMATION_RIGHT_TO_LEFT);
            if (this.PhotoItemListener != null)
                this.PhotoItemListener.itemSwipedToLeft(this);
        }

    }

    private void fadeIn() {
        this.showInteractiveCursor = false;
        this.currentState = PHOTO_VIEW_HIDE;
        new GVROpacityAnimation(this, PHOTO_VIEW_FADE_ANIMATION_TIME, PHOTO_VIEW_OPACITY_HIDE)
                .start(this.gvrContext
                        .getAnimationEngine());

    }

    private void fadeOut() {
        this.showInteractiveCursor = true;
        this.currentState = PHOTO_VIEW_CLOSED;
        new GVROpacityAnimation(this, PHOTO_VIEW_FADE_ANIMATION_TIME, PHOTO_VIEW_OPACITY_VISIBLE)
                .start(this.gvrContext
                        .getAnimationEngine());

    }

    protected void applyOpacityConstrants() {
        this.getRenderData().getMaterial().setColor(Color.GRAY);
        this.getRenderData().setRenderingOrder(RenderingOrderApplication.GALLERY_PHOTO);

        if (this.gridItem.y < PHOTO_VIEW_MIN_Y || this.gridItem.y > PHOTO_VIEW_MAX_Y) {
            this.getRenderData().getMaterial().setOpacity(PHOTO_VIEW_OPACITY_HIDE);
            this.showInteractiveCursor = false;
            this.gridItem.visible = false;
        } else {
            this.getRenderData().getMaterial().setOpacity(PHOTO_VIEW_OPACITY_VISIBLE);
            this.showInteractiveCursor = true;
            this.gridItem.visible = true;
        }
    }

    protected void disappear() {
        new GVROpacityAnimation(this, PHOTO_VIEW_FADE_ANIMATION_TIME,
                PHOTO_VIEW_OPACITY_HIDE).start(this.gvrContext.getAnimationEngine());
        this.gridItem.visible = false;
        this.showInteractiveCursor = false;
    }

    protected void appear() {
        this.getRenderData().getMaterial().setColor(Color.GRAY);

        if (this.gridItem.y < PHOTO_VIEW_MIN_Y || this.gridItem.y > PHOTO_VIEW_MAX_Y) {
            this.getRenderData().getMaterial().setOpacity(PHOTO_VIEW_OPACITY_HIDE);
            this.showInteractiveCursor = false;
            this.gridItem.visible = false;
        } else {

            new GVROpacityAnimation(this, PHOTO_VIEW_FADE_ANIMATION_TIME,
                    PHOTO_VIEW_OPACITY_VISIBLE).start(this.gvrContext.getAnimationEngine());

            this.showInteractiveCursor = true;
            this.gridItem.visible = true;
        }
    }

}
