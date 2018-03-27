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
import android.graphics.Color;

import org.gearvrf.GVRExternalScene;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRScene;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.GVRShaderId;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.utility.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.gearvrf.script.GVRJavascriptScriptFile;
import org.gearvrf.script.javascript.GVRJavascriptV8File;
import org.joml.Matrix3f;
import org.joml.Vector2f;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.gearvrf.x3d.data_types.SFVec2f;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRCamera;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRIndexBuffer;
import org.gearvrf.GVRLODGroup;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSpotLight;
import org.gearvrf.GVRSwitch;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.GVRTextureParameters.TextureWrapType;
import org.gearvrf.GVRTransform;
import org.gearvrf.GVRVertexBuffer;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import org.joml.Vector3f;
import org.joml.Quaternionf;
import java.util.EnumSet;

public class X3Dobject {
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
    static class MeshCreator
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

        private IntArray mPositionIndices = new IntArray(64);
        private IntArray mNormalIndices = new IntArray(64);
        private IntArray mTexcoordIndices = new IntArray(64);
        private FloatArray mInputPositions = new FloatArray(64 * 3);
        private FloatArray mInputNormals = new FloatArray(64 * 3);
        private FloatArray mInputTexCoords = new FloatArray(64 * 3);
        private FloatArray mOutputPositions = new FloatArray(64 * 3);
        private FloatArray mOutputNormals = new FloatArray(64 * 3);
        private FloatArray mOutputTexCoords = new FloatArray(64 * 3);
        private GVRContext mContext;
        private DefinedItem mVertexBufferDefine;
        private boolean mUseNormals;
        private boolean mUseTexCoords;

        MeshCreator(GVRContext ctx, EnumSet<GVRImportSettings> settings)
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
            if (mUseNormals && hasNormals)
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
                    tc[1] = -tc[1];
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
                    texCoords[i] = -texCoords[i];
                }
                vbuffer.setFloatArray("a_texcoord", texCoords, 2, 0);
            }
            ibuf.setIntVec(mPositionIndices.array());
            clear();
            return vbuffer;
        }
    }


    /**
     * Allows developers to access the root of X3D scene graph
     * by calling: GVRSceneObject.getSceneObjectByName(X3D_ROOT_NODE);
     */
    public static final String X3D_ROOT_NODE = "x3d_root_node_";

    private static final String TAG = "X3DObject";

    public GVRShaderId x3DShader;

    // Like a C++ pre-compiler switch to select shaders.
    // Default is true to use Universal lights shader.
    public final static boolean UNIVERSAL_LIGHTS = true;

    private final static String JAVASCRIPT_IMPORT_PACKAGE = "importPackage(org.gearvrf.x3d.data_types)\nimportPackage(org.joml)\nimportPackage(org.gearvrf)";

    // Strings appended to GVRScene names when there are multiple
    // animations on the same <Transform> or GVRSceneObject

    private static final String TRANSFORM_CENTER_ = "_Transform_Center_";
    private static final String TRANSFORM_NEGATIVE_CENTER_ = "_Transform_Neg_Center_";
    public static final String TRANSFORM_ROTATION_ = "_Transform_Rotation_";
    public static final String TRANSFORM_TRANSLATION_ = "_Transform_Translation_";
    public static final String TRANSFORM_SCALE_ = "_Transform_Scale_";
    private static final String TRANSFORM_SCALE_ORIENTATION_ = "_Transform_Scale_Orientation_";
    private static final String TRANSFORM_NEGATIVE_SCALE_ORIENTATION_ = "_Transform_Neg_Scale_Orientation_";

    // Append this incremented value to GVRScene names to insure unique
    // GVRSceneObjects
    // when new GVRScene objects are generated to support animation
    private static int animationCount = 1;


    private final static int verticesComponent = 1;
    private final static int normalsComponent = 2;
    private final static int textureCoordComponent = 3;
    private final static int indexedFaceSetComponent = 4;
    private final static int normalIndexComponent = 5;
    private final static int textureIndexComponent = 6;
    private final static int interpolatorKeyComponent = 7;
    private final static int interpolatorKeyValueComponent = 8;
    private final static int LODComponent = 9;
    private final static int elevationGridHeight = 10;
    private boolean reorganizeVerts = false;

    private static final float CUBE_WIDTH = 20.0f; // used for cube maps
    private GVRAssetLoader.AssetRequest assetRequest = null;
    private GVRContext gvrContext = null;
    private Context activityContext = null;

    private GVRSceneObject root = null;
    /**
     * Array list of DEFined items Clones objects with 'USE' parameter
     * As public, enables implementation of HTML5 DOM's
     * getElementByTagName() method.
     */
    public Vector<DefinedItem> mDefinedItems = new Vector<DefinedItem>();


    // When Translation object has multiple properties (center, scale, rotation
    // plus translation)
    // the mesh must be attached to the bottom of these multiply attached
    // GVRSceneObject's but
    // the currentSceneObject's parent must be with the original Transform's
    // parent.
    private GVRSceneObject currentSceneObject = null;

    // Since Script Object are made of multiple parts during parsing,
    // this variable will be the active ScriptObject being built
    private ScriptObject currentScriptObject = null;

    // Since GVRShapeObject contains LOD range, 'shapeLODSceneObj' is used only
    // when it's embedded into a Level-of-Detail
    private GVRSceneObject shapeLODSceneObject = null;

    // points to a sensor that wraps around other nodes.
    private Sensor currentSensor = null;

    private GVRSceneObject meshAttachedSceneObject = null;
    private GVRRenderData gvrRenderData = null;
    private GVRVertexBuffer gvrVertexBuffer = null;
    private GVRIndexBuffer gvrIndexBuffer = null;
    private GVRMaterial gvrMaterial = null;
    private boolean gvrMaterialUSEd = false; // for DEFine and USE gvrMaterial for
    // x3d APPEARANCE and MATERIAL nodes
    private boolean gvrRenderingDataUSEd = false; // for DEFine and USE
    // gvrRenderingData for x3d
    // SHAPE node
    private boolean gvrGroupingNodeUSEd = false; // for DEFine and USE
    // gvrSceneObject for x3d
    // TRANSFORM and GROUP nodes


    private GVRTextureParameters gvrTextureParameters = null;
    private GVRTexture gvrTexture = null;
    private ArrayList<ScriptObject> scriptObjects = new ArrayList<ScriptObject>();

    private Vector<Key> keys = new Vector<Key>();
    private Vector<KeyValue> keyValues = new Vector<KeyValue>();
    private Vector<Float> floatArray = new Vector<Float>();

    private Vector<TimeSensor> timeSensors = new Vector<TimeSensor>();
    private Vector<Interpolator> interpolators = new Vector<Interpolator>();

    private Vector<InlineObject> inlineObjects = new Vector<InlineObject>();
    private MeshCreator meshCreator = null;

    /**
     * public list of <Viewpoints> since camera position can be
     * changed in real-time
     */
    public Vector<Viewpoint> viewpoints = new Vector<Viewpoint>();

    /**
     * Array List of sensors can be accessed in real-time
     * such as in the onStep() function
     */
    public Vector<Sensor> sensors = new Vector<Sensor>();
    public Vector<EventUtility> eventUtilities = new Vector<EventUtility>();


    private ShaderSettings shaderSettings = null;
    private GVRTextViewSceneObject gvrTextViewSceneObject = null;

    private LODmanager lodManager = null;
    private GVRCameraRig cameraRigAtRoot = null;

    private AnimationInteractivityManager animationInteractivityManager = null;

    // set true in <SCRIPT> tag so SAX XML parser will parse JavaScript
    //    inside its characters function.
    private boolean parseJavaScript = false;
    // holds complete JavaScript code per <SCRIPT> tag
    private String javaScriptCode = "";

    // contains the directory structure for inlines to be appended in front of
    // references to texture map file names (plus their own sub-directory.
    private String inlineSubdirectory = "";

    private String indexedSetDEFName = "";
    private String indexedSetUSEName = "";

    // Internal settings from AssetRequest
    private boolean blockLighting = false;
    private boolean blockTexturing = false;




    // The Text_Font Params class and Reset() function handle
    // the values set in the <Text> and <FontStyle> nodes, which are
    // then passed to the GVRTextViewSceneObject constructor
    private static class Text_FontParams {
        static float[] length = null;
        static float maxExtent = 0;
        static String nameTextAttribute = ""; // DEFind name associated with Text node
        static String string = ""; // the actual text to be shown in the scene
        static boolean solid = false;

        static String nameFontStyle = ""; // DEFind name associated with FontStyle node
        static String family = GVRTextViewSceneObject.DEFAULT_FONT;
        static GVRTextViewSceneObject.justifyTypes justify = GVRTextViewSceneObject.justifyTypes.BEGIN;
        static float spacing = 0.0f;
        static float size = 10.0f;
        static GVRTextViewSceneObject.fontStyleTypes style = GVRTextViewSceneObject.fontStyleTypes.PLAIN;
    };

    private void Init_Text_FontParams() {
        Text_FontParams.length = null;
        Text_FontParams.maxExtent = 0;
        Text_FontParams.nameTextAttribute = ""; // DEFind name associated with Text node
        Text_FontParams.string = "";
        Text_FontParams.solid = false;

        Text_FontParams.nameFontStyle = ""; // DEFind name associated with FontStyle node
        Text_FontParams.family = GVRTextViewSceneObject.DEFAULT_FONT;
        Text_FontParams.justify = GVRTextViewSceneObject.justifyTypes.BEGIN;
        Text_FontParams.spacing = 0.0f;
        Text_FontParams.size = 10.0f;
        Text_FontParams.style = GVRTextViewSceneObject.fontStyleTypes.PLAIN;
    }

    /**
     * X3DObject parses and X3D file using Java SAX parser.
     * Constructor sets up camera rig structure and
     * enables getting to the root of the scene graph by
     * calling GVRSceneObject.getSceneObjectByName(X3D_ROOT_NODE);
     */
    /*********************************************/
    /********** X3Dobject Constructor ************/
    /*********************************************/
    public X3Dobject(GVRAssetLoader.AssetRequest assetRequest,
                     GVRSceneObject root) {
        try {
            this.assetRequest = assetRequest;
            this.gvrContext = assetRequest.getContext();
            this.activityContext = gvrContext.getContext();
            this.root = root;
            x3DShader = gvrContext.getShaderManager().getShaderType(X3DShader.class);


            EnumSet<GVRImportSettings> settings = assetRequest.getImportSettings();
            blockLighting = settings.contains(GVRImportSettings.NO_LIGHTING);
            blockTexturing = settings.contains(GVRImportSettings.NO_TEXTURING);

            meshCreator = new MeshCreator(this.gvrContext, settings);
            // Camera rig setup code based on GVRScene::init()
            GVRCamera leftCamera = new GVRPerspectiveCamera(gvrContext);
            leftCamera.setRenderMask(GVRRenderMaskBit.Left);

            GVRCamera rightCamera = new GVRPerspectiveCamera(gvrContext);
            rightCamera.setRenderMask(GVRRenderMaskBit.Right);

            GVRPerspectiveCamera centerCamera = new GVRPerspectiveCamera(gvrContext);
            centerCamera.setRenderMask(GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
            cameraRigAtRoot = GVRCameraRig.makeInstance(gvrContext);
            cameraRigAtRoot.getOwnerObject().setName("MainCamera");
            cameraRigAtRoot.attachLeftCamera(leftCamera);
            cameraRigAtRoot.attachRightCamera(rightCamera);
            cameraRigAtRoot.attachCenterCamera(centerCamera);
            gvrContext.getMainScene().setBackgroundColor(0, 0, 0, 1);  // black background default

            lodManager = new LODmanager(root);

            animationInteractivityManager = new AnimationInteractivityManager(
                    this, gvrContext, root, mDefinedItems, interpolators,
                    sensors, timeSensors, eventUtilities, scriptObjects,
                    viewpoints, this.assetRequest
            );
        } catch (Exception e) {
            Log.e(TAG, "X3Dobject constructor error: " + e);
        }
    } // end Constructor


    /*********************************************/
    /********** Utility Functions to *************/
    /************* Assist Parsing ****************/
    /*********************************************/


    private void AddKeys(float key)

    {
        Key newKey = new Key(key);
        keys.add(newKey);
    }


    private void AddKeyValues(float[] values)

    {
        KeyValue newKeyValue = new KeyValue(values);
        keyValues.add(newKeyValue);
    }


    /**
     * @author m1.williams
     *         Java SAX parser interface
     */
    class UserHandler extends DefaultHandler {

        String attributeValue = null;

        private float[] parseFixedLengthFloatString(String numberString,
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

        private float parseSingleFloatString(String numberString,
                                             boolean constrained0to1, boolean zeroOrGreater) {
            float[] value = parseFixedLengthFloatString(numberString, 1,
                    constrained0to1,
                    zeroOrGreater);
            return value[0];
        }

        private boolean parseBooleanString(String booleanString) {
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
        }

        // multi-field string
        private String[] parseMFString(String mfString) {
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

        private int parseIntegerString(String numberString) {
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

        private void parseNumbersString(String numberString, int componentType,
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

                        if (componentType == X3Dobject.indexedFaceSetComponent) {
                            if ((int) st.nval != -1) {
                                meshCreator.addPositionIndex((int) st.nval);
                                index++;
                                if (index == componentCount) {
                                    index = 0;
                                }
                            }
                        } else if (componentType == X3Dobject.textureIndexComponent) {
                            if ((int) st.nval != -1) {
                                meshCreator.addTexcoordIndex((int) st.nval);
                                index++;
                                if (index == componentCount) {
                                    index = 0;
                                }
                            }
                        } else if (componentType == X3Dobject.normalIndexComponent) {
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

                        else if (componentType == X3Dobject.verticesComponent) {
                            componentFloat[index] = (float) (st.nval);
                            index++;
                            if (index == componentCount) {
                                    meshCreator.addInputPosition(componentFloat);
                                index = 0;
                            }
                        } else if (componentType == X3Dobject.textureCoordComponent) {
                            componentFloat[index] = (float) st.nval;
                            index++;
                            if (index == componentCount) {
                                    meshCreator.addInputTexcoord(componentFloat);
                                index = 0;
                            }
                        } else if (componentType == X3Dobject.normalsComponent) {
                            componentFloat[index] = (float) st.nval;
                            index++;
                            if (index == componentCount) {
                                    meshCreator.addInputNormal(componentFloat);
                                index = 0;
                            }
                        } else if (componentType == X3Dobject.interpolatorKeyComponent) {
                            componentFloat[index] = (float) st.nval;
                            index++;
                            if (index == componentCount) {
                                AddKeys(componentFloat[0]);
                                index = 0;
                            }
                        } else if (componentType == X3Dobject.interpolatorKeyValueComponent) {
                            componentFloat[index] = (float) st.nval;
                            index++;
                            if (index == componentCount) {
                                AddKeyValues(componentFloat);
                                index = 0;
                            }
                        } else if (componentType == X3Dobject.LODComponent) {
                            componentFloat[index] = (float) st.nval;
                            AddKeys(componentFloat[0]);
                        } else if (componentType == X3Dobject.elevationGridHeight) {
                            floatArray.add(new Float((float) st.nval));
                        }

                    } // end if token = number
                } // end while loop
            } // end try statement
            catch (IOException e) {
                Log.e(TAG, "Error: parseNumbersString - " + e);
            }
        } // parseNumbersString

        private void ReplicateGVRSceneObjStructure(String attributeValue) {
            // TODO: needs to complete implementation.  May instead
            // become a clone() or copy() function in GVRSceneObject
            // Transform or Group node to be shared / re-used
            // Transform or Group constructs a GVRSceneObject. DEF/USE 'shares'
            // that GVRSceneObject. However, having a GVRSceneObject as a child

            // of another GVRSceneObject (which a <Transform USE="..." /> would do)
            // causes an infinite loop in the renderer.
            // Solution therefore is to duplicate GVRSceneObject(s) including children
            // and share the GVRMesh and GVRMaterials.
            DefinedItem useItem = null;
            for (DefinedItem definedItem : mDefinedItems) {
                if (attributeValue.equals(definedItem.getName())) {
                    useItem = definedItem;
                    break;
                }
            }
            if (useItem != null) {
                // Get the GVRSceneObject to replicate from the DEFinedItem list.
                GVRSceneObject gvrSceneObjectDEFitem = useItem.getGVRSceneObject();
                String useItemName = useItem.getName();
                // need to parse through

                while (!gvrSceneObjectDEFitem.hasMesh()) {
                    String name = gvrSceneObjectDEFitem.getName();
                    String[] splitName = name.split(useItemName);
                    currentSceneObject = AddGVRSceneObject();
                    if (splitName.length > 1)
                        currentSceneObject.setName("USE_" + useItemName + splitName[1]);
                    currentSceneObject.getTransform()
                            .setPosition(gvrSceneObjectDEFitem.getTransform().getPositionX(),
                                    gvrSceneObjectDEFitem.getTransform().getPositionY(),
                                    gvrSceneObjectDEFitem.getTransform().getPositionZ());
                    currentSceneObject.getTransform()
                            .setRotation(gvrSceneObjectDEFitem.getTransform().getRotationW(),
                                    gvrSceneObjectDEFitem.getTransform().getRotationX(),
                                    gvrSceneObjectDEFitem.getTransform().getRotationY(),
                                    gvrSceneObjectDEFitem.getTransform().getRotationZ());
                    currentSceneObject.getTransform()
                            .setScale(gvrSceneObjectDEFitem.getTransform().getScaleX(),
                                    gvrSceneObjectDEFitem.getTransform().getScaleY(),
                                    gvrSceneObjectDEFitem.getTransform().getScaleZ());
                    gvrSceneObjectDEFitem = gvrSceneObjectDEFitem.getChildByIndex(0);
                }
                if (gvrSceneObjectDEFitem.hasMesh()) {
                    String name = gvrSceneObjectDEFitem.getName();
                    GVRRenderData gvrRenderDataDEFitem = gvrSceneObjectDEFitem
                            .getRenderData();
                    currentSceneObject = AddGVRSceneObject();
                    currentSceneObject.setName("USE_" + useItemName);
                    gvrRenderData = new GVRRenderData(gvrContext);
                    // we are backface culling by default

                    gvrRenderData.setCullFace(GVRCullFaceEnum.Back);
                    currentSceneObject.attachRenderData(gvrRenderData);
                    gvrRenderData.setMaterial(gvrRenderDataDEFitem.getMaterial());
                    gvrRenderData.setMesh(gvrRenderDataDEFitem.getMesh());
                } else {
                    ;
                }
            }
        } // end ReplicateGVRSceneObjStructure


        private GVRSceneObject AddGVRSceneObject() {
            GVRSceneObject newObject = new GVRSceneObject(gvrContext);
            if (currentSceneObject == null)
                root.addChildObject(newObject);
            else
                currentSceneObject.addChildObject(newObject);
            return newObject;

        } // end AddGVRSceneObject

        /**
         * Called by the Java SAX parser implementation
         * to parse the X3D nodes.
         */

        /*********************************************/
        /*********** Parse the X3D File **************/
        /*********************************************/
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {

            /********** Transform **********/
            if (qName.equalsIgnoreCase("transform")) {

                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) {
                    ReplicateGVRSceneObjStructure(attributeValue);
                } // end USE Transform
                else {
                    // Not a 'Transform USE="..." node
                    // so initialize with default values

                    String name = "";
                    float[] center =
                            {
                                    0, 0, 0
                            };
                    float[] rotation =
                            {
                                    0, 0, 1, 0
                            };
                    float[] scaleOrientation =
                            {
                                    0, 0, 1, 0
                            };
                    float[] scale =
                            {
                                    1, 1, 1
                            };
                    float[] translation =
                            {
                                    0, 0, 0
                            };

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    // Order for Transformations:
                    // P' = T * C * R * SR * S * -SR * -C * P
                    // T=Translation, C=Center, R=Rotation, SR=ScaleOrientation, S=Scale,
                    // and P will the Point
                    // Parsing Center value must occur before Rotation
                    String translationAttribute = attributes.getValue("translation");
                    if (translationAttribute != null) {
                        translation = parseFixedLengthFloatString(translationAttribute, 3,
                                false, false);
                    }
                    String centerAttribute = attributes.getValue("center");
                    if (centerAttribute != null) {
                        center = parseFixedLengthFloatString(centerAttribute, 3, false,
                                false);
                    }
                    String rotationAttribute = attributes.getValue("rotation");
                    if (rotationAttribute != null) {
                        rotation = parseFixedLengthFloatString(rotationAttribute, 4, false,
                                false);
                    }
                    String scaleOrientationAttribute = attributes
                            .getValue("scaleOrientation");
                    if (scaleOrientationAttribute != null) {
                        scaleOrientation = parseFixedLengthFloatString(scaleOrientationAttribute,
                                4, false, false);
                    }
                    attributeValue = attributes.getValue("scale");
                    if (attributeValue != null) {
                        scale = parseFixedLengthFloatString(attributeValue, 3, false, false);
                    }

                    currentSceneObject = AddGVRSceneObject();
                    if (name.isEmpty()) {
                        // There is no DEF, thus no animation or interactivity applied to
                        // this Transform.
                        // Therefore, just set the values in a single GVRSceneObject
                        GVRTransform transform = currentSceneObject.getTransform();
                        transform.setPosition(translation[0], translation[1],
                                translation[2]);
                        transform.rotateByAxisWithPivot((float) Math.toDegrees(rotation[3]),
                                rotation[0], rotation[1],
                                rotation[2], center[0], center[1],
                                center[2]);
                        transform.setScale(scale[0], scale[1], scale[2]);
                    } else {

                        // There is a 'DEF="...."' parameter so save GVRSceneObject
                        // to the DefinedItem's array list in case it's referenced
                        // somewhere else in the X3D file.

                        // Array list of DEFined items
                        // Clones objects with USE
                        // This transform may be animated later, which means we must have
                        // separate GVRSceneObjects
                        // for each transformation plus center and scaleOrientation if
                        // needed
                        // Order for Transformations:
                        // P' = T * C * R * SR * S * -SR * -C * P
                        // First add the translation
                        currentSceneObject.getTransform()
                                .setPosition(translation[0], translation[1], translation[2]);
                        currentSceneObject.setName(name + TRANSFORM_TRANSLATION_);
                        // now check if we have a center value.
                        if ((center[0] != 0) || (center[1] != 0) || (center[2] != 0)) {
                            currentSceneObject = AddGVRSceneObject();
                            currentSceneObject.getTransform()
                                    .setPosition(center[0], center[1], center[2]);
                            currentSceneObject.setName(name + TRANSFORM_CENTER_);
                        }
                        // add rotation
                        currentSceneObject = AddGVRSceneObject();
                        currentSceneObject.getTransform()
                                .setRotationByAxis((float) Math.toDegrees(rotation[3]),
                                        rotation[0], rotation[1], rotation[2]);
                        currentSceneObject.setName(name + TRANSFORM_ROTATION_);
                        // now check if we have a scale orientation value.
                        if ((scaleOrientation[0] != 0) || (scaleOrientation[1] != 0)
                                || (scaleOrientation[2] != 1) || (scaleOrientation[3] != 0)) {
                            currentSceneObject = AddGVRSceneObject();
                            currentSceneObject.getTransform()
                                    .setRotationByAxis((float) Math
                                                    .toDegrees(scaleOrientation[3]), scaleOrientation[0],
                                            scaleOrientation[1], scaleOrientation[2]);
                            currentSceneObject.setName(name + TRANSFORM_SCALE_ORIENTATION_);
                        }
                        // add rotation
                        currentSceneObject = AddGVRSceneObject();
                        currentSceneObject.getTransform().setScale(scale[0], scale[1],
                                scale[2]);
                        currentSceneObject.setName(name + TRANSFORM_SCALE_);
                        // if we had a scale orientation, now we have to negate it.
                        if ((scaleOrientation[0] != 0) || (scaleOrientation[1] != 0)
                                || (scaleOrientation[2] != 1) || (scaleOrientation[3] != 0)) {
                            currentSceneObject = AddGVRSceneObject();
                            currentSceneObject.getTransform()
                                    .setRotationByAxis((float) Math
                                                    .toDegrees(-scaleOrientation[3]), scaleOrientation[0],
                                            scaleOrientation[1], scaleOrientation[2]);
                            currentSceneObject
                                    .setName(name + TRANSFORM_NEGATIVE_SCALE_ORIENTATION_);
                        }
                        // now check if we have a center value.
                        if ((center[0] != 0) || (center[1] != 0) || (center[2] != 0)) {
                            currentSceneObject = AddGVRSceneObject();
                            currentSceneObject.getTransform()
                                    .setPosition(-center[0], -center[1], -center[2]);
                            currentSceneObject.setName(name + TRANSFORM_NEGATIVE_CENTER_);
                        }
                        // Actual object that will have GVRendering and GVRMesh attached
                        currentSceneObject = AddGVRSceneObject();
                        currentSceneObject.setName(name);

                        // save the object for Interactivity, Animation and Scripts
                        // The AxisAngle is saved for Scripts which is how X3D describes
                        // rotations, not quaternions.
                        DefinedItem definedItem = new DefinedItem(name, rotation[3],
                                rotation[0], rotation[1], rotation[2]);
                        definedItem.setGVRSceneObject(currentSceneObject);
                        mDefinedItems.add(definedItem); // Array list of DEFined items
                    } // end if DEF name and thus possible animation / interactivity

                    // Check if there is an active Level-of-Detail (LOD)
                    // and if so add this currentSceneObject if is
                    // a direct child of this LOD.
                    if (lodManager.isActive()) lodManager.AddLODSceneObject( currentSceneObject );

                } // not a 'Transform USE="..."' node

            } // end <Transform> node


            /********** Group **********/
            else if (qName.equalsIgnoreCase("Group")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null)

                {

                    ReplicateGVRSceneObjStructure(attributeValue);
                } else {
                    // There is a 'DEF="...."' parameter so save it to the DefinedItem's
                    // array list

                    currentSceneObject = AddGVRSceneObject();
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        currentSceneObject.setName(attributeValue);
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                        definedItem.setGVRSceneObject(currentSceneObject);
                        mDefinedItems.add(definedItem); // Array list of DEFined items
                    }

                    // Check if there is an active Level-of-Detail (LOD)
                    // and add this currentSceneObject if is
                    // a direct child of this LOD.
                    if (lodManager.isActive()) lodManager.AddLODSceneObject( currentSceneObject );
                }

            } // end <Group> node


            /********** Shape **********/
            else if (qName.equalsIgnoreCase("Shape")) {

                gvrRenderData = new GVRRenderData(gvrContext);
                gvrRenderData.setAlphaToCoverage(true);
                gvrRenderData.setRenderingOrder(GVRRenderingOrder.GEOMETRY);
                gvrRenderData.setCullFace(GVRCullFaceEnum.Back);
                shaderSettings.initializeTextureMaterial(new GVRMaterial(gvrContext, x3DShader));

                // Check if this Shape node is part of a Level-of-Detail
                // If there is an active Level-of-Detail (LOD)
                // add this Shape node to new GVRSceneObject as a
                // a direct child of this LOD.
                if (lodManager.isActive()) {
                    if ( lodManager.transformLODSceneObject == currentSceneObject ) {
                        // <Shape> node not under a <Transform> inside a LOD node
                        // so we need to attach it to a GVRSceneObject, and
                        // then attach it to the LOD
                        lodManager.shapeLODSceneObject = AddGVRSceneObject();
                        currentSceneObject = lodManager.shapeLODSceneObject;
                        lodManager.AddLODSceneObject( currentSceneObject );
                    }
                }

                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // Shape node to be shared / re-used
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {
                        // GVRRenderingData doesn't seem to be shared, but instead has an
                        // owner.  Thus share the GVRMesh and GVRMaterial attached to
                        // GVRRenderingData.
                        GVRRenderData gvrRenderDataDEFined = useItem.getGVRRenderData();
                        gvrRenderData.setMaterial(gvrRenderDataDEFined.getMaterial());
                        gvrRenderData.setMesh(gvrRenderDataDEFined.getMesh());
                        gvrRenderingDataUSEd = true;
                    }
                    else {
                        Log.e(TAG, "Error: Shape USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                    }
                } else {

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                        definedItem.setGVRRenderData(gvrRenderData);
                        mDefinedItems.add(definedItem); // Array list of DEFined items
                        // Clones objects with USE
                    }
                }

            } // end <Shape> node


            /********** Appearance **********/
            else if (qName.equalsIgnoreCase("Appearance")) {
        /* This gives the X3D-only Shader */
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // shared Appearance node, GVRMaterial
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {
                        gvrMaterial = useItem.getGVRMaterial();
                        gvrRenderData.setMaterial(gvrMaterial);
                        gvrMaterialUSEd = true; // for DEFine and USE, we encounter a USE,
                        // and thus have set the material
                    }
                    else {
                        Log.e(TAG, "Error: Appearance USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                    }
                } else {
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        shaderSettings.setAppearanceName(attributeValue);
                    }
                }

            } // end <Appearance> node


            /********** Material **********/
            else if (qName.equalsIgnoreCase("material")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) {
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {
                        gvrMaterial = useItem.getGVRMaterial();
                        gvrRenderData.setMaterial(gvrMaterial);
                        gvrMaterialUSEd = true; // for DEFine and USE, we encounter a USE,
                        // and thus have set the material
                    }
                    else {
                        Log.e(TAG, "Error: Material USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                    }

                } else {
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        shaderSettings.setMaterialName(attributeValue);
                    }
                    String diffuseColorAttribute = attributes.getValue("diffuseColor");
                    if (diffuseColorAttribute != null) {
                        float diffuseColor[] = parseFixedLengthFloatString(diffuseColorAttribute,
                                3, true, false);
                        shaderSettings.setDiffuseColor(diffuseColor);
                    }
                    String specularColorAttribute = attributes.getValue("specularColor");
                    if (specularColorAttribute != null) {
                        float specularColor[] = parseFixedLengthFloatString(specularColorAttribute,
                                3, true, false);
                        shaderSettings.setSpecularColor(specularColor);
                    }
                    String emissiveColorAttribute = attributes.getValue("emissiveColor");
                    if (emissiveColorAttribute != null) {
                        float emissiveColor[] = parseFixedLengthFloatString(emissiveColorAttribute,
                                3, true, false);
                        shaderSettings.setEmmissiveColor(emissiveColor);
                    }
                    String ambientIntensityAttribute = attributes
                            .getValue("ambientIntensity");
                    if (ambientIntensityAttribute != null) {
                        Log.e(TAG, "Material ambientIntensity currently not implemented.");
                        shaderSettings
                                .setAmbientIntensity(parseSingleFloatString(ambientIntensityAttribute,
                                        true, false));
                    }
                    String shininessAttribute = attributes.getValue("shininess");
                    if (shininessAttribute != null) {
                        shaderSettings
                                .setShininess(parseSingleFloatString(shininessAttribute, true,
                                        false));
                    }
                    String transparencyAttribute = attributes.getValue("transparency");
                    if (transparencyAttribute != null) {

                        shaderSettings
                                .setTransparency(parseSingleFloatString(transparencyAttribute,
                                        true, false));
                    }
                } // end ! USE attribute

            } // end <Material> node


            /********** ImageTexture **********/
            else if (qName.equalsIgnoreCase("ImageTexture")) {
                if ( !blockTexturing ) {
                    attributeValue = attributes.getValue("USE");
                    if (attributeValue != null) {
                        DefinedItem useItem = null;
                        for (DefinedItem definedItem : mDefinedItems) {
                            if (attributeValue.equals(definedItem.getName())) {
                                useItem = definedItem;
                                break;
                            }
                        }
                        if (useItem != null) {
                            gvrTexture = useItem.getGVRTexture();
                            shaderSettings.setTexture(gvrTexture);
                        } else {
                            Log.e(TAG, "Error: ImageTexture USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                        }
                    } else {
                        gvrTextureParameters = new GVRTextureParameters(gvrContext);
                        gvrTextureParameters.setWrapSType(TextureWrapType.GL_REPEAT);
                        gvrTextureParameters.setWrapTType(TextureWrapType.GL_REPEAT);
                        gvrTextureParameters.setMinFilterType(GVRTextureParameters.TextureFilterType.GL_LINEAR_MIPMAP_NEAREST);

                        String urlAttribute = attributes.getValue("url");
                        if (urlAttribute != null) {
                            urlAttribute = urlAttribute.replace("\"", ""); // remove double and
                            // single quotes
                            urlAttribute = urlAttribute.replace("\'", "");

                            final String filename = urlAttribute;
                            String repeatSAttribute = attributes.getValue("repeatS");
                            if (repeatSAttribute != null) {
                                if (!parseBooleanString(repeatSAttribute)) {
                                    gvrTextureParameters
                                            .setWrapSType(TextureWrapType.GL_CLAMP_TO_EDGE);
                                }
                            }
                            String repeatTAttribute = attributes.getValue("repeatT");
                            if (repeatTAttribute != null) {
                                if (!parseBooleanString(repeatTAttribute)) {
                                    gvrTextureParameters
                                            .setWrapTType(TextureWrapType.GL_CLAMP_TO_EDGE);
                                }
                            }

                            final String defValue = attributes.getValue("DEF");
                            gvrTexture = new GVRTexture(gvrContext, gvrTextureParameters);
                            GVRAssetLoader.TextureRequest request = new GVRAssetLoader.TextureRequest(assetRequest, gvrTexture, (inlineSubdirectory + filename));
                            assetRequest.loadTexture(request);
                            shaderSettings.setTexture(gvrTexture);
                            if (defValue != null) {
                                DefinedItem item = new DefinedItem(defValue);
                                item.setGVRTexture(gvrTexture);
                                mDefinedItems.add(item);
                            }
                        }
                    }
                }
            } // end <ImageTexture> node


            /********** TextureTransform **********/
            else if (qName.equalsIgnoreCase("TextureTransform")) {
                attributeValue = attributes.getValue("DEF");
                if (attributeValue != null) {
                    shaderSettings.setTextureTransformName(attributeValue);
                }
                String centerAttribute = attributes.getValue("center");
                if (centerAttribute != null) {
                    float[] center = parseFixedLengthFloatString(centerAttribute, 2,
                            false, false);
                    center[0] = -center[0];
                    shaderSettings.setTextureCenter(center);
                }
                String rotationAttribute = attributes.getValue("rotation");
                if (rotationAttribute != null) {
                    float[] rotation = parseFixedLengthFloatString(rotationAttribute, 1,
                            false, false);
                    shaderSettings.setTextureRotation( rotation[0] );
                }
                String scaleAttribute = attributes.getValue("scale");
                if (scaleAttribute != null) {
                    float[] scale = parseFixedLengthFloatString(scaleAttribute, 2, false,
                            true);
                    shaderSettings.setTextureScale(scale);
                }
                String translationAttribute = attributes.getValue("translation");
                if (translationAttribute != null) {
                    float[] translation = parseFixedLengthFloatString(translationAttribute,
                            2, false, false);
                    translation[1] = -translation[1];
                    shaderSettings.setTextureTranslation(translation);
                }
            }  // end TextureTransform

            /********** IndexedFaceSet **********/

            //TODO: eventually include IndexedLineSet **********/

            else if (qName.equalsIgnoreCase("IndexedFaceSet")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // shared GVRIndexBuffer / GVRMesh
                    indexedSetUSEName = attributeValue;
                } else {
                    gvrIndexBuffer = new GVRIndexBuffer(gvrContext, 4, 0);
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        indexedSetDEFName = attributeValue;
                    }
                    attributeValue = attributes.getValue("solid");
                    if (attributeValue != null) {
                        if (parseBooleanString(attributeValue)) {
                            Log.e(TAG, "IndexedFaceSet solid=true attribute not implemented. ");
                        }
                    }
                    attributeValue = attributes.getValue("ccw");
                    if (attributeValue != null) {
                        if ( !parseBooleanString(attributeValue)) {
                            Log.e(TAG, "IndexedFaceSet ccw=false attribute not implemented. ");
                        }
                    }
                    attributeValue = attributes.getValue("colorPerVertex");
                    if (attributeValue != null) {

                        Log.e(TAG,
                                "IndexedFaceSet colorPerVertex attribute not implemented. ");

                    }
                    attributeValue = attributes.getValue("normalPerVertex");
                    if (attributeValue != null) {

                        if ( !parseBooleanString(attributeValue)) {
                            Log.e(TAG,
                                    "IndexedFaceSet normalPerVertex=false attribute not implemented. ");
                        }

                    }
                    String coordIndexAttribute = attributes.getValue("coordIndex");
                    if (coordIndexAttribute != null) {
                        parseNumbersString(coordIndexAttribute,
                                X3Dobject.indexedFaceSetComponent, 3);
                        reorganizeVerts = true;
                    }
                    String normalIndexAttribute = attributes.getValue("normalIndex");
                    if (normalIndexAttribute != null) {
                        parseNumbersString(normalIndexAttribute,
                                X3Dobject.normalIndexComponent, 3);
                    }
                    String texCoordIndexAttribute = attributes.getValue("texCoordIndex");
                    if (texCoordIndexAttribute != null) {
                        parseNumbersString(texCoordIndexAttribute,
                                X3Dobject.textureIndexComponent, 3);
                    }
                }

            } // end <IndexedFaceSet> node


            /********** Coordinate **********/
            else if (qName.equalsIgnoreCase("Coordinate")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // Coordinate node to be shared / re-used
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {

                        // 'useItem' points to GVRMesh who's useItem.getGVRMesh Coordinates
                        // were DEFined earlier. We don't want to share the entire GVRMesh
                        // since the 2 meshes may have different Normals and
                        // Texture Coordinates.  So as an alternative, copy the vertices.
                            gvrVertexBuffer = useItem.getVertexBuffer();
                        reorganizeVerts = false;
                    }
                } // end USE Coordinate
                else {
                    // Not a 'Coordinate USE="..." node
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                            meshCreator.defineVertexBuffer(definedItem);
                        // Array list of DEFined items clones objects with USE
                        mDefinedItems.add(definedItem);
                    }
                    String pointAttribute = attributes.getValue("point");
                    if (pointAttribute != null) {
                        parseNumbersString(pointAttribute, X3Dobject.verticesComponent, 3);
                    }
                } // end NOT a USE Coordinates condition

            } // end <Coordinate> node


            /********** TextureCoordinate **********/
            else if (qName.equalsIgnoreCase("TextureCoordinate")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // Coordinate node to be shared / re-used
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {

                            // 'useItem' points to GVRVertexBuffer who's useItem.getVertexBuffer
                        // TextureCoordinates were DEFined earlier.
                            // We don't want to share the entire GVRVertexBuffer since the
                            // the 2 meshes may have different Normals and Positions
                        // So as an alternative, copy the texture coordinates.
                            gvrVertexBuffer.setFloatArray("a_texcoord", useItem.getVertexBuffer().getFloatArray("a_texcoord"));
                        reorganizeVerts = false;
                    }
                } // end USE TextureCoordinate
                else {
                    // Not a 'TextureCoordinate USE="..." node
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        // This is a 'TextureCoordinate DEF="..." case, so save the item
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                            definedItem.setVertexBuffer(gvrVertexBuffer);
                        // Array list of DEFined items clones objects with USE
                        mDefinedItems.add(definedItem);
                    }
                    // Have to flip the y texture coordinates because the image will be
                    // upside down
                    String pointAttribute = attributes.getValue("point");
                    if (pointAttribute != null) {
                        parseNumbersString(pointAttribute, X3Dobject.textureCoordComponent, 2);
                    }

                } // end NOT a USE TextureCoordinate condition
            } // end <TextureCoordinate> node


            /********** Normal **********/
            else if (qName.equalsIgnoreCase("Normal")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // Coordinate node to be shared / re-used
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {

                            // 'useItem' points to GVRVertexBuffer who's useItem.getVertexBuffer Coordinates
                            // were DEFined earlier. We don't want to share the entire vertex buffer since
                            // the 2 vertex buffers may have different Normals and Texture Coordinates
                        // So as an alternative, copy the normals.
                            gvrVertexBuffer.setFloatArray("a_normal", useItem.getVertexBuffer().getFloatArray("a_normal"));
                        reorganizeVerts = false;
                    }
                } // end USE Coordinate
                else {
                    // Not a 'Normal USE="..." node
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        // This is a 'Normal DEF="..." case, so save the item
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                            definedItem.setVertexBuffer(gvrVertexBuffer);
                        // Array list of DEFined items clones objects with USE
                        mDefinedItems.add(definedItem);
                    }
                    String vectorAttribute = attributes.getValue("vector");
                    if (vectorAttribute != null) {
                        parseNumbersString(vectorAttribute, X3Dobject.normalsComponent, 3);
                    }
                } // end NOT a USE Normals condition
            } // end <Normal> node


            /********** LIGHTS **********/
            /********** PointLight **********/
            else if (qName.equalsIgnoreCase("PointLight")) {
                if (UNIVERSAL_LIGHTS && !blockLighting) {
                    attributeValue = attributes.getValue("USE");
                    if (attributeValue != null) { // shared PointLight
                        DefinedItem useItem = null;
                        for (DefinedItem definedItem : mDefinedItems) {
                            if (attributeValue.equals(definedItem.getName())) {
                                useItem = definedItem;
                                break;
                            }
                        }
                        if (useItem != null) {
                            // GVRf does not allow a light attached at two places
                            // so copy the attributes of the original light into the second
                            // light
                            GVRSceneObject definedSceneObject = useItem.getGVRSceneObject();
                            GVRPointLight definedPtLight = (GVRPointLight) definedSceneObject
                                    .getLight();
                            GVRTransform definedTransform = definedPtLight.getTransform();

                            GVRSceneObject newPtLightSceneObj = AddGVRSceneObject();
                            newPtLightSceneObj.getTransform()
                                    .setPosition(definedTransform.getPositionX(),
                                            definedTransform.getPositionY(),
                                            definedTransform.getPositionZ());
                            // attachLight does not allow a light to be shared so we need to
                            // just create a new light and share its attributes
                            GVRPointLight newPtLight = new GVRPointLight(gvrContext);
                            newPtLightSceneObj.attachLight(newPtLight);
                            float[] attribute = definedPtLight.getAmbientIntensity();
                            newPtLight.setAmbientIntensity(attribute[0], attribute[1],
                                    attribute[2], attribute[3]);
                            attribute = definedPtLight.getDiffuseIntensity();
                            newPtLight.setDiffuseIntensity(attribute[0], attribute[1],
                                    attribute[2], 1);
                            attribute = definedPtLight.getSpecularIntensity();
                            newPtLight.setSpecularIntensity(attribute[0], attribute[1],
                                    attribute[2], 1);
                            newPtLight
                                    .setAttenuation(definedPtLight.getAttenuationConstant(),
                                            definedPtLight.getAttenuationLinear(),
                                            definedPtLight.getAttenuationQuadratic());
                            newPtLight.enable();
                        }
                        else {
                            Log.e(TAG, "Error: PointLight USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                        }
                    } // end reuse a PointLight
                    else {
                        // add a new PointLight
                        float ambientIntensity = 0;
                        float[] attenuation =
                                {
                                        1, 0, 0
                                };
                        float[] color =
                                {
                                        1, 1, 1
                                };
                        boolean global = true;
                        float intensity = 1;
                        float[] location =
                                {
                                        0, 0, 0
                                };
                        boolean on = true;
                        float radius = 100;

                        GVRSceneObject newPtLightSceneObj = AddGVRSceneObject();
                        GVRPointLight newPtLight = new GVRPointLight(gvrContext);
                        newPtLightSceneObj.attachLight(newPtLight);

                        attributeValue = attributes.getValue("DEF");
                        if (attributeValue != null) {
                            newPtLightSceneObj.setName(attributeValue);
                            DefinedItem definedItem = new DefinedItem(attributeValue);
                            definedItem.setGVRSceneObject(newPtLightSceneObj);
                            mDefinedItems.add(definedItem); // Array list of DEFined items
                            // Clones objects with USE
                        }
                        attributeValue = attributes.getValue("ambientIntensity");
                        if (attributeValue != null) {
                            ambientIntensity = parseSingleFloatString(attributeValue, true,
                                    false);
                        }
                        attributeValue = attributes.getValue("attenuation");
                        if (attributeValue != null) {
                            attenuation = parseFixedLengthFloatString(attributeValue, 3,
                                    false, true);
                            if ((attenuation[0] == 0) && (attenuation[1] == 0)
                                    && (attenuation[2] == 0))
                                attenuation[0] = 1;
                        }
                        attributeValue = attributes.getValue("color");
                        if (attributeValue != null) {
                            color = parseFixedLengthFloatString(attributeValue, 3, true,
                                    false);
                        }
                        attributeValue = attributes.getValue("global");
                        if (attributeValue != null) {
                            global = parseBooleanString(attributeValue); // NOT IMPLEMENTED
                            Log.e(TAG, "Point Light global attribute not implemented. ");
                        }
                        attributeValue = attributes.getValue("intensity");
                        if (attributeValue != null) {
                            intensity = parseSingleFloatString(attributeValue, true, false);
                        }
                        attributeValue = attributes.getValue("location");
                        if (attributeValue != null) {
                            location = parseFixedLengthFloatString(attributeValue, 3, false,
                                    false);
                        }
                        attributeValue = attributes.getValue("on");
                        if (attributeValue != null) {
                            on = parseBooleanString(attributeValue);
                        }
                        attributeValue = attributes.getValue("radius");
                        if (attributeValue != null) {
                            radius = parseSingleFloatString(attributeValue, false, true);
                        }
                        // In x3d, ambientIntensity is only 1 value, not 3.
                        newPtLight.setAmbientIntensity(ambientIntensity, ambientIntensity,
                                ambientIntensity, 1);
                        newPtLight.setDiffuseIntensity(color[0] * intensity,
                                color[1] * intensity,
                                color[2] * intensity, 1);
                        // x3d doesn't have an equivalent for specular intensity
                        newPtLight.setSpecularIntensity(color[0] * intensity,
                                color[1] * intensity,
                                color[2] * intensity, 1);
                        newPtLight.setAttenuation(attenuation[0], attenuation[1],
                                attenuation[2]);
                        if (on)
                            newPtLight.enable();
                        else
                            newPtLight.disable();

                        GVRTransform newPtLightSceneObjTransform = newPtLightSceneObj
                                .getTransform();
                        newPtLightSceneObjTransform.setPosition(location[0], location[1],
                                location[2]);
                    } // end a new PointLight
                } // end if UNIVERSAL_LIGHTS

            } // end <PointLight> node


            /********** DirectionalLight **********/
            else if (qName.equalsIgnoreCase("DirectionalLight")) {
                if (UNIVERSAL_LIGHTS && !blockLighting) {
                    attributeValue = attributes.getValue("USE");
                    if (attributeValue != null) { // shared PointLight
                        DefinedItem useItem = null;
                        for (DefinedItem definedItem : mDefinedItems) {
                            if (attributeValue.equals(definedItem.getName())) {
                                useItem = definedItem;
                                break;
                            }
                        }
                        if (useItem != null) {
                            // GVRf does not allow a light attached at two places
                            // so copy the attributes of the original light into the second
                            // light
                            GVRSceneObject definedSceneObject = useItem.getGVRSceneObject();
                            GVRDirectLight definedDirectLight = (GVRDirectLight) definedSceneObject
                                    .getLight();
                            GVRTransform definedTransform = definedDirectLight.getTransform();

                            GVRSceneObject newDirectLightSceneObj = AddGVRSceneObject();
                            newDirectLightSceneObj.getTransform()
                                    .setRotation(definedTransform.getRotationW(),
                                            definedTransform.getRotationX(),
                                            definedTransform.getRotationY(),
                                            definedTransform.getRotationZ());
                            // attachLight does not allow a light to be shared so we need to
                            // just create a new light and share its attributes
                            GVRDirectLight newDirectLight = new GVRDirectLight(gvrContext);
                            newDirectLightSceneObj.attachLight(newDirectLight);
                            float[] attribute = definedDirectLight.getAmbientIntensity();
                            newDirectLight.setAmbientIntensity(attribute[0], attribute[1],
                                    attribute[2], attribute[3]);
                            attribute = definedDirectLight.getDiffuseIntensity();
                            newDirectLight.setDiffuseIntensity(attribute[0], attribute[1],
                                    attribute[2], 1);
                            attribute = definedDirectLight.getSpecularIntensity();
                            newDirectLight.setSpecularIntensity(attribute[0], attribute[1],
                                    attribute[2], 1);
                            newDirectLight.enable();
                        }
                        else {
                            Log.e(TAG, "Error: DirectionalLight USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                        }
                    } // end reuse a DirectionalLight
                    else {
                        // add a new DirectionalLight
                        float ambientIntensity = 0;
                        float[] color =
                                {
                                        1, 1, 1
                                };
                        float[] direction =
                                {
                                        0, 0, -1
                                };
                        boolean global = true;
                        float intensity = 1;
                        boolean on = true;

                        GVRSceneObject newDirectionalLightSceneObj = AddGVRSceneObject();
                        GVRDirectLight newDirectionalLight = new GVRDirectLight(gvrContext);
                        newDirectionalLightSceneObj.attachLight(newDirectionalLight);
                        DefinedItem definedItem = null;

                        attributeValue = attributes.getValue("DEF");
                        if (attributeValue != null) {
                            newDirectionalLightSceneObj.setName(attributeValue);
                            definedItem = new DefinedItem(attributeValue);
                            definedItem.setGVRSceneObject(newDirectionalLightSceneObj);
                            mDefinedItems.add(definedItem); // Array list of DEFined items
                            // Clones objects with USE
                        }
                        attributeValue = attributes.getValue("ambientIntensity");
                        if (attributeValue != null) {
                            ambientIntensity = parseSingleFloatString(attributeValue, true,
                                    false);
                        }
                        attributeValue = attributes.getValue("color");
                        if (attributeValue != null) {
                            color = parseFixedLengthFloatString(attributeValue, 3, true,
                                    false);
                        }
                        attributeValue = attributes.getValue("direction");
                        if (attributeValue != null) {
                            direction = parseFixedLengthFloatString(attributeValue, 3, false,
                                    false);
                        }
                        attributeValue = attributes.getValue("global");
                        if (attributeValue != null) {
                            Log.e(TAG,
                                    "DirectionalLight global attribute not currently implemented. ");
                        }
                        attributeValue = attributes.getValue("intensity");
                        if (attributeValue != null) {
                            intensity = parseSingleFloatString(attributeValue, true, false);
                        }
                        attributeValue = attributes.getValue("on");
                        if (attributeValue != null) {
                            on = parseBooleanString(attributeValue);
                        }

                        newDirectionalLight.setAmbientIntensity(1, 1, 1, 1);
                        newDirectionalLight.setDiffuseIntensity(color[0] * intensity,
                                color[1] * intensity,
                                color[2] * intensity, 1);
                        newDirectionalLight.setSpecularIntensity(1, 1, 1, 1);

                        if (on)
                            newDirectionalLight.enable();
                        else
                            newDirectionalLight.disable();

                        if (definedItem != null) definedItem.setDirection(direction);

                        Quaternionf q = animationInteractivityManager.ConvertDirectionalVectorToQuaternion(
                                new Vector3f(direction[0], direction[1], direction[2]));
                        // set direction in the Light's GVRScene
                        GVRTransform newDirectionalLightSceneObjTransform = newDirectionalLightSceneObj
                                .getTransform();
                        newDirectionalLightSceneObjTransform.setRotation(q.w, q.x, q.y,
                                q.z);
                    } // end if adding new Directional Light
                } // end if Universal Lights

            } // end <Directional Light> node


            /********** SpotLight **********/
            else if (qName.equalsIgnoreCase("SpotLight")) {
                if (UNIVERSAL_LIGHTS && !blockLighting) {
                    attributeValue = attributes.getValue("USE");
                    if (attributeValue != null) { // shared PointLight
                        DefinedItem useItem = null;
                        for (DefinedItem definedItem : mDefinedItems) {
                            if (attributeValue.equals(definedItem.getName())) {
                                useItem = definedItem;
                                break;
                            }
                        }
                        if (useItem != null) {
                            // GVRf does not allow a light attached at two places
                            // so copy the attributes of the original light into the second
                            // light
                            GVRSceneObject definedSceneObject = useItem.getGVRSceneObject();
                            GVRSpotLight definedSpotLight = (GVRSpotLight) definedSceneObject
                                    .getLight();
                            GVRTransform definedTransform = definedSpotLight.getTransform();

                            GVRSceneObject newSpotLightSceneObj = AddGVRSceneObject();
                            newSpotLightSceneObj.getTransform()
                                    .setPosition(definedTransform.getPositionX(),
                                            definedTransform.getPositionY(),
                                            definedTransform.getPositionZ());
                            newSpotLightSceneObj.getTransform()
                                    .setRotation(definedTransform.getRotationW(),
                                            definedTransform.getRotationX(),
                                            definedTransform.getRotationY(),
                                            definedTransform.getRotationZ());
                            // attachLight does not allow a light to be shared so we need to
                            // just create a new light and share its attributes
                            GVRSpotLight newSpotLight = new GVRSpotLight(gvrContext);
                            newSpotLightSceneObj.attachLight(newSpotLight);
                            float[] attribute = definedSpotLight.getAmbientIntensity();
                            newSpotLight.setAmbientIntensity(attribute[0], attribute[1],
                                    attribute[2], attribute[3]);
                            attribute = definedSpotLight.getDiffuseIntensity();
                            newSpotLight.setDiffuseIntensity(attribute[0], attribute[1],
                                    attribute[2], 1);
                            attribute = definedSpotLight.getSpecularIntensity();
                            newSpotLight.setSpecularIntensity(attribute[0], attribute[1],
                                    attribute[2], 1);
                            newSpotLight
                                    .setAttenuation(definedSpotLight.getAttenuationConstant(),
                                            definedSpotLight.getAttenuationLinear(),
                                            definedSpotLight.getAttenuationQuadratic());
                            newSpotLight
                                    .setInnerConeAngle(definedSpotLight.getInnerConeAngle());
                            newSpotLight
                                    .setOuterConeAngle(definedSpotLight.getOuterConeAngle());
                            newSpotLight.enable();
                        }
                        else {
                            Log.e(TAG, "Error: SpotLight USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                        }
                    } // end reuse a SpotLight
                    else {
                        // add a new SpotLight
                        float ambientIntensity = 0;
                        float[] attenuation =
                                {
                                        1, 0, 0
                                };
                        float beamWidth = (float) Math.PI / 4; // range is 0 to PI
                        float[] color =
                                {
                                        1, 1, 1
                                };
                        float cutOffAngle = (float) Math.PI / 2; // range is 0 to PI
                        float[] direction =
                                {
                                        0, 0, -1
                                };
                        boolean global = true;
                        float intensity = 1;
                        float[] location =
                                {
                                        0, 0, 0
                                };
                        boolean on = true;
                        float radius = 100;

                        GVRSceneObject newSpotLightSceneObj = AddGVRSceneObject();
                        GVRSpotLight newSpotLight = new GVRSpotLight(gvrContext);
                        newSpotLightSceneObj.attachLight(newSpotLight);

                        DefinedItem definedItem = null;

                        attributeValue = attributes.getValue("DEF");
                        if (attributeValue != null) {
                            newSpotLightSceneObj.setName(attributeValue);
                            definedItem = new DefinedItem(attributeValue);
                            definedItem.setGVRSceneObject(newSpotLightSceneObj);
                            mDefinedItems.add(definedItem); // Array list of DEFined items
                            // Clones objects with USE
                        }
                        attributeValue = attributes.getValue("ambientIntensity");
                        if (attributeValue != null) {
                            ambientIntensity = parseSingleFloatString(attributeValue, true,
                                    false);
                        }
                        attributeValue = attributes.getValue("attenuation");
                        if (attributeValue != null) {
                            attenuation = parseFixedLengthFloatString(attributeValue, 3,
                                    false, true);
                            if ((attenuation[0] == 0) && (attenuation[1] == 0)
                                    && (attenuation[2] == 0))
                                attenuation[0] = 1;
                        }
                        attributeValue = attributes.getValue("beamWidth");
                        if (attributeValue != null) {
                            beamWidth = parseSingleFloatString(attributeValue, false, true);
                            if (beamWidth > (float) Math.PI / 2) {
                                beamWidth = (float) Math.PI / 2;
                                Log.e(TAG, "Spot Light beamWidth cannot exceed PI/2.");
                            }
                        }
                        attributeValue = attributes.getValue("color");
                        if (attributeValue != null) {
                            color = parseFixedLengthFloatString(attributeValue, 3, true,
                                    false);
                        }
                        attributeValue = attributes.getValue("cutOffAngle");
                        if (attributeValue != null) {
                            cutOffAngle = parseSingleFloatString(attributeValue, false, true);
                            if (cutOffAngle > (float) Math.PI / 2) {
                                cutOffAngle = (float) Math.PI / 2;
                                Log.e(TAG, "Spot Light cutOffAngle cannot exceed PI/2.");
                            }
                        }
                        attributeValue = attributes.getValue("direction");
                        if (attributeValue != null) {
                            direction = parseFixedLengthFloatString(attributeValue, 3, false,
                                    false);
                        }
                        attributeValue = attributes.getValue("global");
                        if (attributeValue != null) {
                            Log.e(TAG,
                                    "Spot Light global attribute not currently implemented. ");
                        }
                        attributeValue = attributes.getValue("intensity");
                        if (attributeValue != null) {
                            intensity = parseSingleFloatString(attributeValue, true, false);
                        }
                        attributeValue = attributes.getValue("location");
                        if (attributeValue != null) {
                            location = parseFixedLengthFloatString(attributeValue, 3, false,
                                    false);
                        }
                        attributeValue = attributes.getValue("on");
                        if (attributeValue != null) {
                            on = parseBooleanString(attributeValue);
                        }
                        attributeValue = attributes.getValue("radius");
                        if (attributeValue != null) {
                            radius = parseSingleFloatString(attributeValue, false, true);
                        }
                        // x3d only has a single value for ambient intensity
                        newSpotLight.setAmbientIntensity(ambientIntensity, ambientIntensity,
                                ambientIntensity, 1);
                        newSpotLight.setDiffuseIntensity(color[0] * intensity,
                                color[1] * intensity,
                                color[2] * intensity, 1);
                        newSpotLight.setSpecularIntensity(0, 0, 0, 1);
                        newSpotLight.setAttenuation(attenuation[0], attenuation[1],
                                attenuation[2]);

                        if (on)
                            newSpotLight.enable();
                        else
                            newSpotLight.disable();
                        newSpotLight.setInnerConeAngle(beamWidth * 180 / (float) Math.PI);
                        newSpotLight.setOuterConeAngle(cutOffAngle * 180 / (float) Math.PI);

                        if (definedItem != null) definedItem.setDirection(direction);
                        Quaternionf q = animationInteractivityManager.ConvertDirectionalVectorToQuaternion(
                                new Vector3f(direction[0], direction[1], direction[2]));
                        // set position and direction in the SpotLight's GVRScene
                        GVRTransform newSpotLightSceneObjTransform = newSpotLightSceneObj
                                .getTransform();
                        newSpotLightSceneObjTransform.setPosition(location[0], location[1],
                                location[2]);
                        newSpotLightSceneObjTransform.setRotation(q.w, q.x, q.y, q.z);
                    } // end adding a new SpotLight

                } // end if UNIVERSAL_LIGHTS

            } // end <SpotLight> node


            /********** TimeSensor **********/
            else if (qName.equalsIgnoreCase("TimeSensor")) {
                String name = null;
                float cycleInterval = 1;
                boolean enabled = true;
                boolean loop = false;
                float pauseTime = 0;
                float resumeTime = 0;
                float startTime = 0;
                float stopTime = 0;

                attributeValue = attributes.getValue("DEF");
                if (attributeValue != null) {
                    name = attributeValue;
                }
                attributeValue = attributes.getValue("cycleInterval");
                if (attributeValue != null) {
                    cycleInterval = parseSingleFloatString(attributeValue, false, true);
                }
                attributeValue = attributes.getValue("enabled");
                if (attributeValue != null) {
                    enabled = parseBooleanString(attributeValue);
                }
                attributeValue = attributes.getValue("loop");
                if (attributeValue != null) {
                    loop = parseBooleanString(attributeValue);
                }
                attributeValue = attributes.getValue("pauseTime");
                if (attributeValue != null) {
                    pauseTime = parseSingleFloatString(attributeValue, false, true);
                    Log.e(TAG, "Timer pauseTime not currently implemented. ");
                }
                attributeValue = attributes.getValue("resumeTime");
                if (attributeValue != null) {
                    resumeTime = parseSingleFloatString(attributeValue, false, true);
                    Log.e(TAG, "Timer resumeTime not currently implemented. ");
                }
                attributeValue = attributes.getValue("startTime");
                if (attributeValue != null) {
                    startTime = parseSingleFloatString(attributeValue, false, true);
                    Log.e(TAG, "Timer startTime not currently implemented. ");
                }
                attributeValue = attributes.getValue("stopTime");
                if (attributeValue != null) {
                    stopTime = parseSingleFloatString(attributeValue, false, true);
                    Log.e(TAG, "Timer stopTime not currently implemented. ");
                }

                TimeSensor newTimeSensor = new TimeSensor(name, cycleInterval, enabled,
                        loop, pauseTime, resumeTime, startTime, stopTime);
                timeSensors.add(newTimeSensor);

            } // end <TimeSensor> node


            /********** ROUTE **********/
            else {
                if (qName.equalsIgnoreCase("ROUTE")) {
                    String fromNode = null;
                    String fromField = null;
                    String toNode = null;
                    String toField = null;
                    attributeValue = attributes.getValue("fromNode");
                    if (attributeValue != null) {
                        fromNode = attributeValue;
                    }
                    attributeValue = attributes.getValue("fromField");
                    if (attributeValue != null) {
                        fromField = attributeValue;
                    }
                    attributeValue = attributes.getValue("toNode");
                    if (attributeValue != null) {
                        toNode = attributeValue;
                    }
                    attributeValue = attributes.getValue("toField");
                    if (attributeValue != null) {
                        toField = attributeValue;
                    }

                    animationInteractivityManager.buildInteractiveObject(fromNode, fromField, toNode, toField);
                } // end <ROUTE> node


                /********** PositionInterpolator **********/
                else if (qName.equalsIgnoreCase("PositionInterpolator")) {
                    String name = null;
                    float[] keysList = null;
                    float[] keyValuesList = null;

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("key");
                    if (attributeValue != null) {
                        parseNumbersString(attributeValue, X3Dobject.interpolatorKeyComponent,
                                1);

                        keysList = new float[keys.size()];
                        for (int i = 0; i < keysList.length; i++) {
                            Key keyObject = keys.get(i);
                            keysList[i] = keyObject.key;
                        }
                        keys.clear();
                    }
                    attributeValue = attributes.getValue("keyValue");
                    if (attributeValue != null) {
                        parseNumbersString(attributeValue,
                                X3Dobject.interpolatorKeyValueComponent, 3);

                        keyValuesList = new float[keyValues.size() * 3];
                        for (int i = 0; i < keyValues.size(); i++) {
                            KeyValue keyValueObject = keyValues.get(i);
                            for (int j = 0; j < 3; j++) {
                                keyValuesList[i * 3 + j] = keyValueObject.keyValues[j];
                            }
                        }
                        keyValues.clear();
                    }
                    Interpolator newInterporlator = new Interpolator(name, keysList,
                            keyValuesList);
                    interpolators.add(newInterporlator);

                } // end <PositionInterpolator> node


                /********** OrientationInterpolator **********/
                else if (qName.equalsIgnoreCase("OrientationInterpolator")) {
                    String name = null;
                    float[] keysList = null;
                    float[] keyValuesList = null;

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("key");
                    if (attributeValue != null) {
                        parseNumbersString(attributeValue, X3Dobject.interpolatorKeyComponent,
                                1);

                        keysList = new float[keys.size()];
                        for (int i = 0; i < keysList.length; i++) {
                            Key keyObject = keys.get(i);
                            keysList[i] = keyObject.key;
                        }
                        keys.clear();
                    }
                    attributeValue = attributes.getValue("keyValue");
                    if (attributeValue != null) {
                        parseNumbersString(attributeValue,
                                X3Dobject.interpolatorKeyValueComponent, 4);

                        keyValuesList = new float[keyValues.size() * 4];
                        for (int i = 0; i < keyValues.size(); i++) {
                            KeyValue keyValueObject = keyValues.get(i);
                            for (int j = 0; j < 4; j++) {
                                keyValuesList[i * 4 + j] = keyValueObject.keyValues[j];
                            }
                        }
                        keyValues.clear();
                    }
                    Interpolator newInterporlator = new Interpolator(name, keysList,
                            keyValuesList);
                    interpolators.add(newInterporlator);

                } // end <OrientationInterpolator> node


                /********** Box **********/
                else if (qName.equalsIgnoreCase("Box")) {
                    float[] size =
                            {
                                    2, 2, 2
                            };
                    boolean solid = true; // box visible from inside

                    attributeValue = attributes.getValue("size");
                    if (attributeValue != null) {
                        size = parseFixedLengthFloatString(attributeValue, 3, false, true);
                    }
                    attributeValue = attributes.getValue("solid");
                    if (attributeValue != null) {
                        solid = parseBooleanString(attributeValue);
                    }
                    Vector3f sizeVector = new Vector3f(size[0], size[1], size[2]);
                    GVRCubeSceneObject gvrCubeSceneObject = new GVRCubeSceneObject(
                            gvrContext, solid, sizeVector);
                    currentSceneObject.addChildObject(gvrCubeSceneObject);
                    meshAttachedSceneObject = gvrCubeSceneObject;


                } // end <Box> node


                /********** Cone **********/
                else if (qName.equalsIgnoreCase("Cone")) {
                    boolean bottom = true;
                    float bottomRadius = 1;
                    float height = 2;
                    boolean side = true;
                    boolean solid = true; // cone visible from inside

                    attributeValue = attributes.getValue("bottom");
                    if (attributeValue != null) {
                        bottom = parseBooleanString(attributeValue);
                    }
                    attributeValue = attributes.getValue("bottomRadius");
                    if (attributeValue != null) {
                        bottomRadius = parseSingleFloatString(attributeValue, false, true);
                    }
                    attributeValue = attributes.getValue("height");
                    if (attributeValue != null) {
                        height = parseSingleFloatString(attributeValue, false, true);
                    }
                    attributeValue = attributes.getValue("side");
                    if (attributeValue != null) {
                        side = parseBooleanString(attributeValue);
                    }
                    attributeValue = attributes.getValue("solid");
                    if (attributeValue != null) {
                        solid = parseBooleanString(attributeValue);
                    }
                    GVRCylinderSceneObject.CylinderParams params = new GVRCylinderSceneObject.CylinderParams();
                    params.BottomRadius = bottomRadius;
                    params.TopRadius = 0;
                    params.Height = height;
                    params.FacingOut = solid;
                    params.HasTopCap = false;
                    params.HasBottomCap = bottom;
                    params.Material = new GVRMaterial(gvrContext, x3DShader);
                    GVRCylinderSceneObject cone = new GVRCylinderSceneObject(gvrContext,
                            params);

                    currentSceneObject.addChildObject(cone);
                    meshAttachedSceneObject = cone;

                }  // end <Cone> node


                /********** Cylinder **********/
                else if (qName.equalsIgnoreCase("Cylinder")) {
                    boolean bottom = true;
                    float height = 2;
                    float radius = 1;
                    boolean side = true;
                    boolean solid = true; // cylinder visible from inside
                    boolean top = true;

                    attributeValue = attributes.getValue("bottom");
                    if (attributeValue != null) {
                        bottom = parseBooleanString(attributeValue);
                    }
                    attributeValue = attributes.getValue("height");
                    if (attributeValue != null) {
                        height = parseSingleFloatString(attributeValue, false, true);
                    }
                    attributeValue = attributes.getValue("radius");
                    if (attributeValue != null) {
                        radius = parseSingleFloatString(attributeValue, false, true);
                    }
                    attributeValue = attributes.getValue("side");
                    if (attributeValue != null) {
                        side = parseBooleanString(attributeValue);
                    }
                    attributeValue = attributes.getValue("solid");
                    if (attributeValue != null) {
                        solid = parseBooleanString(attributeValue);
                    }
                    attributeValue = attributes.getValue("top");
                    if (attributeValue != null) {
                        top = parseBooleanString(attributeValue);
                    }
                    GVRCylinderSceneObject.CylinderParams params = new GVRCylinderSceneObject.CylinderParams();
                    params.BottomRadius = radius;
                    params.TopRadius = radius;
                    params.Height = height;
                    params.HasBottomCap = bottom;
                    params.HasTopCap = top;
                    params.FacingOut = solid;
                    params.Material = new GVRMaterial(gvrContext, x3DShader);
                    GVRCylinderSceneObject gvrCylinderSceneObject = new GVRCylinderSceneObject(
                            gvrContext, params);
                    currentSceneObject.addChildObject(gvrCylinderSceneObject);
                    meshAttachedSceneObject = gvrCylinderSceneObject;

                } // end <Cylinder> node


                /********** Sphere **********/
                else if (qName.equalsIgnoreCase("Sphere")) {
                    float radius = 1;
                    boolean solid = true; // sphere visible from inside
                    attributeValue = attributes.getValue("radius");
                    if (attributeValue != null) {
                        radius = parseSingleFloatString(attributeValue, false, true);
                    }
                    attributeValue = attributes.getValue("solid");
                    if (attributeValue != null) {
                        solid = parseBooleanString(attributeValue);
                    }
                    GVRSphereSceneObject gvrSphereSceneObject = new GVRSphereSceneObject(
                            gvrContext, solid, new GVRMaterial(gvrContext, x3DShader), radius);
                    currentSceneObject.addChildObject(gvrSphereSceneObject);
                    meshAttachedSceneObject = gvrSphereSceneObject;

                } // end <Sphere> node

                // Less frequent commands and thus moved to end of if-then-else.

                /********** Viewpoint **********/
                else if (qName.equalsIgnoreCase("Viewpoint")) {
                    float[] centerOfRotation =
                            {
                                    0, 0, 0
                            };
                    String description = "";
                    float fieldOfView = (float) Math.PI / 4;
                    boolean jump = true;
                    String name = "";
                    float[] orientation =
                            {
                                    0, 0, 1, 0
                            };
                    float[] position =
                            {
                                    0, 0, 10
                            };
                    boolean retainUserOffsets = false;

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("centerOfRotation");
                    if (attributeValue != null) {
                        centerOfRotation = parseFixedLengthFloatString(attributeValue, 3,
                                false, false);
                        Log.e(TAG, "X3D Viewpoint centerOfRotation not implemented in GearVR.");
                    }
                    attributeValue = attributes.getValue("description");
                    if (attributeValue != null) {
                        description = attributeValue;
                    }
                    attributeValue = attributes.getValue("fieldOfView");
                    if (attributeValue != null) {
                        fieldOfView = parseSingleFloatString(attributeValue, false, true);
                        if (fieldOfView > (float) Math.PI)
                            fieldOfView = (float) Math.PI;
                        Log.e(TAG, "Viewpoint fieldOfView attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("jump");
                    if (attributeValue != null) {
                        jump = parseBooleanString(attributeValue);
                    }
                    attributeValue = attributes.getValue("orientation");
                    if (attributeValue != null) {
                        orientation = parseFixedLengthFloatString(attributeValue, 4, false,
                                false);
                    }
                    attributeValue = attributes.getValue("position");
                    if (attributeValue != null) {
                        position = parseFixedLengthFloatString(attributeValue, 3, false,
                                false);
                    }
                    attributeValue = attributes.getValue("retainUserOffsets");
                    if (attributeValue != null) {
                        retainUserOffsets = parseBooleanString(attributeValue);
                        Log.e(TAG, "Viewpoint retainUserOffsets attribute not implemented. ");
                    }
                    // Add viewpoint to the list.
                    // Since viewpoints can be under a Transform, save the parent.
                    Viewpoint viewpoint = new Viewpoint(centerOfRotation, description,
                            fieldOfView, jump, name, orientation, position, retainUserOffsets,
                            currentSceneObject);
                    viewpoints.add(viewpoint);

                    if ( !name.equals("") ) {
                        DefinedItem definedItem = new DefinedItem(name);
                        definedItem.setViewpoint(viewpoint);
                        mDefinedItems.add(definedItem); // Array list of DEFined items
                    }


                } // end <Viewpoint> node


                /********** Text **********/
                else if (qName.equalsIgnoreCase("Text")) {
                    Init_Text_FontParams();
                    attributeValue = attributes.getValue("USE");
                    if (attributeValue != null) {
                        Log.e(TAG, "Text node USE name not currently implemented.");
                    }

                   attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        Text_FontParams.nameTextAttribute = attributeValue;
                    }
                    attributeValue = attributes.getValue("length");
                    if (attributeValue != null) {
                        float[] length = null;
                        // reusing the keys parsing here cause it works
                        parseNumbersString(attributeValue, X3Dobject.interpolatorKeyComponent,
                                1);
                        length = new float[keys.size()];
                        for (int i = 0; i < length.length; i++) {
                            Key keyObject = keys.get(i);
                            length[i] = keyObject.key;
                        }
                        keys.clear();
                        Log.e(TAG, "Text 'length' attribute currently not implemented.");
                    }
                    attributeValue = attributes.getValue("maxExtent");
                    if (attributeValue != null) {
                        Text_FontParams.maxExtent = parseSingleFloatString(attributeValue, false, true);
                        Log.e(TAG, "Text 'maxExtent' attribute currently not implemented. ");
                    }
                    attributeValue = attributes.getValue("string");
                    if (attributeValue != null) {
                        String[] string = parseMFString(attributeValue);
                        String text = "";
                        for (int i = 0; i < string.length; i++) {
                            if (i > 0) text += "\n";
                            text += string[i];
                        }
                        Text_FontParams.string = text;
                    }
                    attributeValue = attributes.getValue("solid");
                    if (attributeValue != null) {
                        Text_FontParams.solid = parseBooleanString(attributeValue);
                        Log.e(TAG, "Text 'solid' attribute currently not implemented. ");
                    }
                } // end <Text> node


                /********** FontStyle **********/
                else if (qName.equalsIgnoreCase("FontStyle")) {
                    attributeValue = attributes.getValue("USE");
                    if (attributeValue != null) { // shared FontStyle
                        // copy the values from a defined style type
                        GVRSceneObject definedSceneObject = root.getSceneObjectByName(attributeValue);
                        if ( definedSceneObject.getClass().equals(GVRTextViewSceneObject.class) ) {
                            GVRTextViewSceneObject gvrTextViewSceneObject = (GVRTextViewSceneObject) definedSceneObject;
                            Text_FontParams.family = gvrTextViewSceneObject.getFontFamily();
                            Text_FontParams.justify = gvrTextViewSceneObject.getJustification();
                            Text_FontParams.spacing = gvrTextViewSceneObject.getLineSpacing();
                            Text_FontParams.size = gvrTextViewSceneObject.getSize();
                            Text_FontParams.style = gvrTextViewSceneObject.getStyleType();
                        }
                        else {
                            Log.e(TAG, "Error: FontStyle USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                        }
                    }
                    else {
                        attributeValue = attributes.getValue("DEF");
                        if (attributeValue != null) {
                            Text_FontParams.nameFontStyle = attributeValue;
                        }
                        attributeValue = attributes.getValue("family");
                        if (attributeValue != null) {
                            String[] family = parseMFString(attributeValue);
                            // handle spaces in the font name
                            if (family.length > 1) {
                                for (int i = 1; i < family.length; i++) {
                                    family[0] += (" " + family[i]);
                                }
                            }
                            Text_FontParams.family = family[0]; // we only accept one family per string
                        }
                        attributeValue = attributes.getValue("horizontal");
                        if (attributeValue != null) {
                            boolean horizontal = parseBooleanString(attributeValue);
                            Log.e(TAG, "horizontal feature of FontStyle not implemented");
                        }
                        attributeValue = attributes.getValue("justify");
                        if (attributeValue != null) {
                            String[] justifyMFString = parseMFString(attributeValue);
                            GVRTextViewSceneObject.justifyTypes[] justify = new GVRTextViewSceneObject.justifyTypes[justifyMFString.length];
                            for (int i = 0; i < justify.length; i++) {
                                if (justifyMFString[i].equalsIgnoreCase("END"))
                                    justify[i] = GVRTextViewSceneObject.justifyTypes.END;
                                else if (justifyMFString[i].equalsIgnoreCase("FIRST"))
                                    justify[i] = GVRTextViewSceneObject.justifyTypes.FIRST;
                                else if (justifyMFString[i].equalsIgnoreCase("MIDDLE"))
                                    justify[i] = GVRTextViewSceneObject.justifyTypes.MIDDLE;
                                else justify[i] = GVRTextViewSceneObject.justifyTypes.BEGIN;
                            }
                            Text_FontParams.justify = justify[0]; // we only accept one justification per string
                        }
                        attributeValue = attributes.getValue("language");
                        if (attributeValue != null) {
                            String language = attributeValue;
                            Log.e(TAG, "language feature of FontStyle not implemented");
                        }
                        attributeValue = attributes.getValue("leftToRight");
                        if (attributeValue != null) {
                            boolean leftToRight = parseBooleanString(attributeValue);
                            Log.e(TAG, "leftToRight feature of FontStyle not implemented");
                        }
                        attributeValue = attributes.getValue("spacing");
                        if (attributeValue != null) {
                            Text_FontParams.spacing = 10.0f * (parseSingleFloatString(attributeValue, false, true) - 1);
                            //Text_FontParams.spacing = parseSingleFloatString(attributeValue, false, true);
                            //Text_FontParams.spacing = 10.0f * (Text_FontParams.spacing - 1.0f);
                        }
                        attributeValue = attributes.getValue("size");
                        if (attributeValue != null) {
                            Text_FontParams.size = 10.0f * parseSingleFloatString(attributeValue, false, true);
                        }
                        attributeValue = attributes.getValue("style");
                        if (attributeValue != null) {
                            if (attributeValue.equalsIgnoreCase("BOLD")) {
                                Text_FontParams.style = GVRTextViewSceneObject.fontStyleTypes.BOLD;
                            } else if (attributeValue.equalsIgnoreCase("ITALIC")) {
                                Text_FontParams.style = GVRTextViewSceneObject.fontStyleTypes.ITALIC;
                            } else if (attributeValue.equalsIgnoreCase("BOLDITALIC")) {
                                Text_FontParams.style = GVRTextViewSceneObject.fontStyleTypes.BOLDITALIC;
                            } else {
                                Text_FontParams.style = GVRTextViewSceneObject.fontStyleTypes.PLAIN;
                            }
                        }
                        attributeValue = attributes.getValue("topToBottom");
                        if (attributeValue != null) {
                            boolean topToBottom = parseBooleanString(attributeValue);
                            Log.e(TAG, "topToBottom feature of FontStyle not implemented");
                        }
                    } // not re-USE FontStyle
                } // end <FontStyle> node

                /********** Billboard **********/
                else if (qName.equalsIgnoreCase("Billboard")) {
                    Log.e(TAG, "X3D Billboard currently not implemented. ");
                    //TODO: Billboard not currently implemented
                    String name = "";
                    float[] axisOfRotation =
                            {
                                    0, 1, 0
                            };
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("axisOfRotation");
                    if (attributeValue != null) {
                        axisOfRotation = parseFixedLengthFloatString(attributeValue, 3, true,
                                false);
                    }

                } // end <Billboard> node


                /********** Inline **********/
                else if (qName.equalsIgnoreCase("Inline")) {
                    // Inline data saved, and added after the inital .x3d program is parsed
                    String name = "";
                    String[] url = new String[1];
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("url");
                    if (attributeValue != null) {
                        //url = parseMFString(attributeValue);
                        url[0] = attributeValue;
                        GVRSceneObject inlineGVRSceneObject = currentSceneObject; // preserve
                        // the
                        // currentSceneObject
                        if (lodManager.isActive()  &&
                                (inlineGVRSceneObject.getComponent(GVRLODGroup.getComponentType()) != null)) {
                            inlineGVRSceneObject = AddGVRSceneObject();
                            inlineGVRSceneObject.setName("inlineGVRSceneObject"
                                    + lodManager.getCurrentRangeIndex());
                            final GVRSceneObject parent = inlineGVRSceneObject.getParent();
                            if (null == parent.getComponent(GVRLODGroup.getComponentType())) {
                                parent.attachComponent(new GVRLODGroup(gvrContext));
                            }
                            final GVRLODGroup lodGroup = (GVRLODGroup) parent.getComponent(GVRLODGroup.getComponentType());
                            lodGroup.addRange(lodManager.getMinRange(), inlineGVRSceneObject);
                            lodManager.increment();
                        }
                        InlineObject inlineObject = new InlineObject(inlineGVRSceneObject,
                                url);
                        inlineObjects.add(inlineObject);
                    }

                    // LOD has it's own GVRSceneObject which has a
                    // GVRLODGroup component attached
                    if (lodManager.isActive() && lodManager.transformLODSceneObject == null) {
                    //if (lodManager.transformLODSceneObject == null) {
                        lodManager.transformLODSceneObject = AddGVRSceneObject();
                        lodManager.transformLODSceneObject.attachComponent(new GVRLODGroup(gvrContext));
                        currentSceneObject = lodManager.transformLODSceneObject;
                    }
                } // end <Inline> node


                /********** LOD **********/
                else if (qName.equalsIgnoreCase("LOD")) {
                    String name = "";
                    float[] center =
                            {
                                    0, 0, 0
                            };
                    float[] range = null;
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("center");
                    if (attributeValue != null) {
                        center = parseFixedLengthFloatString(attributeValue, 3, false, false);
                    }
                    attributeValue = attributes.getValue("range");
                    if (attributeValue != null) {
                        parseNumbersString(attributeValue, X3Dobject.LODComponent, 1);
                        range = new float[keys.size() + 2];
                        range[0] = 0;
                        for (int i = 0; i < keys.size(); i++) {
                            Key keyObject = keys.get(i);
                            range[i + 1] = keyObject.key;
                        }
                        range[range.length - 1] = Float.MAX_VALUE;
                        keys.clear();
                    }
                    lodManager.set(range, center);

                    // LOD has it's own GVRSceneObject which has a
                    // GVRLODGroup component attached
                    if (lodManager.transformLODSceneObject == null) {
                        lodManager.transformLODSceneObject = AddGVRSceneObject();
                        lodManager.transformLODSceneObject.attachComponent(new GVRLODGroup(gvrContext));
                        currentSceneObject = lodManager.transformLODSceneObject;
                    }

                } // end <LOD> Level-of-Detail node


                /********** Switch **********/
                else if (qName.equalsIgnoreCase("Switch")) {
                    String name = "";
                    int whichChoice = -1;

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("whichChoice");
                    if (attributeValue != null) {
                        whichChoice = parseIntegerString(attributeValue);
                    }
                    currentSceneObject = AddGVRSceneObject();
                    currentSceneObject.setName( name );

                    GVRSwitch gvrSwitch = new GVRSwitch( gvrContext );
                    gvrSwitch.setSwitchIndex( whichChoice );
                    currentSceneObject.attachComponent(gvrSwitch);

                    DefinedItem definedItem = new DefinedItem(currentSceneObject.getName());
                    definedItem.setGVRSceneObject(currentSceneObject);
                    mDefinedItems.add(definedItem); // Array list of DEFined items in the X3D scene
                } // end <Switch> node


                /********** Anchor **********/
                else if (qName.equalsIgnoreCase("Anchor")) {
                    String name = "";
                    String description = "";
                    String[] parameter = null;
                    String url = "";
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("description");
                    if (attributeValue != null) {
                        description = attributeValue;
                    }
                    attributeValue = attributes.getValue("parameter");
                    if (attributeValue != null) {
                        parameter = parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("url");
                    if (attributeValue != null) {

                        // url = parseMFString(attributeValue);
                        // TODO: issues with parsing

                        // multiple strings with special chars
                        url = attributeValue;
                    }
                    // Set the currentSensor pointer so that child objects will be added
                    // to the list of eye pointer objects.
                    currentSceneObject = AddGVRSceneObject();
                    currentSceneObject.setName(name);
                    Sensor sensor = new Sensor(name, Sensor.Type.ANCHOR,
                            currentSceneObject);
                    sensor.setAnchorURL(url);
                    sensors.add(sensor);
                    animationInteractivityManager.BuildInteractiveObjectFromAnchor(sensor, url);
                } // end <Anchor> node


                /********** TouchSensor **********/
                else if (qName.equalsIgnoreCase("TouchSensor")) {
                    String name = "";
                    String description = "";
                    boolean enabled = true;
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("description");
                    if (attributeValue != null) {
                        description = attributeValue;
                    }
                    attributeValue = attributes.getValue("enabled");
                    if (attributeValue != null) {
                        enabled = parseBooleanString(attributeValue);
                    }

                    Sensor sensor = new Sensor(name, Sensor.Type.TOUCH, currentSceneObject);
                    sensors.add(sensor);
                    // add colliders to all objects under the touch sensor
                    currentSceneObject.attachCollider(new GVRMeshCollider(gvrContext, true));
                } // end <TouchSensor> node


                /********** ProximitySensor **********/
                else if (qName.equalsIgnoreCase("ProximitySensor")) {
                    Log.e(TAG, "ProximitySensor currently not implemented. ");
                    //TODO Proximity Sensor not currently implemented
                    String name = "";
                    String description = "";
                    String[] parameter;
                    String[] url;
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("url");
                    if (attributeValue != null) {
                        url = parseMFString(attributeValue);
                    }
                }  //  end <ProximitySensor> node


                /********** Script **********/
                else if (qName.equalsIgnoreCase("Script")) {
                    String name = "";
                    Boolean directOutput = false;
                    Boolean mustEvaluate = false;
                    String[] url = null;

                    // The EcmaScript / JavaScript will be parsed inside
                    // SAX's characters method
                    parseJavaScript = true;
                    //reset.  This will hold complete JavaScript function(s)
                    javaScriptCode = "";

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("url");
                    if (attributeValue != null) {
                        url = parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("directOutput");
                    if (attributeValue != null) {
                        directOutput = parseBooleanString(attributeValue);
                    }
                    attributeValue = attributes.getValue("mustEvaluate");
                    if (attributeValue != null) {
                        mustEvaluate = parseBooleanString(attributeValue);
                    }
                    currentScriptObject = new ScriptObject(name, directOutput, mustEvaluate, url);
                }  //  end <Script> node


                /******* field (embedded inside <Script>) node *******/
                else if (qName.equalsIgnoreCase("field")) {

                    String name = "";
                    ScriptObject.AccessType accessType = ScriptObject.AccessType.INPUT_OUTPUT;
                    String type = "";

                    attributeValue = attributes.getValue("accessType");
                    if (attributeValue != null) {
                        if (attributeValue.equals("inputOnly")) {
                            accessType = ScriptObject.AccessType.INPUT_ONLY;
                        } else if (attributeValue.equals("outputOnly")) {
                            accessType = ScriptObject.AccessType.OUTPUT_ONLY;
                        } else if (attributeValue.equals("inputOutput")) {
                            accessType = ScriptObject.AccessType.INPUT_OUTPUT;
                        } else if (attributeValue.equals("initializeOnly")) {
                            accessType = ScriptObject.AccessType.INITIALIZE_ONLY;
                        }
                    }
                    attributeValue = attributes.getValue("name");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("type");
                    if (attributeValue != null) {
                        type = attributeValue;
                    }
                    if (currentScriptObject != null) {
                        currentScriptObject.addField(name, accessType, type);
                    }
                }  //  end <field> node


                /********** BooleanToggle **********/
                else if (qName.equalsIgnoreCase("BooleanToggle")) {
                    String name = "";
                    boolean toggle = false;

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("toggle");
                    if (attributeValue != null) {
                        toggle = parseBooleanString(attributeValue);
                    }
                    EventUtility eventUtility = new EventUtility(name, EventUtility.DataType.BOOLEAN, EventUtility.Type.TOGGLE, toggle);
                    eventUtilities.add(eventUtility);
                }  //  end <BooleanToggle> node


                /********** ElevationGrid **********/
                else if (qName.equalsIgnoreCase("ElevationGrid")) {
                    Log.e(TAG, "X3D ElevationGrid not currently implemented. ");
                    String name = "";
                    float creaseAngle = 0;
                    float[] height = null;
                    boolean solid = true;
                    int xDimension = 0;
                    float xSpacing = 1;
                    int zDimension = 0;
                    float zSpacing = 1;

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("xDimension");
                    if (attributeValue != null) {
                        xDimension = (int) parseSingleFloatString(attributeValue, false,
                                true);
                    }
                    attributeValue = attributes.getValue("xSpacing");
                    if (attributeValue != null) {
                        xSpacing = (int) parseSingleFloatString(attributeValue, false, true);
                    }
                    attributeValue = attributes.getValue("zDimension");
                    if (attributeValue != null) {
                        zDimension = (int) parseSingleFloatString(attributeValue, false,
                                true);
                    }
                    attributeValue = attributes.getValue("zSpacing");
                    if (attributeValue != null) {
                        zSpacing = parseSingleFloatString(attributeValue, false, true);
                    }
                    attributeValue = attributes.getValue("height");
                    if (attributeValue != null) {
                        parseNumbersString(attributeValue, X3Dobject.elevationGridHeight,
                                xDimension * zDimension);
                        height = new float[(xDimension + 1) * (zDimension + 1)];
                        for (int i = 0; i < height.length; i++) {
                            height[i] = floatArray.get(i);
                        }
                        floatArray.clear();
                    }

                    if (height != null) {

                        float[][] vertices = new float[height.length][3];

                        for (int i = 0; i < (zDimension + 1); i++) {
                            for (int j = 0; j < (xDimension + 1); j++) {
                                vertices[i * (xDimension + 1) + j][0] = (j * xSpacing); // vertex
                                // x value
                                vertices[i * (xDimension + 1)
                                        + j][1] = (height[i * (xDimension + 1) + j]); // vertex y
                                // value
                                vertices[i * (xDimension + 1) + j][2] = (i * zSpacing); // vertex
                                // z value
                            }
                        }
                        // char[] ifs = new char[(xDimension-1)*(zDimension-1)*6]; //
                        // dimensions * 2 polygons per 4 vertices * 3 for x,y,z vertices per
                        // polygon to create a face.
                        Vector3f[] polygonNormals = new Vector3f[xDimension * zDimension * 2];
                        for (int i = 0; i < xDimension * zDimension * 2; i++) {
                            polygonNormals[i] = new Vector3f();
                        }
                        Vector3f[] vertexNormals = new Vector3f[(xDimension + 1)
                                * (zDimension + 1)];
                        for (int i = 0; i < (xDimension + 1) * (zDimension + 1); i++) {
                            vertexNormals[i] = new Vector3f();
                        }

                        // Polygon Normal found by cross product using 2 of the 3 sides of a
                        // polygon
                        // we know vertices are: (i*xSpacing, height, j*zSpacing),
                        // ((i+1)*xSpacing, height+1, (j+1)*zSpacing),
                        // (i*xSpacing, height, j*zSpacing), ((i+1)*xSpacing, height+1,
                        // (j+1)*zSpacing)
                        Vector3f[] vLine = new Vector3f[3];
                        for (int i = 0; i < 3; i++) {
                            vLine[i] = new Vector3f();
                        }
                        Vector3f[] crossProduct = new Vector3f[2];
                        crossProduct[0] = new Vector3f();
                        crossProduct[1] = new Vector3f();

                        for (int i = 0; i < zDimension; i++) {
                            for (int j = 0; j < xDimension; j++) {
                                // line 0 is the 'top' line, and line 1 is the 'bottom' line of
                                // the rectangle
                                vLine[0].set(
                                        vertices[i * (xDimension + 1) + j + 1][0]
                                                - vertices[i * (xDimension + 1) + j][0],
                                        vertices[i * (xDimension + 1) + j + 1][1]
                                                - vertices[i * (xDimension + 1) + j][1],
                                        vertices[i * (xDimension + 1) + j + 1][2]
                                                - vertices[i * (xDimension + 1) + j][2]);
                                vLine[1]
                                        .set(vertices[i * (xDimension + 1) + j + xDimension + 2][0]
                                                        - vertices[i * (xDimension + 1) + j + xDimension + 1][0],
                                                vertices[i * (xDimension + 1) + j + xDimension + 2][1]
                                                        - vertices[i * (xDimension + 1) + j + xDimension
                                                        + 1][1],
                                                vertices[i * (xDimension + 1) + j + xDimension + 2][2]
                                                        - vertices[i * (xDimension + 1) + j + xDimension
                                                        + 1][2]);
                                // hypotenuse of the 4 vertices that create a rectangle
                                vLine[2].set(
                                        vertices[i * (xDimension + 1) + j + 1][0]
                                                - vertices[i * (xDimension + 1) + j + xDimension
                                                + 1][0],
                                        vertices[i * (xDimension + 1) + j + 1][1]
                                                - vertices[i * (xDimension + 1) + j + xDimension
                                                + 1][1],
                                        vertices[i * (xDimension + 1) + j + 1][2]
                                                - vertices[i * (xDimension + 1) + j + xDimension
                                                + 1][2]);

                                // cross product to determine normal and save the value: line0 x
                                // hypotenuse, line1 x hypotenuse
                                vLine[0].cross(vLine[2], crossProduct[0]);
                                vLine[1].cross(vLine[2], crossProduct[1]);
                                polygonNormals[(i * xDimension + j) * 2]
                                        .set(crossProduct[0].normalize());
                                polygonNormals[(i * xDimension + j) * 2 + 1]
                                        .set(crossProduct[1].normalize());
                            }
                        } // end getting the polygon normals

                        // calculate the vertex normals
                        Vector3f accumNormal = new Vector3f();
                        // for (int i = 0; i < vertexNormals.length; i++) {
                        for (int i = 0; i < 3; i++) {
                            accumNormal.set(0, 0, 0);
                            if (i > 1)
                                accumNormal.add(polygonNormals[i - 1]);
                            accumNormal.add(polygonNormals[i]);

                        }
                        /*********** Calculate vertex normals next"); */
                        //TODO: ElevationGrid not completed
                        // gvrMesh = new GVRMesh(gvrContext);
                    }
                } // end <ElevationGrid> node


                /********** Navigation Info **********/
                else if (qName.equalsIgnoreCase("NavigationInfo")) {
                    String name = "";
                    float[] avatarSize =
                            {
                                    0.25f, 1.6f, 0.75f
                            };
                    boolean headlight = true;
                    float speed = 1;
                    float transitionTime = 1;
                    float visibilityLimit = 0;

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("avatarSize");
                    if (attributeValue != null) {
                        avatarSize = parseFixedLengthFloatString(attributeValue, 3, false,
                                true);
                        Log.e(TAG, "NavigationInfo avatarSize attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("headlight");
                    if (attributeValue != null) {
                        headlight = parseBooleanString(attributeValue);
                    }
                    attributeValue = attributes.getValue("speed");
                    if (attributeValue != null) {
                        speed = parseSingleFloatString(attributeValue, false, true);
                        Log.e(TAG, "NavigationInfo speed attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("transitionTime");
                    if (attributeValue != null) {
                        transitionTime = parseSingleFloatString(attributeValue, false, true);
                        Log.e(TAG,
                                "NavigationInfo transitionTime attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("type");
                    if (attributeValue != null) {
                        Log.e(TAG, "NavigationInfo type attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("visibilityLimit");
                    if (attributeValue != null) {
                        visibilityLimit = parseSingleFloatString(attributeValue, false, true);
                        Log.e(TAG,
                                "NavigationInfo visibilityLimit attribute not implemented. ");
                    }
                    if (headlight) {
                        GVRSceneObject headlightSceneObject = new GVRSceneObject(gvrContext);
                        GVRDirectLight headLight = new GVRDirectLight(gvrContext);
                        headlightSceneObject.attachLight(headLight);
                        headLight.setDiffuseIntensity(1, 1, 1, 1);
                        headlightSceneObject.setName("HeadLight");
                        cameraRigAtRoot.addChildObject(headlightSceneObject);
                    }

                } // end <NavigationInfo> node


                /********** Background **********/
                else if (qName.equalsIgnoreCase("Background")) {
                    float[] skycolor =
                            {
                                    0, 0, 0
                            };
                    String[] backUrl = {};
                    String[] bottomUrl = {};
                    String[] frontUrl = {};
                    String[] leftUrl = {};
                    String[] rightUrl = {};
                    String[] topUrl = {};
                    float transparency = 0;
                    float groundAngle = 0;

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        Log.e(TAG, "Background DEF attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("groundColor");
                    if (attributeValue != null) {
                        Log.e(TAG, "Background groundColor attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("skyColor");
                    if (attributeValue != null) {
                        skycolor = parseFixedLengthFloatString(attributeValue, 3, true,
                                false);
                    }
                    attributeValue = attributes.getValue("backUrl");
                    if (attributeValue != null) {
                        backUrl = parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("bottomUrl");
                    if (attributeValue != null) {
                        bottomUrl = parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("frontUrl");
                    if (attributeValue != null) {
                        frontUrl = parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("leftUrl");
                    if (attributeValue != null) {
                        leftUrl = parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("rightUrl");
                    if (attributeValue != null) {
                        rightUrl = parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("topUrl");
                    if (attributeValue != null) {
                        topUrl = parseMFString(attributeValue);
                    }
                    attributeValue = attributes.getValue("transparency");
                    if (attributeValue != null) {
                        transparency = parseSingleFloatString(attributeValue, true, false);
                        Log.e(TAG, "Background transparency attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("groundAngle");
                    if (attributeValue != null) {
                        Log.e(TAG, "Background groundAngle attribute not implemented. ");
                        groundAngle = parseSingleFloatString(attributeValue, false, true);
                        if (groundAngle > (float) Math.PI / 2) {
                            groundAngle = (float) Math.PI / 2;
                            Log.e(TAG, "Background groundAngle cannot exceed PI/2.");
                        }
                    }

                    // if url's defined, use cube mapping for the background
                    if ((backUrl.length > 0) && (bottomUrl.length > 0)
                            && (frontUrl.length > 0) && (leftUrl.length > 0)
                            && (rightUrl.length > 0) && (topUrl.length > 0)) {

                        ArrayList<GVRTexture> textureList = new ArrayList<GVRTexture>(6);
                        GVRAssetLoader loader = gvrContext.getAssetLoader();
                        String urlAttribute = backUrl[0].substring(0,
                                backUrl[0].indexOf("."));
                        int assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            textureList
                                    .add(loader.loadTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = rightUrl[0].substring(0, rightUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            textureList
                                    .add(loader.loadTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = frontUrl[0].substring(0, frontUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            textureList
                                    .add(loader.loadTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = leftUrl[0].substring(0, leftUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            textureList
                                    .add(loader.loadTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = topUrl[0].substring(0, topUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            textureList
                                    .add(loader.loadTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = bottomUrl[0].substring(0, bottomUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            textureList
                                    .add(loader.loadTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        GVRCubeSceneObject mCubeEvironment = new GVRCubeSceneObject(
                                gvrContext, false, textureList);
                        mCubeEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                                CUBE_WIDTH);

                        root.addChildObject(mCubeEvironment);
                    } else {
                        // Not cubemapping, then set default skyColor
                        gvrContext.getMainScene().setBackgroundColor(skycolor[0], skycolor[1], skycolor[2], 1);
                    }

                } // end <Background> node

                // These next few nodes are used once per file and thus moved
                //  to the end of the parsing's if-then-else statement

                /********** X3D **********/
                else if (qName.equalsIgnoreCase("x3d")) {
                    attributeValue = attributes.getValue("version");
                    if (attributeValue != null) {
                        // currently, we don't do anything with the version information
                    }
                    attributeValue = attributes.getValue("profile");
                    if (attributeValue != null) {
                        // currently, we don't do anything with the profile information
                    }

                }  //  end <X3D> node

                /********** Scene **********/
                else if (qName.equalsIgnoreCase("scene")) {
                    ;

                }  //  end <Sene> node

                /***** end of parsing the nodes currently parsed *****/
                else {
                    Log.e(TAG, "X3D node " + qName + " not implemented.");
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (qName.equalsIgnoreCase("Transform")) {
                if (!gvrGroupingNodeUSEd) {
                    if (currentSceneObject.getParent() == root)
                        currentSceneObject = null;
                    else {
                        String name = currentSceneObject.getName();
                        currentSceneObject = currentSceneObject.getParent();
                        while (currentSceneObject.getName()
                                .equals(name + TRANSFORM_ROTATION_)
                                || currentSceneObject.getName()
                                .equals(name + TRANSFORM_TRANSLATION_)
                                || currentSceneObject.getName().equals(name + TRANSFORM_SCALE_)
                                || currentSceneObject.getName().equals(name + TRANSFORM_CENTER_)
                                || currentSceneObject.getName()
                                .equals(name + TRANSFORM_NEGATIVE_CENTER_)
                                || currentSceneObject.getName()
                                .equals(name + TRANSFORM_SCALE_ORIENTATION_)
                                || currentSceneObject.getName()
                                .equals(name + TRANSFORM_NEGATIVE_SCALE_ORIENTATION_)) {
                            currentSceneObject = currentSceneObject.getParent();
                        }
                    }
                }
                gvrGroupingNodeUSEd = false;
            } // end </Transform> parsing
            else if (qName.equalsIgnoreCase("Group")) {
                if (currentSceneObject.getParent() == root)
                    currentSceneObject = null;
                else
                    currentSceneObject = currentSceneObject.getParent();
            } else if (qName.equalsIgnoreCase("Shape")) {
                if (!gvrRenderingDataUSEd) {
                    // SHAPE node not being USEd (shared) elsewhere

                    // Shape containts Text
                    if (gvrTextViewSceneObject != null) {
                        gvrTextViewSceneObject.setTextColor((((0xFF << 8)
                                + (int) (shaderSettings.diffuseColor[0] * 255) << 8)
                                + (int) (shaderSettings.diffuseColor[1] * 255) << 8)
                                + (int) (shaderSettings.diffuseColor[2] * 255));
                        gvrTextViewSceneObject = null;
                    }

                    {
                        // UNIVERSAL_LIGHTS

                        if (!gvrMaterialUSEd) { // if GVRMaterial is NOT set by a USE statement.

                            if (meshAttachedSceneObject == null) {
                                gvrMaterial = shaderSettings.material;
                                gvrRenderData.setMaterial(gvrMaterial);
                            } else {
                                // This GVRSceneObject came with a GVRRenderData and GVRMaterial

                                // already attached.  Examples of this are Text or primitives
                                // such as the Box, Cone, Cylinder, Sphere

                                DefinedItem definedGRRenderingData = null;
                                if (gvrRenderData != null) {
                                    // <Shape> node created an unused gvrRenderData
                                    // Check if we had a DEF in Shape node so that we can point to

                                    // the new gvrRenderData
                                    for (DefinedItem definedItem : mDefinedItems) {
                                        if (definedItem.getGVRRenderData() == gvrRenderData) {
                                            definedGRRenderingData = definedItem;
                                            break;
                                        }
                                    }
                                }
                                gvrRenderData = meshAttachedSceneObject.getRenderData();
                                // reset the DEF item to now point to the shader
                                if (definedGRRenderingData != null)
                                    definedGRRenderingData.setGVRRenderData(gvrRenderData);
                                gvrMaterial = gvrRenderData.getMaterial();
                            }
                            // X3D doesn't have an ambient color so need to do color
                            // calibration tests on how to set this.
                            gvrMaterial.setVec4("diffuse_color",
                                    shaderSettings.diffuseColor[0],
                                    shaderSettings.diffuseColor[1],
                                    shaderSettings.diffuseColor[2],
                                    (1.0f - shaderSettings.getTransparency()) );
                            gvrMaterial.setVec4("specular_color",
                                    shaderSettings.specularColor[0],
                                    shaderSettings.specularColor[1],
                                    shaderSettings.specularColor[2], 1.0f);
                            gvrMaterial.setVec4("emissive_color",
                                    shaderSettings.emissiveColor[0],
                                    shaderSettings.emissiveColor[1],
                                    shaderSettings.emissiveColor[2], 1.0f);
                            gvrMaterial.setFloat("specular_exponent",
                                    128.0f * shaderSettings.shininess);

                            if (!shaderSettings.getMaterialName().isEmpty()) {
                                DefinedItem definedItem = new DefinedItem(
                                        shaderSettings.getMaterialName());
                                definedItem.setGVRMaterial(gvrMaterial);
                                mDefinedItems.add(definedItem); // Add gvrMaterial to Array list
                                // of DEFined items Clones
                                // objects with USE
                            }

                            if (shaderSettings.texture != null) {
                                gvrMaterial.setTexture("diffuseTexture",
                                        shaderSettings.texture);
                                // if the TextureMap is a DEFined item, then set the
                                // GVRMaterial to it as well to help if we set the
                                // in a SCRIPT node.
                                for (DefinedItem definedItem: mDefinedItems) {
                                    if ( definedItem.getGVRTexture() == shaderSettings.texture) {
                                        definedItem.setGVRMaterial(gvrMaterial);
                                        break;
                                    }
                                }
                            }

                            // Texture Transform
                            // If DEFined iteam, add to the DeFinedItem list. Maay be interactive
                            // GearVR may not be doing texture transforms on primitives or text
                            // crash otherwise.

                            if (meshAttachedSceneObject == null) {
                                if (!shaderSettings.getTextureTransformName().isEmpty()) {
                                    DefinedItem definedItem = new DefinedItem(
                                            shaderSettings.getTextureTransformName());
                                    definedItem.setGVRMaterial(gvrMaterial);
                                    definedItem.setTextureTranslation(shaderSettings.getTextureTranslation());
                                    definedItem.setTextureCenter(shaderSettings.getTextureCenter());
                                    definedItem.setTextureScale(shaderSettings.getTextureScale());
                                    definedItem.setTextureRotation(shaderSettings.getTextureRotation().getValue());
                                    definedItem.setName(shaderSettings.getTextureTransformName());
                                    mDefinedItems.add(definedItem); // Add gvrMaterial to Array list
                                }
                                // Texture Transform Matrix equation:
                                // TC' = -C * S * R * C * T * TC
                                //where TC' is the transformed texture coordinate
                                //   TC is the original Texture Coordinate
                                //   C = center, S = scale, R = rotation, T = translation
                                Matrix3f textureTransform = animationInteractivityManager.SetTextureTransformMatrix(
                                        shaderSettings.getTextureTranslation(),
                                        shaderSettings.getTextureCenter(),
                                        shaderSettings.getTextureScale(),
                                        shaderSettings.getTextureRotation());

                                shaderSettings.textureMatrix = textureTransform;
                                float[] texMtx = new float[9];
                                shaderSettings.textureMatrix.get(texMtx);
                                gvrMaterial.setFloatArray("texture_matrix", texMtx);
                            }

                            // Appearance node thus far contains properties of GVRMaterial
                            // node
                            if (!shaderSettings.getAppearanceName().isEmpty()) {
                                DefinedItem definedItem = new DefinedItem(
                                        shaderSettings.getAppearanceName());
                                definedItem.setGVRMaterial(gvrMaterial);
                                mDefinedItems.add(definedItem);
                                // Add gvrMaterial to Array list
                                // of DEFined items Clones
                                // objects with USE
                            }

                            if ((shaderSettings.getTransparency() != 0) && (shaderSettings.getTransparency() != 1)) {
                                gvrRenderData.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
                            }

                        } // end ! gvrMaterialUSEd

                        gvrTexture = null;
                    }
                } // end !gvrRenderingDataUSEd

                if (meshAttachedSceneObject != null) {
                    // gvrRenderData already attached to a GVRSceneObject such as a
                    // Cone or Cylinder
                    meshAttachedSceneObject = null;
                } else
                    currentSceneObject.attachRenderData(gvrRenderData);

                if (lodManager.shapeLODSceneObject != null) {
                    // if this Shape node was a direct child of a
                    // Level-of-Detial (LOD),then restore the parent object
                    // since we had to add a GVRSceneObject to support
                    // the Shape node's attachement to LOD.
                    currentSceneObject = currentSceneObject.getParent();
                    lodManager.shapeLODSceneObject = null;
                }

                gvrMaterialUSEd = false; // for DEFine and USE, true if we encounter a
                // USE
                gvrRenderingDataUSEd = false; // for DEFine and USE gvrRenderingData for
                // x3d SHAPE node
                gvrRenderData = null;
            } // end of ending Shape node
            else if (qName.equalsIgnoreCase("Appearance")) {
                ;
            } else if (qName.equalsIgnoreCase("Material")) {
                ;
            } else if (qName.equalsIgnoreCase("ImageTexture")) {
                ;
            } else if (qName.equalsIgnoreCase("TextureTransform")) {
                ;
            } else if (qName.equalsIgnoreCase("IndexedFaceSet")) {
                if (indexedSetUSEName.length() > 0) {
                    //Using previously defined mesh
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (indexedSetUSEName.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {
                        gvrRenderData.setMesh( useItem.getGVRMesh() );
                    }
                    else {
                        Log.e(TAG, "Error: IndexedFaceSet USE='" + attributeValue + "'; No matching DEF='" + attributeValue + "'.");
                    }
                }
                else {
                    if (reorganizeVerts) {
                        gvrVertexBuffer = meshCreator.organizeVertices(gvrIndexBuffer);
                        reorganizeVerts = false;
                    }
                    GVRMesh mesh = new GVRMesh(gvrContext, gvrVertexBuffer.getDescriptor());
                    if (indexedSetDEFName.length() > 0) {
                        // Save GVRMesh since it may be reused later.
                        DefinedItem definedItem = new DefinedItem(indexedSetDEFName);
                        definedItem.setGVRMesh(mesh);
                        mDefinedItems.add(definedItem); // Array list of DEFined items
                    }
                    gvrRenderData.setMesh(mesh);
                    mesh.setIndexBuffer(gvrIndexBuffer);
                    mesh.setVertexBuffer(gvrVertexBuffer);
                }
                gvrVertexBuffer = null;
                gvrIndexBuffer = null;
                indexedSetDEFName = "";
                indexedSetUSEName = "";
            } else if (qName.equalsIgnoreCase("Coordinate")) {
                // vertices.clear(); // clean up this Vector<Vertex> list.
            } else if (qName.equalsIgnoreCase("TextureCoordinate")) {
                // textureCoord.clear(); // clean up this Vector<TextureValues> list.
            } else if (qName.equalsIgnoreCase("Normal")) {
                // vertexNormal.clear(); // clean up this Vector<VertexNormal> list.
            } else if (qName.equalsIgnoreCase("DirectionalLight")) {
                ;
            } else if (qName.equalsIgnoreCase("PointLight")) {
                ;
            } else if (qName.equalsIgnoreCase("SpotLight")) {
                ;
            } else if (qName.equalsIgnoreCase("TimeSensor")) {
                ;
            } else if (qName.equalsIgnoreCase("PositionInterpolator")) {
                ;
            } else if (qName.equalsIgnoreCase("OrientationInterpolator")) {
                ;
            } else if (qName.equalsIgnoreCase("ROUTE")) {
                ;
            } else if (qName.equalsIgnoreCase("TouchSensor")) {
                ;
            } else if (qName.equalsIgnoreCase("ProximitySensor")) {
                ;
            } else if (qName.equalsIgnoreCase("Text")) {
                gvrTextViewSceneObject = new GVRTextViewSceneObject(gvrContext,
                        Text_FontParams.nameFontStyle,
                        Text_FontParams.string, Text_FontParams.family, Text_FontParams.justify,
                        Text_FontParams.spacing, Text_FontParams.size, Text_FontParams.style);

                GVRRenderData gvrRenderData = gvrTextViewSceneObject.getRenderData();
                gvrRenderData.setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);


                if ( !Text_FontParams.nameTextAttribute.equals("")) {
                    // add it to the list of DEFined objects
                        DefinedItem definedItem = new DefinedItem(Text_FontParams.nameTextAttribute);
                        definedItem.setGVRTextViewSceneObject(gvrTextViewSceneObject);
                        mDefinedItems.add(definedItem); // Array list of DEFined items
                }
                if ( !Text_FontParams.nameFontStyle.equals("")) {
                    // add FontStyle to the list of DEFined objects
                    DefinedItem definedItem = new DefinedItem(Text_FontParams.nameFontStyle);
                    definedItem.setGVRTextViewSceneObject(gvrTextViewSceneObject);
                    mDefinedItems.add(definedItem); // Array list of DEFined items
                }

                gvrTextViewSceneObject.setTextColor(Color.WHITE); // default
                gvrTextViewSceneObject.setBackgroundColor(Color.TRANSPARENT); // default
                currentSceneObject.addChildObject(gvrTextViewSceneObject);
            } else if (qName.equalsIgnoreCase("FontStyle")) {
                ;
            } else if (qName.equalsIgnoreCase("Billboard")) {
                ;
            } else if (qName.equalsIgnoreCase("Anchor")) {
                if (currentSceneObject.getParent() == root)
                    currentSceneObject = null;
                else
                    currentSceneObject = currentSceneObject.getParent();
            } else if (qName.equalsIgnoreCase("Inline")) {
                ;
            } else if (qName.equalsIgnoreCase("LOD")) {
                // End of LOD so go to the parent of the current
                // GVRSceneObject which was added to support LOD
                if (currentSceneObject == lodManager.transformLODSceneObject) {
                    currentSceneObject = currentSceneObject.getParent();
                }
                lodManager.transformLODSceneObject = null;
            } else if (qName.equalsIgnoreCase("Switch")) {
                // Verify the Switch index is between 0 and (max number of children - 1)
                // if it is not, then no object should appear per the X3D spec.
                GVRSwitch gvrSwitch = (GVRSwitch)currentSceneObject.getComponent(GVRSwitch.getComponentType());
                if ( gvrSwitch != null) {
                    if ( (gvrSwitch.getSwitchIndex() < 0) || (gvrSwitch.getSwitchIndex() >= currentSceneObject.getChildrenCount()) ) {
                        // if the switch index is outside the array of possible children (which is legal in X3D)
                        // then no object should appear, which occurs when GVRSwitch is set higher than possilble # children.
                        gvrSwitch.setSwitchIndex( currentSceneObject.getChildrenCount() );
                    }
                }
                currentSceneObject = currentSceneObject.getParent();
            } else if (qName.equalsIgnoreCase("Box")) {
                ;
            } else if (qName.equalsIgnoreCase("Cone")) {
                ;
            } else if (qName.equalsIgnoreCase("Cylinder")) {
                ;
            } else if (qName.equalsIgnoreCase("Sphere")) {
                ;
            }

            /*********
             * Less frequently used commands and thus moved to end of a long
             * if-then-else.
             ********/
            else if (qName.equalsIgnoreCase("Viewpoint")) {
                ;
            } else if (qName.equalsIgnoreCase("Script")) {
                javaScriptCode = JAVASCRIPT_IMPORT_PACKAGE + '\n' + javaScriptCode  + '\n';
                currentScriptObject.setJavaScriptCode(javaScriptCode);
                if ( animationInteractivityManager.V8JavaScriptEngine) {
                    GVRJavascriptV8File gvrJavascriptV8File = new GVRJavascriptV8File(gvrContext, javaScriptCode);
                    currentScriptObject.setGVRJavascriptV8File( gvrJavascriptV8File );
                }
                else {
                    // using Mozila Rhino js engine
                    GVRJavascriptScriptFile gvrJavascriptScriptFile = new GVRJavascriptScriptFile(gvrContext, javaScriptCode);
                    currentScriptObject.setGVRJavascriptScriptFile(gvrJavascriptScriptFile);
                }
                scriptObjects.add(currentScriptObject);

                parseJavaScript = false;
                currentScriptObject = null;
            } else if (qName.equalsIgnoreCase("field")) {
                ; // embedded inside a <SCRIPT> node
            } else if (qName.equalsIgnoreCase("BooleanToggle")) {
                ;
            } else if (qName.equalsIgnoreCase("NavigationInfo")) {
                ;
            } else if (qName.equalsIgnoreCase("Background")) {
                ;
            } else if (qName.equalsIgnoreCase("ElevationGrid")) {
                ;
            }
            /*********
             * These are once per file commands and thus moved to the end of the
             * if-then-else statement
             ********/
            else if (qName.equalsIgnoreCase("scene")) {
                // Now that the scene is over, we can set construct the animations since
                // we now have all the ROUTES, and set up either the default or an actual
                // camera based on a <Viewpoint> in the scene.

                // First, set up the camera / Viewpoint
                // The camera rig is indirectly attached to the root

                if (cameraRigAtRoot != null) {

                    GVRCameraRig mainCameraRig = gvrContext.getMainScene().getMainCameraRig();

                    float[] cameraPosition = {0, 0, 10}; // X3D's default camera position
                    if ( !viewpoints.isEmpty()) {

                        // X3D file contained a <Viewpoint> node.
                        // Per X3D spec., when there is 1 or more Viewpoints in the
                        // X3D file, init with the first viewpoint in the X3D file
                        Viewpoint viewpoint = viewpoints.firstElement();
                        viewpoint.setIsBound(true);
                        cameraPosition = viewpoint.getPosition();
                    } // <Viewpoint> node existed
                    mainCameraRig.getTransform().setPosition(cameraPosition[0], cameraPosition[1], cameraPosition[2]);
                    GVRCursorController gazeController = null;
                    GVRInputManager inputManager = gvrContext.getInputManager();

                    // Set up cursor based on camera position
                    List<GVRCursorController> controllerList = inputManager.getCursorControllers();

                    for(GVRCursorController controller: controllerList){
                        if(controller.getControllerType() == GVRControllerType.GAZE);
                        {
                            gazeController = controller;
                            break;
                        }
                    }
                    if ( gazeController != null) {
                        gazeController.setOrigin(cameraPosition[0], cameraPosition[1], cameraPosition[2]);
                    }
                } // end setting based on new camera rig

                try {
                    animationInteractivityManager.initAnimationsAndInteractivity();
                    // Need to build a JavaScript function that constructs the
                    // X3D data type objects used with a SCRIPT.
                    // Scripts can also have an initialize() method.
                    animationInteractivityManager.InitializeScript();
                }
                catch (Exception exception) {
                    Log.e(TAG, "Error initialing X3D <ROUTE> or <Script> node related to Animation or Interactivity.");
                }

            } // end </scene>
            else if (qName.equalsIgnoreCase("x3d")) {
                ;
            } // end </x3d>
        }


        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            if (parseJavaScript) {
                // Each JavaScript must start with the importPackage line
                // to include the X3D Data types like SFColor
                String js = "";
                boolean leadingNonprintChars = true;
                for (int i = start; i < length; i++) {
                    if ((ch[i] == ' ') || (ch[i] == '\t')) {
                        if (!leadingNonprintChars && (ch[i] == ' ')) {
                            js += ch[i];
                        }
                    } else {
                        js += ch[i];
                        leadingNonprintChars = false;
                    }
                }
                javaScriptCode += js;
            }
        }  //  end characters method

    } // end UserHandler

    public void Parse(InputStream inputStream, ShaderSettings shaderSettings) {
        try {
            this.shaderSettings = shaderSettings;

            // Parse the initial X3D file
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            UserHandler userhandler = new UserHandler();
            saxParser.parse(inputStream, userhandler);

            // parse the Inline files
            if (inlineObjects.size() != 0) {
                for (int i = 0; i < inlineObjects.size(); i++) {
                    InlineObject inlineObject = inlineObjects.get(i);
                    String[] urls = inlineObject.getURL();
                    for (int j = 0; j < urls.length; j++) {
                        GVRAndroidResource gvrAndroidResource = null;
                        GVRResourceVolume gvrResourceVolume = null;
                        try {
                            String filename = urls[j];
                            inlineSubdirectory = "";
                            int lastIndex = urls[j].lastIndexOf('/');
                            if (lastIndex != -1) {
                                inlineSubdirectory = urls[j].substring(0, urls[j].lastIndexOf('/')+1);
                                filename = urls[j].substring(urls[j].lastIndexOf('/')+1, urls[j].length());
                            }

                            gvrResourceVolume = new GVRResourceVolume(gvrContext, urls[j]);
                            gvrAndroidResource = gvrResourceVolume.openResource( filename );

                            if ( filename.toLowerCase().endsWith(".x3d")) {
                                inputStream = gvrAndroidResource.getStream();
                                currentSceneObject = inlineObject.getInlineGVRSceneObject();
                                saxParser.parse(inputStream, userhandler);
                            }
                            else {
                                GVRExternalScene gvrExternalScene = new GVRExternalScene(gvrContext, urls[j], false);
                                currentSceneObject = inlineObject.getInlineGVRSceneObject();
                                if (currentSceneObject == null) root.attachComponent(gvrExternalScene);
                                else currentSceneObject.attachComponent(gvrExternalScene);
                                GVRScene gvrScene = gvrContext.getMainScene();
                                gvrExternalScene.load(gvrScene);
                                GVRAnimator gvrAnimator = gvrExternalScene.getAnimator();
                            }
/*
                            gvrAndroidResource = new GVRAndroidResource(gvrContext, urls[j]);
                            inputStream = gvrAndroidResource.getStream();

                            currentSceneObject = inlineObject.getInlineGVRSceneObject();
                            saxParser.parse(inputStream, userhandler);
                            */
                        } catch (FileNotFoundException e) {
                            Log.e(TAG,
                                    "Inline file reading: File Not Found: url " + urls[j] + ", Exception "
                                            + e);
                        } catch (IOException ioException) {
                            Log.e(TAG,
                                    "Inline file reading url " + urls[j]);
                            Log.e(TAG, "IOException: " + ioException.toString());
                        } catch (Exception exception) {
                            Log.e(TAG, "Inline file reading error: Exception "
                                    + exception);
                        }
                    }
                }
            }
        } catch (Exception exception) {
            Log.e(TAG, "X3D/XML Parsing Exception = " + exception);
        }

    } // end Parse
}
