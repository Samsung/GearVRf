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


package org.gearvrf.sixaxissensortest;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;

/**
 * A class which creates Bitmaps with texts on them.
 * 
 * @author hanseul
 */
public class GVRTextBitmapFactory2 {

    private GVRTextBitmapFactory2() {
    }

    /**
     * Creates a Bitmap with texts.
     * 
     * @param width
     *            The width of the Bitmap.
     * @param height
     *            The height of the Bitmap.
     * @param text
     *            The text.
     * @param textSize
     *            The size of the texts.
     * @param textAlign
     *            The way to align the texts.
     * @param textColor
     *            The color of the texts.
     * @param backgroundColor
     *            The color of the background.
     * @return The Bitmap.
     */
    public static Bitmap create(int width, int height, String text,
            int textSize, Align textAlign, int textColor, int backgroundColor) {
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint p = new Paint();
        p.setTextSize(textSize);
        p.setTextAlign(textAlign);
        p.setColor(textColor);

        int x = (int) (canvas.getWidth() / 2.0f);
        int y = (int) (canvas.getHeight() / 2.0f);

        canvas.drawColor(backgroundColor);
        canvas.drawText(text, x, y, p);

        return bitmap;
    }
}
