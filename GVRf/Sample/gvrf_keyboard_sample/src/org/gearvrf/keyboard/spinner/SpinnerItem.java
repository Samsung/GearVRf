
package org.gearvrf.keyboard.spinner;

import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.keyboard.textField.Text;
import org.gearvrf.keyboard.textField.TextFieldItem;
import org.gearvrf.keyboard.util.GVRTextBitmapFactory;

public class SpinnerItem extends TextFieldItem {

    private boolean cacheTestOn = true;

    public SpinnerItem(GVRContext gvrContext, float sceneObjectWidth, float sceneObjectHeigth, int bitmapWidth, int bitmapHeigth, int position,
            Text text) {
        super(gvrContext, sceneObjectWidth, sceneObjectHeigth, bitmapWidth, bitmapHeigth, text, position);

    }

    @Override
    public void updateText(GVRContext context) {
        if (cacheTestOn) {

            GVRBitmapTexture tex = new GVRBitmapTexture(context, SpinnerItemFactory.getInstance(getGVRContext()).getBitmap(charItem.getMode(),
                    charItem.getPosition()));
            getRenderData().getMaterial().setMainTexture(tex);

        } else {
            GVRBitmapTexture tex = new GVRBitmapTexture(context, GVRTextBitmapFactory.create(context.getContext(), width, height, currentText, 0));
            getRenderData().getMaterial().setMainTexture(tex);
        }
    }

}
