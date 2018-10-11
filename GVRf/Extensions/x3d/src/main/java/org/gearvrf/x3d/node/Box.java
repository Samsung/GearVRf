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
import org.gearvrf.x3d.data_types.SFBool;
import org.gearvrf.x3d.data_types.SFVec3f;

public class Box extends X3DNode implements Cloneable
{

    private static final String TAG = Box.class.getSimpleName();

    SFVec3f size = new SFVec3f(2, 2, 2);
    SFBool solid = new SFBool(true);

    public Box() {
    }

    public Box(float[] _size, boolean _solid) {
        setSize(_size);
        setSolid(_solid);
    }

    public Box(float[] _size, boolean _solid, String _DEF) {
        setSize(_size);
        setSolid(_solid);
        setDEF(_DEF);
    }


    public Box clone() throws
            CloneNotSupportedException
    {
        try {
            Box cloneObj = (Box) super.clone();
            cloneObj.size = this.size.clone();
            cloneObj.solid = this.solid.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide float array value from initializeOnly SFFloat field named size.
     * @return
     */
    public float[] getSize() {
        return size.getValue();
    }

    /**
     * Provide boolean value from initializeOnly SFBool field named solid.
     * Determines if normals are reversed so object is inside-out
     * @return
     */
    public boolean getSolid() {
        return solid.getValue();
    }

    /**
     * Assign float[] value to initializeOnly SFFloat field named size.
     * @param newValue
     */
    public void setSize(float[] newValue) {
        this.size.setValue(newValue);
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

} // end Box
