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

import org.gearvrf.GVRSceneObject;

/**
 * 
 * @author m1.williams
 * Class of an array list of <Inline> objects
 * These are added to the Scene Graph after parsing
 * the original file.  
 */

public class InlineObject {

  private GVRSceneObject inlineGVRSceneObject = null;
  private String[] url = {};

  public InlineObject() { }

  public InlineObject(GVRSceneObject inlineGVRSceneObject, String[] url) {
     this.inlineGVRSceneObject = inlineGVRSceneObject;
     this.url = url;
  }

  public String[] getURL() {
     return this.url;
  }

  public int getTotalURL() {
     return url.length;
  }

  public GVRSceneObject getInlineGVRSceneObject() {
     return this.inlineGVRSceneObject;
  }

}



