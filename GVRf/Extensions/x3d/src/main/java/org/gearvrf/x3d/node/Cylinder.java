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
import org.gearvrf.x3d.data_types.SFFloat;

public class Cylinder extends X3DNode  implements Cloneable
{

    private static final String TAG = Cylinder.class.getSimpleName();

    SFBool bottom = new SFBool(true);
    SFFloat height = new SFFloat(2);
    SFFloat radius = new SFFloat(1);
    SFBool side = new SFBool(true);
    SFBool solid = new SFBool(true);
    SFBool top = new SFBool(true);

    public Cylinder() {
    }

    public Cylinder(boolean _bottom, float _height, float _radius,
                    boolean _side, boolean _solid, boolean _top) {
        setBottom( _bottom );
        setHeight(_height);
        setRadius(_radius);
        setSide(_side);
        setSolid(_solid);
        setTop(_top);
    }

    public Cylinder(boolean _bottom, float _height, float _radius,
                    boolean _side, boolean _solid, boolean _top, String _DEF) {
        setBottom( _bottom );
        setHeight(_height);
        setRadius(_radius);
        setSide(_side);
        setSolid(_solid);
        setTop(_top);
        setDEF(_DEF);
    }


    public Cylinder clone() throws
            CloneNotSupportedException
    {
        try {
            Cylinder cloneObj = (Cylinder) super.clone();
            cloneObj.bottom = this.bottom.clone();
            cloneObj.height = this.height.clone();
            cloneObj.radius = this.radius.clone();
            cloneObj.side = this.side.clone();
            cloneObj.solid = this.solid.clone();
            cloneObj.top = this.top.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide boolean value from initializeOnly SFBool field named bottom.
     * @return
     */
    public boolean getBottom() {
        return bottom.getValue();
    }

    /**
     * Provide float value within allowed range of (0,infinity) from initializeOnly SFFloat field named height..
     * @return
     */
    public float getHeight() {
        return height.getValue();
    }

    /**
     * Provide float value within allowed range of (0,infinity) from initializeOnly SFFloat field named radius.
     * @return
     */
    public float getRadius() {
        return radius.getValue();
    }

    /**
     * Provide boolean value from initializeOnly SFBool field named side.
     * @return
     */
    public boolean getSide() {
        return side.getValue();
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
     * Provide boolean value from initializeOnly SFBool field named top.
     * @return
     */
    public boolean getTop() {
        return top.getValue();
    }

    /**
     * Assign boolean value to initializeOnly SFBool field named bottom.
     * @param newValue
     */
    public void setBottom(boolean newValue) {
        this.bottom.setValue( newValue );
    }

    /**
     * Assign float value within allowed range of (0,infinity) to initializeOnly SFFloat field named height.
     * @param newValue
     */
    public void setHeight(float newValue) {
        if ( newValue < 0 ) newValue = 0;
        this.height.setValue(newValue);
    }

    /**
     * Assign float value within allowed range of (0,infinity) to initializeOnly SFFloat field named radius.
     * @param newValue
     */
    public void setRadius(float newValue) {
        if ( newValue < 0 ) newValue = 0;
        this.radius.setValue( newValue );
    }

    /**
     * Assign boolean value to initializeOnly SFBool field named side.
     * @param newValue
     */
    public void setSide(boolean newValue) {
        this.side.setValue(newValue);
    }

    /**
     * Assign boolean value to initializeOnly SFBool field named solid.
     * @param newValue
     */
    public void setSolid(boolean newValue) {
        this.solid.setValue( newValue );
    }

    /**
     * Assign boolean value to initializeOnly SFBool field named top.
     * @param newValue
     */
    public void setTop(boolean newValue) {
        this.top.setValue( newValue );
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

} // end Cylinder
