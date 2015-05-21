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

package org.gearvrf.scene_objects;

import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMaterial;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


public class GVRTextSceneObject extends GVRSceneObject {

    private static final String TAG = "GVRTextSceneObject";

    private int textColor;
    private float textSize;

    private Bitmap textBitmap = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Config.ARGB_8888);
    private Canvas canvas = new Canvas(textBitmap);
    private final Paint paint = new Paint();
    private final float defaultTextSize = paint.getTextSize();
    private GVRBitmapTexture texture = null;
    private String textString;
    private static final float BASE_TEXT_SIZE = 3f;
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 64;
    private static final int DEFAULT_COLOR = Color.GREEN;
    private static final float DEFAULT_QUAD_WIDTH = 2.0f;
    private static final float DEFAULT_QUAD_HEIGHT = 1.0f;
    private float quadWidth = DEFAULT_QUAD_WIDTH;
    private float quadHeight = DEFAULT_QUAD_HEIGHT;

    /**
     * Constructs a text scene object
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     */
    public GVRTextSceneObject(GVRContext gvrContext) {
        this(gvrContext, "TextSceneObject");
    }


    /**
     * Constructs a text scene object
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param string
     *            string to display
     */
    public GVRTextSceneObject(GVRContext gvrContext, String string) {
        super(gvrContext);

        createObject(gvrContext, quadWidth, quadHeight);
        setString(string);
        setTextColor(DEFAULT_COLOR);
        setTextSize(1);
    }


    /**
     * Constructs a text scene object
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param string
     *            string to display
     * @param objectWidth
     *            width of the quad scene object.
     * @param objectHeight
     *            height of the quad scene object.
     * @param canvasWidth
     *            width of the canvas to draw the text.
     * @param canvasHeight
     *            height of the canvas to draw the text.
     */
    public GVRTextSceneObject(GVRContext gvrContext, String string, float objectWidth, float objectHeight, int width, int height) {
        super(gvrContext);
        if(width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0.  Values passed were: width="+width+", height="+height);
        }

        createObject(gvrContext, objectWidth, objectHeight);
        textBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        canvas = new Canvas(textBitmap);
        setString(string);
        setTextColor(DEFAULT_COLOR);
        setTextSize(1);
    }

    private void createObject(GVRContext gvrContext,float width, float height) {
        quadWidth = width;
        quadHeight = height;

        GVRMesh mesh = gvrContext.createQuad(quadWidth, quadHeight);
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        GVRMaterial material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Unlit.ID);
        renderData.setMaterial(material);
        attachRenderData(renderData);
        renderData.setMesh(mesh);
    }

    /**
     * Get the text color.
     * 
     * @return The current text color, in Android {@link Color} format
     */
    public int getTextColor() {
        return textColor;
    }

    /**
     * Set the text color.
     * 
     * @param color
     *            The text color, in Android {@link Color} format. The
     *            {@linkplain Color#alpha(int) alpha component} is ignored.
     */
    public void setTextColor(int color) {
        textColor = color;
        paint.setColor(textColor);
        updateText();
    }

    /**
     * Get the current text size.
     * 
     * The default text size is somewhat bigger than the default Android
     * {@link Paint} text size: this method returns the current text as a
     * multiple of this component's default text size, not the standard Android
     * text size.
     * 
     * @return The current text size factor.
     */
    public float getTextSize() {
        return textSize;
    }

    /**
     * Set the text size.
     * 
     * @param newSize
     *            The new text size, as a multiple of the default text size.
     */
    public void setTextSize(float newSize) {
        textSize = newSize;
        paint.setTextSize(defaultTextSize * textSize * BASE_TEXT_SIZE);
        updateText();
    }

    /**
     * Get the current text string.
     * 
     * @return The current text string that is being displayed.
     */
    public String getString() {
        return textString;
    }

    /**
     * Set the text string to be displayed.
     * 
     * @param string
     *            The new text string to be displayed.
     */
    public void setString(String string) {
        textString = string;
        updateText();
    }

    private void updateText() {
        textBitmap.eraseColor(Color.TRANSPARENT);
        canvas.drawText(textString, 0, paint.getFontSpacing(), paint);
        setMainTexture();
    }

    private void setMainTexture() {
        boolean textureUpdated = false;
        if (texture != null) {
            textureUpdated = texture.update(textBitmap);
        }

        if (textureUpdated != true) {
            texture = new GVRBitmapTexture(getGVRContext(), textBitmap);
            getRenderData().getMaterial().setMainTexture(texture);
        }
    }
}
