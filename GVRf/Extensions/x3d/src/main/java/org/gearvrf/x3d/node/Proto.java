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

import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;
import org.gearvrf.x3d.ScriptObject;
import org.gearvrf.x3d.Utility;
import org.gearvrf.x3d.X3Dobject;

import java.util.ArrayList;

/**
 *
 */

public class Proto
{

    private static final String TAG = Proto.class.getSimpleName();

    public enum data_types {
        MFString, MFVec3f, SFBool, SFColor, SFFloat, SFInt32, SFRotation,
        SFString, SFTime, SFVec2f, SFVec3f, SFNode }
    private enum proto_States {
        None, ProtoDeclare, ProtoInterface, ProtoBody, ProtoIS }
    private proto_States proto_State = proto_States.None;

    public class Field {
        private String mName = "";
        private String mNode = "";
        private ScriptObject.AccessType mAccessType = null;
        private data_types mType;
        private float[] mFloatValue;     // SFColor, SFFloat, SFRotation, SFvec2f, SFVec3f
        private boolean mBooleanValue;   // SFBool
        private String[] mStringValue;   // SFString and MFString
        private int mIntValue;           // SFInt32
        private long mLongValue;         // SFTime

        public Field(String name)
        {
            mName = name;
        }

    }

    private X3Dobject mX3Dobject;
    private Utility mUtility;
    private GVRSceneObject mGVRSceneObject = null;
    private String mName;
    private ScriptObject mScriptObject;
    private ArrayList<Field> mFieldObjects = new ArrayList<Field>();

    private Shape mShape = null;
    private Appearance mAppearance = null;
    private Geometry mGeometry = null;
    private Geometry mGeometryInstance = null;
    private Transform mTransform = null;


    public Proto(X3Dobject x3dObject)
    {
        mX3Dobject = x3dObject;
        mScriptObject = new ScriptObject();
        mUtility = new Utility();
        mAppearance = null;
        mGeometry = null;
    }

    public Appearance getAppearance() {
        return mAppearance;
    }

    /**
     * Provide X3DGeometryNode instance (using a properly typed node) from inputOutput SFNode field material.
     * @param newValue
     */
    public Geometry getGeometry() {
        return mGeometry;
    }
    public Geometry getGeometryInstance() {
        return mGeometryInstance;
    }

    public void setAppearance(Appearance appearance) {
        mAppearance = appearance;
    }

    /**
     * Assign X3DGeometryNode instance (using a properly typed node) to inputOutput SFNode field material.
     */
    public void setGeometry(Geometry newValue) {
        mGeometry = newValue;
    }
    public void setGeometryInstance(Geometry newValue) {
        mGeometryInstance = newValue;
    }

    public Shape getShape() {
        return mShape;
    }

    public void setShape(Shape shape ) {
        mShape = shape;
    }

    public Transform getTransform() {
        return mTransform;
    }

    public void setTransform(Transform transform ) {
        mTransform = transform;
    }

    public boolean isProtoStateNone() {
        if ( proto_State == proto_States.None) return true;
        else return false;
    }

    public void setProtoStateNone() {
        proto_State = proto_States.None;
    }

    public boolean isProtoStateProtoDeclare() {
        if ( proto_State == proto_States.ProtoDeclare) return true;
        else return false;
    }

    public void setProtoStateProtoDeclare() {
        proto_State = proto_States.ProtoDeclare;
    }

    public boolean isProtoStateProtoInterface() {
        if ( proto_State == proto_States.ProtoInterface) return true;
        else return false;
    }

    public void setProtoStateProtoInterface() {
        proto_State = proto_States.ProtoInterface;
    }

    public boolean isProtoStateProtoBody() {
        if ( proto_State == proto_States.ProtoBody) return true;
        else return false;
    }

    public void setProtoStateProtoBody() {
        proto_State = proto_States.ProtoBody;
    }

    public boolean isProtoStateProtoIS() {
        if ( proto_State == proto_States.ProtoIS) return true;
        else return false;
    }

    public void setProtoStateProtoIS() {
        proto_State = proto_States.ProtoIS;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public void AddField(ScriptObject.AccessType accessType, String name, String type, String value) {
        boolean error = false;
        Field field = new Field( name );
        field.mAccessType = accessType;
        if (type.equalsIgnoreCase("SFBool")) {
            field.mType = data_types.SFBool;
            if ( !value.isEmpty() ) {
                field.mBooleanValue = mUtility.parseBooleanString( value );
            }
        }
        else if  (type.equalsIgnoreCase("SFFloat")) {
            field.mType = data_types.SFFloat;
            if ( !value.isEmpty() ) {
                field.mFloatValue = new float[1];
                field.mFloatValue[0] = mUtility.parseSingleFloatString(value, false, false);
            }
        }
        else if  (type.equalsIgnoreCase("SFColor")) {
            field.mType = data_types.SFColor;
            if ( !value.isEmpty() ) {
                field.mFloatValue = new float[3];
                field.mFloatValue = mUtility.parseFixedLengthFloatString(value, 3, true, true);
            }
        }
        else if  (type.equalsIgnoreCase("SFVec3f")) {
            field.mType = data_types.SFVec3f;
            if ( !value.isEmpty() ) {
                field.mFloatValue = new float[3];
                field.mFloatValue = mUtility.parseFixedLengthFloatString(value, 3, false, false);
            }
        }
        else if  (type.equalsIgnoreCase("SFRotation")) {
            field.mType = data_types.SFRotation;
            if ( !value.isEmpty() ) {
                field.mFloatValue = new float[4];
                field.mFloatValue = mUtility.parseFixedLengthFloatString(value, 4, false, false);
            }
        }
        else if  (type.equalsIgnoreCase("SFVec2f")) {
            field.mType = data_types.SFVec2f;
            if ( !value.isEmpty() ) {
                field.mFloatValue = new float[2];
                field.mFloatValue = mUtility.parseFixedLengthFloatString(value, 2, false, false);
            }
        }
        else if  (type.equalsIgnoreCase("SFString")) {
            field.mType = data_types.SFString;
            if ( !value.isEmpty() ) {
                field.mStringValue = new String[1];
                field.mStringValue = mUtility.parseMFString(value);
            }
        }
        else if  (type.equalsIgnoreCase("SFTime")) {
            field.mType = data_types.SFTime;
            if ( !value.isEmpty() ) {
                field.mLongValue = (long) mUtility.parseSingleFloatString(value, false, true);
            }
        }
        else if  (type.equalsIgnoreCase("SFInt32")) {
            field.mType = data_types.SFInt32;
            if ( !value.isEmpty() ) {
                field.mIntValue = mUtility.parseIntegerString(value);
            }
        }
        else if  (type.equalsIgnoreCase("MFVec3f")) {
            field.mType = data_types.MFVec3f;
            if ( !value.isEmpty() ) {
                //TODO: not implemented in Utility.
            }
        }
        else if  (type.equalsIgnoreCase("MFString")) {
            field.mType = data_types.MFString;
            if ( !value.isEmpty() ) {
                String[] mfString = mUtility.parseMFString(value);
                field.mStringValue = new String[mfString.length];
                for (int i = 0; i < mfString.length; i++) {
                    field.mStringValue[i] = mfString[i];
                }
            }
        }
        else if  (type.equalsIgnoreCase("SFNode")) {
            field.mType = data_types.SFNode;
            if ( !value.isEmpty() ) {
                //TODO: not implemented in Utility.
            }
        }
        else {
            Log.e(TAG, "Error, X3D Data Type  " + type + " not supported.");
            error = true;
        }
        if ( !error ) {
            mFieldObjects.add( field );
        }
    }  // end AddField

    public Field getField(String name) {
        for (int i = 0; i < mFieldObjects.size(); i++) {
            Field field = mFieldObjects.get(i);
            if (field.mName.equalsIgnoreCase(name)) {
                return field;
            }

        }
        return null;
    }

    public Field getField(int index) {
        if (index < mFieldObjects.size()) {
            Field field = mFieldObjects.get(index);
            return field;
        }
        return null;
    }

    public int getFieldSize() {
        return mFieldObjects.size();
    }

    public data_types getData_type(Field field) {
        return field.mType;
    }

    public void setNodeField(Field field, String nodeField) {
        field.mNode = nodeField;
    }

    public String getNodeField( Field field ) {
        return field.mNode;
    }

    public boolean getField_SFBool( Field field ) {
        return field.mBooleanValue;
    }

    public float[] getField_SFFloat( Field field ) {
        return field.mFloatValue;
    }

    public float[] getField_SFVec3f( Field field ) {
        return field.mFloatValue;
    }

    public float[] getField_SFVec2f( Field field ) {
        return field.mFloatValue;
    }

    public String getField_SFString( Field field ) {
        return field.mStringValue[0];
    }

    public String[] getField_MFString( Field field ) {
    return field.mStringValue;
}

} // end Proto
