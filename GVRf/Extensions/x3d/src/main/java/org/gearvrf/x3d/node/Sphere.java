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

public class Sphere extends X3DNode  implements Cloneable
{

    private static final String TAG = Sphere.class.getSimpleName();

    SFFloat radius = new SFFloat(1);
    SFBool solid = new SFBool(true);

    public Sphere() {
    }

    public Sphere(float _radius, boolean _solid) {
        setRadius(_radius);
        setSolid(_solid);
    }

    public Sphere(float _radius, boolean _solid, String _DEF) {
        setRadius(_radius);
        setSolid(_solid);
        setDEF(_DEF);
    }


    public Sphere clone() throws
            CloneNotSupportedException
    {
        try {
            Sphere cloneObj = (Sphere) super.clone();
            cloneObj.radius = this.radius.clone();
            cloneObj.solid = this.solid.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide float value within allowed range of (0,infinity) from initializeOnly SFFloat field named radius.
     * @return
     */
    public float getRadius() {
        return radius.getValue();
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
     * Assign float value within allowed range of (0,infinity) to initializeOnly SFFloat field named radius.
     * @param newValue
     */
    public void setRadius(float newValue) {
        if ( newValue < 0 ) newValue = 0;
        this.radius.setValue( newValue );
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

} // end Sphere
