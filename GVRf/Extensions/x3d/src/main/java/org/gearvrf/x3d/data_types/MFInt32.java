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
 * Defines the X3D MFInt32 data type
 * Spec: http://www.web3d.org/specifications/java/javadoc/org/web3d/x3d/sai/MFInt32.html
 */
public class MFInt32 implements MField, Cloneable {

    private static final String TAG = MFInt32.class.getSimpleName();

    private ArrayList<Integer> value = new ArrayList<Integer>();

    public MFInt32() {
    }


    public MFInt32(int[] newValue) {
       for (int i = 0; i < newValue.length; i++) {
            value.add( newValue[i] );
        }
    }

    public MFInt32 clone() throws CloneNotSupportedException
    {
        try {
            MFInt32 cloneObj = (MFInt32) super.clone();
            return cloneObj;
        }
        catch (CloneNotSupportedException e) {
        }
        return null;
    }


    public void clear() { value.clear(); }

    public void remove(int index) { value.remove(index);}

    public int size() {
        return value.size();
    }

    /**
     * Places a new value at the end of the existing value array, increasing the field length accordingly.
     * @param newValue - one integer appended to the MFInt32 array list
     */
    public void append(int newValue) {
        value.add( newValue );
    }

    /**
     * Get an individual value from the existing field array.
     * @param index - the value within the array
     */
    public int get1Value(int index) {
        try {
            return( value.get(index) );
        }
        catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "X3D MFInt32 get1Value(index) out of bounds." + e);
        }
        catch (Exception e) {
            Log.e(TAG, "X3D MFInt32 get1Value(index) exception " + e);
        }
        return -1;
    }

    /**
     * Write out the current value of this field into the external valueDestination array.
     * @param valueDestination - where all the MFInt32 object values are returned
     *                         in a single-dimentional array
     */
    /*
    public void getValue(int[] valueDestination) {
        valueDestination = new int[size()];
        for (int i = 0; i < size(); i++) {
            valueDestination[i] = value.get(i);
        }
    }
    */
    public int[] getValue() {
        int[] valueDestination = new int[size()];
        for (int i = 0; i < size(); i++) {
            valueDestination[i] = value.get(i);
        }
        return valueDestination;
    }

    /**
     * Insert a new value prior to the index location in the existing value array,
     * increasing the field length accordingly.
     * @param index - where the new value in an MFInt32 object will be placed in the array list
     */
    public void insertValue(int index, int newValue) {
            try {
                value.add( index, newValue );
            }
            catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "X3D MFInt32 insertValue index out of bounds." + e);
            }
            catch (Exception e) {
                Log.e(TAG, "X3D MFInt32 get1Value(index, int newValue) exception " + e);
            }
    }

    /**
     * Replace a single value at the appropriate location in the existing value array.
     * @param index - location in the array list
     */
    public void set1Value(int index, int newValue) {
            try {
                value.set( index, newValue );
            }
            catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "X3D MFInt32 get1Value(index) out of bounds." + e);
            }
            catch (Exception e) {
                Log.e(TAG, "X3D MFInt32 get1Value(index) exception " + e);
            }
    }

    /**
     * Assign an array subset to this field.
     * @param size - number of new values
     */
    public void setValue(int size, int[] newValue) {
            try {
                for (int i = 0; i < size; i++) {
                    value.add(i, newValue[i]);
                    //value.set( i, newValue );
                }
            }
            catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "X3D MFInt32 setValue(size,newValue[]) out of bounds." + e);
            }
            catch (Exception e) {
                Log.e(TAG, "X3D MFInt32 setValue(size, newValue[]) exception " + e);
            }
    }

}


