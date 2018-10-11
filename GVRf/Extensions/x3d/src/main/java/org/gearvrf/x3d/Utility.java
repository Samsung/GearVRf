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

import android.content.Context;
import android.net.Uri;
import android.view.Surface;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.DataSource;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRIndexBuffer;
import org.gearvrf.GVRVertexBuffer;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import static org.gearvrf.x3d.X3Dobject.*;


/**
 *
 */

public class Utility
{

    private static final String TAG = Utility.class.getSimpleName();

    X3Dobject mX3DObject;
    MeshCreatorX meshCreator;
    GVRContext gvrContext;

    public Utility()
    {
    }

/*
    public Utility(X3Dobject x3dObject, MeshCreator _meshCreator)
    {
        //mMeshCreator = _meshCreator;
        //mMeshCreator = new MeshCreatorX();
        mX3DObject = x3dObject;
    }
*/
    public Utility(X3Dobject x3dObject, GVRContext _gvrContext, EnumSet<GVRImportSettings> settings) {
        mX3DObject = x3dObject;
        this.gvrContext = _gvrContext;
        meshCreator = new MeshCreatorX(gvrContext, settings);
    }


    public float[] parseFixedLengthFloatString(String numberString,
                                                int componentCount, boolean constrained0to1, boolean zeroOrGreater) {
        StringReader sr = new StringReader(numberString);
        StreamTokenizer st = new StreamTokenizer(sr);
        st.parseNumbers();
        int tokenType;
        float componentFloat[] = new float[componentCount];
        try {
            for (int i = 0; i < componentCount; i++) {
                if ((tokenType = st.nextToken()) == StreamTokenizer.TT_NUMBER) {
                    componentFloat[i] = (float) st.nval;
                } else { // check for an exponent 'e'
                    if (tokenType == StreamTokenizer.TT_WORD) {
                        String word = st.sval;
                        if (word.startsWith("e-")) { // negative exponent
                            String exponentString = word.substring(2, word.length());
                            try {
                                --i; // with this exponent, we are still working with the
                                // previous number
                                Integer exponentInt = Integer.parseInt(exponentString);
                                componentFloat[i] *= (float) Math
                                        .pow(10, -exponentInt.intValue());
                            } catch (NumberFormatException e) {
                                Log.e(TAG,
                                        "parsing fixed length string, exponent number conversion error: "
                                                + exponentString);
                            }
                        } else if (word.equalsIgnoreCase("e")) { // exponent with plus sign
                            tokenType = st.nextToken();
                            if (tokenType == 43) { // "+" plus sign
                                if ((tokenType = st.nextToken()) == StreamTokenizer.TT_NUMBER) {
                                    --i; // with this exponent, we are still working with the
                                    // previous number
                                    float exponent = (float) st.nval;
                                    componentFloat[i] *= (float) Math.pow(10, exponent);
                                } else {
                                    st.pushBack();
                                    Log.e(TAG,
                                            "Error: exponent in X3D parser with fixed length float");
                                }
                            } else
                                st.pushBack();
                        } else
                            st.pushBack();
                    }
                } // end check for 'e' exponent
                if (constrained0to1) {
                    if (componentFloat[i] < 0)
                        componentFloat[i] = 0;
                    else if (componentFloat[i] > 1)
                        componentFloat[i] = 1;
                } else if (zeroOrGreater) {
                    if (componentFloat[i] < 0)
                        componentFloat[i] = 0;
                }
            } // end for-loop
        } // end 'try'
        catch (IOException e) {
            Log.d(TAG, "Error parsing fixed length float string: " + e);
        }
        return componentFloat;
    } // end parseFixedLengthFloatString

    public float parseSingleFloatString(String numberString,
                                         boolean constrained0to1, boolean zeroOrGreater) {
        float[] value = parseFixedLengthFloatString(numberString, 1,
                constrained0to1,
                zeroOrGreater);
        return value[0];
    }  //  end parseSingleFloatString

    public boolean parseBooleanString(String booleanString) {
        StringReader sr = new StringReader(booleanString);
        StreamTokenizer st = new StreamTokenizer(sr);
        boolean value = false;
        int tokenType;
        try {
            tokenType = st.nextToken();
            if (tokenType == StreamTokenizer.TT_WORD) {
                if (st.sval.equalsIgnoreCase("true"))
                    value = true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Boolean Error: " + e);
            e.printStackTrace();
        }
        return value;
    }  //  end parseBooleanString

    public int parseIntegerString(String numberString) {
        StringReader sr = new StringReader(numberString);
        StreamTokenizer st = new StreamTokenizer(sr);
        st.parseNumbers();
        int tokenType;
        int returnValue = 0;

        try {
            if ((tokenType = st.nextToken()) != StreamTokenizer.TT_EOF) {
                if (tokenType == StreamTokenizer.TT_NUMBER) {
                    returnValue = (int) st.nval;
                }
            }
        }
        catch (IOException e) {
            Log.e(TAG, "Error: parseIntegerString - " + e);
        }
        return returnValue;
    } // end parseIntegerString

    // multi-field string
    public String[] parseMFString(String mfString) {
        Vector<String> strings = new Vector<String>();

        StringReader sr = new StringReader(mfString);
        StreamTokenizer st = new StreamTokenizer(sr);
        st.quoteChar('"');
        st.quoteChar('\'');
        String[] mfStrings = null;

        int tokenType;
        try {
            while ((tokenType = st.nextToken()) != StreamTokenizer.TT_EOF) {

                strings.add(st.sval);

            }
        } catch (IOException e) {

            Log.d(TAG, "String parsing Error: " + e);

            e.printStackTrace();
        }
        mfStrings = new String[strings.size()];
        for (int i = 0; i < strings.size(); i++) {
            mfStrings[i] = strings.get(i);
        }
        return mfStrings;
    } // end parseMFString

    protected void parseNumbersString(String numberString, int componentType,
                                    int componentCount) {
        //preprocessing to get rid of 'e' or 'E' exponent
        // otherwise the SAX parser splits 3e-2 (which is .03) into two
        // number, 3 and -2.
        if ( (numberString.indexOf('e') != -1) || (numberString.indexOf('E') != -1) ) {
            int stringPos = 0;
            String newReplacementString = "";
            // we do assume the numberString won't be mixing 'e' and 'E'
            // but we want to handle any either character
            char eChar = 'e';
            if ( numberString.indexOf('E') != -1 ) eChar = 'E';
            while (numberString.indexOf(eChar, stringPos) != -1)  {
                int ePos = numberString.indexOf(eChar, stringPos);
                // check for the first space or comma before 'e'.
                int prevSpaceBefore_e = Math.max( (numberString.lastIndexOf(' ', ePos)), (numberString.lastIndexOf(',', ePos)) );
                prevSpaceBefore_e++;
                // Copy from the current position until the comma or space before the 'e'
                newReplacementString += numberString.substring(stringPos, prevSpaceBefore_e);
                // check for the next space or comma after 'e'.
                // Otherwise, we might be at the end of the string.
                int nextSpaceAfter_e = Math.min( (numberString.indexOf(' ', ePos)), (numberString.indexOf(',', ePos)) );
                if (nextSpaceAfter_e == -1) {
                    // at the end of the original string, find the last space, comma or EOL of the string
                    nextSpaceAfter_e = Math.max( (numberString.indexOf(' ', ePos)), (numberString.indexOf(',', ePos)) );
                    if (nextSpaceAfter_e == -1) nextSpaceAfter_e = Math.max( nextSpaceAfter_e, numberString.length() );
                }
                String exponentString = numberString.substring(ePos+1, nextSpaceAfter_e);
                try {
                    double newReplacementNumber =
                            ( (float) (new Float( numberString.substring(prevSpaceBefore_e, ePos) )) )
                                    * Math.pow(10, (int) (new Integer(exponentString)) );
                    // At < 1/1000th, it might be more efficient as 0.
                    if ( Math.abs(newReplacementNumber) < .001 ) newReplacementNumber = 0;
                    newReplacementString += " " + newReplacementNumber + " ";
                }
                catch (java.lang.NumberFormatException exception) {
                    Log.e(TAG, "NumberFormatException in " + numberString.substring(prevSpaceBefore_e, nextSpaceAfter_e) + "; " + exception);
                }
                catch (Exception exception) {
                    Log.e(TAG, "Exception in " + numberString.substring(prevSpaceBefore_e, nextSpaceAfter_e) + "; " + exception);
                }
                stringPos = nextSpaceAfter_e;
            }
            // Add any remainder of a string
            numberString = newReplacementString + numberString.substring(stringPos, numberString.length());
        }
        StringReader sr = new StringReader(numberString);
        StreamTokenizer st = new StreamTokenizer(sr);
        st.parseNumbers();
        int tokenType;
        short componentShort[] = new short[componentCount];
        float componentFloat[] = new float[componentCount];
        try {
            int index = 0;
            while ((tokenType = st.nextToken()) != StreamTokenizer.TT_EOF) {
                if (tokenType == StreamTokenizer.TT_NUMBER) {

                    // first componentType's parse for short values
                    // (integers) and will have no exponents

                    if (componentType == indexedFaceSetComponent) {
                        if ((int) st.nval != -1) {
                            meshCreator.addPositionIndex((int) st.nval);
                            index++;
                            if (index == componentCount) {
                                index = 0;
                            }
                        }
                    } else if (componentType == textureIndexComponent) {
                        if ((int) st.nval != -1) {
                            meshCreator.addTexcoordIndex((int) st.nval);
                            index++;
                            if (index == componentCount) {
                                index = 0;
                            }
                        }
                    } else if (componentType == normalIndexComponent) {
                        if ((int) st.nval != -1) {
                            meshCreator.addNormalIndex((int) st.nval);
                            index++;
                            if (index == componentCount) {
                                index = 0;
                            }
                        }
                    }

                    // The rest of these will be parsing floats that could
                    // have 'e' exponent value.  3DSMax will export X3D/VRML
                    // with the 'e' exponent
                    //TODO: check for 'e' exponent values.

                    else if (componentType == verticesComponent) {
                        componentFloat[index] = (float) (st.nval);
                        index++;
                        if (index == componentCount) {
                            meshCreator.addInputPosition(componentFloat);
                            index = 0;
                        }
                    } else if (componentType == textureCoordComponent) {
                        componentFloat[index] = (float) st.nval;
                        index++;
                        if (index == componentCount) {
                            meshCreator.addInputTexcoord(componentFloat);
                            index = 0;
                        }
                    } else if (componentType == normalsComponent) {
                        componentFloat[index] = (float) st.nval;
                        index++;
                        if (index == componentCount) {
                            meshCreator.addInputNormal(componentFloat);
                            index = 0;
                        }
                    } else if (componentType == interpolatorKeyComponent) {
                        componentFloat[index] = (float) st.nval;
                        index++;
                        if (index == componentCount) {
                            mX3DObject.AddKeys(componentFloat[0]);
                            index = 0;
                        }
                    } else if (componentType == interpolatorKeyValueComponent) {
                        componentFloat[index] = (float) st.nval;
                        index++;
                        if (index == componentCount) {
                            mX3DObject.AddKeyValues(componentFloat);
                            index = 0;
                        }
                    } else if (componentType == LODComponent) {
                        componentFloat[index] = (float) st.nval;
                        mX3DObject.AddKeys(componentFloat[0]);
                    } else if (componentType == elevationGridHeight) {
                        // Elevation Grid not currently supported
                        mX3DObject.floatArray.add(new Float((float) st.nval));
                    }

                } // end if token = number
            } // end while loop
        } // end try statement
        catch (IOException e) {
            Log.e(TAG, "Error: parseNumbersString - " + e);
        }
    } // parseNumbersString

    /*
    private void AddKeys(float key)
    {
        Key newKey = new Key(key);
        X3Dobject.keys.add(newKey);
    }


    private void AddKeyValues(float[] values)

    {
        KeyValue newKeyValue = new KeyValue(values);
        keyValues.add(newKeyValue);
    }
    */



    /**
     * This class facilitates construction of GearVRF meshes from X3D data.
     * X3D can have different indices for positions, normals and texture coordinates.
     * GearVRF has a single set of indices into a vertex array which may have
     * position, normal and texcoord components.
     * <p>
     * As the X3D file is parsed, the indices and vertex data are accumulated
     * internally to this class. When the entire mesh has been parsed,
     * a GVRIndexBuffer and GVRVertexBuffer is produced from the X3D data.
     * Every effort is made to use the original vertices and indices if possible.
     * Vertices are only duplicated if necessary.
     * <p>
     * This class uses the same data areas over again so you will require an
     * instance for each mesh you want to parse simultaneously. The current
     * X3D parser is sequential so it only needs a single instance of this
     * class per X3D file parsed.
     */
    static class MeshCreatorX
    {
        static class FloatArray
        {
            private float[] mData;
            private int     mCurSize;
            private int     mMinSize;

            FloatArray(int initialSize)
            {
                mMinSize = initialSize;
            }

            float[] array() { return mData; }

            int getSize() { return mCurSize; }

            void fill(float v) { Arrays.fill(mData, v); }

            void setCapacity(int c)
            {
                if ((mData == null) || (c > mData.length))
                {
                    mData = new float[c];
                }
            }

            void clear()
            {
                mCurSize = 0;
            }

            void get(int index, float[] entry)
            {
                for (int i = 0; i < entry.length; ++i)
                {
                    entry[i] = mData[index + i];
                }
            }

            float get(int index)
            {
                return mData[index];
            }

            void set(int index, Vector3f v)
            {
                mData[index] = v.x;
                mData[index + 1] = v.y;
                mData[index + 2] = v.z;
            }

            void get(int index, Vector3f v)
            {
                v.x = mData[index];
                v.y = mData[index + 1];
                v.z = mData[index + 2];
            }

            void add(float[] entry)
            {
                if (mData == null)
                {
                    mData = new float[mMinSize];
                }
                else if (mCurSize + entry.length > mData.length)
                {
                    mData = Arrays.copyOf(mData, (mCurSize * 3) / 2);
                }
                for (int i = 0; i < entry.length; ++i)
                {
                    mData[mCurSize + i] = entry[i];
                }
                mCurSize += entry.length;
            }
        };

        static class IntArray
        {
            private int[]   mData;
            private int     mCurSize;
            private int     mMinSize;

            IntArray(int initialSize)
            {
                mMinSize = initialSize;
            }

            int[] array() { return mData; }

            int getSize() { return mCurSize; }

            void setCapacity(int c)
            {
                if ((mData == null) || (c > mData.length))
                {
                    mData = new int[c];
                }
            }

            void clear()
            {
                mCurSize = 0;
            }

            int get(int index)
            {
                return mData[index];
            }

            void add(int v)
            {
                if (mData == null)
                {
                    mData = new int[mMinSize];
                }
                else if (mCurSize + 1 > mData.length)
                {
                    mData = Arrays.copyOf(mData, (mCurSize * 3) / 2);
                }
                mData[mCurSize++] = v;
            }
        };
/*
        private X3Dobject.MeshCreator.IntArray mPositionIndices = new X3Dobject.MeshCreator.IntArray(64);
        private X3Dobject.MeshCreator.IntArray mNormalIndices = new X3Dobject.MeshCreator.IntArray(64);
        private X3Dobject.MeshCreator.IntArray mTexcoordIndices = new X3Dobject.MeshCreator.IntArray(64);
        private X3Dobject.MeshCreator.FloatArray mInputPositions = new X3Dobject.MeshCreator.FloatArray(64 * 3);
        private X3Dobject.MeshCreator.FloatArray mInputNormals = new X3Dobject.MeshCreator.FloatArray(64 * 3);
        private X3Dobject.MeshCreator.FloatArray mInputTexCoords = new X3Dobject.MeshCreator.FloatArray(64 * 3);
        private X3Dobject.MeshCreator.FloatArray mOutputPositions = new X3Dobject.MeshCreator.FloatArray(64 * 3);
        private X3Dobject.MeshCreator.FloatArray mOutputNormals = new X3Dobject.MeshCreator.FloatArray(64 * 3);
        private X3Dobject.MeshCreator.FloatArray mOutputTexCoords = new X3Dobject.MeshCreator.FloatArray(64 * 3);

 */

        protected IntArray mPositionIndices = new IntArray(64);
        protected IntArray mNormalIndices = new IntArray(64);
        protected IntArray mTexcoordIndices = new IntArray(64);
        protected FloatArray mInputPositions = new FloatArray(64 * 3);
        protected FloatArray mInputNormals = new FloatArray(64 * 3);
        protected FloatArray mInputTexCoords = new FloatArray(64 * 3);
        private FloatArray mOutputPositions = new FloatArray(64 * 3);
        private FloatArray mOutputNormals = new FloatArray(64 * 3);
        private FloatArray mOutputTexCoords = new FloatArray(64 * 3);
        private GVRContext mContext;
        private DefinedItem mVertexBufferDefine;
        private float mMaxYTexcoord = Float.NEGATIVE_INFINITY;
        private boolean mUseNormals;
        private boolean mUseTexCoords;

        MeshCreatorX(GVRContext ctx, EnumSet<GVRImportSettings> settings)
        {
            mContext = ctx;
            mVertexBufferDefine = null;
            mUseNormals = !settings.contains(GVRImportSettings.NO_LIGHTING);
            mUseTexCoords = !settings.contains(GVRImportSettings.NO_TEXTURING);
        }

        void clear()
        {
            mOutputPositions.clear();
            mOutputNormals.clear();
            mOutputTexCoords.clear();
            mInputPositions.clear();
            mInputNormals.clear();
            mInputTexCoords.clear();
            mPositionIndices.clear();
            mNormalIndices.clear();
            mTexcoordIndices.clear();
            mMaxYTexcoord = Float.NEGATIVE_INFINITY;
        }

        void defineVertexBuffer(DefinedItem item)
        {
            mVertexBufferDefine = item;
        }

        /*
         * Add a new X3D position index to use in later generating the vertex buffer.
         * These indices are the same as those in the X3D file.
         */
        void addPositionIndex(int index)
        {
            mPositionIndices.add(index);
        }

        /*
         * Add a new X3D normal index to use in later generating vertex buffer.
         * These indices are the same as those in the X3D file.
         */
        void addNormalIndex(int index)
        {
            if (mUseNormals)
            {
                mNormalIndices.add(index);
            }
        }

        /*
         * Add a new X3D texture coordinate index to use in later generating the vertex buffer.
         * These indices are the same as those in the X3D file.
         */
        void addTexcoordIndex(int index)
        {
            if (mUseTexCoords) {
                mTexcoordIndices.add(index);
            }
        }

        /*
         * Add a position to the input vertex storage array.
         * These positions are the same as in the X3D file
         * and they will probably not match the output positions
         * because vertices may be duplicated. X3D keeps a separate
         * index table for positions, normals and texture coordinates.
         * GearVRF keeps a single index table.
         */
        void addInputPosition(float[] pos)
        {
            mInputPositions.add(pos);
        }

        /*
         * Add a normal to the input vertex storage array.
         * These normals are the same as in the X3D file
         * and they will probably not match the output normals
         * because vertices may be duplicated. X3D keeps a separate
         * index table for positions, normals and texture coordinates.
         * GearVRF keeps a single index table.
         */
        void addInputNormal(float[] norm)
        {
            if (mUseNormals) {
                mInputNormals.add(norm);
            }
        }

        /*
         * Add a texture coordinate to the input vertex storage array.
         * These texture coordinates are the same as in the X3D file
         * and they will probably not match the output texture coordinates
         * because vertices may be duplicated. X3D keeps a separate
         * index table for positions, normals and texture coordinates.
         * GearVRF keeps a single index table.
         */
        void addInputTexcoord(float[] tc)
        {
            if (mUseTexCoords) {
                if (tc[1] > mMaxYTexcoord)
                {
                    mMaxYTexcoord = tc[1];
                }
                mInputTexCoords.add(tc);
            }
        }

        /*
         * Generates normals for the output vertices by computing
         * face normals and averaging them.
         * First generate the polygon normal from the cross product of any
         * 2 lines of the polygon.  Second, for each vertex, sum the polygon
         * normals shared by this vertex. Then normalize the normals.
         * The resulting normals are in mOutputNormals.
         */
        private void generateNormals(int[] faces, int numIndices, FloatArray positions)
        {
            Vector3f side0 = new Vector3f();
            Vector3f side1 = new Vector3f();
            Vector3f normal = new Vector3f();
            try
            {
                mInputNormals.setCapacity(numIndices * 3);
                mOutputNormals.setCapacity(positions.getSize());
                mOutputNormals.fill(0.0f);
                /*
                 * Compute face normals
                 */
                for (int f = 0; f < numIndices; f += 3)
                {
                    int v1Index = faces[f] * 3;
                    int v2Index = faces[f + 1] * 3;
                    int v3Index = faces[f + 2] * 3;

                    side0.setComponent(0, positions.get(v1Index) - positions.get(v2Index));
                    side0.setComponent(1, positions.get(v1Index + 1) - positions.get(v2Index + 1));
                    side0.setComponent(2, positions.get(v1Index + 2) - positions.get(v2Index + 2));
                    side1.setComponent(0, positions.get(v2Index) - positions.get(v3Index));
                    side1.setComponent(1, positions.get(v2Index + 1) - positions.get(v3Index + 1));
                    side1.setComponent(2, positions.get(v2Index + 2) - positions.get(v3Index + 2));
                    side0.cross(side1, normal);
                    normal.normalize();
                    mInputNormals.set(f * 3, normal);
                }
                /*
                 * Add face normals to produce vertex normals
                 */
                float[] normals = mOutputNormals.array();
                for (int f = 0; f < numIndices; f += 3)
                {
                    int v1Index = faces[f] * 3;
                    int v2Index = faces[f + 1] * 3;
                    int v3Index = faces[f + 2] * 3;

                    mInputNormals.get(f * 3, normal);
                    normals[v1Index] += normal.x;
                    normals[v1Index + 1] += normal.y;
                    normals[v1Index + 2] += normal.z;
                    normals[v2Index] += normal.x;
                    normals[v2Index + 1] += normal.y;
                    normals[v2Index + 2] += normal.z;
                    normals[v3Index] += normal.x;
                    normals[v3Index + 1] += normal.y;
                    normals[v3Index + 2] += normal.z;
                }
                /*
                 * Normalize output normals
                 */
                for (int i = 0; i < mOutputNormals.getSize(); ++i)
                {
                    int nindex = i * 3;
                    mOutputNormals.get(nindex, normal);
                    normal.normalize();
                    mOutputNormals.set(nindex, normal);
                }
            }
            catch (Exception e)
            {
                Log.e(TAG, e.toString());
            }
        }  //  end generateNormals

        /*
         * Create a vertex and index buffer from the X3D indices,
         * positions, normals and texture coordinates.
         * X3D keeps a separate index table for positions, normals
         * and texture coordinates which allows for more sharing.
         * GearVRF keeps a single index table for the triangles
         * so there must be the same number of positions, normals
         * and texture coordinates. This function converts the
         * X3D input data into a GVRVertexBuffer and GVRIndexBuffer.
         */
        GVRVertexBuffer organizeVertices(GVRIndexBuffer ibuf)
        {
            boolean hasTexCoords = mUseTexCoords & (mInputTexCoords.getSize() > 0);;
            boolean hasNormals = mInputNormals.getSize() > 0;
            String descriptor = "float3 a_position";

            if (hasTexCoords)
            {
                descriptor += " float2 a_texcoord";
            }
            if (mUseNormals)
            {
                descriptor += " float3 a_normal";
            }
            /*
             * If there are no texture coordinates or normals,
             * we can just copy the input positions directly from
             * X3D and generate normals if necessary.
             */
            if (!hasTexCoords && !hasNormals)
            {
                return copyVertices(descriptor, ibuf, mUseNormals);
            }
            /*
             * If the X3D file does not have normal or texcoord indices,
             * we can just copy the input data directly from X3D
             * because the positions, normals and texcoord arrays
             * are all in the same order.
             */
            if ((mTexcoordIndices.getSize() == 0) &&
                    (mNormalIndices.getSize() == 0))
            {
                return copyVertices(descriptor, ibuf, mUseNormals);
            }

            /*
             * The X3D file has different index tables for positions,
             * normals and texture coordinates. We must regenerate the
             * vertex table to duplicate vertices in the cases where
             * a position has more than one normal or textoord.
             */
            Map<String, Integer> vertexMap = new LinkedHashMap<String, Integer>();
            int[] newIndices = new int[mPositionIndices.getSize()];
            float[] pos = new float[3];
            float[] norm = new float[3];
            float[] tc = new float[2];
            int[] normalIndices = (mNormalIndices.getSize() > 0) ? mNormalIndices.array() : mPositionIndices.array();
            int[] texcoordIndices = (mTexcoordIndices.getSize() > 0) ? mTexcoordIndices.array() : mPositionIndices.array();

            /*
             * Scan all the faces and compose the set of unique vertices
             * (where a vertex has a position, normal and texcoord)
             */
            mOutputPositions.setCapacity(mInputPositions.getSize());
            for (char f = 0; f < mPositionIndices.getSize(); f++)
            {
                String key = "";
                int vindex = mPositionIndices.get(f) * 3;

                mInputPositions.get(vindex, pos);
                key += String.valueOf(pos[0]) + String.valueOf(pos[1]) + String.valueOf(pos[2]);
                if (hasTexCoords)
                {
                    int tindex = texcoordIndices[f] * 2;
                    mInputTexCoords.get(tindex, tc);
                    // flip the Y texture coordinate
                    //tc[1] = -tc[1];
                    tc[1] = mMaxYTexcoord - tc[1];
                    key += String.valueOf(tc[0]) + String.valueOf(tc[1]);
                }
                if (hasNormals)
                {
                    int nindex = normalIndices[f] * 3;
                    mInputNormals.get(nindex, norm);
                    key += String.valueOf(norm[0]) + String.valueOf(norm[1]) + String.valueOf(norm[2]);
                }
                Integer newindex = vertexMap.get(key);
                if (newindex == null)
                {
                    newindex = vertexMap.size();
                    vertexMap.put(key, newindex);
                    mOutputPositions.add(pos);
                    if (hasNormals)
                    {
                        mOutputNormals.add(norm);
                    }
                    if (hasTexCoords)
                    {
                        mOutputTexCoords.add(tc);
                    }
                }
                newIndices[f] = newindex;
            }
            GVRVertexBuffer vbuffer = new GVRVertexBuffer(mContext, descriptor, mOutputPositions.getSize() / 3);
            if (mVertexBufferDefine != null)
            {
                mVertexBufferDefine.setVertexBuffer(vbuffer);
            }
            vbuffer.setFloatArray("a_position", mOutputPositions.array(), 3, 0);
            if (!hasNormals)
            {
                generateNormals(newIndices, newIndices.length, mOutputPositions);
            }
            else if (mUseNormals)
            {
                vbuffer.setFloatArray("a_normal", mOutputNormals.array(), 3, 0);
            }
            if (hasTexCoords)
            {
                vbuffer.setFloatArray("a_texcoord", mOutputTexCoords.array(), 2, 0);
            }
            ibuf.setIntVec(newIndices);
            clear();
            return vbuffer;
        }

        /*
         * Create a vertex and index buffer from the X3D indices,
         * and positions.
         * X3D keeps a separate index table for positions, normals
         * and texture coordinates which allows for more sharing.
         * GearVRF keeps a single index table for the triangles
         * so there must be the same number of positions, normals
         * and texture coordinates. This function copies the positions
         * from X3D input data into a GVRVertexBuffer and GVRIndexBuffer.
         * It optionally generates normals. Because there are no texture
         * coordinates, the order of the vertices is the same as in
         * the X3D file.
         */
        public GVRVertexBuffer copyVertices(String descriptor, GVRIndexBuffer ibuf, boolean makeNormals)
        {
            GVRVertexBuffer vbuffer = new GVRVertexBuffer(mContext, descriptor, mInputPositions.getSize() / 3);
            if (mVertexBufferDefine != null)
            {
                mVertexBufferDefine.setVertexBuffer(vbuffer);
            }
            vbuffer.setFloatArray("a_position", mInputPositions.array(), 3, 0);
            if (mInputNormals.getSize() == 0)
            {
                if (makeNormals)
                {
                    generateNormals(mPositionIndices.array(), mPositionIndices.getSize(), mInputPositions);
                    vbuffer.setFloatArray("a_normal", mOutputNormals.array(), 3, 0);
                }
            }
            else
            {
                if (mInputNormals.getSize() != mInputPositions.getSize())
                {
                    throw new UnsupportedOperationException("MeshCreator.copyVertices requires input positions and normals to be the same length");
                }
                vbuffer.setFloatArray("a_normal", mInputNormals.array(), 3, 0);
            }
            if (mInputTexCoords.getSize() > 0)
            {
                // flip the Y texture coordinate

                float[] texCoords = mInputTexCoords.array().clone();
                int n = texCoords.length;
                for(int i=1; i<n; i+=2)
                {
                    //texCoords[i] = -texCoords[i];
                    texCoords[i] = mMaxYTexcoord - texCoords[i];
                }
                vbuffer.setFloatArray("a_texcoord", texCoords, 2, 0);
            }
            ibuf.setIntVec(mPositionIndices.array());
            clear();
            return vbuffer;
        }
    }  //  end MeshCreator




    protected GVRVideoSceneObjectPlayer<ExoPlayer> makeExoPlayer(String movieFileName ) {

        GVRVideoSceneObjectPlayer<ExoPlayer> gvrVideoSceneObjectPlayer = null;

        try {
            //final Context context = activityContext;
            final Context context = gvrContext.getContext();
            final String movieFileNameFinal = movieFileName;

            DataSource.Factory dataSourceFactory = new DataSource.Factory() {
                @Override
                public DataSource createDataSource() {
                    return new AssetDataSource(context);
                }
            };

            final MediaSource mediaSource = new ExtractorMediaSource(Uri.parse("asset:///" + movieFileName),
                    dataSourceFactory,
                    new DefaultExtractorsFactory(), null, null);

            final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context,
                    new DefaultTrackSelector());
            player.prepare(mediaSource);
            Log.e(TAG, "Load movie " + movieFileNameFinal + ".");

            gvrVideoSceneObjectPlayer = new GVRVideoSceneObjectPlayer<ExoPlayer>() {
                @Override
                public ExoPlayer getPlayer() {
                    return player;
                }

                @Override
                public void setSurface(final Surface surface) {
                    player.addListener(new Player.DefaultEventListener() {
                        @Override
                        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                            switch (playbackState) {
                                case Player.STATE_BUFFERING:
                                    break;
                                case Player.STATE_ENDED:
                                    player.seekTo(0);
                                    break;
                                case Player.STATE_IDLE:
                                    break;
                                case Player.STATE_READY:
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    player.setVideoSurface(surface);
                }

                @Override
                public void release() {
                    player.release();
                }

                @Override
                public boolean canReleaseSurfaceImmediately() {
                    return false;
                }

                @Override
                public void pause() {
                    player.setPlayWhenReady(false);
                }

                @Override
                public void start() {
                    Log.e(TAG, "movie start.");
                    player.setPlayWhenReady(true);
                }

                @Override
                public boolean isPlaying() {
                    return player.getPlayWhenReady();
                }
            };
        }
        catch (Exception e) {
            Log.e(TAG, "Exception makeExoPlayer: " + e);
        }
        return gvrVideoSceneObjectPlayer;
    }  //  end makeExoPlayer



} // end Utility
