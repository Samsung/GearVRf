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
    private String name = null;

    /**
     * Construct an empty Inline object
     */
    public InlineObject() { }

    /**
     * Construct an Inline Object
     * @param inlineGVRSceneObject
     *          the GVRSceneObject that this Inlined object will be attached
     *          For example, if the X3D scene is:
     *          <Transform name="myTransform">
     *              <Inline url='urlInlinedObject'/>
     *          </Transform>
     *          Then the inlineGVRSceneObject will be the GVRSceneObject associated with 'myTransform'
     * @param url
     *          The url (essentially X3D file whose file name ends with '.x3d') that will be inlined.
     */
    public InlineObject(GVRSceneObject inlineGVRSceneObject, String[] url) {
        this.inlineGVRSceneObject = inlineGVRSceneObject;
        this.url = url;
    }

    /**
     * Similar to the above Inline Constructor
     * @param inlineGVRSceneObject
     * @param url
     * @param name
     *          The name DEFined in the <Inline /> tag. For example
     *          <Inline url='myURL' name='myInlineName'/>
     *          Then name will be set to 'myInlineName'
     */
    public InlineObject(GVRSceneObject inlineGVRSceneObject, String[] url, String name) {
        this.inlineGVRSceneObject = inlineGVRSceneObject;
        this.url = url;
        this.name = name;
    }

    /**
     * Gets the url which will be a file name ending in '.x3d'
     * @return
     */
    public String[] getURL() {
        return this.url;
    }

    /**
     * Get the GVRSceneObject that this Inline object will eventually
     * be added as a child.
     * @return
     */
    public GVRSceneObject getInlineGVRSceneObject() {
        return this.inlineGVRSceneObject;
    }

    /**
     * Get the name saved in the Inline file
     *          <Inline url='myURL' name='myInlineName'/>
     *          then 'myInlineName' will be returned.
     * @return
     */
    public String getName() {
        return name;
    }

}



