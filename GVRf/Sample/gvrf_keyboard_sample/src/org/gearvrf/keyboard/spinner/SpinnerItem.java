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

package org.gearvrf.keyboard.spinner;

import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.keyboard.textField.Text;
import org.gearvrf.keyboard.textField.TextFieldItem;
import org.gearvrf.keyboard.util.GVRTextBitmapFactory;

public class SpinnerItem extends TextFieldItem {

    private boolean cacheTestOn = true;

    public SpinnerItem(GVRContext gvrContext, float sceneObjectWidth, float sceneObjectHeigth,
            int bitmapWidth, int bitmapHeigth, int position,
            Text text) {
        super(gvrContext, sceneObjectWidth, sceneObjectHeigth, bitmapWidth, bitmapHeigth, text,
                position);

    }

    @Override
    public void updateText(GVRContext context) {
        if (cacheTestOn) {

            GVRBitmapTexture tex = new GVRBitmapTexture(context, SpinnerItemFactory.getInstance(
                    getGVRContext()).getBitmap(charItem.getMode(),
                    charItem.getPosition()));
            getRenderData().getMaterial().setMainTexture(tex);

        } else {
            GVRBitmapTexture tex = new GVRBitmapTexture(context, GVRTextBitmapFactory.create(
                    context.getContext(), width, height, currentText, 0));
            getRenderData().getMaterial().setMainTexture(tex);
        }
    }

}
