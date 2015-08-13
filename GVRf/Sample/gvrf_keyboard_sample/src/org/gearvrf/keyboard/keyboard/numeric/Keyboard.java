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

package org.gearvrf.keyboard.keyboard.numeric;

import android.content.res.Resources;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.keyboard.model.KeyboardBase;
import org.gearvrf.keyboard.keyboard.model.KeyboardEventListener;
import org.gearvrf.keyboard.keyboard.model.KeyboardItemBase;
import org.gearvrf.keyboard.keyboard.model.KeyboardLine;
import org.gearvrf.keyboard.model.AudioClip;
import org.gearvrf.keyboard.model.Dashboard;
import org.gearvrf.keyboard.model.KeyboardCharItem;
import org.gearvrf.keyboard.util.SceneObjectNames;

/**
 * @author Douglas and SIDIA VR TEAM
 */
public class Keyboard extends GVRSceneObject {

    public static final int SOFT_KEYBOARD_UPPERCASE = 0;
    public static final int SOFT_KEYBOARD_LOWERCASE = 1;
    public static final int NUMERIC_KEYBOARD = 2;
    public static final int SOFT_KEYBOARD_SPECIAL = 3;
    public static int mode = NUMERIC_KEYBOARD;

    public final int SHIFT_LOWERCASE = 0;
    public final int SHIFT_FIRST_LETTER_UPPERCASE = 1;
    public final int SHIFT_UPPERCASE = 2;
    public int shift = SHIFT_LOWERCASE;

    public enum KeyboardType {
        NUMERIC, ALPHA
    }

    private static final float ANIMATION_TOTAL_TIME = 2.6f;

    private GVRSceneObject currentSelection;
    private boolean isEnabled = false;
    private KeyboardBase keyboard;
    private KeyboardAlphabetic keyboardAlphabetic;
    private NumericKeyboard numericKeyboard;
    private KeyboardType currentType;
    private Resources androidResources;

    public KeyboardType getCurrentType() {
        return currentType;
    }

    private KeyboardEventListener keyboardEventListener;

    public Keyboard(GVRContext gvrContext) {
        super(gvrContext);
        setName(SceneObjectNames.KEYBOARD);
        keyboardAlphabetic = new KeyboardAlphabetic(getGVRContext());
        numericKeyboard = new NumericKeyboard(getGVRContext());
        androidResources = this.getGVRContext().getContext().getApplicationContext().getResources();
    }

    private void createSoftMode() {
        mode = SOFT_KEYBOARD_LOWERCASE;
        keyboard = keyboardAlphabetic;
        changeToLowercase();
        configureKeyboardParentation(keyboard);
        currentType = KeyboardType.ALPHA;
        getTransform().setScale(1.5f, 1.5f, 1.5f);
    }

    private void createNumericMode() {
        mode = NUMERIC_KEYBOARD;
        keyboard = numericKeyboard;
        configureKeyboardParentation(keyboard);
        currentType = KeyboardType.NUMERIC;
        getTransform().setScale(1.5f, 1.5f, 1.5f);
    }

    public void setOnKeyboardEventListener(KeyboardEventListener keyboardEventListener) {
        this.keyboardEventListener = keyboardEventListener;
    }

    private void configureKeyboardParentation(KeyboardBase keyboard) {

        if (keyboard.getListKeyboardLine() != null) {

            for (KeyboardLine item : keyboard.getListKeyboardLine()) {
                addChildObject(item);
            }
        }
    }

    private void configureKeyboardRemoveParentation() {

        if (keyboard.getListKeyboardLine() != null) {

            for (KeyboardLine item : keyboard.getListKeyboardLine()) {
                removeChildObject(item);
            }
        }
    }

    public void tapKeyboard() {

        AudioClip.getInstance(getGVRContext().getContext()).playSound(
                AudioClip.getKeyEnterSoundID(), 1.0f, 1.0f);

        if (currentSelection != null) {

            if (keyboardEventListener != null) {

                KeyboardCharItem currentItem = ((KeyboardItemBase) currentSelection)
                        .getKeyboardCharItem();

                if (currentItem.getCharacter().equalsIgnoreCase(
                        androidResources.getString(R.string.btn_rmv))) {

                    keyboardEventListener.onKeyDelete();

                } else if (currentItem.getCharacter().equalsIgnoreCase(
                        androidResources.getString(R.string.btn_ok))) {

                    getTransform().setRotation(1, 0, 0, 0);
                    getTransform().setPosition(0, 0, 0);
                    hideKeyboard();

                    keyboardEventListener.onKeyConfirm();

                } else if (currentItem.getCharacter().equalsIgnoreCase(
                        androidResources.getString(R.string.btn_shift))) {
                    shiftKeys();
                }

                else if (currentItem.getCharacter().equalsIgnoreCase(
                        androidResources.getString(R.string.btn_123))) {
                    changeToSpecialCharacter();

                } else if (currentItem.getCharacter().equalsIgnoreCase(
                        androidResources.getString(R.string.btn_abc))) {
                    changeToLowercase();

                } else {

                    keyboardEventListener.onKeyPressedWhitItem(currentItem);

                    if (getShift() == SHIFT_FIRST_LETTER_UPPERCASE) {
                        shiftKeys();
                        shiftKeys();
                    }
                }
            }
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void showKeyboard(KeyboardType keyboardType) {

        if (KeyboardType.ALPHA == keyboardType) {

            createSoftMode();

        } else {
            createNumericMode();
        }

        isEnabled = true;

        getGVRContext().getMainScene().addSceneObject(this);

        for (KeyboardLine item : keyboard.getListKeyboardLine()) {

            for (int i = 0; i < item.getChildrenCount(); i++) {

                if (item.getChildByIndex(i).getRenderData() == null) {
                    return;
                }

                GVROpacityAnimation anim3 = new GVROpacityAnimation(item.getChildByIndex(i),
                        ANIMATION_TOTAL_TIME, 1);

                anim3.start(getGVRContext().getAnimationEngine());
            }
        }
    }

    public void hideKeyboard() {

        isEnabled = false;

        getGVRContext().getMainScene().removeSceneObject(this);
        configureKeyboardRemoveParentation();
    }

    public void setHoverMaterial(GVRSceneObject obj) {

        KeyboardItemBase t = (KeyboardItemBase) obj;
        t.setHoverMaterial();
    }

    public void setNormalMaterial(GVRSceneObject obj) {

        KeyboardItemBase t = (KeyboardItemBase) obj;
        t.setNormalMaterial();
    }

    public void onSingleTap(MotionEvent e) {
        tapKeyboard();
    }

    public void update() {
        changeTexture();
    }

    boolean test = true;

    private void changeTexture() {

        GVREyePointeeHolder[] holders = GVRPicker.pickScene(getGVRContext().getMainScene());

        if (holders.length <= 1) {

            if (currentSelection != null) {
                setNormalMaterial(currentSelection);
            }

            currentSelection = null;
        }

        for (GVREyePointeeHolder eph : GVRPicker.pickScene(getGVRContext().getMainScene())) {

            if (eph.getOwnerObject().hashCode() == Dashboard.currentDashboardHashCode) {
                continue;
            }

            for (GVRSceneObject object : keyboard.getObjects()) {

                if (eph.getOwnerObject().equals(object)) {

                    setHoverMaterial(object);

                    if (object.equals(currentSelection)) {
                        setHoverMaterial(object);
                    } else {

                        if (currentSelection != null) {
                            setNormalMaterial(currentSelection);
                        }

                        currentSelection = object;
                    }

                    break;

                } else {

                    if (currentSelection != null) {
                        setNormalMaterial(currentSelection);
                    }

                    currentSelection = null;
                }
            }
        }
    }

    public void shiftKeys() {
        switch (shift) {
            case SHIFT_LOWERCASE:
                changeToUppercase();
                shift = SHIFT_FIRST_LETTER_UPPERCASE;
                break;
            case SHIFT_FIRST_LETTER_UPPERCASE:
                shift = SHIFT_UPPERCASE;
                break;
            case SHIFT_UPPERCASE:
                changeToLowercase();
                shift = SHIFT_LOWERCASE;
                break;
            default:
                changeToLowercase();
                shift = SHIFT_LOWERCASE;
                break;
        }
    }

    public int getShift() {
        return shift;
    }

    private void changeToLowercase() {
        mode = SOFT_KEYBOARD_LOWERCASE;
        for (GVRSceneObject object : keyboard.getObjects()) {
            ((KeyboardItemBase) object).switchMaterialState(SOFT_KEYBOARD_LOWERCASE);
        }
    }

    private void changeToUppercase() {
        mode = SOFT_KEYBOARD_UPPERCASE;
        for (GVRSceneObject object : keyboard.getObjects()) {
            ((KeyboardItemBase) object).switchMaterialState(SOFT_KEYBOARD_UPPERCASE);
        }
    }

    public void changeToSpecialCharacter() {

        mode = SOFT_KEYBOARD_SPECIAL;

        for (GVRSceneObject object : keyboard.getObjects()) {
            ((KeyboardItemBase) object).switchMaterialState(SOFT_KEYBOARD_SPECIAL);
        }
    }

    public void changeToNormalCharacter() {

        switch (mode) {

            case SHIFT_LOWERCASE:
                changeToUppercase();
                shift = SHIFT_FIRST_LETTER_UPPERCASE;
                break;
            case SHIFT_FIRST_LETTER_UPPERCASE:
                shift = SHIFT_UPPERCASE;
                break;
            case SHIFT_UPPERCASE:
                changeToLowercase();
                shift = SHIFT_LOWERCASE;
                break;
            default:
                changeToLowercase();
                shift = SHIFT_LOWERCASE;
                break;
        }
    }
}
