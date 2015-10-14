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

package org.gearvrf.controls.input;

public class Button {

    protected boolean down;
    protected boolean up;
    protected boolean pressed;

    public boolean isDown() {
        return down;
    }

    public boolean isUp() {
        return up;
    }

    public boolean isPressed() {
        return pressed;
    }

    protected void replicateValues(Button button) {
        this.down = button.down;
        this.up = button.up;
        this.pressed = button.pressed;
    }

}
