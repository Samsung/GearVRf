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
import org.gearvrf.utility.Log;

/**
 * 
 * @author m1.williams
 * Class of an array list of <Inline> objects
 * These are added to the Scene Graph after parsing
 * the original file.  
 */

public class InlineObject {

    private static final String TAG = InlineObject.class.getSimpleName();
    private GVRSceneObject inlineGVRSceneObject = null;
    private String[] url = {};
    private String name = null;

    public static final int UNSET_KEY_FRAME = -1;
    private int beginKeyFrame = UNSET_KEY_FRAME;
    private int endKeyFrame = UNSET_KEY_FRAME;

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

    public void copy(InlineObject original) {
        this.inlineGVRSceneObject = original.inlineGVRSceneObject;
        this.url = original.url;
        this.name = original.name;
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


    /**
     * An extension to X3D to enable animations between
     * specific key frames.  This value will be set in
     * the 'toField' of a ROUTE to an Inline object
     * @param frameRange
     *          Must be two comma-separated values
     */
    public void setKeyFrames(String frameRange) {
        if ( !frameRange.contains("enabled") ) {
            try {
                beginKeyFrame = Integer.valueOf( frameRange.substring(0,frameRange.indexOf(',')).trim() ).intValue();
                endKeyFrame = Integer.valueOf( frameRange.substring((frameRange.indexOf(',')+1), frameRange.length()).trim() ).intValue();
            }
            catch (NumberFormatException e) {
                Log.e(TAG, "Inline node, NumberFormatException on " + frameRange + ": " + e );
            }
            catch (Exception e) {
                Log.e(TAG, "Inline node, Exception on " + frameRange + ": "  + e);
            }
            if ( beginKeyFrame >= endKeyFrame) {
                Log.e(TAG, "Inline node error: Begining frame must be less than ending frame instead of " + frameRange );
                beginKeyFrame = UNSET_KEY_FRAME;
                endKeyFrame = UNSET_KEY_FRAME;
            }
        }
    }

    /**
     * Get the begin key frame
     * @return
     */
    public int getBeginKeyFrame() {
        return beginKeyFrame;
    }

    /**
     * get the ending KeyFrame
     * @return
     */
    public int getEndKeyFrame() {
        return endKeyFrame;
    }

}



