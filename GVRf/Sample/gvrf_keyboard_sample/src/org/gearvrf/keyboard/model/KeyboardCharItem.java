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

public class KeyboardCharItem {

    private String character;
    private String specialCharacter;

    public KeyboardCharItem(String character, String specialCharacter) {
        super();
        this.character = character;
        this.specialCharacter = specialCharacter;
    }

    public String getCharacter() {

        switch (Keyboard.mode) {

            case Keyboard.SOFT_KEYBOARD_UPPERCASE:

                return character.toUpperCase();

            case Keyboard.SOFT_KEYBOARD_SPECIAL:

                return specialCharacter;

            default:
                return character;
        }
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getSpecialCharacter() {
        return specialCharacter;
    }

    public void setSpecialCharacter(String specialCharacter) {
        this.specialCharacter = specialCharacter;
    }
}
