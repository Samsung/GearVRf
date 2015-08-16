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

package org.gearvrf.keyboard.keyboard.model;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import java.util.ArrayList;
import java.util.List;

public class KeyboardBase extends GVRSceneObject {

    public float softLineWidth = 0;
    private List<GVRSceneObject> objects = null;
    private List<KeyboardLine> mListKeyboardLine = new ArrayList<>();

    public KeyboardBase(GVRContext gvrContext) {
        super(gvrContext);
        setName("KEYBOARD_BASE");
    }

    public List<KeyboardLine> getListKeyboardLine() {
        return mListKeyboardLine;
    }

    public void addLine(KeyboardLine keyboardLine) {
        mListKeyboardLine.add(keyboardLine);
    }

    public void setListKeyboardLine(List<KeyboardLine> listKeyboardLine) {
        this.mListKeyboardLine = listKeyboardLine;
    }

    public List<GVRSceneObject> getObjects() {

        if (objects == null) {

            objects = new ArrayList<GVRSceneObject>();

            for (KeyboardLine line : mListKeyboardLine) {
                objects.addAll(line.getChildren());
            }
        }

        return objects;
    }
}
