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

public class Group extends X3DNode implements Cloneable
{

    private static final String TAG = Group.class.getSimpleName();

    private Shape mShape = null;

    public Group() {
    }

    public Group(String _DEF) {
        setDEF(_DEF);
    }

    public Group clone() throws
            CloneNotSupportedException
    {
        try {
            Group cloneObj = (Group) super.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide array of 3-tuple float results from inputOutput SFVec3f field named translation.
     * @return
     */
    public Shape getShape() {
        return mShape;
    }

    public void setShape(Shape _shape) {
        mShape = _shape;
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

} // end Group
