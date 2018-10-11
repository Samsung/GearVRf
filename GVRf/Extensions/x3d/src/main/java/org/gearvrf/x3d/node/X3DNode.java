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

import org.gearvrf.x3d.data_types.SFString;

public class X3DNode implements Cloneable
{

    private static final String TAG = X3DNode.class.getSimpleName();
    private SFString name = new SFString();

    public X3DNode()
    {
    }

    /**
     * Constructor
     * @param newValue
     */
    public X3DNode( String newValue )
    {
        name.setValue( newValue );
    }

    public X3DNode clone() throws
            CloneNotSupportedException
    {
        try {
            X3DNode cloneObj = (X3DNode) super.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Returns SFString field named DEF.
     */
    public SFString getDEF() {
        return name;
    }

    /**
     * Assign String value to inputOutput SFString field named DEF.
     */
    public void setDEF( String newValue ) {
        if (newValue != null) name.setValue( newValue );
    }

    /**
     * Assign String value to inputOutput SFString field named USE.
     */
    public void setUSE( String newValue ) {
        //TODO make setUse work.
    }

} // end X3DNode
