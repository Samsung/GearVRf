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

package org.gearvrf.keyboard.textField;

import android.graphics.Color;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.main.MainScript;
import org.gearvrf.keyboard.model.CharItem;
import org.gearvrf.keyboard.spinner.Spinner;
import org.gearvrf.keyboard.util.RenderingOrder;
import org.gearvrf.keyboard.util.SceneObjectNames;
import org.gearvrf.keyboard.util.Util;

import java.util.ArrayList;
import java.util.List;

public class TextField extends GVRSceneObject {

    private static final float SUB_LINE_Y = -0.2f;
    private static final float INTERACTIVE_TEXT_WIDTH = 118 / 2;
    private static final float TEXT_WIDTH = 0.279f;
    public static final int LAST_CHARACTER = -1;
    protected List<TextFieldItem> mListFieldItems = new ArrayList<TextFieldItem>();
    protected List<GVRSceneObject> mListFieldSubLines = new ArrayList<GVRSceneObject>();

    protected TextFieldItem currentCharSelected;
    private int maxNumberOfCharecters;
    protected int currentCharPosition;
    private Spinner spinner;

    public TextField(GVRContext gvrContext, MainScript mainScript) {
        super(gvrContext);
        setName(SceneObjectNames.TEXT_FIELD);

        mListFieldItems = new ArrayList<TextFieldItem>();
    }

    public String getCurrentText() {

        String result = "";
        for (TextFieldItem text : mListFieldItems) {
            result += text.currentText.text;
        }

        return result;
    }

    public float getSize() {
        return Util.convertPixelToVRFloatValue((TEXT_WIDTH + INTERACTIVE_TEXT_WIDTH)
                * maxNumberOfCharecters);
    }

    public void append(CharItem charItem) {

        append(mListFieldItems.size(), charItem);
    }

    public void append(final int position, final CharItem charItem) {
        addCharacter(position, charItem);

    }

    public void addAllSubLine() {

        for (int i = 0; i < maxNumberOfCharecters; i++) {

            addSubLine(i);
        }
    }

    private void addSubLineOnPosition(final int position) {
        if (mListFieldSubLines.size() < maxNumberOfCharecters) {
            this.getGVRContext().runOnGlThread(new Runnable() {

                @Override
                public void run() {

                    GVRSceneObject space = newSpaceLine(position);
                    mListFieldSubLines.add(position, space);
                    addChildObject(space);
                }
            });
        }
    }

    private GVRSceneObject newSpaceLine(int position) {

        GVRSceneObject space = new GVRSceneObject(TextField.this.getGVRContext(),
                Util.convertPixelToVRFloatValue(40),
                Util.convertPixelToVRFloatValue(5),
                TextField.this.getGVRContext().loadTexture(
                        new GVRAndroidResource(TextField.this.getGVRContext(),
                                R.drawable.key_space_active)));

        space.getTransform().setPosition(position * TEXT_WIDTH, SUB_LINE_Y, 0);
        space.getRenderData().setRenderingOrder(RenderingOrder.KEYBOARD);

        return space;
    }

    public void removeCharacter(int position) {

        if (mListFieldItems.size() == 0)
            return;
        if (position == LAST_CHARACTER)
            position = mListFieldItems.size() - 1;
        TextFieldItem character = mListFieldItems.get(position);
        removeChildObject(character);

        mListFieldItems.remove(position);
        adjustPosition(position);
    }

    public void removeAllTextFieldItem() {

        if (mListFieldItems.size() > 0) {

            for (TextFieldItem textFieldItem : mListFieldItems) {
                removeChildObject(textFieldItem);
            }
            mListFieldItems = new ArrayList<TextFieldItem>();
        }
    }

    protected TextFieldItem getObjectInHitArea() {

        GVREyePointeeHolder[] eyePointeeHolders = GVRPicker.pickScene(this.getGVRContext()
                .getMainScene());
       
        for (GVREyePointeeHolder eph : eyePointeeHolders) {

            for (int i = 0; i < mListFieldItems.size(); i++) {

                if (eph.getOwnerObject().hashCode() == mListFieldItems.get(i)
                        .getEyePointeeHolder().getOwnerObject().hashCode()) {
                    
                    currentCharPosition = i;
                    return mListFieldItems.get(i);

                }
            }
        }
        return null;
    }

    private void adjustPosition(int positionInitial) {
        for (int i = positionInitial; i < mListFieldItems.size(); i++) {
            mListFieldItems.get(i).getTransform().setPosition(i * TEXT_WIDTH, 0, 0);
        }
    }

    private void addCharacter(final int position, final CharItem charItem) {

        this.getGVRContext().runOnGlThread(new Runnable() {

            @Override
            public void run() {
                if (mListFieldItems.size() < maxNumberOfCharecters) {
                    float sceneObjectWidth = 0.19f;
                    float sceneObjectHeigth = 0.29f;
                    int bitmapWidth = 45;
                    int bitmapHeigth = 78;

                    Text text = new Text();
                    text.textSize = 75;
                    text.backgroundColor = Color.argb(0, 0, 0, 0);

                    TextFieldItem character = new TextFieldItem(getGVRContext(),
                            sceneObjectWidth, sceneObjectHeigth,
                            bitmapWidth, bitmapHeigth,
                            text, position);

                    character.currentText.maxLength = 1;
                    character.setText(getGVRContext(), charItem);
                    character.getRenderData().setRenderingOrder(RenderingOrder.KEYBOARD - 30);
                    character.getTransform().setPosition(position * TEXT_WIDTH, 0, 0);
                    character.attachEyePointeeHolder();
                    mListFieldItems.add(position, character);
                    addChildObject(character);
                }
            }
        });

    }

    public void cleanText() {
        for (int i = 0; i < mListFieldItems.size(); i++) {
            TextFieldItem character = mListFieldItems.get(i);
            character.currentText.text = "";
            removeChildObject(character);
        }

        mListFieldItems.clear();
    }

    public void spinnerUpdate() {

        spinner.getSpinnerRoulette().onStep();

  
        if (!mListFieldItems.isEmpty()) {

            currentCharSelected = getObjectInHitArea();
           
            
            if (currentCharSelected != null) {

                tryShowSpinner(spinner);

            }

            if (spinner.isHitArea()) {
                tryHideSpinner();
            }
        }

    }

    private synchronized void tryHideSpinner() {
        if (spinner.isActive() && !spinner.isShuttingDown()) {

            mListFieldItems.get(spinner.getSpinnerRoulette().getPosition()).getRenderData()
                    .getMaterial().setOpacity(1);
            mListFieldItems.get(spinner.getSpinnerRoulette().getPosition()).getCharItem()
                    .setMode(spinner.getSpinnerRoulette().getCurrentValue().getMode());
            mListFieldItems.get(spinner.getSpinnerRoulette().getPosition()).setText(
                    getGVRContext(), spinner.getSpinnerRoulette().getCurrentValue());

            spinner.off();

        }
    }

    private synchronized void tryShowSpinner(Spinner spinner) {
       
        if (!spinner.isActive() && !spinner.isShuttingDown()) {

            spinner.on(currentCharSelected.getCharItem().getPosition(), currentCharSelected
                    .getCharItem().getMode(),
                    currentCharPosition);

            setPositionSpinner(currentCharSelected, spinner);

            mListFieldItems.get(spinner.getSpinnerRoulette().getPosition()).getRenderData()
                    .getMaterial().setOpacity(0);

        }
    }

    private void setPositionSpinner(TextFieldItem currentChar, Spinner spinner) {
        spinner.move(currentChar);
    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;

    }

    public void setNumberOfCharecters(int numberOfCharecters) {

        this.maxNumberOfCharecters = numberOfCharecters;
        updateSubLines(numberOfCharecters);
    }

    public void addSubLine(int positionInitial) {

        int position = positionInitial;
        addSubLineOnPosition(position++);
    }

    private void updateSubLines(int numberOfCharecters) {

        if (mListFieldSubLines.size() == numberOfCharecters) {
            return;

        } else if (mListFieldSubLines.size() > numberOfCharecters) {

            removeToMach(numberOfCharecters);

        } else {

            addToMach(numberOfCharecters);
        }
    }

    private void addToMach(int numberOfCharecters) {
        int oldSize = mListFieldSubLines.size();

        for (int i = oldSize; i < numberOfCharecters; i++) {

            addSubLine(i);
        }
    }

    private void removeToMach(int numberOfCharecters2) {

        int oldSize = mListFieldSubLines.size();

        for (int i = oldSize; i > maxNumberOfCharecters; i--) {

            removeSubLine(i - 1);
        }
    }

    private void removeSubLine(int i) {
        GVRSceneObject subLine = mListFieldSubLines.get(i);
        removeChildObject(subLine);
        mListFieldSubLines.remove(i);
    }

    public float getInitialPosition() {
        return (maxNumberOfCharecters - 1) * TEXT_WIDTH;
    }

}
