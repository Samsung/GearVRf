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

package org.gearvrf.keyboard.spinner;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.gearvrf.GVRContext;
import org.gearvrf.keyboard.keyboard.numeric.Keyboard;
import org.gearvrf.keyboard.model.CharList;
import org.gearvrf.keyboard.textField.Text;
import org.gearvrf.keyboard.util.GVRTextBitmapFactory;

import java.util.ArrayList;
import java.util.List;

public class SpinnerItemFactory {

    private static SpinnerItemFactory instance;
    private List<Bitmap> numericBitmapList = new ArrayList<Bitmap>();
    private List<Bitmap> alphaLowerBitmapList = new ArrayList<Bitmap>();
    private List<Bitmap> alphaUpperBitmapList = new ArrayList<Bitmap>();
    private List<Bitmap> specialBitmapList = new ArrayList<Bitmap>();
    private GVRContext gvrContext;

    public SpinnerItemFactory(GVRContext gvrContext) {
        this.gvrContext = gvrContext;
    }

    public static synchronized SpinnerItemFactory getInstance(GVRContext gvrContext) {

        if (instance == null) {
            instance = new SpinnerItemFactory(gvrContext);
        }

        return instance;
    }

    private Bitmap createSpinnerItem(String spinnerText) {

        int bitmapWidth = 45;
        int bitmapHeigth = 72;

        Text text = new Text();
        text.textSize = 75;
        // text.backgroundColor = Color.BLACK;
        text.backgroundColor = Color.parseColor("#00204d");
        text.text = spinnerText;

        return GVRTextBitmapFactory.create(gvrContext.getContext(), bitmapWidth, bitmapHeigth,
                text, 0);
    }

    public void init() {

        for (int i = 0; i < CharList.getInstance(gvrContext)
                .getList(Keyboard.SOFT_KEYBOARD_LOWERCASE).size(); i++) {
            alphaLowerBitmapList
                    .add(createSpinnerItem(CharList.getInstance(gvrContext)
                            .getList(Keyboard.SOFT_KEYBOARD_LOWERCASE).get(i)));

        }

        for (int i = 0; i < CharList.getInstance(gvrContext)
                .getList(Keyboard.SOFT_KEYBOARD_UPPERCASE).size(); i++) {
            alphaUpperBitmapList.add(createSpinnerItem(CharList.getInstance(gvrContext)
                    .getList(Keyboard.SOFT_KEYBOARD_UPPERCASE).get(i)
                    .toUpperCase()));
        }

        for (int i = 0; i < CharList.getInstance(gvrContext).getList(Keyboard.NUMERIC_KEYBOARD)
                .size(); i++) {
            numericBitmapList.add(createSpinnerItem(CharList.getInstance(gvrContext)
                    .getList(Keyboard.NUMERIC_KEYBOARD).get(i)));
        }

        for (int i = 0; i < CharList.getInstance(gvrContext)
                .getList(Keyboard.SOFT_KEYBOARD_SPECIAL).size(); i++) {
            specialBitmapList.add(createSpinnerItem(CharList.getInstance(gvrContext)
                    .getList(Keyboard.SOFT_KEYBOARD_SPECIAL).get(i)));
        }

    }

    public Bitmap getBitmap(int mode, int position) {
        return getList(mode).get(position);

    }

    public List<Bitmap> getList(int mode) {
        List<Bitmap> list = null;

        switch (mode) {
            case Keyboard.NUMERIC_KEYBOARD:
                list = numericBitmapList;
                break;
            case Keyboard.SOFT_KEYBOARD_LOWERCASE:
                list = alphaLowerBitmapList;
                break;
            case Keyboard.SOFT_KEYBOARD_SPECIAL:
                list = specialBitmapList;
                break;
            case Keyboard.SOFT_KEYBOARD_UPPERCASE:
                list = alphaUpperBitmapList;
                break;
        }

        return list;
    }

}
