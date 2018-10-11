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
 * Defines the X3D MFVec2f data type
 * Spec: http://www.web3d.org/specifications/java/javadoc/org/web3d/x3d/sai/MFVec2f.html
 */
public class MFVec2f implements MField, Cloneable {

    private static final String TAG = MFVec2f.class.getSimpleName();

    private ArrayList<SFVec2f> value = new ArrayList<SFVec2f>();

    public MFVec2f() {
    }


    public MFVec2f(float[] newVals) {
        if ( (newVals.length % 2) == 0) {
            for (int i = 0; i < (newVals.length/3); i++) {
                value.add( new SFVec2f(newVals[i*3], newVals[i*3+1]) );
            }
        }
        else {
            Log.e(TAG, "X3D MFVec2f constructor set with array length not divisible by 2");
        }
    }

    public MFVec2f clone() throws CloneNotSupportedException
    {
        try {
            MFVec2f cloneObj = (MFVec2f) super.clone();
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
     * @param newValue - the 2 floats making the new SFVec2f appended to the MFVec2f array list
     */
    public void append(float[] newValue) {
        if ( (newValue.length % 2) == 0) {
            for (int i = 0; i < (newValue.length/2); i++) {
                value.add( new SFVec2f(newValue[i*2], newValue[i*2+1]) );
            }
        }
        else {
            Log.e(TAG, "X3D MFVec3f append set with array length not divisible by 2");
        }
    }

    /**
     * Get an individual value from the existing field array.
     * @param index -
     * @param valueDestination - where the SFVec2f value is returned
     */
    public void get1Value(int index, float[] valueDestination) {
        try {
            SFVec2f sfVec2f = value.get(index);
            valueDestination[0] = sfVec2f.x;
            valueDestination[1] = sfVec2f.y;
        }
        catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "X3D MFVec2f get1Value(index) out of bounds." + e);
        }
        catch (Exception e) {
            Log.e(TAG, "X3D MFVec2f get1Value(index) exception " + e);
        }
    }

    /**
     * Write out the current value of this field into the external valueDestination array.
     * @param valueDestination - where all the SFVec3f object values are returned
     *                         in a 2-dimentional array
     */
    public void getValue(float[][] valueDestination) {
        valueDestination = new float[size()][2];
        for (int i = 0; i < size(); i++) {
            SFVec2f sfVec2f = value.get(i);
            valueDestination[i][0] = sfVec2f.x;
            valueDestination[i][1] = sfVec2f.y;
        }
    }

    /**
     * Write out the current value of this field into the external valueDestination array.
     * @param valueDestination - where all the SFVec2f object values are returned
     *                         in a single-dimentional array
     */
    public void getValue(float[] valueDestination) {
        valueDestination = new float[size() * 2];
        for (int i = 0; i < size(); i++) {
            SFVec2f sfVec2f = value.get(i);
            valueDestination[i*3] = sfVec2f.x;
            valueDestination[i*3 + 1] = sfVec2f.y;
        }
    }

    /**
     * Insert a new value prior to the index location in the existing value array,
     * increasing the field length accordingly.
     * @param index - where the new values in an SFVec2f object will be placed in the array list
     * @param newValue - the new x, y value for the array list
     */
    public void insertValue(int index, float[] newValue) {
        if ( newValue.length == 2) {
            try {
                value.add( index, new SFVec2f(newValue[0], newValue[1]) );
            }
            catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "X3D MFVec2f get1Value(index) out of bounds." + e);
            }
            catch (Exception e) {
                Log.e(TAG, "X3D MFVec2f get1Value(index) exception " + e);
            }
        }
        else {
            Log.e(TAG, "X3D MFVec2f insertValue set with array length not equal to 2");
        }
    }

    /**
     * Replace a single value at the appropriate location in the existing value array.
     * @param index - location in the array list
     * @param newValue - the new x,y value
     */
    public void set1Value(int index, float[] newValue) {
        if ( newValue.length == 2) {
            try {
                value.set( index, new SFVec2f(newValue[0], newValue[1]) );
            }
            catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "X3D MFVec2f get1Value(index) out of bounds." + e);
            }
            catch (Exception e) {
                Log.e(TAG, "X3D MFVec2f get1Value(index) exception " + e);
            }
        }
        else {
            Log.e(TAG, "X3D MFVec2f set1Value set with array length not equal to 3");
        }
    }

    /**
     * Assign an array subset to this field.
     * @param size - number of new values
     * @param newValue - array of the new x,y values, must be divisible by 2,
     *                 in a single dimensional array
     */
    public void setValue(int size, float[] newValue) {
        if ( ((newValue.length%2) == 0) && ((newValue.length/2) == size)) {
            try {
                for (int i = 0; i < size; i++) {
                    value.set(i, new SFVec2f(newValue[i*3], newValue[i*3+1]));
                }
            }
            catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "X3D MFVec2f setValue(size,newValue[]) out of bounds." + e);
            }
            catch (Exception e) {
                Log.e(TAG, "X3D MFVec2f setValue(size, newValue[]) exception " + e);
            }
        }
        else {
            Log.e(TAG, "X3D MFVec2f setValue() set with newValue[] length not multiple of 2, or equal to size parameter");
        }
    }

    /**
     * Assign an array subset to this field.
     * @param size- number of new values
     * @param newValue - array of the new x,y values, must be divisible by 2,
     *                 in a single dimensional array
     */
    public void setValue(int size, float[][] newValue) {
        if ( newValue.length == size) {
            try {
                for (int i = 0; i < size; i++) {
                    value.set(i, new SFVec2f(newValue[i][0], newValue[i][1]));
                }
            }
            catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "X3D MFVec2f setValue(size,newValue[][]) out of bounds." + e);
            }
            catch (Exception e) {
                Log.e(TAG, "X3D MFVec2f setValue(size, newValue[][]) exception " + e);
            }
        }
        else {
            Log.e(TAG, "X3D MFVec2f setValue() set with newValue[][] length not multiple of 2, or equal to size parameter");
        }
    }

}  // end MFVec2f


