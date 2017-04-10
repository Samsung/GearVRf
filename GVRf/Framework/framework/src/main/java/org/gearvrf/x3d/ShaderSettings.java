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

package org.gearvrf.x3d;

import java.util.concurrent.Future;

import org.gearvrf.GVRMaterial;


import org.gearvrf.GVRTexture;

/**
 * 
 * @author m1.williams
 * ShaderSettings retain Material values and Textures for X3D
 * This sets shader values in GearVRf's Vertex and Fragment
 * Shader.
 * Can also be set to overwrite or construct X3D programmatically
 * the shader values while still using GearVR's internal Shader.
 */

/*
 * TODO: May eventually include the Texture parameters of wrapping settings

 */
public class ShaderSettings
{
  private String nameAppearance = ""; // set if there is a DEF in Appearance
                                      // node
  private String nameMaterial = ""; // set if there is a DEF in Material node
  // private String nameTextureTransform = ""; // set if there is a DEF in
  // TextureTransform node

  public float[] textureCenter =
  {
      0, 0
  };
  public float[] textureScale =
  {
      1, 1
  };
  public float textureRotation = 0;
  public float[] textureTranslation =
  {
      0, 0
  };

  public float ambientIntensity = 0.2f;
  public float[] diffuseColor =
  {
      0.8f, 0.8f, 0.8f
  };
  public float[] emissiveColor =
  {
      0, 0, 0
  };
  public float shininess = 0.2f;
  public float[] specularColor =
  {
      0, 0, 0
  };
  private float transparency = 0;

  public float[] modelMatrix = new float[16];


  public Future<GVRTexture> texture = null;

  public String fragmentShaderLights = "";
  
  public GVRMaterial material;

  public void initializeTextureMaterial(GVRMaterial m)
  {
	material = m;
    nameAppearance = ""; // set if there is a DEF in Appearance node
    nameMaterial = ""; // set if there is a DEF in Material node
    // nameTextureTransform = ""; // set if there is a DEF in TextureTransform
    // node

    // initialize texture values
    for (int i = 0; i < 2; i++)
    {
      textureCenter[i] = 0;
      textureScale[i] = 1;
      textureTranslation[i] = 0;
    }
    textureRotation = 0;

    // initialize X3D Material values
    for (int i = 0; i < 3; i++)
    {
      diffuseColor[i] = 0.8f;
      emissiveColor[i] = 0;
      specularColor[i] = 0;
    }
    ambientIntensity = 0.2f;
    shininess = 0.2f;
    transparency = 0;

    // modelMatrix set to identity matrix
    for (int i = 0; i < 4; i++)
    {
      for (int j = 0; j < 4; j++)
      {
        modelMatrix[i * 4 + j] = 0;
        if (i == j)
          modelMatrix[i * 4 + j] = 1;
      }
    }

    texture = null;
  }



  public ShaderSettings(GVRMaterial material)
  {
    initializeTextureMaterial(material);
  }

  public void appendFragmentShaderLights(String lightString)
  {
    fragmentShaderLights += lightString;
  }

  public void setAppearanceName(String name)
  {
    this.nameAppearance = name;
  }

  public String getAppearanceName()
  {
    return this.nameAppearance;
  }

  public void setMaterialName(String name)
  {
    this.nameMaterial = name;
  }

  public String getMaterialName()
  {
    return this.nameMaterial;
  }

  public void setDiffuseColor(float[] diffuseColor)
  {
    for (int i = 0; i < 3; i++)
    {
      this.diffuseColor[i] = diffuseColor[i];
    }
  }

  public void setEmmissiveColor(float[] emissiveColor)
  {
    for (int i = 0; i < 3; i++)
    {
      this.emissiveColor[i] = emissiveColor[i];
    }
  }

  public void setSpecularColor(float[] specularColor)
  {
    for (int i = 0; i < 3; i++)
    {
      this.specularColor[i] = specularColor[i];
    }
  }

  public void setAmbientIntensity(float ambientIntensity)
  {
    this.ambientIntensity = ambientIntensity;
  }

  public void setShininess(float shininess)
  {
    this.shininess = shininess;
  }

  public void setTransparency(float transparency)
  {
    this.transparency = transparency;
  }

  public float getTransparency()
  {
    return this.transparency;
  }

  public void setTexture(Future<GVRTexture> texture)
  {
    this.texture = texture;
    this.material.setTexture("diffuseTexture", texture);
  }

  public void setTextureCenter(float[] textureCenter)
  {
    for (int i = 0; i < 2; i++)
    {
      this.textureCenter[i] = textureCenter[i];
    }
  }

  public void setTextureScale(float[] textureScale)
  {
    for (int i = 0; i < 2; i++)
    {
      this.textureScale[i] = textureScale[i];
    }
  }

  public void setTextureRotation(float textureRotation)
  {
    for (int i = 0; i < 2; i++)
    {
      this.textureRotation = textureRotation;
    }
  }

  public void setTextureTranslation(float[] textureTranslation)
  {
    for (int i = 0; i < 2; i++)
    {
      this.textureTranslation[i] = textureTranslation[i];
    }
  }
}
