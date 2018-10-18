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

import org.gearvrf.utility.Log;
import java.util.ArrayList;

/**
 * Defines the X3D MFString data type
 * Spec: http://www.web3d.org/specifications/java/javadoc/org/web3d/x3d/sai/MFString.html
 */
public class MFString implements MField {

    private static final String TAG = MFString.class.getSimpleName();

    private ArrayList<String> value = new ArrayList<String>();

    public MFString() {
    }

    public void clear() { value.clear(); }

    public void remove(int index) { value.remove(index);}

    public int size() {
        return value.size();
    }

    public MFString(String[] newValue) {
        setValue(newValue.length, newValue);
    }

    public MFString(String newValue) {
        String[] newValues = new String[1];
        newValues[0] = newValue;
        setValue(1, newValues);
    }

    /**
     * Places a new value at the end of the existing value array, increasing the field length accordingly.
     * @param newValue added String
     */
    public void append(String newValue) {
        value.add( newValue );
    }

    /**
     * Get an individual value from the existing field array.
     * @param index returns the record based on the index into the array list
     *              throws an exception if out of bounds.
     * @return
     */
    public String get1Value(int index) {
        try {
            return value.get(index);
        }
        catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "X3D MFString get1Value(index) out of bounds." + e);
        }
        catch (Exception e) {
            Log.e(TAG, "X3D MFString get1Value(index) exception " + e);
        }
        return "";
    }

    /**
     * Write out the current value of this field into the external valueDestination array.
     * @param valueDestination - where the Strings will be written
     */
    public void getValue(String[] valueDestination) {
        if ( valueDestination.length < value.size()) {
          Log.e(TAG, "X3D MFString ArrayIndexOutOfBoundsException");
          Log.e(TAG, "array size " + valueDestination.length + " < MFString value.size = " + size() );
        }
        else {
            for (int i = 0; i < valueDestination.length; i++) {
                valueDestination[i] = get1Value(i);
            }
        }
    }

    /**
     * Insert a new value prior to the index location in the existing value array, increasing the field length accordingly.
     * @param index inserts the new String into this position in the array list
     * @param newValue the new String
     */
    public void insertValue(int index, String newValue) {
        try {
            value.add( index, newValue );
        }
        catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "X3D MFString insertValue(int index, ...) out of bounds." + e);
        }
        catch (Exception e) {
            Log.e(TAG, "X3D MFString insertValue(int index, ...)  exception " + e);
        }
    }

    /**
     * Replace a single value at the appropriate location in the existing value array.
     * @param index - location in the array list
     * @param newValue - the new String
     */
    public void set1Value(int index, String newValue) {
        try {
            value.set( index, newValue );
        }
        catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "X3D MFString set1Value(int index, ...) out of bounds." + e);
        }
        catch (Exception e) {
            Log.e(TAG, "X3D MFString set1Value(int index, ...)  exception " + e);
        }
    }

    /**
     * Assign a new value to this field.
     * @param numStrings - number of strings
     * @param newValues - the new strings
     */
    public void setValue(int numStrings, String[] newValues) {
        value.clear();
        if (numStrings == newValues.length) {
            for (int i = 0; i < newValues.length; i++) {
                value.add(newValues[i]);
            }
        }
        else {
            Log.e(TAG, "X3D MFString setValue() numStrings not equal total newValues");
        }
    }

}

