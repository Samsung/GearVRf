
package org.gearvrf.keyboard.keyboard.numeric;

import android.content.res.Resources;
import android.content.res.TypedArray;

import org.gearvrf.GVRContext;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.keyboard.model.KeyboardBase;
import org.gearvrf.keyboard.keyboard.model.KeyboardItemStyle;
import org.gearvrf.keyboard.keyboard.model.KeyboardLine;
import org.gearvrf.keyboard.keyboard.model.KeyboardSoftItem;
import org.gearvrf.keyboard.model.KeyboardCharItem;
import org.gearvrf.keyboard.util.SceneObjectNames;

/**
 * @author Douglas
 */
public class NumericKeyboard extends KeyboardBase {

    private Resources res = null;
    private static final String RESOURCE_TYPE = "array";
    private GVRContext gvrContext;
    private int notFoundResource = -1;

    public NumericKeyboard(GVRContext gvrContext) {
        super(gvrContext);
        setName(SceneObjectNames.KEYBOARD_NUMERIC);

        this.gvrContext = gvrContext;

        res = gvrContext.getContext().getResources();
        TypedArray softKeyboard = res.obtainTypedArray(R.array.new_numeric_lines);

        int n = softKeyboard.length();

        for (int lineIndex = 0; lineIndex < n; ++lineIndex) {

            int lineId = softKeyboard.getResourceId(lineIndex, notFoundResource);
            int keyId = getResourceId(res.getResourceEntryName(lineId), RESOURCE_TYPE, gvrContext.getContext().getPackageName());

            parserLinesArray(keyId, lineIndex);
        }

        softKeyboard.recycle();
    }

    private void parserLinesArray(int keyId, int lineIndex2) {

        TypedArray keys = res.obtainTypedArray(keyId);
        int linesLenght = keys.length();

        KeyboardLine mKeyboardLine = new KeyboardLine(this.getGVRContext());

        for (int lineIndex = 0; lineIndex < linesLenght; ++lineIndex) {

            int idRow = keys.getResourceId(lineIndex, notFoundResource);
            TypedArray key = res.obtainTypedArray(idRow);

            String character = key.getString(0);
            KeyboardItemStyle style = getStyleFromTypedArray(key);

            mKeyboardLine.addItemKeyboard(new KeyboardSoftItem(getGVRContext(), new KeyboardCharItem(character, ""), style));

            key.recycle();
        }

        mKeyboardLine.alingCenter(lineIndex2);
        addLine(mKeyboardLine);

        keys.recycle();
    }

    private KeyboardItemStyle getStyleFromTypedArray(TypedArray key) {

        TypedArray styles = res.obtainTypedArray(key.getResourceId(1, notFoundResource));
        KeyboardItemStyle keyStyle = new KeyboardItemStyle(this.gvrContext.getContext(), styles);

        return keyStyle;
    }

    public int getResourceId(String resourceName, String resourceType, String packageName) {

        try {
            return res.getIdentifier(resourceName, resourceType, packageName);
        } catch (Exception e) {
            return notFoundResource;
        }
    }
}
