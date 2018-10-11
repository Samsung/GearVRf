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
package org.gearvrf.x3d.data_types;

/**
 * /**
 * Defines the X3D SFString data type
 * Spec: http://www.web3d.org/specifications/java/javadoc/org/web3d/x3d/sai/SFString.html
 */
public class SFString implements Cloneable {

    private String value = "";

    public SFString() {
    }

    public SFString(String value) {
        setValue(value);
    }

    public SFString clone() throws CloneNotSupportedException
    {
        try {
            SFString cloneObj = (SFString) super.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * Assign a new value to this field.
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get the current value.
     * @return
     */
    public String getValue() {
        return this.value;
    }

}


