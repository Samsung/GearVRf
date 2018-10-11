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
import org.gearvrf.x3d.data_types.SFFloat;
import org.gearvrf.x3d.data_types.SFVec2f;

public class TextureTransform extends X3DNode implements Cloneable
{

    private static final String TAG = TextureTransform.class.getSimpleName();

    SFVec2f center = new SFVec2f(0, 0);
    SFFloat rotation = new SFFloat(0);
    SFVec2f scale = new SFVec2f(1, 1);
    SFVec2f translation = new SFVec2f(0, 0);

    public TextureTransform() {
    }

    public TextureTransform(float[] _center, float _rotation, float[] _scale, float[] _translation ) {
        setCenter( _center );
        setRotation(_rotation);
        setScale(_scale);
        setTranslation(_translation);
    }

    public TextureTransform(float[] _center, float _rotation, float[] _scale, float[] _translation,
                    String _DEF) {
        setCenter( _center );
        setRotation(_rotation);
        setScale(_scale);
        setTranslation(_translation);
        setDEF(_DEF);
    }


    public TextureTransform clone() throws
            CloneNotSupportedException
    {
        try {
            TextureTransform cloneObj = (TextureTransform) super.clone();
            cloneObj.center = this.center.clone();
            cloneObj.rotation = this.rotation.clone();
            cloneObj.scale = this.scale.clone();
            cloneObj.translation = this.translation.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide array of 2-tuple float results from inputOutput SFVec2f field named center.
     * @float
     */
    public float[] getCenter() {
        return this.center.getValue();
    }

    /**
     * Provide float value unit axis, angle (in radians) from inputOutput SFFloat field named rotation.
     */
    public float getRotation() {
        return this.rotation.getValue();
    }

    /**
     * Provide array of 2-tuple float results from inputOutput SFVec2f field named scale.
     * @float
     */
    public float[] getScale() {
        return this.scale.getValue();
    }

    /**
     * Provide array of 2-tuple float results from inputOutput SFVec2f field named translation.
     * @float
     */
    public float[] getTranslation() {
        return this.translation.getValue();
    }


    /**
     * Assign String array to inputOutput MFString field named url.
     * @param newValue
     */
    public void setCenter(float[] newValue) {
        this.center.setValue( newValue );
    }

    /**
     * Assign float value unit axis, angle (in radians) to inputOutput SFFloat field named rotation.
     * @param newValue
     */
    public void setRotation(float newValue) {
        this.rotation.setValue( newValue );
    }

    /**
     * Assign 2-tuple float array to inputOutput SFVec2f field named scale.
     * @param newValue
     */
    public void setScale(float[] newValue) {
        this.scale.setValue( newValue );
    }

    /**
     * Assign 2-tuple float array to inputOutput SFVec2f field named translation.
     * @param newValue
     */
    public void setTranslation(float[] newValue) {
        this.translation.setValue( newValue );
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

} // end TextureTransform
