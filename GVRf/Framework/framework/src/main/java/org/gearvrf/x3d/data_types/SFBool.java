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
 * Defines the X3D SFBool[ean] data type
 */
public class SFBool {

    private boolean value = false;

    public SFBool() {
    }

    public SFBool(boolean value) {
        setValue(value);
    }
    public SFBool(int intValue) {
        setValue(intValue);
    }
    public SFBool(String stringValue) {
        setValue(stringValue);
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public void setValue(int intValue) {
        if (intValue == 0) setValue( false );
        else setValue( true );
    }

    public void setValue(String stringValue) {
        if ( stringValue.equalsIgnoreCase("false")) setValue( false );
        else setValue( true );
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.value);
        return buf.toString();
    }

}



