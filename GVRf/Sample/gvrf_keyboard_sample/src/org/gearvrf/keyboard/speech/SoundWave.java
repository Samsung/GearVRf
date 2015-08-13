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

package org.gearvrf.keyboard.speech;

import android.graphics.Color;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.mic.RecognitionRmsChangeListener;
import org.gearvrf.keyboard.util.Constants;
import org.gearvrf.keyboard.util.RenderingOrder;
import org.gearvrf.keyboard.util.SceneObjectNames;
import org.gearvrf.utility.Log;

import java.util.ArrayList;

public class SoundWave extends GVRSceneObject implements RecognitionRmsChangeListener {

    private GVRSceneObject[] boxes;
    private ArrayList<GVRSceneObject> columns;
    private int currentBox = 0, currentColumn = 0;
    private float minRange, maxRange;
    private boolean canAnimate = false;

    public SoundWave(GVRContext gvrContext, int width, float minRange, float maxRange) {

        super(gvrContext);
        setName(SceneObjectNames.SOUND_WAVE);
        boxes = new GVRSceneObject[width * 5];
        columns = new ArrayList<GVRSceneObject>();
        this.minRange = minRange;
        this.maxRange = maxRange;

        for (int i = 0; i < width; i++)
            createColumn(gvrContext);

        currentColumn = 0;
        hide();
    }

    private void createColumn(GVRContext context) {

        GVRSceneObject column = new GVRSceneObject(context);
        column.addChildObject(createBox(context, Color.argb(1, 230, 72, 50)));
        column.addChildObject(createBox(context, Color.argb(1, 209, 60, 43)));
        column.addChildObject(createBox(context, Color.argb(1, 186, 47, 35)));
        column.addChildObject(createBox(context, Color.argb(1, 163, 34, 27)));
        column.addChildObject(createBox(context, Color.argb(1, 142, 22, 20)));
        columns.add(column);
        this.addChildObject(column);
        currentColumn++;
        currentBox = 0;
    }

    public void update(float newSize, float newPositionX) {

        float waveSize = newSize - 0.23f;
        getTransform().setPositionX(newPositionX);
        getTransform().setPositionY(0.1f);
        getTransform().setPositionZ(Constants.CAMERA_DISTANCE);
        getTransform().setScale(waveSize, 0.5f, 0.5f);
    }

    private GVRSceneObject createBox(GVRContext context, int color) {

        GVRSceneObject box1 = new GVRSceneObject(
                context,
                0.1f,
                0.1f,
                context.loadTexture(new GVRAndroidResource(context, R.drawable.soundwave_wave_block)));
        box1.getRenderData().getMaterial().setColor(color);
        box1.getRenderData().getMaterial().setOpacity(1);
        box1.getRenderData().setRenderingOrder(RenderingOrder.KEYBOARD_SOUND_WAVE);
        box1.getTransform().setPosition(0 - 0.11f * currentColumn, 0 - 0.11f * currentBox, 0);
        boxes[currentBox] = box1;
        currentBox++;
        context.getMainScene().addSceneObject(box1);
        return box1;

    }

    private void setColumn(float amplitude) {

        float valueBlock = (maxRange - minRange) / 5;
        GVRSceneObject currentColumn = columns.get(0);

        for (int i = 0; i < columns.size(); i++) {

            columns.get(i).getTransform().setPositionX(
                    columns.get(i).getTransform().getPositionX() + 0.11f);

        }
        columns.get(0).getTransform().setPositionX(
                columns.get(0).getTransform().getPositionX() - (columns.size()) * 0.11f);

        for (int i = 0; i < columns.get(0).getChildrenCount(); i++) {
            if (valueBlock * (1 + i) <= amplitude || i == 0)
                columns.get(0).getChildByIndex(4 - i).getRenderData().getMaterial().setOpacity(1);
            else
                columns.get(0).getChildByIndex(4 - i).getRenderData().getMaterial().setOpacity(0);

        }
        columns.remove(0);
        columns.add(currentColumn);

    }

    public void enableAnimation() {

        canAnimate = true;

    }

    private void hide() {

        canAnimate = false;
        for (int i = 0; i < columns.size(); i++) {

            for (int o = 0; o < columns.get(i).getChildrenCount(); o++) {
                columns.get(i).getChildByIndex(o).getRenderData().getMaterial().setOpacity(0);
            }
        }
    }

    @Override
    public void onRmsChanged(float rmsdB) {

        if (!canAnimate)
            return;
        setColumn(rmsdB);
        Log.e(null, "" + rmsdB);
    }

    @Override
    public void onRmsEnd() {

        hide();
        Log.e(null, "END");
    }
}
