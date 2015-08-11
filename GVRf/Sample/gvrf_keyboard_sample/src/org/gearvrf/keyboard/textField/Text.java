
package org.gearvrf.keyboard.textField;

import android.graphics.Color;
import android.graphics.Paint.Align;

public class Text {

    public String text;
    public Align align;
    public int textSize;
    public int textColor;
    public int backgroundColor;
    public int maxLength;

    public Text() {
        align = Align.CENTER;
        textSize = 55;
        textColor = Color.WHITE;
        backgroundColor = Color.argb(0, 0, 0, 0);
        maxLength = 4;
        text = "";
    }

}
