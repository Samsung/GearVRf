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

package org.gearvrf.keyboard.util;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;

import org.gearvrf.GVRContext;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.keyboard.numeric.Keyboard;
import org.gearvrf.keyboard.model.CharItem;
import org.gearvrf.keyboard.spinner.SpinnerItem;
import org.gearvrf.keyboard.textField.Text;

import java.util.ArrayList;

public class BitmapFactory {

    private static BitmapFactory instance;
    private CircularList<SpinnerItem> numericSpinnerItems = new CircularList<SpinnerItem>(
            new ArrayList<SpinnerItem>());;
    private CircularList<SpinnerItem> alphaLowerCaseSpinnerItems = new CircularList<SpinnerItem>(
            new ArrayList<SpinnerItem>());;
    private CircularList<SpinnerItem> alphaUpperCaseSpinnerItems = new CircularList<SpinnerItem>(
            new ArrayList<SpinnerItem>());;
    private CircularList<SpinnerItem> specialCharacterSpinnerItems = new CircularList<SpinnerItem>(
            new ArrayList<SpinnerItem>());;
    private CircularList<SpinnerItem> defaultSpinnerItem = new CircularList<SpinnerItem>(
            new ArrayList<SpinnerItem>());;
    private static final int SPINNER_ITEM_SIZE = 8;

    public BitmapFactory(GVRContext gvrContext) {
    }

    public static synchronized BitmapFactory getInstance(GVRContext gvrContext) {

        if (instance == null) {
            instance = new BitmapFactory(gvrContext);
        }

        return instance;
    }

    private SpinnerItem createSpinnerItem(GVRContext gvrContext, String spinnerText, int position,
            int mode) {

        float sceneObjectWidth = 0.19f;
        float sceneObjectHeigth = 0.29f;
        int bitmapWidth = 45;
        int bitmapHeigth = 72;

        Text text = new Text();
        text.textSize = 75;
        text.backgroundColor = Color.BLACK;

        SpinnerItem textSpinner = new SpinnerItem(gvrContext, sceneObjectWidth, sceneObjectHeigth,
                bitmapWidth, bitmapHeigth, position, text);
        textSpinner.setText(gvrContext, new CharItem(mode, position, spinnerText));

        return textSpinner;
    }

    public void init(GVRContext gvrContext) {

        Resources res = gvrContext.getContext().getResources();
        TypedArray softKeyboard = res.obtainTypedArray(R.array.soft_keyboard);
        TypedArray numericKeyboard = res.obtainTypedArray(R.array.soft_keyboard_number);

        for (int i = 0; i < softKeyboard.length(); i++) {
            alphaLowerCaseSpinnerItems.add(createSpinnerItem(gvrContext, softKeyboard.getString(i),
                    i, Keyboard.SOFT_KEYBOARD_LOWERCASE));

        }

        for (int i = 0; i < softKeyboard.length(); i++) {
            alphaUpperCaseSpinnerItems
                    .add(createSpinnerItem(gvrContext, softKeyboard.getString(i).toUpperCase(), i,
                            Keyboard.SOFT_KEYBOARD_UPPERCASE));
        }

        for (int i = 0; i < numericKeyboard.length(); i++) {
            numericSpinnerItems.add(createSpinnerItem(gvrContext, numericKeyboard.getString(i)
                    .toUpperCase(), i, Keyboard.NUMERIC_KEYBOARD));
        }

        for (int i = 0; i < SPINNER_ITEM_SIZE; i++) {
            defaultSpinnerItem.add(createSpinnerItem(gvrContext, numericKeyboard.getString(i)
                    .toUpperCase(), i, Keyboard.NUMERIC_KEYBOARD));
        }

        populateSpecialList(gvrContext);
    }

    private void populateSpecialList(GVRContext gvrContext) {

        Resources res = gvrContext.getContext().getResources();
        TypedArray specialKeyboard = res.obtainTypedArray(R.array.soft_keyboard_special);

        int n = specialKeyboard.length();

        for (int i = 0; i < n; i++) {

            specialCharacterSpinnerItems.add(createSpinnerItem(gvrContext, res.getString(i), i,
                    Keyboard.SOFT_KEYBOARD_SPECIAL));
        }

        specialKeyboard.recycle();
    }

    public CircularList<SpinnerItem> getNumericSpinnerItems() {
        return numericSpinnerItems;
    }

    public CircularList<SpinnerItem> getAlphaLowerCaseSpinnerItems() {
        return alphaLowerCaseSpinnerItems;
    }

    public CircularList<SpinnerItem> getAlphaUpperCaseSpinnerItems() {
        return alphaUpperCaseSpinnerItems;
    }

    public CircularList<SpinnerItem> getSpecialCharacterSpinnerItems() {
        return specialCharacterSpinnerItems;
    }

    public CircularList<SpinnerItem> getDefaultSpinnerItem() {
        return defaultSpinnerItem;
    }

    public CircularList<SpinnerItem> getList(int mode) {
        CircularList<SpinnerItem> list = null;

        switch (mode) {
            case Keyboard.NUMERIC_KEYBOARD:
                list = numericSpinnerItems;
                break;
            case Keyboard.SOFT_KEYBOARD_LOWERCASE:
                list = alphaLowerCaseSpinnerItems;
                break;
            case Keyboard.SOFT_KEYBOARD_SPECIAL:
                list = specialCharacterSpinnerItems;
                break;
            case Keyboard.SOFT_KEYBOARD_UPPERCASE:
                list = alphaUpperCaseSpinnerItems;
                break;
        }

        return list;
    }
}
