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
import org.gearvrf.x3d.data_types.SFString;

public class FontStyle extends X3DNode implements Cloneable
{

    private static final String TAG = FontStyle.class.getSimpleName();

    MFString family = new MFString("SERIF");
    SFBool horizontal = new SFBool(true); // not currently supported
    MFString justify = new MFString("BEGIN");
    SFString language = new SFString(""); // not currently supported
    SFBool leftToRight = new SFBool(true); // not currently supported
    SFFloat size = new SFFloat(10);
    SFFloat spacing = new SFFloat(1);
    SFString style = new SFString("PLAIN");
    SFBool topToBottom = new SFBool(true); // not currently supported

    public FontStyle() {
    }

    public FontStyle(String[] _family, String[] _justify, float _size,
                    float _spacing, String _style) {
        setFamily( _family );
        setJustify(_justify);
        setSize(_size);
        setSpacing(_spacing);
        setStyle(_style);
    }

    public FontStyle(String[] _family, String[] _justify, float _size,
                    float _spacing, String _style, String _DEF) {
        setFamily( _family );
        setJustify(_justify);
        setSize(_size);
        setSpacing(_spacing);
        setStyle(_style);
        setDEF(_DEF);
    }


    public FontStyle clone() throws
            CloneNotSupportedException
    {
        try {
            FontStyle cloneObj = (FontStyle) super.clone();
            cloneObj.family = this.family.clone();
            cloneObj.justify = this.justify.clone();
            cloneObj.size = this.size.clone();
            cloneObj.spacing = this.spacing.clone();
            cloneObj.style = this.style.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide array of String enumeration results ['"SANS"'|'"SERIF"'|'"TYPEWRITER"'|'"etc."'] from initializeOnly MFString field named family.
     * @array saved in valueDestination
     */
    public String[] getFamily() {
        String[] valueDestination = new String[ family.size() ];
        this.family.getValue(valueDestination);
        return valueDestination;
    }

    /**
     * Provide array of String enumeration results ['"MIDDLE" | "BEGIN" | "END" | "FIRST"'] from initializeOnly MFString field named justify.
     * @array saved in valueDestination
     */
    public String[] getJustify() {
        String[] valueDestination = new String[ justify.size() ];
        this.justify.getValue(valueDestination);
        return valueDestination;
    }

    /**
     * Provide float value within allowed range of (0,infinity) from initializeOnly SFFloat field named size.
     * Tooltip: (0,+infinity) Nominal height (in local coordinate system) of text glyphs Also sets default spacing between adjacent lines of text. *
     * @return value of size field
     */
    public float getSize() {
        return size.getValue();
    }

    /**
     * Provide float value within allowed range of [0,infinity) from initializeOnly SFFloat field named spacing.
     * @return
     */
    public String getStyle() {
        return style.getValue();
    }

    /**
     * Provide float value within allowed range of [0,infinity) from initializeOnly SFFloat field named spacing.
     * @return
     */
    public float getSpacing() {
        return spacing.getValue();
    }

    /**
     * Assign String enumeration array (""SANS""|""SERIF""|""TYPEWRITER"") ['"SANS"'|'"SERIF"'|'"TYPEWRITER"'|'"etc."'] to initializeOnly MFString field named family.
     * @param newValue
     */
    public void setFamily(String[] newValue) {
        this.family.setValue(newValue.length, newValue );
    }

    /**
     * Assign String enumeration array ['"MIDDLE" | "BEGIN" | "END" | "FIRST"'].
     * @param newValue
     */
    public void setJustify(String[] newValue) {
        this.justify.setValue( newValue.length, newValue );
    }

    /**
     * Assign float value within allowed range of (0,infinity) to initializeOnly SFFloat field named size.
     * @param newValue
     */
    public void setSize(float newValue) {
        if ( newValue < 0 ) newValue = 0;
        this.size.setValue(newValue);
    }

    /**
     * Assign float value within allowed range of [0,infinity) to initializeOnly SFFloat field named spacing.
     * @param newValue
     */
    public void setSpacing(float newValue) {
        this.spacing.setValue(newValue);
    }

    /**
     * Assign String enumeration value ("PLAIN"|"BOLD"|"ITALIC"|"BOLDITALIC") ['PLAIN'|'BOLD'|'ITALIC'|'BOLDITALIC'] to initializeOnly SFString field named style.
     * @param newValue
     */
    public void setStyle(String newValue) {
        this.style.setValue( newValue );
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

} // end FontStyle
