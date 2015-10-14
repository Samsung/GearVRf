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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;

/**
 * A class which creates Bitmaps with texts on them.
 * 
 * @author hanseul
 */
public class GVRTextBitmapFactory {

    public static boolean cacheOn = true;

    private GVRTextBitmapFactory() {
    }

    /**
     * Creates a Bitmap with texts.
     * 
     * @param width The width of the Bitmap.
     * @param height The height of the Bitmap.
     * @param text The text.
     * @param textSize The size of the texts.
     * @param textAlign The way to align the texts.
     * @param textColor The color of the texts.
     * @param backgroundColor The color of the background.
     * @return The Bitmap.
     */
    public static Bitmap create(float width, float height, String character, int textSize,
            Align textAlign, int textColor, int backgroundColor, Context context) {

        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        paint.setTextSize(textSize);
        paint.setTextAlign(textAlign);
        paint.setFakeBoldText(true);
        paint.setColor(textColor);

        canvas.drawColor(backgroundColor);
        canvas.drawText(character, width / 2, height / 1.6f, paint);

        return bitmap;
    }

    public static Bitmap create(float width, float height, Text text) {

        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        paint.setTextSize(text.textSize);
        paint.setTextAlign(text.align);
        paint.setFakeBoldText(true);
        paint.setColor(text.textColor);

        canvas.drawColor(text.backgroundColor);

        if (text.align == Align.CENTER) {

            canvas.drawText(text.text, width / 2, height / 1.6f, paint);

        } else if (text.align == Align.LEFT) {

            canvas.drawText(text.text, 0, height / 1.6f, paint);
        }

        return bitmap;
    }

    public static Bitmap create(Context context, float width, float height, Text text, String font) {

        Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), font);

        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setTypeface(myTypeface);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Style.FILL);
        paint.setTextSize(text.textSize);
        paint.setFakeBoldText(true);
        paint.setColor(text.textColor);
        paint.setFilterBitmap(true);

        Rect rectText = new Rect();
        paint.getTextBounds(text.text, 0, text.text.length(), rectText);

        canvas.drawColor(text.backgroundColor);
        
        if(text.align == Align.CENTER){
        
            canvas.drawText(text.text, width / 2 - rectText.exactCenterX(), height / 2 - rectText.exactCenterY(), paint);
        
        } else if(text.align == Align.LEFT){
            
            canvas.drawText(text.text, 0, height / 2 - rectText.exactCenterY(), paint);
        }

        return bitmap;
    }

    public static Bitmap create(Context context, int width, int height, Text text, String font) {
        
        Resources res = context.getResources();
        float scale = res.getDisplayMetrics().density;
        
        Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), font);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setTypeface(myTypeface);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Style.FILL);
        // text size in pixels
        paint.setTextSize(text.textSize * scale);
        paint.setFakeBoldText(true);
        paint.setColor(text.textColor);
        paint.setFilterBitmap(true);

        Rect rectText = new Rect();
        paint.getTextBounds(text.text, 0, text.text.length(), rectText);

        canvas.drawColor(text.backgroundColor);
        
        if(text.align == Align.CENTER){
        
            canvas.drawText(text.text, width / 2 - rectText.exactCenterX(), height / 2 - rectText.exactCenterY(), paint);
        
        } else if(text.align == Align.LEFT){
            
            canvas.drawText(text.text, 0, height / 2 - rectText.exactCenterY(), paint);
        }

        return bitmap;
    }
}
