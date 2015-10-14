/* Copyright 2015 Samsung Electronics import android.util.SparseArray;
import android.view.KeyEvent;

import java.util.ArrayList;
t use this file except in compliance with the License.
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

import android.util.SparseArray;
import android.view.KeyEvent;

import java.util.ArrayList;

public class GamepadMap {

    public static final int KEYCODE_BUTTON_L1 = KeyEvent.KEYCODE_BUTTON_L1;
    public static final int KEYCODE_BUTTON_R1 = KeyEvent.KEYCODE_BUTTON_R1;
    public static final int KEYCODE_BUTTON_X = KeyEvent.KEYCODE_BUTTON_X;
    public static final int KEYCODE_BUTTON_Y = KeyEvent.KEYCODE_BUTTON_Y;
    public static final int KEYCODE_BUTTON_A = KeyEvent.KEYCODE_BUTTON_A;
    public static final int KEYCODE_BUTTON_B = KeyEvent.KEYCODE_BUTTON_B;

    public static final int KEYCODE_DPAD_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
    public static final int KEYCODE_DPAD_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
    public static final int KEYCODE_DPAD_UP = KeyEvent.KEYCODE_DPAD_UP;
    public static final int KEYCODE_DPAD_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;

    public static final int KEYCODE_BUTTON_SELECT = KeyEvent.KEYCODE_BUTTON_SELECT;
    public static final int KEYCODE_BUTTON_START = KeyEvent.KEYCODE_BUTTON_START;

    public float axisX;
    public float axisY;
    public float axisHatX;
    public float axisHatY;
    public float axisRX;
    public float axisRY;

    public float centeredAxisX;
    public float centeredAxisY;
    public float centeredAxisHatX;
    public float centeredAxisHatY;
    public float centeredAxisRX;
    public float centeredAxisRY;

    protected ArrayList<Integer> buttonsKeyCode;
    public SparseArray<Button> buttons = new SparseArray<Button>();

    public GamepadMap() {
        super();
        init();
    }

    public void init() {

        populateKeys();

        for (int i : buttonsKeyCode) {
            buttons.append(i, new Button());

        }

    }

    private void populateKeys() {
        buttonsKeyCode = new ArrayList<Integer>();
        buttonsKeyCode.add(KeyEvent.KEYCODE_BUTTON_L1);
        buttonsKeyCode.add(KeyEvent.KEYCODE_BUTTON_R1);
        buttonsKeyCode.add(KeyEvent.KEYCODE_BUTTON_X);
        buttonsKeyCode.add(KeyEvent.KEYCODE_BUTTON_Y);
        buttonsKeyCode.add(KeyEvent.KEYCODE_BUTTON_A);
        buttonsKeyCode.add(KeyEvent.KEYCODE_BUTTON_B);

        buttonsKeyCode.add(KeyEvent.KEYCODE_DPAD_LEFT);
        buttonsKeyCode.add(KeyEvent.KEYCODE_DPAD_RIGHT);
        buttonsKeyCode.add(KeyEvent.KEYCODE_DPAD_UP);
        buttonsKeyCode.add(KeyEvent.KEYCODE_DPAD_DOWN);

        buttonsKeyCode.add(KeyEvent.KEYCODE_BUTTON_SELECT);
        buttonsKeyCode.add(KeyEvent.KEYCODE_BUTTON_START);

        buttonsKeyCode.add(KeyEvent.KEYCODE_BACK);

    }

    public void resetIntermadiateState() {

        for (Integer key : buttonsKeyCode) {
            Button button = buttons.get(key);
            button.down = false;
            button.up = false;
        }

    }

}
