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

package org.gearvrf.scene_objects;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.ITouchEvents;
import org.gearvrf.IViewEvents;
import org.gearvrf.utility.Log;

/**
 * This class represents a {@linkplain GVRSceneObject Scene object} that shows a {@link View}
 * into the scene with an arbitrarily complex geometry.
 */
public class GVRViewSceneObject extends GVRSceneObject {
    private View mView;
    private RootViewGroup mRootViewGroup;
    private IViewEvents mEventsListener;

    /**
     * Constructs a scene object that inflate a view from an XML resource. The scene object
     * notifies the {@linkplain IViewEvents listener} of the view to set its initial properties
     * after it has been inflated.
     * Internally the scene object uses the {@linkplain GVRContext context} to create
     * its own quad mesh based on the the width and height of the view:
     *     gvrContext.createQuad(getWidth() / Math.max(getWidth(), getHeight()),
     *                           getHeight() / Math.max(getWidth(), getHeight()));
     *
     * @param gvrContext current {@link GVRContext}.
     * @param viewId The resource ID to inflate. See {@link LayoutInflater}.
     * @param eventsListener Listener to be notified after the view has been inflated.
     */
    public GVRViewSceneObject(GVRContext gvrContext, int viewId, IViewEvents eventsListener) {
        this(gvrContext, null, null);

        mEventsListener = eventsListener;

        inflateView(viewId);
    }

    /**
     * Shows {@link View} in a 2D, rectangular {@linkplain GVRViewSceneObject scene object.}
     *
     * @param gvrContext current {@link GVRContext}.
     * @param view The {@link View} to be shown.
     */
    public GVRViewSceneObject(GVRContext gvrContext, View view) {
        this(gvrContext, view, null);
    }

    /**
     * Shows {@link View} in a 2D, rectangular {@linkplain GVRViewSceneObject scene object.}
     *
     * @param gvrContext current {@link GVRContext}
     * @param view The {@link View} to be shown.
     * @param width the rectangle's width
     * @param height the rectangle's height
     */
    public GVRViewSceneObject(GVRContext gvrContext, View view, float width, float height) {
        this(gvrContext, view, gvrContext.createQuad(width, height));
    }

    /**
     * Shows any {@link View} into the {@linkplain GVRViewSceneObject scene object} with an
     * arbitrarily complex geometry.
     * 
     * @param gvrContext current {@link GVRContext}
     * @param view The {@link View} to be shown.
     * @param mesh a {@link GVRMesh} - see
     *            {@link GVRContext#getAssetLoader()#loadMesh(org.gearvrf.GVRAndroidResource)} and
     *            {@link GVRContext#createQuad(float, float)}
     */
    public GVRViewSceneObject(final GVRContext gvrContext, final View view, final GVRMesh mesh) {
        super(gvrContext, mesh);

        mView = view;
        mEventsListener = null;

        if (view != null) {
            getGVRContext().getActivity().runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            startRenderingView();
                        }
                    });
        }
    }

    private void inflateView(final int viewId) {
        final  GVRActivity activity = getGVRContext().getActivity();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mView = View.inflate(activity, viewId, null);

                startRenderingView();
            }
        });
    }

    public View getView() {
        return mView;
    }

    public void invalidate() {
        if (mRootViewGroup != null)
            mRootViewGroup.postInvalidate();
    }

    public View findFocus() {
        if (mRootViewGroup != null) {
            return mRootViewGroup.findFocus();
        }

        return null;
    }

    public void setFocusedView(int id) {
        if (mRootViewGroup != null) {
            mRootViewGroup.setCurrentFocusedView( mRootViewGroup.findViewById(id));
        }
    }

    public void setFocusedView(View view) {
        if (mRootViewGroup != null) {
            mRootViewGroup.setCurrentFocusedView(view);
        }
    }

    public View findViewById(int id) {
        if (mRootViewGroup != null) {
            return mRootViewGroup.findViewById(id);
        }

        return null;
    }

    public View findViewWithTag(Object tag) {
        if (mRootViewGroup != null) {
            return mRootViewGroup.findViewWithTag(tag);
        }

        return null;
    }

    private void startRenderingView() {
        mRootViewGroup = new RootViewGroup(getGVRContext().getActivity(), this);

        if (mView.getParent() != null) {
            // To keep compatibility with GVRView
            ((ViewGroup)mView.getParent()).removeView(mView);
        }

        mRootViewGroup.addView(mView);
        mRootViewGroup.startRendering();

        getEventReceiver().addListener(mRootViewGroup);
    }

    /**
     * To set initial properties before attach the view to the View three;
     *
     * @param sceneObject This scene object
     * @param view The view to be initialized;
     */
    private void onInitView(GVRViewSceneObject sceneObject, View view) {
        if (mEventsListener != null) {
            mEventsListener.onInitView(sceneObject, mView);
        }
    }

    private void onStartDraw(GVRViewSceneObject sceneObject, View view) {
        if (mEventsListener != null) {
            mEventsListener.onStartDraw(sceneObject, mView);
        }
    }

    private void onTouchView(GVRViewSceneObject sceneObject, View view) {
        if (mEventsListener != null) {
            mEventsListener.onTouchView(sceneObject, view);
        }
    }

    /**
     * Internal class to draw the Android view into canvas at UI thread and
     * update the GL texture of the scene object at GL thread.
     *
     * This is the root view to overwrite the default canvas of the view by the
     * canvas of the texture attached to the scene object.
     */
    private static class RootViewGroup extends FrameLayout implements ITouchEvents {
        final GVRContext mGVRContext;
        final GVRViewSceneObject mSceneObject;
        Surface mSurface;
        SurfaceTexture mSurfaceTexture;
        float mViewSize;
        float mQuadWidth;
        float mQuadHeight;
        float mActionDownX;
        float mActionDownY;
        float mHitLocationX;
        float mHitLocationY;
        View mFocusedView;

        public RootViewGroup(GVRActivity gvrActivity, GVRViewSceneObject sceneObject) {
            super(gvrActivity);

            setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));

            mGVRContext = gvrActivity.getGVRContext();
            mSceneObject = sceneObject;
            // Default values to quad size. See onLayoutReady()
            mViewSize = 0.0f;
            mQuadWidth = 1.0f;
            mQuadHeight = 1.0f;
            mActionDownX = 0.0f;
            mActionDownY = 0.0f;
            mHitLocationX = 0.0f;
            mHitLocationY = 0.0f;

            mFocusedView = null;

            // To optimization
            setWillNotDraw(true);

            // To block Android's popups
            // setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        }

        @Override
        public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
            // To fix the issue of not redrawing the children after its invalidation.
            // FIXME: Improve this fix.
            postInvalidate();

            return super.invalidateChildInParent(location, dirty);
        }

        public boolean dispatchPickerInputEvent(MotionEvent ev, float[] hitLocation) {
            return super.dispatchTouchEvent(ev);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            // Doesn't handle default touch from screen.
            return false;
        }

        @Override
        public View findFocus() {
            if (mFocusedView == null)
                mFocusedView = super.findFocus();
            return mFocusedView;
        }

        public void setCurrentFocusedView(View view) {
            mFocusedView = view;
        }

        @Override
        // Android UI thread
        protected void dispatchDraw(Canvas canvas) {
            // Canvas attached to GVRViewSceneObject to draw on
            Canvas attachedCanvas = mSurface.lockCanvas(null);
            // Clear the canvas
            attachedCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            // draw the view to provided canvas
            super.dispatchDraw(attachedCanvas);

            mSurface.unlockCanvasAndPost(attachedCanvas);
        }

        // UI Thread
        public void startRendering() {

            mSceneObject.onInitView(mSceneObject, this);

            // To just set the layout's dimensions but don't call draw(...) after it
            setVisibility(INVISIBLE);

            /**
             * To be notified when when the layout gets ready.
             * So after that create render data and material
             * to the scene object.
             */
            getViewTreeObserver().addOnGlobalLayoutListener (
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            RootViewGroup.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            onLayoutReady();
                        }
                    });

            mGVRContext.getActivity().registerView(RootViewGroup.this);
        }

        private void onLayoutReady() {
            /**
             * Creates render data and material to its scene object on GL Thread.
             */
            mGVRContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    createRenderData();

                    onRenderDataReady();
                }
            });
        }

        private void onRenderDataReady() {
            mViewSize = Math.max(getWidth(), getHeight());
            mQuadWidth = getWidth() / mViewSize;
            mQuadHeight = getHeight() / mViewSize;

            /**
             * Makes the view visible to call draw(...) and start rendering it.
             */
            mGVRContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // To call draw(...) after renderData has been created
                    setVisibility(VISIBLE);

                    onViewVisible();
                }
            });

            mSceneObject.onStartDraw(mSceneObject, this);
        }

        private void onViewVisible() {
            /**
             * To adjust the default buffer size of the surface texture according to
             * changes of layout's size.
             */
            getViewTreeObserver().addOnGlobalLayoutListener (
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            onLayoutChanged();
                        }
                    });
        }

        private void onLayoutChanged() {
            mSurfaceTexture.setDefaultBufferSize(getWidth(), getHeight());
        }

        private void createRenderData() {
            final GVRTexture texture = new GVRExternalTexture(mGVRContext);
            final GVRMaterial material = new GVRMaterial(mGVRContext, GVRShaderType.OES.ID);
            final GVRCollider collider = new GVRMeshCollider(mGVRContext, true);

            if (mSceneObject.getRenderData() == null) {
                final GVRRenderData renderData = new GVRRenderData(mGVRContext);
                renderData.setMesh(mGVRContext.createQuad(mQuadWidth, mQuadHeight));
                mSceneObject.attachComponent(renderData);
            }

            material.setMainTexture(texture);
            mSceneObject.getRenderData().setMaterial(material);
            mSceneObject.attachComponent(collider);

            mSurfaceTexture = new SurfaceTexture(texture.getId());
            mSurface = new Surface(mSurfaceTexture);

            mSurfaceTexture.setDefaultBufferSize(getWidth(), getHeight());

            mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                GVRDrawFrameListener drawFrameListener = new GVRDrawFrameListener() {
                    @Override
                    public void onDrawFrame(float frameTime) {
                        mSurfaceTexture.updateTexImage();
                        mGVRContext.unregisterDrawFrameListener(this);
                    }
                };

                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    mGVRContext.registerDrawFrameListener(drawFrameListener);
                }
            });

        }

        @Override
        public void onTouch(GVRSceneObject sceneObject, MotionEvent event, float[] hitLocation) {
            final int action = event.getAction();
            float x = event.getX();
            float y = event.getY();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mActionDownX = x;
                    mActionDownY = y;

                    if (hitLocation != null) {
                        mHitLocationX = (hitLocation[0] + 0.5f) * getWidth();
                        mHitLocationY = (-hitLocation[1] + 0.5f) * getHeight();
                    } else {
                        mHitLocationX = getWidth() * 0.5f;
                        mHitLocationY = getHeight() * 0.5f;
                    }

                    x = mHitLocationX;
                    y = mHitLocationY;
                    break;
                case MotionEvent.ACTION_UP:
                default:
                    x = x - mActionDownX + mHitLocationX;
                    y = y - mActionDownY + mHitLocationY;
                    break;
            }

            event.setLocation(x, y);

            dispatchPickerInputEvent(event, hitLocation);
        }
    }
}
