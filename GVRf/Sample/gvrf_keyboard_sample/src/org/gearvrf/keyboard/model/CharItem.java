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

import org.gearvrf.keyboard.keyboard.numeric.Keyboard;

public class CharItem {

    private int mode;
    private int position;
    private String character;

    public CharItem(int mode, int position, String character) {
        super();
        this.mode = mode;
        this.position = position;
        this.character = character;
    }

    public int getMode() {
        return mode;
    }

    public int getPosition() {
        return position;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getCharacter() {

        switch (Keyboard.mode) {

            case Keyboard.SOFT_KEYBOARD_UPPERCASE:

                return character.toUpperCase();

            default:
                return character;
        }
    }

    public void setCharacter(String character) {
        this.character = character;
    }
}
