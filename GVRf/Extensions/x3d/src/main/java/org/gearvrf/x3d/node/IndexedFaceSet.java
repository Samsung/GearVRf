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
import org.gearvrf.x3d.data_types.MFInt32;
import org.gearvrf.x3d.data_types.SFBool;

public class IndexedFaceSet extends X3DNode  implements Cloneable
{

    private static final String TAG = IndexedFaceSet.class.getSimpleName();

    private Coordinate coordinate = null;
    private Normal normal = null;
    private TextureCoordinate textureCoordinate = null;
    private MFInt32 coordIndex = new MFInt32();
    private MFInt32 normalIndex = new MFInt32();
    private MFInt32 texCoordIndex = new MFInt32();
    private SFBool  solid = new SFBool(true);

    public IndexedFaceSet() {
    }

    public IndexedFaceSet clone() throws
            CloneNotSupportedException
    {
        try {
            IndexedFaceSet cloneObj = (IndexedFaceSet) super.clone();
            cloneObj.coordIndex = this.coordIndex.clone();
            cloneObj.normalIndex = this.normalIndex.clone();
            cloneObj.texCoordIndex = this.texCoordIndex.clone();
            cloneObj.coordinate = this.coordinate.clone();
            cloneObj.normal = this.normal.clone();
            cloneObj.textureCoordinate = this.textureCoordinate.clone();
            cloneObj.solid = this.solid.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide X3DCoordinateNode instance (using a properly typed node) from inputOutput SFNode field coord.
     * @return
     */
    public Coordinate getCoord() {
        return coordinate;
    }

    /**
     * Provide array of int results within allowed range of [-1,infinity) from initializeOnly MFInt32 field named coordIndex.
     * @return
     */
    public int[] getCoordIndex() {
        int[] valueDestination = coordIndex.getValue();
        //coordIndex.getValue( valueDestination );
        return valueDestination;
    }

    /**
     * Provide X3DNormalCoordinateNode instance (using a properly typed node) from inputOutput SFNode field normal.
     * @return
     */
    public Normal getNormal() {
        return normal;
    }

    /**
     * Provide array of int results within allowed range of [-1,infinity) from initializeOnly MFInt32 field named normalIndex.
     * @return
     */
    public int[] getNormalIndex() {
        int[] valueDestination = normalIndex.getValue();
        return valueDestination;
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
     * Provide X3DTextureCoordinateNode instance (using a properly typed node) from inputOutput SFNode field texCoord.
     * @return
     */
    public TextureCoordinate getTexCoord() {
        return textureCoordinate;
    }

    /**
     * Provide array of int results within allowed range of [-1,infinity) from initializeOnly MFInt32 field named texCoordIndex.
     * @return
     */
    public int[] getTexCoordIndex() {
        int[] valueDestination = texCoordIndex.getValue( );
        return valueDestination;
    }

    /**
     * Assign X3DCoordinateNode instance (using a properly typed node) to inputOutput SFNode field coord.
     * @param newValue
     */
    public void setCoord(Coordinate _coordinate) {
        coordinate = _coordinate;
    }

    public void setCoordValues(float[] newValue) {
        coordinate.setPoint( newValue );
    }

    /**
     * Assign int array within allowed range of [-1,infinity) to initializeOnly MFInt32 field named coordIndex.
     * @param newValue
     */
    public void setCoordIndex(int[] newValue) {
        coordIndex.setValue( newValue.length, newValue);
    }

    /**
     * Assign X3DNormalNode instance (using a properly typed node) to inputOutput SFNode field normal.
     * @param newValue
     */
    public void setNormal(Normal _normal) {
        normal = _normal;
    }

    public void setNormalValues(float[] newValue) {
        normal.setVector( newValue );
    }

    /**
     * Assign int array within allowed range of [-1,infinity) to initializeOnly MFInt32 field named normalIndex.
     * @param newValue
     */
    public void setNormalIndex(int[] newValue) {
        normalIndex.setValue( newValue.length, newValue);
    }

    /**
     * Assign boolean value to initializeOnly SFBool field named solid.
     * @param newValue
     */
    public void setSolid(boolean newValue) {
        this.solid.setValue( newValue );
    }

    public void setTetureCoordinate(TextureCoordinate _textureCoordinate) {
        this.textureCoordinate = _textureCoordinate;
    }
    /**
     * Assign X3DTextureCoordinateNode instance (using a properly typed node) to inputOutput SFNode field texCoord.
     * @param newValue
     */
    public void setTexureCoordValues(float[] newValue) {
        textureCoordinate.setPoint( newValue );
    }

    /**
     * Assign X3DTextureCoordinateNode instance (using a properly typed node) to inputOutput SFNode field texCoord.
     * @param newValue
     */
    public void setTexCoordIndex(int[] newValue) {
        texCoordIndex.setValue( newValue.length, newValue);
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

} // end IndexedFaceSet
