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

package org.gearvrf.debug;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRCamera;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCustomPostEffectShaderId;
import org.gearvrf.GVRPostEffect;
import org.gearvrf.GVRPostEffectMap;
import org.gearvrf.GVRPostEffectShaderId;
import org.gearvrf.GVRPostEffectShaderManager;
import org.gearvrf.debug.GVRConsole.EyeMode;

import org.gearvrf.R;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

/**
 * A debugging console for VR apps.
 * 
 * Lets you put text messages on both or either eye. This can be a useful way to
 * get feedback on your code as you exercise it, instead of having to use
 * {@code logcat} asynchronously.
 * 
 * <p>
 * You can have multiple consoles. The point of this is to let you write
 * different messages to the left eye than to the right eye: putting multiple
 * consoles on the same eye <em>will</em> make both hard/impossible to read.
 * 
 * <p>
 * <b>Known Limitations:</b>
 * <ul>
 * <li>Is implemented as a "post effect" so the messages are written before lens
 * distortion is applied.
 * <li>Does not do line wrap. Long messages will be truncated.
 * <li>Does not support Java escape characters like \n or \t.
 * </ul>
 */
public class GVRConsole extends GVRPostEffect {

    /**
     * Specify where the message(s) are displayed. You pass an {@code EyeMode}
     * value to {@linkplain GVRConsole#GVRConsole(GVRContext, EyeMode) the
     * constructor} but can change it <i>via</i>
     * {@link GVRConsole#setEyeMode(EyeMode)}
     */
    public enum EyeMode {
        /** This console's messages will be displayed on the left eye */
        LEFT_EYE,
        /** This console's messages will be displayed on the right eye */
        RIGHT_EYE,
        /** This console's messages will be displayed on both eyes */
        BOTH_EYES,
        /**
         * This console's messages will not be displayed. Specifying
         * {@link #NEITHER_EYE} means that you do have a component updating a
         * console texture every time you call
         * {@link GVRConsole#writeLine(String, Object...) writeLine()}, but that
         * component is <em>not</em> in the render pipeline, so does not add to
         * render costs.
         */
        NEITHER_EYE
    };

    private EyeMode eyeMode;
    private int textColor;
    private float textSize;

    private final List<String> lines = new ArrayList<String>();
    private final Bitmap HUD = Bitmap.createBitmap(HUD_WIDTH, HUD_HEIGHT,
            Config.ARGB_8888);
    private final Canvas canvas = new Canvas(HUD);
    private final Paint paint = new Paint();
    private final float defaultTextSize = paint.getTextSize();
    private GVRBitmapTexture texture = null;

    /**
     * Create a console, specifying the initial eye mode.
     * 
     * @param gvrContext
     *            The GVR context.
     * @param startMode
     *            The initial eye mode; you can change this <i>via</i>
     *            {@link #setEyeMode(EyeMode)}
     */
    public GVRConsole(GVRContext gvrContext, EyeMode startMode) {
        super(gvrContext, getShaderId(gvrContext));
        setEyeMode(startMode);
        setMainTexture();

        setTextColor(DEFAULT_COLOR);
        setTextSize(1);
    }

    /**
     * Write a message to the console.
     * 
     * @param pattern
     *            A {@link String#format(String, Object...)} pattern
     * @param parameters
     *            Optional parameters to plug into the pattern
     */
    public void writeLine(String pattern, Object... parameters) {
        String line = (parameters == null || parameters.length == 0) ? pattern
                : String.format(pattern, parameters);
        lines.add(0, line); // we'll write bottom to top, then purge unwritten
                            // lines from end
        updateHUD();
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
    }

    /**
     * Get the current eye mode, or where the console messages are displayed.
     * 
     * This may be the value passed to
     * {@linkplain #GVRConsole(GVRContext, EyeMode) the constructor,} but you
     * can also change that at any time with {@link #setEyeMode(EyeMode)}.
     * 
     * @return The current eye mode.
     */
    public EyeMode getEyeMode() {
        return eyeMode;
    }

    /**
     * Set the current eye mode, or where the console messages are displayed.
     * 
     * Always 'edits' the list of post-effects; setting the mode to
     * {@link EyeMode#NEITHER_EYE} means this component will not affect render
     * times at all.
     * 
     * @param newMode
     *            Left, right, both, or neither.
     */
    public void setEyeMode(EyeMode newMode) {
        eyeMode = newMode;

        GVRCameraRig cameraRig = getGVRContext().getMainScene()
                .getMainCameraRig();
        GVRCamera leftCamera = cameraRig.getLeftCamera(), rightCamera = cameraRig
                .getRightCamera();

        // Remove from both (even if not present) add back later
        leftCamera.removePostEffect(this);
        rightCamera.removePostEffect(this);

        if (eyeMode == EyeMode.LEFT_EYE || eyeMode == EyeMode.BOTH_EYES) {
            leftCamera.addPostEffect(this);
        }
        if (eyeMode == EyeMode.RIGHT_EYE || eyeMode == EyeMode.BOTH_EYES) {
            rightCamera.addPostEffect(this);
        }
    }

    private static void log(String TAG, String pattern, Object... parameters) {
        Log.d(TAG, String.format(pattern, parameters));
    }

    private void updateHUD() {
        // TODO Line wrap!

        HUD.eraseColor(Color.TRANSPARENT);
        float textHeight = paint.getFontSpacing();
        int rowsOnScreen = (int) (HUD_HEIGHT / textHeight);
        float baseLine = HUD.getHeight()
                - Math.max(0, rowsOnScreen - lines.size()) * textHeight;
        log("updateHUD",
                "textHeight = %.2f, rowsOnScreen = %d, lines.size() = %d, baseLine = %.2f",
                textHeight, rowsOnScreen, lines.size(), baseLine);
        int written = 0;
        for (String line : lines) {
            canvas.drawText(line, 0, baseLine - textHeight + TOP_FUDGE, paint);
            written += 1;
            baseLine -= textHeight;
            if (baseLine < textHeight) {
                break;
            }
        }
        for (int index = lines.size() - 1; index > written; --index) {
            lines.remove(index);
        }

        setMainTexture();
    }

    private void setMainTexture() {

        boolean textureUpdated = false;
        if (texture != null) {
            textureUpdated = texture.update(HUD);
        }

        if (textureUpdated != true) {
            texture = new GVRBitmapTexture(getGVRContext(), HUD);
            setMainTexture(texture);
        }
    }

    private static synchronized GVRPostEffectShaderId getShaderId(
            GVRContext gvrContext) {
        if (shaderId == null) {
            GVRPostEffectShaderManager shaderManager = gvrContext
                    .getPostEffectShaderManager();
            shaderId = shaderManager.addShader(R.raw.posteffect_quad,
                    R.raw.hud_console);

            shaderMap = shaderManager.getShaderMap(shaderId);
            shaderMap.addTextureKey("u_overlay", MAIN_TEXTURE);
        }
        return shaderId;
    }

    private static GVRCustomPostEffectShaderId shaderId;
    private static GVRPostEffectMap shaderMap;

    private static final float BASE_TEXT_SIZE = 3f;
    private static final int HUD_HEIGHT = 1024;
    private static final int HUD_WIDTH = 1024;
    private static final int DEFAULT_COLOR = Color.GREEN;
    /**
     * The baseline calculation seems right ... looks like some of the scene is
     * getting stenciled out
     */
    private static final float TOP_FUDGE = 20;
}
