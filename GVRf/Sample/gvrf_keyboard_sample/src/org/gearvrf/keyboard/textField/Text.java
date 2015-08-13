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
import android.graphics.Paint.Align;

public class Text {

    public String text;
    public Align align;
    public int textSize;
    public int textColor;
    public int backgroundColor;
    public int maxLength;

    public Text() {
        align = Align.CENTER;
        textSize = 55;
        textColor = Color.WHITE;
        backgroundColor = Color.argb(0, 0, 0, 0);
        maxLength = 4;
        text = "";
    }

}
