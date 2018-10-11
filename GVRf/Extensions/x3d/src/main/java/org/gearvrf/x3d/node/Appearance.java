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

public class Appearance extends X3DNode implements Cloneable
{

    private static final String TAG = Appearance.class.getSimpleName();

    private Material material = null;
    private ImageTexture imageTexture = null;
    private TextureTransform textureTransform = null;
    private MovieTexture movieTexture = null;

    public Appearance() {
    }

    public Appearance(String _DEF) {
        setDEF(_DEF);
    }

    public Appearance clone() throws
            CloneNotSupportedException
    {
        try {
            Appearance cloneObj = (Appearance) super.clone();
            cloneObj.material = this.material.clone();
            cloneObj.imageTexture = this.imageTexture.clone();
            cloneObj.textureTransform = this.textureTransform.clone();
            cloneObj.movieTexture = this.movieTexture.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide X3DMaterialNode instance (using a properly typed node) from inputOutput SFNode field material.
     * @param newValue
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Provide X3DTextureNode instance (using a properly typed node) from inputOutput SFNode field texture.
     * @param newValue
     */
    public ImageTexture getTexture() {
        return imageTexture;
    }

    /**
     * Provide X3DTextureTransformNode instance (using a properly typed node) from inputOutput SFNode field textureTransform.
     * @param newValue
     */
    public TextureTransform getTextureTransform() {
        return textureTransform;
    }

    /**
     * Provide X3DMovieTextureNode instance (using a properly typed node) from inputOutput SFNode field movieTexture.
     * @param newValue
     */
    public MovieTexture getMovieTexture() {
        return movieTexture;
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
     * Assign X3DMaterialNode instance (using a properly typed node) to inputOutput SFNode field material.
     */
    public void setMaterial(Material newValue) {
        material = newValue;
    }

    /**
     * Assign X3DTextureNode instance (using a properly typed node) to inputOutput SFNode field texture.
     */
    public void setTexture(ImageTexture newValue) {
        imageTexture = newValue;
    }

    /**
     * Provide X3DTextureTransformNode instance (using a properly typed node) from inputOutput SFNode field textureTransform.
     * @param newValue
     */
    public void setTextureTransform(TextureTransform newValue) {
        textureTransform = newValue;
    }

    /**
     * Provide X3DMovieTextureNode instance (using a properly typed node) from inputOutput SFNode field movieTexture.
     * @param newValue
     */
    public void setMovieTexture(MovieTexture newValue) {
        movieTexture = newValue;
    }


    //TODO: the following methods are not implemented
    /*
    addShaders(X3DNode[] newValue)
    getFillProperties()
    getLineProperties()
    getMetadata()
    getShaders()
    setCssClass(java.lang.String newValue)
    setFillProperties(FillProperties newValue)
    setLineProperties(LineProperties newValue)
    setMetadata(X3DMetadataObject newValue)
    void  setShaders(X3DNode newValue)
    Appearance  setShaders(X3DNode[] newValue)
    */
} // end Appearance
