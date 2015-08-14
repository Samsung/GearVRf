
package org.gearvrf.controls.util;

import android.graphics.Paint.Align;

public class Text {

    public String text;
    public Align align;
    public int textSize;
    public int textColor;
    public int backgroundColor;
    public int maxLength;

    public Text(String text, Align align, int textSize, int textColor, int backgroundColor, int maxLength) {
        this.align = align;
        this.textSize = textSize;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.maxLength = maxLength;
        this.text = text;
    }
}
