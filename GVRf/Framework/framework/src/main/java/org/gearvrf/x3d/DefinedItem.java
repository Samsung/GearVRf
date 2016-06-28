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

/*
 * Used in Array List for items DEFined in X3D (using the DEF="...." parameter).
 * Another item using the USE="...." parameter will search the array list for the 
 * matching item.
 */

import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class DefinedItem {

  private String name = "";
  private GVRMesh gvrMesh = null;
  private GVRSceneObject gvrSceneObject = null;
  private Future<GVRTexture> gvrTexture = null;
  private GVRRenderData gvrRenderData = null;

  // Only have ShaderSetting set until parsing the end Shape </Shape> node
  //private ShaderSettings shaderSettingsMaterial = null;
  private GVRMaterial gvrMaterial = null;

  
  public DefinedItem () {
  }

  public DefinedItem (String name) {
    this.name = name;
  }

  public void setName (String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
  
  public void setGVRRenderData (GVRRenderData gvrRenderData) {
	  this.gvrRenderData = gvrRenderData;
  }

  public GVRRenderData getGVRRenderData() {
	  return this.gvrRenderData;
  }
  
  public void setGVRTexture (Future<GVRTexture> gvrTexture) {
	  this.gvrTexture = gvrTexture;
  }

  public Future<GVRTexture> getGVRTexture() {
	  return this.gvrTexture;
  }

  public void setGVRMesh (GVRMesh gvrMesh) {
    this.gvrMesh = gvrMesh;
  }

  public GVRMesh getGVRMesh() {
    return this.gvrMesh;
  }

  public void setGVRMaterial (GVRMaterial gvrMaterial) {
    this.gvrMaterial = gvrMaterial;
  }

  public GVRMaterial getGVRMaterial() {
    return this.gvrMaterial;
  }

  /*
  public void setShaderSettingsMaterial(ShaderSettings shaderSettingsMaterial) {
    this.shaderSettingsMaterial = shaderSettingsMaterial;
  }

  public ShaderSettings getShaderSettingsMaterial() {
    return this.shaderSettingsMaterial;
  }
  */


  public void setGVRSceneObject (GVRSceneObject gvrSceneObject) {
    this.gvrSceneObject = gvrSceneObject;
  }

  public GVRSceneObject getGVRSceneObject() {
    return this.gvrSceneObject;
  }

}
