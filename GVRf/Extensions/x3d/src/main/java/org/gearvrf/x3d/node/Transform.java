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

import org.gearvrf.x3d.data_types.SFRotation;
import org.gearvrf.x3d.data_types.SFVec3f;

public class Transform extends Group implements Cloneable
{

    private static final String TAG = Transform.class.getSimpleName();

    SFVec3f center = new SFVec3f(0, 0, 0 );
    SFRotation rotation = new SFRotation(0, 0, 1, 0);
    SFRotation scaleOrientation = new SFRotation(0, 0, 1, 0 );
    SFVec3f scale = new SFVec3f(1, 1, 1 );
    SFVec3f translation = new SFVec3f( 0, 0, 0 );

    public Transform() {
    }

    public Transform(float[] _center, float[] _rotation, float[] _scale, float[] _scaleOrientation,
                     float[] _translation) {
        setCenter( _center );
        setRotation(_rotation);
        setScale(_scale);
        setScaleOrientation(_scaleOrientation);
        setTranslation(_translation);
    }

    public Transform(float[] _center, float[] _rotation, float[] _scale, float[] _scaleOrientation,
                     float[] _translation, String _DEF) {
        setCenter( _center );
        setRotation(_rotation);
        setScale(_scale);
        setScaleOrientation(_scaleOrientation);
        setTranslation(_translation);
        setDEF(_DEF);
    }

    public Transform clone() throws
            CloneNotSupportedException
    {
        try {
            Transform cloneObj = (Transform) super.clone();
            cloneObj.center = this.center.clone();
            cloneObj.rotation = this.rotation.clone();
            cloneObj.scaleOrientation = this.scaleOrientation.clone();
            cloneObj.scale = this.scale.clone();
            cloneObj.translation = this.translation.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide array of 3-tuple float results from inputOutput SFVec3f field named center.
     * @return
     */
    public float[] getCenter() {
        float[] _center = { this.center.x, this.center.y, this.center.z };
        return _center;
    }

    /**
     * Provide array of 4-tuple float results unit axis, angle (in radians) from inputOutput SFRotation field named rotation.
     * @return
     */
    public float[] getRotation() {
        float[] _rotation = {this.rotation.angle, this.rotation.x, this.rotation.y, this.rotation.z};
        return _rotation;
    }

    /**
     * Provide array of 3-tuple float results from inputOutput SFVec3f field named scale.
     * @return
     */
    public float[] getScale() {
        float[] _scale = { this.scale.x, this.scale.y, this.scale.z };
        return _scale;
    }

    /**
     * Provide array of 4-tuple float results unit axis, angle (in radians) from inputOutput SFRotation field named scaleOrientation.
     * @return
     */
    public float[] getScaleOrientation() {
        float[] _scaleOrientation = {this.scaleOrientation.angle, this.scaleOrientation.x, this.scaleOrientation.y, this.scaleOrientation.z};
        return _scaleOrientation;
    }

    /**
     * Provide array of 3-tuple float results from inputOutput SFVec3f field named translation.
     * @return
     */
    public float[] getTranslation() {
        float[] _translation = { this.translation.x, this.translation.y, this.translation.z };
        return _translation;
    }

    /**
     * Assign 3-tuple float array to inputOutput SFVec3f field named center.
     * @param newValue
     */
    public void setCenter(float[] newValue) {
        this.center.setValue( newValue[0], newValue[1], newValue[2] );
    }

    /**
     * Assign 4-tuple float array unit axis, angle (in radians) to inputOutput SFRotation field named rotation.
     * @param newValue
     */
    public void setRotation(float[] newValue) {
        this.rotation.setValue(newValue[3], newValue[0], newValue[1], newValue[2]);
    }

    /**
     * Assign 3-tuple float array to inputOutput SFVec3f field named scale.
     * @param newValue
     */
    public void setScale(float[] newValue) {
        this.scale.setValue( newValue[0], newValue[1], newValue[2] );
    }

    /**
     * Assign 4-tuple float array unit axis, angle (in radians) to inputOutput SFRotation field named scaleOrientation.
     * @param newValue
     */
    public void setScaleOrientation(float[] newValue) {
        this.scaleOrientation.setValue(newValue[3], newValue[0], newValue[1], newValue[2]);
    }

    /**
     * Assign 3-tuple float array to inputOutput SFVec3f field named translation.
     * @param newValue
     */
    public void setTranslation(float[] newValue) {
        this.translation.setValue( newValue[0], newValue[1], newValue[2] );
    }

} // end Transform
