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
 * Defines the X3D MFVec3f data type
 * Spec: http://www.web3d.org/specifications/java/javadoc/org/web3d/x3d/sai/MFVec3f.html
 */
public class MFVec3f implements MField {

    private static final String TAG = MFVec3f.class.getSimpleName();

    private ArrayList<SFVec3f> value = new ArrayList<SFVec3f>();

    public MFVec3f() {
    }


    public MFVec3f(float[] newVals) {
        if ( (newVals.length % 3) == 0) {
            for (int i = 0; i < (newVals.length/3); i++) {
                value.add( new SFVec3f(newVals[i*3], newVals[i*3+1], newVals[i*3+2]) );
            }
        }
        else {
            Log.e(TAG, "X3D MFVec3f constructor set with array length not divisible by 3");
        }
    }


    public void clear() { value.clear(); }

    public void remove(int index) { value.remove(index);}

    public int size() {
        return value.size();
    }

    /**
     * Places a new value at the end of the existing value array, increasing the field length accordingly.
     * @param newValue - the 3 floats making the new SFVec3f appended to the MFVec3 array list
     */
    public void append(float[] newValue) {
        if ( (newValue.length % 3) == 0) {
            for (int i = 0; i < (newValue.length/3); i++) {
                value.add( new SFVec3f(newValue[i*3], newValue[i*3+1], newValue[i*3+2]) );
            }
        }
        else {
            Log.e(TAG, "X3D MFVec3f append set with array length not divisible by 3");
        }
    }

    /**
     * Get an individual value from the existing field array.
     * @param index -
     * @param valueDestination - where the SFVec3f value is returned
     */
    public void get1Value(int index, float[] valueDestination) {
        try {
            SFVec3f sfVec3f = value.get(index);
            valueDestination[0] = sfVec3f.x;
            valueDestination[1] = sfVec3f.y;
            valueDestination[2] = sfVec3f.z;
        }
        catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "X3D MFVec3f get1Value(index) out of bounds." + e);
        }
        catch (Exception e) {
            Log.e(TAG, "X3D MFVec3f get1Value(index) exception " + e);
        }
    }

    /**
     * Write out the current value of this field into the external valueDestination array.
     * @param valueDestination - where all the SFVec3f object values are returned
     *                         in a 2-dimentional array
     */
    public void getValue(float[][] valueDestination) {
        valueDestination = new float[size()][3];
        for (int i = 0; i < size(); i++) {
            SFVec3f sfVec3f = value.get(i);
            valueDestination[i][0] = sfVec3f.x;
            valueDestination[i][1] = sfVec3f.y;
            valueDestination[i][2] = sfVec3f.z;
        }
    }

    /**
     * Write out the current value of this field into the external valueDestination array.
     * @param valueDestination - where all the SFVec3f object values are returned
     *                         in a single-dimentional array
     */
    public void getValue(float[] valueDestination) {
        valueDestination = new float[size() * 3];
        for (int i = 0; i < size(); i++) {
            SFVec3f sfVec3f = value.get(i);
            valueDestination[i*3] = sfVec3f.x;
            valueDestination[i*3 + 1] = sfVec3f.y;
            valueDestination[i*3 + 2] = sfVec3f.z;
        }
    }

    /**
     * Insert a new value prior to the index location in the existing value array,
     * increasing the field length accordingly.
     * @param index - where the new values in an SFVec3f object will be placed in the array list
     * @param newValue - the new x, y, z value for the array list
     */
    public void insertValue(int index, float[] newValue) {
        if ( newValue.length == 3) {
            try {
                value.add( index, new SFVec3f(newValue[0], newValue[1], newValue[2]) );
            }
            catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "X3D MFVec3f get1Value(index) out of bounds." + e);
            }
            catch (Exception e) {
                Log.e(TAG, "X3D MFVec3f get1Value(index) exception " + e);
            }
        }
        else {
            Log.e(TAG, "X3D MFVec3f insertValue set with array length not equal to 3");
        }
    }

    /**
     * Replace a single value at the appropriate location in the existing value array.
     * @param index - location in the array list
     * @param newValue - the new x,y,z value
     */
    public void set1Value(int index, float[] newValue) {
        if ( newValue.length == 3) {
            try {
                value.set( index, new SFVec3f(newValue[0], newValue[1], newValue[2]) );
            }
            catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "X3D MFVec3f get1Value(index) out of bounds." + e);
            }
            catch (Exception e) {
                Log.e(TAG, "X3D MFVec3f get1Value(index) exception " + e);
            }
        }
        else {
            Log.e(TAG, "X3D MFVec3f set1Value set with array length not equal to 3");
        }
    }

    /**
     * Assign an array subset to this field.
     * @param size - number of new values
     * @param newValue - array of the new x,y,z values, must be divisible by 3,
     *                 in a single dimensional array
     */
    public void setValue(int size, float[] newValue) {
        if ( ((newValue.length%3) == 0) && ((newValue.length/3) == size)) {
            try {
                for (int i = 0; i < size; i++) {
                    value.set(i, new SFVec3f(newValue[i*3], newValue[i*3+1], newValue[i*3+2]));
                }
            }
            catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "X3D MFVec3f setValue(size,newValue[]) out of bounds." + e);
            }
            catch (Exception e) {
                Log.e(TAG, "X3D MFVec3f setValue(size, newValue[]) exception " + e);
            }
        }
        else {
            Log.e(TAG, "X3D MFVec3f setValue() set with newValue[] length not multiple of 3, or equal to size parameter");
        }
    }

    /**
     * Assign an array subset to this field.
     * @param size- number of new values
     * @param newValue - array of the new x,y,z values, must be divisible by 3,
     *                 in a single dimensional array
     */
    public void setValue(int size, float[][] newValue) {
        if ( newValue.length == size) {
            try {
                for (int i = 0; i < size; i++) {
                    value.set(i, new SFVec3f(newValue[i][0], newValue[i][1], newValue[i][2]));
                }
            }
            catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "X3D MFVec3f setValue(size,newValue[][]) out of bounds." + e);
            }
            catch (Exception e) {
                Log.e(TAG, "X3D MFVec3f setValue(size, newValue[][]) exception " + e);
            }
        }
        else {
            Log.e(TAG, "X3D MFVec3f setValue() set with newValue[][] length not multiple of 3, or equal to size parameter");
        }
    }

}


