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

public class ImageTexture extends X3DNode implements Cloneable
{

    private static final String TAG = ImageTexture.class.getSimpleName();

    MFString url = new MFString();
    SFBool repeatS = new SFBool(true); // not currently supported
    SFBool repeatT = new SFBool(true); // not currently supported

    public ImageTexture() {
    }

    public ImageTexture(String[] _url, boolean _repeatS, boolean _repeatT) {
        setUrl( _url );
        setRepeatS(_repeatS);
        setRepeatT(_repeatT);
    }

    public ImageTexture(String[] _url, boolean _repeatS, boolean _repeatT,
                    String _DEF) {
        setUrl( _url );
        setRepeatS(_repeatS);
        setRepeatT(_repeatT);
        setDEF(_DEF);
    }


    public ImageTexture clone() throws
            CloneNotSupportedException
    {
        try {
            ImageTexture cloneObj = (ImageTexture) super.clone();
            cloneObj.url = this.url.clone();
            cloneObj.repeatS = this.repeatS.clone();
            cloneObj.repeatT = this.repeatT.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide array of String results from inputOutput MFString field named url.
     * @array saved in valueDestination
     */
    public String[] getUrl() {
        String[] valueDestination = new String[ url.size() ];
        this.url.getValue(valueDestination);
        return valueDestination;
    }

    /**
     * Provide boolean value from initializeOnly SFBool field named repeatS.
     */
    public boolean getRepeatS() {
        return this.repeatS.getValue();
    }

    /**
     * Provide boolean value from initializeOnly SFBool field named repeatT.
     */
    public boolean getRepeatT() {
        return this.repeatS.getValue();
    }

    /**
     * Assign String array to inputOutput MFString field named url.
     * @param newValue
     */
    public void setUrl(String[] newValue) {
        this.url.setValue(newValue.length, newValue );
    }

    /**
     * Assign boolean value to initializeOnly SFBool field named repeatS.
     * @param newValue
     */
    public void setRepeatS(boolean newValue) {
        this.repeatS.setValue( newValue );
    }

    /**
     * Assign boolean value to initializeOnly SFBool field named repeatT.
     * @param newValue
     */
    public void setRepeatT(boolean newValue) {
        this.repeatT.setValue( newValue );
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

} // end ImageTexture
