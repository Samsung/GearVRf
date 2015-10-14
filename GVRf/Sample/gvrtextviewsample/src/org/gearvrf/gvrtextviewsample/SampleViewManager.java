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

import java.util.ArrayList;
import java.util.Random;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScript;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;

import android.graphics.Color;
import android.widget.LinearLayout;

public class SampleViewManager extends GVRScript {

    GVRTextViewSceneObject sceneObject;
    SampleActivity mActivity;
    private final String[] strings = new String[] { "good", "verygood",
            "veryverygood", "veryverygood", "veryveryverygood" };
    private final int[] colors = new int[] { Color.RED, Color.YELLOW,
            Color.GREEN, Color.WHITE, Color.MAGENTA };
    private final IntervalFrequency[] frequencies = new IntervalFrequency[] {
            IntervalFrequency.HIGH, IntervalFrequency.MEDIUM,
            IntervalFrequency.LOW };
    private float textSize;
    private int counter = 0;
    Random random = new Random();

    ArrayList<GVRTextViewSceneObject> textviews = new ArrayList<GVRTextViewSceneObject>();

    SampleViewManager(SampleActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        for (int i = 0; i < 5; i++) {
            sceneObject = new GVRTextViewSceneObject(gvrContext, mActivity);
            textSize = sceneObject.getTextSize();

            // set the scene object position
            float x = i * 2.0f;// i * 2.0f - 4.0f;
            sceneObject.getTransform().setPosition(x - 4.0f, 0.0f, -2.0f);
            sceneObject.setText(strings[i]);
            sceneObject.setTextColor(colors[i]);
            sceneObject.setTextSize(textSize * (i + 1) / 2);
            sceneObject.setRefreshFrequency(frequencies[i % 3]);
            // add the scene object to the scene graph
            gvrContext.getNextMainScene().addSceneObject(sceneObject);
            sceneObject.getTransform().setPositionZ(-3.0f);
            textviews.add(sceneObject);
        }
    }

    @Override
    public void onStep() {
        counter++;
        if(counter % 10 == 0){
            int viewIndex = random.nextInt(5);
            sceneObject = textviews.get(viewIndex);
            int colorIndex = random.nextInt(5);
            sceneObject.setTextColor(colors[colorIndex]);
        }
    }

}