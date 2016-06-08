package org.gearvrf.x3d;

import static org.gearvrf.utility.Assert.checkNotNull;
import static org.gearvrf.utility.Assert.checkStringNotNullOrEmpty;

import org.gearvrf.GVRTexture;

// class sets the default material values, saves any settings
// and then sets GVRMaterail when the VERTEX and FRAGMENT
// shader values are set.

/*
 * May want to include the Texture parameters of wrapping settings
 */
public class ShaderSettings {
	public float[] textureCenter = { 0, 0 };
	public float[] textureScale = { 1, 1 };
	public float textureRotation = 0;
	public float[] textureTranslation = { 0, 0 };

	public float ambientIntensity = 0.2f;
	public float[] diffuseColor = { 0.8f, 0.8f, 0.8f };
	public float[] emissiveColor = { 0, 0, 0 };
	public float shininess = 0.2f;
	public float[] specularColor = { 0, 0, 0 };
	public float transparency = 0;

	public float[] modelMatrix = new float[16];
	
	public GVRTexture texture = null;
	
	public String fragmentShaderLights = "";
	
	public void initializeTextureMaterial() {
		// initialize texture values
		for (int i = 0; i < 2; i++) {
			textureCenter[i] = 0;
			textureScale[i] = 1;
			textureTranslation[i] = 0;			
		}
		textureRotation = 0;

		// initialize X3D Material values
		for (int i = 0; i < 3; i++) {
			diffuseColor[i] = 0.8f;
			emissiveColor[i] = 0;
			specularColor[i] = 0;		
		}
		ambientIntensity = 0.2f;
		shininess = 0.2f;
		transparency = 0;

		// modelMatrix set to identity matrix
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				modelMatrix[i*4 + j] = 0;
				if (i == j) modelMatrix[i*4 + j] = 1;
			}
		}
		texture = null;
	}
	
	public ShaderSettings() {
		initializeTextureMaterial();
	}
	
	public void appendFragmentShaderLights(String lightString) {
		fragmentShaderLights += lightString;
	}

	public void setDiffuseColor(float[] diffuseColor) {
		for (int i = 0; i < 3; i++) {
			this.diffuseColor[i] = diffuseColor[i];
		}
	}
	public void setEmmissiveColor(float[] emissiveColor) {
		for (int i = 0; i < 3; i++) {
			this.emissiveColor[i] = emissiveColor[i];
		}
	}
	public void setSpecularColor(float[] specularColor) {
		for (int i = 0; i < 3; i++) {
			this.specularColor[i] = specularColor[i];
		}
	}
	public void setAmbientIntensity(float ambientIntensity) {
		this.ambientIntensity = ambientIntensity;
	}
	public void setShininess(float shininess) {
		this.shininess = shininess;
	}
	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}
    public void setTexture(GVRTexture texture) {
        this.texture = texture;
    }
	public void setTextureCenter(float[] textureCenter) {
		for (int i = 0; i < 2; i++) {
			this.textureCenter[i] = textureCenter[i];
		}
	}
	public void setTextureScale(float[] textureScale) {
		for (int i = 0; i < 2; i++) {
			this.textureScale[i] = textureScale[i];
		}
	}
	public void setTextureRotation(float textureRotation) {
		for (int i = 0; i < 2; i++) {
			this.textureRotation = textureRotation;
		}
	}
	public void setTextureTranslation(float[] textureTranslation) {
		for (int i = 0; i < 2; i++) {
			this.textureTranslation[i] = textureTranslation[i];
		}
	}
	/*
	public float textureRotation = 0;

	 */

}



