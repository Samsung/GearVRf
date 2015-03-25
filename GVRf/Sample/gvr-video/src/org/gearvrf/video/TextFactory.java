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


package org.gearvrf.video;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;

import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRTexture;

public class TextFactory {

    private TextFactory() {
    }

    public static GVRTexture create(GVRContext gvrContext, String text) {
        Bitmap bitmap = Bitmap.createBitmap(256, 23, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(24);
        p.setTextAlign(Align.CENTER);
        p.setColor(Color.WHITE);

        int x = (int) (canvas.getWidth() / 2.0f);
        int y = canvas.getHeight();

        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawText(text, x, y, p);

        return new GVRBitmapTexture(gvrContext, bitmap);
    }
}
