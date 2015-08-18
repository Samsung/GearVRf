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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.model.CharItem;
import org.gearvrf.keyboard.util.GVRTextBitmapFactory;
import org.gearvrf.keyboard.util.SceneObjectNames;

public class TextFieldItem extends GVRSceneObject {

    public Text currentText;
    protected int width;
    protected int height;
    protected CharItem charItem;
    private int position;

    public TextFieldItem(GVRContext gvrContext, float sceneObjectWidth, float sceneObjectHeigth,
            int bitmapWidth, int bitmapHeigth, Text text,
            int position) {
        super(gvrContext, sceneObjectWidth, sceneObjectHeigth, gvrContext
                .loadTexture(new GVRAndroidResource(gvrContext, R.raw.empty)));
        setName(SceneObjectNames.TEXT_FIELD_ITEM);

        currentText = text;
        this.width = bitmapWidth;
        this.height = bitmapHeigth;
        updateText(gvrContext);
        this.position = position;
    }

    public int getPosition() {
        return this.position;
    }

    public void updateText(GVRContext context) {
        GVRBitmapTexture tex = new GVRBitmapTexture(context, GVRTextBitmapFactory.create(
                context.getContext(), width, height, currentText, 0));
        getRenderData().getMaterial().setMainTexture(tex);
    }

    public void setTextAdditive(GVRContext context, String newText) {
        setText(context, currentText.text.concat(newText));
    }

    public void setText(final GVRContext context, final String newText) {

        context.runOnGlThread(new Runnable() {

            @Override
            public void run() {

                if (currentText.text == newText) {
                    return;
                }

                if (newText.length() > currentText.maxLength) {
                    return;
                }

                currentText.text = newText;
                updateText(context);
            }
        });
    }

    public void removeCharacter(GVRContext context) {

        if (currentText.text.length() <= 1)
            setText(context, "");
        else if (currentText.text.length() > 1) {
            setText(context, currentText.text.substring(0, currentText.text.length() - 1));
        }
    }

    public void setText(GVRContext gvrContext, CharItem charItem) {
        setText(gvrContext, charItem.getCharacter());
        this.charItem = charItem;
    }

    public CharItem getCharItem() {
        return charItem;
    }

}
