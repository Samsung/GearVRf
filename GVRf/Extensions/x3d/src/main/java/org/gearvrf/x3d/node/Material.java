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

import org.gearvrf.x3d.data_types.SFColor;
import org.gearvrf.x3d.data_types.SFFloat;

public class Material extends X3DNode implements Cloneable
{

    private static final String TAG = Material.class.getSimpleName();
    private static float AMBIENTINTENSITY_DEFAULT_VALUE = 0.2f;
    private static float[] DIFFUSECOLOR_DEFAULT_VALUE = {0.8f, 0.8f, 0.8f};
    private static float[] EMISSIVECOLOR_DEFAULT_VALUE = {0.0f, 0.0f, 0.0f};
    private static float SHININESS_DEFAULT_VALUE = 0.2f;
    private static float[] SPECULARCOLOR_DEFAULT_VALUE = {0.0f, 0.0f, 0.0f};
    private static float TRANSPARENCY_DEFAULT_VALUE = 0.0f;

    SFFloat ambientIntensity = new SFFloat( AMBIENTINTENSITY_DEFAULT_VALUE );
    SFColor diffuseColor = new SFColor(DIFFUSECOLOR_DEFAULT_VALUE[0], DIFFUSECOLOR_DEFAULT_VALUE[1],  DIFFUSECOLOR_DEFAULT_VALUE[2]  );
    SFColor emissiveColor = new SFColor(EMISSIVECOLOR_DEFAULT_VALUE[0], EMISSIVECOLOR_DEFAULT_VALUE[1], EMISSIVECOLOR_DEFAULT_VALUE[2] );
    SFFloat shininess = new SFFloat( SHININESS_DEFAULT_VALUE );
    SFColor specularColor = new SFColor(SPECULARCOLOR_DEFAULT_VALUE[0], SPECULARCOLOR_DEFAULT_VALUE[1], SPECULARCOLOR_DEFAULT_VALUE[2] );
    SFFloat transparency = new SFFloat( TRANSPARENCY_DEFAULT_VALUE );


    public Material() {
    }

    public Material(float ambientIntensity, float[] diffuseColor, float[] emissiveColor,
                    float shininess, float[] specularColor, float transparency) {
        setAmbientIntensity( ambientIntensity );
        setDiffuseColor( diffuseColor );
        setEmissiveColor( emissiveColor );
        setShininess( shininess );
        setSpecularColor( specularColor );
        setTransparency( transparency );
    }

    public Material(float ambientIntensity, float[] diffuseColor, float[] emissiveColor,
                    float shininess, float[] specularColor, float transparency, String DEF) {
        setAmbientIntensity( ambientIntensity );
        setDiffuseColor( diffuseColor );
        setEmissiveColor( emissiveColor );
        setShininess( shininess );
        setSpecularColor( specularColor );
        setTransparency( transparency );
        setDEF( DEF );
    }


    public Material clone() throws
            CloneNotSupportedException
    {
        try {
            Material cloneObj = (Material) super.clone();
            cloneObj.ambientIntensity = this.ambientIntensity.clone();
            cloneObj.diffuseColor = this.diffuseColor.clone();
            cloneObj.emissiveColor = this.emissiveColor.clone();
            cloneObj.shininess = this.shininess.clone();
            cloneObj.specularColor = this.specularColor.clone();
            cloneObj.transparency = this.transparency.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide float value within allowed range of [0,1] from inputOutput SFFloat field named ambientIntensity.
     * @return
     */
    public float getAmbientIntensity() {
        return ambientIntensity.getValue();
    }

    /**
     * Provide array of 3-tuple float results using RGB values [0..1] using RGB values [0..1] from inputOutput SFColor field named diffuseColor.
     * @return
     */
    public float[] getDiffuseColor() {
        float[] _diffuseColor = {this.diffuseColor.getRed(), this.diffuseColor.getGreen(), this.diffuseColor.getBlue()};
        return _diffuseColor;
    }

    /**
     * Provide array of 3-tuple float results using RGB values [0..1] using RGB values [0..1] from inputOutput SFColor field named emissiveColor.
     * @return
     */
    public float[] getEmissiveColor() {
        float[] _emissiveColor = {this.emissiveColor.getRed(), this.emissiveColor.getGreen(), this.emissiveColor.getBlue()};
        return _emissiveColor;
    }

    /**
     * Provide float value within allowed range of [0,1] from inputOutput SFFloat field named shininess.
     * @return
     */
    public float getShininess() {
        return shininess.getValue();
    }

    /**
     * Provide array of 3-tuple float results using RGB values [0..1] using RGB values [0..1] from inputOutput SFColor field named specularColor.
     * @return
     */
    public float[] getSpecularColor() {
        float[] _specularColor = {this.specularColor.getRed(), this.specularColor.getGreen(), this.specularColor.getBlue()};
        return _specularColor;
    }

    /**
     * Provide float value within allowed range of [0,1] from inputOutput SFFloat field named transparency.
     * @return
     */
    public float getTransparency() {
        return transparency.getValue();
    }

    /**
     * Assign float value within allowed range of [0,1] to inputOutput SFFloat field named ambientIntensity.
     * @param newValue
     */
    public void setAmbientIntensity(float newValue) {
        this.ambientIntensity.setValue( newValue );
    }

    /**
     * Assign 3-tuple float array using RGB values [0..1] using RGB values [0..1] to inputOutput SFColor field named diffuseColor.
     * @param newValue
     */
    public void setDiffuseColor(float[] newValue) {
        this.diffuseColor.setValue( newValue[0], newValue[1], newValue[2]);
    }

    /**
     * Assign 3-tuple float array using RGB values [0..1] using RGB values [0..1] to inputOutput SFColor field named emissiveColor.
     * @param newValue
     */
    public void setEmissiveColor(float[] newValue) {
        this.emissiveColor.setValue( newValue[0], newValue[1], newValue[2]);
    }

    /**
     * Assign float value within allowed range of [0,1] to inputOutput SFFloat field named shininess.
     * @param newValue
     */
    public void setShininess(float newValue) {
        this.shininess.setValue( newValue );
    }

    /**
     * Assign 3-tuple float array using RGB values [0..1] using RGB values [0..1] to inputOutput SFColor field named specularColor.
     * @param newValue
     */
    public void setSpecularColor(float[] newValue) {
        this.specularColor.setValue( newValue[0], newValue[1], newValue[2]);
    }

    /**
     * Assign float value within allowed range of [0,1] to inputOutput SFFloat field named transparency.
     * @param newValue
     */
    public void setTransparency(float newValue) {
        this.transparency.setValue( newValue );
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

    //TODO: the following methods are not implemented
    // getMetadata()
    // setCssClass(java.lang.String newValue)
    // setMetadata(X3DMetadataObject newValue)

} // end Material
