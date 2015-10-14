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

package org.gearvrf.controls.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;

public class Text {

    public String text;
    public Align align;
    public float textSize;
    public int textColor;
    public int backgroundColor;
    public int maxLength;

    public Text(String text, Align align, float textSize, int textColor, int backgroundColor,
            int maxLength) {
        this.align = align;
        this.textSize = textSize;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.maxLength = maxLength;
        this.text = text;
    }

    public static Bitmap createWithCustomFont(Context context, float width, float height,
            Text text, int test, String customFontPath) { // spinner

        Typeface myTypeface = Typeface.createFromAsset(context.getAssets(),
                customFontPath);

        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setTypeface(myTypeface);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Style.FILL);
        paint.setTextSize(text.textSize);
        paint.setFakeBoldText(false);
        paint.setColor(text.textColor);
        paint.setFilterBitmap(true);

        Rect rectText = new Rect();
        paint.getTextBounds(text.text, 0, text.text.length(), rectText);

        canvas.drawColor(text.backgroundColor);

        canvas.drawText(text.text, width / 2 - rectText.exactCenterX(),
                height / 2 - rectText.exactCenterY(), paint);

        return bitmap;
    }

}
