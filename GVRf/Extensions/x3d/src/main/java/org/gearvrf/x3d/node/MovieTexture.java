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
import org.gearvrf.x3d.data_types.MFString;
import org.gearvrf.x3d.data_types.SFBool;
import org.gearvrf.x3d.data_types.SFFloat;

public class MovieTexture extends X3DNode implements Cloneable
{

    private static final String TAG = MovieTexture.class.getSimpleName();

    SFBool loop = new SFBool(false);
    SFFloat pitch = new SFFloat(1.0f);
    SFFloat speed = new SFFloat(1.0f);
    MFString url = new MFString();

    public MovieTexture() {
    }

    public MovieTexture(boolean _loop, float _pitch, float _speed, String[] _url) {
        setLoop(_loop);
        setPitch(_pitch);
        setSpeed(_speed);
        setUrl( _url );
    }

    public MovieTexture(boolean _loop, float _pitch, float _speed, String[] _url,
                    String _DEF) {
        setLoop(_loop);
        setPitch(_pitch);
        setSpeed(_speed);
        setUrl( _url );
        setDEF(_DEF);
    }


    public MovieTexture clone() throws
            CloneNotSupportedException
    {
        try {
            MovieTexture cloneObj = (MovieTexture) super.clone();
            cloneObj.loop = this.loop.clone();
            cloneObj.pitch = this.pitch.clone();
            cloneObj.speed = this.speed.clone();
            cloneObj.url = this.url.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Provide boolean value from inputOutput SFBool field named loop.
     */
    public boolean getLoop() {
        return this.loop.getValue();
    }

    /**
     * Provide float value within allowed range of (0,infinity) from inputOutput SFFloat field named pitch.
     * Note that our implementation with ExoPlayer that pitch and speed will be set to the same value.
     */
    public float getPitch() {
        return getSpeed();
    }

    /**
     * Provide float value from inputOutput SFFloat field named speed.
     * Note that our implementation with ExoPlayer that pitch and speed will be set to the same value.
    */
    public float getSpeed() {
        return this.pitch.getValue();
    }

    /**
     * Provide array of String results from inputOutput MFString field named url.
     * @array saved in valueDestination
     */
    public String[] getUrl() {
        String[] valueDestination = new String[ url.size() ];
        this.url.getValue(valueDestination);
        return valueDestination;
    }

    /**
     * Assign boolean value to inputOutput SFBool field named loop.
     * @param newValue
     */
    public void setLoop(boolean newValue) {
        this.loop.setValue( newValue );
    }

    /**
     * Assign float value within allowed range of (0,infinity) to inputOutput SFFloat field named pitch.
     * Note that our implementation with ExoPlayer that pitch and speed will be set to the same value.
     * @param newValue
     */
    public void setPitch(float newValue) {
        setSpeed( newValue );
    }

    /**
     * Assign float value to inputOutput SFFloat field named speed.
     * Note that our implementation with ExoPlayer that pitch and speed will be set to the same value.
     * @param newValue
     */
    public void setSpeed(float newValue) {
        if (newValue < 0) newValue = 0;
        this.pitch.setValue( newValue );
        this.speed.setValue( newValue );
    }

    /**
     * Assign String array to inputOutput MFString field named url.
     * @param newValue
     */
    public void setUrl(String[] newValue) {
        this.url.setValue(newValue.length, newValue );
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

} // end MovieTexture
