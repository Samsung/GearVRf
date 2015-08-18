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

package org.gearvrf.keyboard.model;

import android.content.res.Resources;
import android.content.res.TypedArray;

import org.gearvrf.GVRContext;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.keyboard.numeric.Keyboard;
import org.gearvrf.keyboard.util.CircularList;

import java.util.ArrayList;

public class CharList {

    private static CharList instance;
    private ArrayList<String> softKeyboardListUpperCase = new ArrayList<>();
    private ArrayList<String> softKeyboardListLowerCase = new ArrayList<>();
    private ArrayList<String> numericKeyboardList = new ArrayList<>();
    private ArrayList<String> specialKeyboardList = new ArrayList<>();

    private CircularList<CharItem> softKeyboardListUpperCaseCircular = new CircularList<CharItem>(
            new ArrayList<CharItem>());
    private CircularList<CharItem> numericKeyboardListCircular = new CircularList<CharItem>(
            new ArrayList<CharItem>());
    private CircularList<CharItem> softKeyboardListLowerCaseCircular = new CircularList<CharItem>(
            new ArrayList<CharItem>());
    private CircularList<CharItem> softKeyboardListSpecialCircular = new CircularList<CharItem>(
            new ArrayList<CharItem>());

    private CharList(GVRContext gvrContext) {

        Resources res = gvrContext.getContext().getResources();
        TypedArray softKeyboard = res.obtainTypedArray(R.array.soft_keyboard);
        TypedArray numericKeyboard = res.obtainTypedArray(R.array.soft_keyboard_number);
        TypedArray specialKeyboard = res.obtainTypedArray(R.array.soft_keyboard_special);

        for (int i = 0; i < softKeyboard.length(); i++) {
            softKeyboardListUpperCase.add(softKeyboard.getString(i));
        }

        for (int i = 0; i < softKeyboard.length(); i++) {
            softKeyboardListLowerCase.add(softKeyboard.getString(i).toLowerCase());
        }

        for (int i = 0; i < numericKeyboard.length(); i++) {
            numericKeyboardList.add(numericKeyboard.getString(i));
        }

        for (int i = 0; i < specialKeyboard.length(); i++) {
            specialKeyboardList.add(specialKeyboard.getString(i));
        }

        // Circular //

        for (int i = 0; i < softKeyboard.length(); i++) {
            softKeyboardListUpperCaseCircular.add(new CharItem(Keyboard.SOFT_KEYBOARD_UPPERCASE, i,
                    softKeyboard.getString(i)));
        }

        for (int i = 0; i < softKeyboard.length(); i++) {
            softKeyboardListLowerCaseCircular.add(new CharItem(Keyboard.SOFT_KEYBOARD_LOWERCASE, i,
                    softKeyboard.getString(i).toLowerCase()));
        }

        for (int i = 0; i < numericKeyboard.length(); i++) {
            numericKeyboardListCircular.add(new CharItem(Keyboard.NUMERIC_KEYBOARD, i,
                    numericKeyboard.getString(i)));
        }

        for (int i = 0; i < specialKeyboard.length(); i++) {
            softKeyboardListSpecialCircular.add(new CharItem(Keyboard.SOFT_KEYBOARD_SPECIAL, i,
                    specialKeyboard.getString(i)));
        }
    }

    public static synchronized CharList getInstance(GVRContext gvrContext) {
        if (instance == null) {
            instance = new CharList(gvrContext);
        }
        return instance;
    }

    public ArrayList<String> getList() {
        // Keyboard.getMode()
        switch (Keyboard.mode) {

            case Keyboard.SOFT_KEYBOARD_UPPERCASE:
                return softKeyboardListUpperCase;

            case Keyboard.SOFT_KEYBOARD_LOWERCASE:
                return softKeyboardListLowerCase;

            case Keyboard.NUMERIC_KEYBOARD:
                return numericKeyboardList;

            default:
                return specialKeyboardList;
        }
    }

    public CircularList<CharItem> getListCircular(int mode) {

        switch (mode) {

            case Keyboard.SOFT_KEYBOARD_UPPERCASE:
                return softKeyboardListUpperCaseCircular;

            case Keyboard.SOFT_KEYBOARD_LOWERCASE:

                return softKeyboardListLowerCaseCircular;

            case Keyboard.NUMERIC_KEYBOARD:
                return numericKeyboardListCircular;

            default:
                return softKeyboardListSpecialCircular;
        }
    }

    public ArrayList<String> getList(int mode) {

        switch (mode) {

            case Keyboard.SOFT_KEYBOARD_UPPERCASE:
                return softKeyboardListUpperCase;

            case Keyboard.SOFT_KEYBOARD_LOWERCASE:
                return softKeyboardListLowerCase;

            case Keyboard.NUMERIC_KEYBOARD:
                return numericKeyboardList;

            default:
                return specialKeyboardList;
        }
    }

    public int getMode(Character character) {

        if (Character.isDigit(character)) {
            return Keyboard.NUMERIC_KEYBOARD;
        }
        else if (Character.isLowerCase(character)) {
            return Keyboard.SOFT_KEYBOARD_LOWERCASE;
        } else if (Character.isUpperCase(character)) {
            return Keyboard.SOFT_KEYBOARD_UPPERCASE;
        } else {
            return Keyboard.SOFT_KEYBOARD_SPECIAL;
        }

    }

    public int indexOf(String character, int keyboardmode) {

        switch (keyboardmode) {
            case Keyboard.SOFT_KEYBOARD_UPPERCASE:
                return softKeyboardListUpperCase.indexOf(character);

            case Keyboard.SOFT_KEYBOARD_LOWERCASE:
                return softKeyboardListLowerCase.indexOf(character);

            case Keyboard.NUMERIC_KEYBOARD:
                return numericKeyboardList.indexOf(character);

            default:
                return specialKeyboardList.indexOf(character);
        }
    }

    public int indexOf(String character) {

        switch (Keyboard.mode) {
            case Keyboard.SOFT_KEYBOARD_UPPERCASE:
                return softKeyboardListUpperCase.indexOf(character);

            case Keyboard.SOFT_KEYBOARD_LOWERCASE:
                return softKeyboardListLowerCase.indexOf(character);

            case Keyboard.NUMERIC_KEYBOARD:
                return numericKeyboardList.indexOf(character);

            default:
                return specialKeyboardList.indexOf(character);
        }
    }
}
