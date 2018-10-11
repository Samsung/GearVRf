/* Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf.x3d.node;

import org.gearvrf.utility.Log;
import org.gearvrf.x3d.data_types.MFString;
import org.gearvrf.x3d.data_types.SFBool;
import org.gearvrf.x3d.data_types.SFFloat;

public class Text extends X3DNode  implements Cloneable
{

    private static final String TAG = Text.class.getSimpleName();

    FontStyle fontStyle = new FontStyle();
    SFFloat length = new SFFloat(0);
    SFFloat maxExtent = new SFFloat(0);
    MFString string = new MFString("");
    SFBool solid = new SFBool(false); // not currently supported

    public Text() {
    }

    public Text(FontStyle _fontStyle, float _length, float _maxExtent,
                    String[] _string, boolean _solid) {
        setFontStyle( _fontStyle );
        setLength( _length );
        setMaxExtent(_maxExtent);
        setString(_string);
        setSolid(_solid);
    }

    public Text(FontStyle _fontStyle, float _length, float _maxExtent,
                    String[] _string, boolean _solid, String _DEF) {
        setFontStyle( _fontStyle );
        setLength( _length );
        setMaxExtent(_maxExtent);
        setString(_string);
        setSolid(_solid);
        setDEF(_DEF);
    }


    public Text clone() throws
            CloneNotSupportedException
    {
        try {
            Text cloneObj = (Text) super.clone();
            cloneObj.fontStyle.family = this.fontStyle.family.clone();
            cloneObj.fontStyle.justify = this.fontStyle.justify.clone();
            cloneObj.length = this.length.clone();
            cloneObj.maxExtent = this.maxExtent.clone();
            cloneObj.string = this.string.clone();
            cloneObj.solid = this.solid.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
            Log.e(TAG, "<Text> clone error: " + e);
        }
        catch (Exception e) {
            Log.e(TAG, "<Text> Exception error: " + e);
        }
        return null;
    }

    /**
     * Provide X3DFontStyleNode instance (using a properly typed node) from inputOutput SFNode field fontStyle.
     * @return
     */
    public FontStyle getFontStyle() {
        return fontStyle;
    }

    /**
     * Provide array of float results within allowed range of [0,infinity) from inputOutput MFFloat field named length.
     * Tooltip: Array of length values for each text string in the local coordinate system. Each string is stretched or compressed to fit. *
     * @return value of length field
     */
    public float getLength() {
        return length.getValue();
    }

    /**
     * Provide float value within allowed range of [0,infinity) from inputOutput SFFloat field named maxExtent.
     * Tooltip: Limits/compresses all text strings if max string length is longer than maxExtent, as measured in local coordinate system. *
     * @return value of maxExtent field
     */
    public float getMaxExtent() {
        return maxExtent.getValue();
    }

    /**
     * Provide array of String results from inputOutput MFString field named string.
     * @return value of string field
     */
    public String[] getString() {
        String[] valueDestination = new String[ string.size() ];
        this.string.getValue(valueDestination);
        return valueDestination;
    }

    /**
     * Assign X3DFontStyleNode instance (using a properly typed node) to inputOutput SFNode field fontStyle.
     * @param newValue
     */
    public void setFontStyle(FontStyle newValue) {
        this.fontStyle = newValue;
    }

    /**
     * Assign float array within allowed range of [0,infinity) to inputOutput MFFloat field named length.
     * Tooltip: Array of length values for each text string in the local coordinate system. Each string is stretched or compressed to fit.
     * @param newValue
     */
    public void setLength(float newValue) {
        if ( newValue < 0 ) newValue = 0;
        this.length.setValue( newValue );
    }

    /**
     * Assign float value within allowed range of [0,infinity) to inputOutput SFFloat field named maxExtent.
     * @param newValue
     */
    public void setMaxExtent(float newValue) {
        if ( newValue < 0 ) newValue = 0;
        this.maxExtent.setValue(newValue);
    }

    /**
     * Assign float value within allowed range of [0,infinity) to initializeOnly SFFloat field named spacing.
     * @param newValue
     */
    public void setString(String[] newValue) {
        if ( newValue != null ) {
            this.string.setValue(newValue.length, newValue);
        }
    }

    /**
     * Assign boolean value to initializeOnly SFBool field named solid.
     * @param newValue
     */
    public void setSolid(boolean newValue) {
        this.solid.setValue( newValue );
    }

    /**
     * Assign String value to inputOutput SFString field named DEF.
     * @param newValue
     */
    public void setDEF(String newValue) {
        super.setDEF(newValue);
    }

    /**
     * Assign String value to inputOutput SFString field named USE.
     * @param newValue
     */
    public void setUSE(String newValue) {
        super.setUSE(newValue);
    }

} // end Text
