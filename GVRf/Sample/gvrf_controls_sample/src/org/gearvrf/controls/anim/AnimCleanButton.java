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

package org.gearvrf.controls.anim;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.controls.R;
import org.gearvrf.controls.menu.MenuControlSceneObject;
import org.gearvrf.controls.shaders.ButtonShader;
import org.gearvrf.controls.util.RenderingOrder;
import org.gearvrf.controls.util.Text;

public class AnimCleanButton extends MenuControlSceneObject {

    private final int IDLE_STATE = 0;
    private final int HOVER_STATE = 1;
    private final int SELECTED_STATE = 2;

    private int WIDTH = 0;
    private int HEIGHT = 0;

    private float quadWidth = .4f;
    private float quadHeigth = .2f;

    private String title;

    public AnimCleanButton(GVRContext gvrContext, String title) {
        super(gvrContext);

        this.title = title;

        GVRMesh sMesh = getGVRContext().createQuad(quadWidth, quadHeigth);

        WIDTH = (int) (100.0f * quadWidth);
        HEIGHT = (int) (100.0f * quadHeigth);

        attachRenderData(new GVRRenderData(gvrContext));
        getRenderData().setMaterial(
                new GVRMaterial(gvrContext, new ButtonShader(gvrContext).getShaderId()));
        getRenderData().setMesh(sMesh);

        createTextures();

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        getRenderData().setRenderingOrder(RenderingOrder.MOVE_BUTON);

        attachEyePointeeHolder();
    }

    private void createTextures() {

        Text text = new Text(title, Align.CENTER, 3f, Color.parseColor("#000000"),
                Color.parseColor("#ffffff"), 45);
        String font = "fonts/samsung-if-bold.ttf";

        GVRBitmapTexture bitmapIddle = new GVRBitmapTexture(getGVRContext(),
                create(getGVRContext().getContext(), WIDTH, HEIGHT, text, font));

        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_BACKGROUND_TEXTURE,
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.raw.empty)));
        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_TEXT_TEXTURE, bitmapIddle);

        text.textSize = 3.3f;

        GVRBitmapTexture bitmapHover = new GVRBitmapTexture(getGVRContext(),
                create(getGVRContext().getContext(), WIDTH, HEIGHT, text, font));

        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_BACKGROUND_TEXTURE,
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.raw.empty)));
        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_TEXT_TEXTURE, bitmapHover);

        GVRBitmapTexture bitmapSelected = new GVRBitmapTexture(getGVRContext(),
                create(getGVRContext().getContext(), WIDTH, HEIGHT, text, font));

        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_BACKGROUND_TEXTURE,
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.raw.empty)));
        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_TEXT_TEXTURE, bitmapSelected);
    }

    @Override
    protected void gainedFocus() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, HOVER_STATE);
    }

    @Override
    protected void lostFocus() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
    }

    @Override
    protected void singleTap() {
        super.singleTap();
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
    }

    public static Bitmap create(Context context, int width, int height, Text text, String font) {

        Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), font);

        Resources res = context.getResources();
        float scale = res.getDisplayMetrics().density;

        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        final Paint paint2 = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint2.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint2.setColor(text.backgroundColor);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint2);

        paint2.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint2);

        Paint paint = new Paint();
        paint.setTypeface(myTypeface/*
                                     * Typeface.create(Typeface.DEFAULT,
                                     * Typeface.BOLD)
                                     */);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Style.FILL);
        paint.setTextSize(text.textSize * scale);
        paint.setFakeBoldText(true);
        paint.setColor(text.textColor);
        paint.setFilterBitmap(true);

        Rect rectText = new Rect();
        paint.getTextBounds(text.text, 0, text.text.length(), rectText);

        canvas.drawText(text.text, width / 2 - rectText.exactCenterX(),
                height / 1.5f /*- rectText.exactCenterY()*/, paint);

        return bitmap;
    }

    @Override
    public void unselect() {
    }

    @Override
    public void select() {
    }
}
