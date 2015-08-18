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

package org.gearvrf.keyboard.util;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.textField.Text;

public class InteractiveText extends GVRSceneObject {

    public Text currentText;
    private int width, height;

    public InteractiveText(GVRContext gvrContext, int width, int height) {

        super(gvrContext, Util.convertPixelToVRFloatValue(width), Util
                .convertPixelToVRFloatValue(height),
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.raw.empty)));
        setName(SceneObjectNames.INTERACTIVE_TEXT);

        currentText = new Text();
        this.width = width;
        this.height = height;
        updateText(gvrContext);

    }

    public void updateText(GVRContext context) {

        GVRBitmapTexture tex = new GVRBitmapTexture(context, GVRTextBitmapFactory.create(width,
                height, currentText));
        getRenderData().getMaterial().setMainTexture(tex);
    }

    public void setTextAdditive(GVRContext context, String newText) {

        setText(context, currentText.text.concat(newText));
    }

    public void setText(final GVRContext context, final String newText) {

        context.runOnGlThread(new Runnable() {

            @Override
            public void run() {
                if (currentText.text == newText)
                    return;
                if (newText.length() > currentText.maxLength)
                    return;

                currentText.text = newText;
                updateText(context);

            }
        });

    }

}
