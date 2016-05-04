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

package org.gearvrf.scene_objects.view;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRViewSceneObject;

import android.graphics.Canvas;
import android.view.View;
import android.webkit.WebView;

/**
 * This class represents a {@link WebView} that is rendered
 * into the attached {@link GVRViewSceneObject}
 * See {@link GVRView} and {@link GVRViewSceneObject}
 */
public class GVRWebView extends WebView implements GVRView {
    private GVRViewSceneObject mSceneObject = null;

    public GVRWebView(GVRActivity context) {
        super(context);

        context.registerView(this);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mSceneObject == null)
            return;

        // Canvas attached to GVRViewSceneObject to draw on
        Canvas attachedCanvas = mSceneObject.lockCanvas();
        // translate canvas to reflect view scrolling
        attachedCanvas.scale(attachedCanvas.getWidth() / (float) canvas.getWidth(),
                attachedCanvas.getHeight() / (float) canvas.getHeight());
        attachedCanvas.translate(-getScrollX(), -getScrollY());
        // draw the view to provided canvas
        super.draw(attachedCanvas);

        mSceneObject.unlockCanvasAndPost(attachedCanvas);
    }

    @Override
    public void setSceneObject(GVRViewSceneObject sceneObject) {
        mSceneObject = sceneObject;
    }

    @Override
    public View getView() {
        return this;
    }
}
