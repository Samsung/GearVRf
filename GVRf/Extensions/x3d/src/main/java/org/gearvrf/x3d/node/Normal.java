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
import org.gearvrf.x3d.data_types.MFVec3f;

public class Normal extends X3DNode  implements Cloneable
{

    private static final String TAG = Normal.class.getSimpleName();

    private MFVec3f vector = new MFVec3f();
    private float[] meshCreatorInputNormals;

    public Normal() {
    }

    public Normal(float[] _point) {
        setVector( _point );
    }

    public Normal(float[] _point, String _DEF) {
        setVector( _point );
        setDEF(_DEF);
    }


    public Normal clone() throws
            CloneNotSupportedException
    {
        try {
            Normal cloneObj = (Normal) super.clone();
            cloneObj.vector = this.vector.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide array of 3-tuple float results from inputOutput MFVec3f field named vector.
     * @return
     */
    public float[] getVector() {

        float[] valueDestination = null;
        vector.getValue( valueDestination );
        return valueDestination;
    }

    /**
     * Assign 3-tuple float array within allowed range of [-1,1] to inputOutput MFVec3f field named vector.
     * @param newValue
     */
    public void setVector(float[] newValue) {
        vector.setValue( newValue.length, newValue );
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

    /**
     * Save the list of coordinates which has been reorganized by the X3D meshCreator
     * @param values
     */
    public void setMeshCreatorInputNormals(float[] values) {
        meshCreatorInputNormals = new float[ values.length ];
        for (int i = 0; i < values.length; i++) {
            meshCreatorInputNormals[i] = values[i];
        }
    }

    /**
     * Return the reorganized coordinates from the meshCreator
     * @return
     */
    public float[] getMeshCreatorInputNormals() {
        return meshCreatorInputNormals;
    }

} // end Normal
