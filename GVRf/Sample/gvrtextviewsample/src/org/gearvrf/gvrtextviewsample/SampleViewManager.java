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

package org.gearvrf.gvrtextviewsample;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScript;
import org.gearvrf.debug.GVRConsole;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import android.graphics.Color;
import android.util.Log;
import android.widget.LinearLayout;

public class SampleViewManager extends GVRScript {

    GVRTextViewSceneObject sceneObject;
    LinearLayout mTextView;
    GVRConsole console;
    SampleActivity mActivity;
    boolean init = false;
    private final String[] strings = new String[] { "good", "verygood",
            "veryverygood", "veryverygood", "veryveryverygood" };
    private final int[] colors = new int[] { Color.BLUE, Color.BLACK,
            Color.GREEN, Color.WHITE, Color.MAGENTA };
    private float textSize = 0.0f;
    private int counter = 0;

    SampleViewManager(SampleActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        sceneObject = new GVRTextViewSceneObject(gvrContext, mActivity);
        textSize = sceneObject.getTextSize();

        // set the scene object position
        sceneObject.getTransform().setPosition(0.0f, 0.0f, -2.0f);

        // add the scene object to the scene graph
        gvrContext.getNextMainScene().getMainCameraRig().getOwnerObject()
                .addChildObject(sceneObject);

    }

    @Override
    public void onStep() {
        counter++;
        if (counter % 50 == 0) {
            int currentState = (counter / 50) % 5;
            Log.d("change", "change to state " + currentState);
            sceneObject.setText(strings[currentState]);
            sceneObject.setTextColor(colors[currentState]);
            sceneObject.setTextSize(textSize * currentState * 0.5f);
            Log.d("change", "change to state end " + currentState);
        }
    }

}
