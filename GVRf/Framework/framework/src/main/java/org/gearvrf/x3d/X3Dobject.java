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
import org.gearvrf.utility.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Future;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.gearvrf.script.GVRJavascriptScriptFile;
import org.gearvrf.script.javascript.GVRJavascriptV8File;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRCamera;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRLODGroup;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSpotLight;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.GVRTextureParameters.TextureWrapType;
import org.gearvrf.GVRTransform;

import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import org.joml.Vector3f;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;


public class X3Dobject {

    /**
     * Allows developers to access the root of X3D scene graph
     * by calling: GVRSceneObject.getSceneObjectByName(X3D_ROOT_NODE);
     */
    public static final String X3D_ROOT_NODE = "x3d_root_node_";

    private static final String TAG = "X3DObject";

    // Like a C++ pre-compiler switch to select shaders.
    // Default is true to use Universal lights shader.
    public final static boolean UNIVERSAL_LIGHTS = true;

    private final static String JAVASCRIPT_IMPORT_PACKAGE = "importPackage(org.gearvrf.x3d.data_types)\nimportPackage(org.joml)";

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

    private static final float CUBE_WIDTH = 20.0f; // used for cube maps, based on

    private GVRAssetLoader.AssetRequest assetRequest = null;
    private GVRContext gvrContext = null;
    private Context activityContext = null;

    private GVRSceneObject root = null;
    private GVRSceneObject mainCamera = null;
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
    private GVRMesh gvrMesh = null;
    private GVRMaterial gvrMaterial = null;
    private boolean gvrMaterialUSEd = false; // for DEFine and USE gvrMaterial for
    // x3d APPEARANCE and MATERIAL nodes
    private boolean gvrRenderingDataUSEd = false; // for DEFine and USE
    // gvrRenderingData for x3d
    // SHAPE node
    private boolean gvrGroupingNodeUSEd = false; // for DEFine and USE
    // gvrSceneObject for x3d
    // TRANSFORM and GROUP nodes


    private X3DTandLShader mX3DTandLShaderTest = null;

    private GVRTextureParameters gvrTextureParameters = null;
    private Future<GVRTexture> gvrTexture = null;

    private Vector<Vertex> vertices = new Vector<Vertex>(); // vertices
    private Vector<VertexNormal> vertexNormal = new Vector<VertexNormal>();
    private Vector<TextureValues> textureCoord = new Vector<TextureValues>();

    private Vector<Coordinates> indexedFaceSet = new Vector<Coordinates>();
    private Vector<Coordinates> indexedVertexNormals = new Vector<Coordinates>();
    private Vector<TextureCoordinates> indexedTextureCoord = new Vector<TextureCoordinates>();
    private ArrayList<Integer> texcoordIndices = new ArrayList<Integer>();
    private ArrayList<Integer> normalIndices = new ArrayList<Integer>();
    private ArrayList<ScriptObject> scriptObjects = new ArrayList<ScriptObject>();

    private Vector<Key> keys = new Vector<Key>();
    private Vector<KeyValue> keyValues = new Vector<KeyValue>();
    private Vector<Float> floatArray = new Vector<Float>();

    private Vector<TimeSensor> timeSensors = new Vector<TimeSensor>();
    private Vector<Interpolator> interpolators = new Vector<Interpolator>();

    private Vector<InlineObject> inlineObjects = new Vector<InlineObject>();


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

            // Camera rig setup code based on GVRScene::init()
            GVRCamera leftCamera = new GVRPerspectiveCamera(gvrContext);
            leftCamera.setRenderMask(GVRRenderMaskBit.Left);

            GVRCamera rightCamera = new GVRPerspectiveCamera(gvrContext);
            rightCamera.setRenderMask(GVRRenderMaskBit.Right);

            GVRPerspectiveCamera centerCamera = new GVRPerspectiveCamera(gvrContext);
            centerCamera
                    .setRenderMask(GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
            cameraRigAtRoot = GVRCameraRig.makeInstance(gvrContext);
            this.mainCamera = cameraRigAtRoot.getOwnerObject();
            this.mainCamera.setName("MainCamera");
            cameraRigAtRoot.attachLeftCamera(leftCamera);
            cameraRigAtRoot.attachRightCamera(rightCamera);
            cameraRigAtRoot.attachCenterCamera(centerCamera);
            cameraRigAtRoot.getLeftCamera().setBackgroundColor(Color.BLACK);
            cameraRigAtRoot.getRightCamera().setBackgroundColor(Color.BLACK);
            this.root.addChildObject(this.mainCamera);

            //this.mAnimations = root.getAnimations();
            lodManager = new LODmanager();

            animationInteractivityManager = new AnimationInteractivityManager(
                    this, gvrContext, root, mDefinedItems, interpolators,
                    sensors, timeSensors, eventUtilities, scriptObjects
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


    private void AddVertex(float[] values)

    {
        Vertex newVertex = new Vertex(values);
        vertices.add(newVertex);
    }


    private void AddVertexNormal(float[] vn)

    {
        VertexNormal newVertex = new VertexNormal(vn);
        vertexNormal.add(newVertex);
    }


    private void AddTextureCoord(float[] tc)

    {
        TextureValues newTextureCoord = new TextureValues(tc);
        textureCoord.add(newTextureCoord);
    }


    private void AddIndexedFaceSet(short[] coord) {
        Coordinates newCoordinates = new Coordinates(coord);
        indexedFaceSet.add(newCoordinates);
    }

    private void AddTextureCoordinateSet(short[] tc) {
        TextureCoordinates newCoordinates = new TextureCoordinates(tc);
        indexedTextureCoord.add(newCoordinates);
    }

    private TextureCoordinates GetTexturedCoordSet(int index)

    {
        return indexedTextureCoord.get(index);
    }


    private void AddIndexedVertexNormals(short[] normalIndex) {
        Coordinates newCoordinates = new Coordinates(normalIndex);
        indexedVertexNormals.add(newCoordinates);
    }

    private Coordinates GetIndexedVertexNormals(int index)

    {
        return indexedVertexNormals.get(index);
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

        private void parseNumbersString(String numberString, int componentType,
                                        int componentCount) {
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
                            if ((short) st.nval != -1) {
                                componentShort[index] = (short) st.nval;
                                index++;
                                if (index == componentCount) {
                                    AddIndexedFaceSet(componentShort);
                                    index = 0;
                                }
                            }
                        } else if (componentType == X3Dobject.textureIndexComponent) {
                            if ((short) st.nval != -1) {
                                componentShort[index] = (short) st.nval;
                                index++;
                                if (index == componentCount) {
                                    AddTextureCoordinateSet(componentShort);
                                    index = 0;
                                }
                            }
                        } else if (componentType == X3Dobject.normalIndexComponent) {
                            if ((short) st.nval != -1) {
                                componentShort[index] = (short) st.nval;
                                index++;
                                if (index == componentCount) {
                                    AddIndexedVertexNormals(componentShort);
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
                                AddVertex(componentFloat);
                                index = 0;
                            }
                        } else if (componentType == X3Dobject.textureCoordComponent) {
                            componentFloat[index] = (float) st.nval;
                            index++;
                            if (index == componentCount) {
                                AddTextureCoord(componentFloat);
                                index = 0;
                            }
                        } else if (componentType == X3Dobject.normalsComponent) {
                            componentFloat[index] = (float) st.nval;
                            index++;
                            if (index == componentCount) {
                                AddVertexNormal(componentFloat);
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
                    // Likely need a stack to include all the child object(s).
                    // if ( gvrSceneObjectDEFitem.getChildrenCount() > 1)
                    // gvrSceneObjectDEFitem = gvrSceneObjectDEFitem.getChildByIndex(1);
                    // else
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
                        scale = parseFixedLengthFloatString(attributeValue, 3, false, true);
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
                }

            } // end <Group> node


            /********** Shape **********/
            else if (qName.equalsIgnoreCase("shape")) {

                gvrRenderData = new GVRRenderData(gvrContext);
                // gvrRenderData.setCullFace(GVRCullFaceEnum.None);
                gvrRenderData.setCullFace(GVRCullFaceEnum.Back);
                shaderSettings.initializeTextureMaterial(new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.BeingGenerated.ID));
                gvrRenderData.setShaderTemplate(GVRPhongShader.class);

                // Check if this is part of a Level-of-Detail
                if (lodManager.isActive()) {
                    shapeLODSceneObject = AddGVRSceneObject();
                    final GVRSceneObject parent = shapeLODSceneObject.getParent();
                    if (null == parent.getComponent(GVRLODGroup.getComponentType())) {
                        parent.attachComponent(new GVRLODGroup(gvrContext));
                    }
                    final GVRLODGroup lodGroup = (GVRLODGroup)parent.getComponent(GVRLODGroup.getComponentType());
                    lodGroup.addRange(lodManager.getMinRange(), shapeLODSceneObject);
                    currentSceneObject = shapeLODSceneObject;
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
            else if (qName.equalsIgnoreCase("appearance")) {
        /* This gives the X3D-only Shader */
                if (!UNIVERSAL_LIGHTS)

                    mX3DTandLShaderTest = new X3DTandLShader(gvrContext);


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
                        Log.e(TAG, "ambientIntensity currently not implemented.");


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
                        urlAttribute = urlAttribute.toLowerCase();

                        // urlAttribute = urlAttribute.substring(0,
                        // urlAttribute.indexOf("."));

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
                        GVRAssetLoader.TextureRequest request = new GVRAssetLoader.TextureRequest(assetRequest,
                                filename, gvrTextureParameters);
                        Future<GVRTexture> texture = assetRequest
                                .loadFutureTexture(request);
                        shaderSettings.setTexture(texture);
                        if (defValue != null) {
                            DefinedItem item = new DefinedItem(defValue);
                            item.setGVRTexture(texture);
                            mDefinedItems.add(item);
                        }
                    }
                }
            } // end <ImageTexture> node


            /********** TextureTransform **********/
            else if (qName.equalsIgnoreCase("TextureTransform")) {
                attributeValue = attributes.getValue("DEF");
                if (attributeValue != null) {
                    Log.e(TAG,
                            "TextureTransform DEF attribute not currently implemented");
                }
                String centerAttribute = attributes.getValue("center");
                if (centerAttribute != null) {
                    float[] center = parseFixedLengthFloatString(centerAttribute, 2,
                            false, false);
                    shaderSettings.setTextureCenter(center);
                }
                String rotationAttribute = attributes.getValue("rotation");
                if (rotationAttribute != null) {
                    float[] rotation = parseFixedLengthFloatString(rotationAttribute, 1,
                            false, false);
                    shaderSettings.setTextureRotation(rotation[0]);
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
                    shaderSettings.setTextureTranslation(translation);
                }
            }

            /********** IndexedFaceSet **********/

            //TODO: eventually include IndexedLineSet **********/

            else if (qName.equalsIgnoreCase("IndexedFaceSet")) {
                attributeValue = attributes.getValue("USE");
                if (attributeValue != null) { // shared GVRMesh
                    DefinedItem useItem = null;
                    for (DefinedItem definedItem : mDefinedItems) {
                        if (attributeValue.equals(definedItem.getName())) {
                            useItem = definedItem;
                            break;
                        }
                    }
                    if (useItem != null) {
                        gvrMesh = useItem.getGVRMesh();
                    }
                } else {
                    gvrMesh = new GVRMesh(gvrContext);
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                        definedItem.setGVRMesh(gvrMesh);
                        mDefinedItems.add(definedItem); // Array list of DEFined items
                        // Clones objects with USE
                    }
                    attributeValue = attributes.getValue("solid");
                    if (attributeValue != null) {
                        Log.e(TAG, "IndexedFaceSet solid attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("ccw");
                    if (attributeValue != null) {
                        Log.e(TAG, "IndexedFaceSet ccw attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("colorPerVertex");
                    if (attributeValue != null) {

                        Log.e(TAG,
                                "IndexedFaceSet colorPerVertex attribute not implemented. ");

                    }
                    attributeValue = attributes.getValue("normalPerVertex");
                    if (attributeValue != null) {

                        Log.e(TAG,
                                "IndexedFaceSet normalPerVertex attribute not implemented. ");

                    }
                    String coordIndexAttribute = attributes.getValue("coordIndex");
                    if (coordIndexAttribute != null) {
                        parseNumbersString(coordIndexAttribute,
                                X3Dobject.indexedFaceSetComponent, 3);

                        char[] ifs = new char[indexedFaceSet.size() * 3];

                        for (int i = 0; i < indexedFaceSet.size(); i++) {

                            Coordinates coordinate = indexedFaceSet.get(i);
                            for (int j = 0; j < 3; j++) {
                                ifs[i * 3 + j] = (char) coordinate.getCoordinate(j);

                            }
                        }
                        gvrMesh.setIndices(ifs);
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
                        gvrMesh.setVertices(useItem.getGVRMesh().getVertices());
                        reorganizeVerts = false;
                    }
                } // end USE Coordinate
                else {
                    // Not a 'Coordinate USE="..." node
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                        definedItem.setGVRMesh(gvrMesh);
                        // Array list of DEFined items clones objects with USE
                        mDefinedItems.add(definedItem);
                    }
                    String pointAttribute = attributes.getValue("point");
                    if (pointAttribute != null) {
                        parseNumbersString(pointAttribute, X3Dobject.verticesComponent, 3);

                        float[] vertexList = new float[vertices.size() * 3];
                        for (int i = 0; i < vertices.size(); i++) {
                            Vertex vertex = vertices.get(i);
                            for (int j = 0; j < 3; j++) {

                                vertexList[i * 3 + j] = vertex.getVertexCoord(j);

                            }
                        }
                        gvrMesh.setVertices(vertexList);
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

                        // 'useItem' points to GVRMesh who's useItem.getGVRMesh
                        // TextureCoordinates were DEFined earlier.
                        // We don't want to share the entire GVRMesh since the
                        // the 2 meshes may have different Normals and Coordinates
                        // So as an alternative, copy the texture coordinates.
                        gvrMesh.setTexCoords(useItem.getGVRMesh().getTexCoords());
                        reorganizeVerts = false;
                    }
                } // end USE TextureCoordinate
                else {
                    // Not a 'TextureCoordinate USE="..." node
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        // This is a 'TextureCoordinate DEF="..." case, so save the item
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                        definedItem.setGVRMesh(gvrMesh);
                        // Array list of DEFined items clones objects with USE
                        mDefinedItems.add(definedItem);
                    }
                    // Have to flip the y texture coordinates because the image will be
                    // upside down
                    String pointAttribute = attributes.getValue("point");
                    if (pointAttribute != null) {
                        parseNumbersString(pointAttribute, X3Dobject.textureCoordComponent, 2);
                        // initialize the list
                        // Reorganize the order of the texture coordinates if there
                        // isn't a 1-to-1 match of coordinates, and texture coordinates.

                        // check if an indexedTextureCoordinat list is present
                        texcoordIndices.clear();
                        if (indexedTextureCoord.size() != 0) {
                            // current indexedFaceSet has a textureCoordIndex.
                            texcoordIndices.ensureCapacity(indexedTextureCoord.size() * 3);
                            for (int i = 0; i < indexedTextureCoord.size(); i++) {
                                TextureCoordinates tcIndex = GetTexturedCoordSet(i);

                                for (int j = 0; j < 3; j++) {
                                    texcoordIndices.add((int) tcIndex.coords[j]);
                                }
                            }
                        } else {
                            // use the coordIndex if there is no indexedTextureCoord.
                            texcoordIndices.ensureCapacity(indexedFaceSet.size() * 3);
                            for (int i = 0; i < indexedFaceSet.size(); i++) {
                                Coordinates coordinate = indexedFaceSet.get(i);
                                for (int j = 0; j < 3; j++) {
                                    texcoordIndices.add((int) coordinate.getCoordinate(j));
                                }
                            }
                        }
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

                        // 'useItem' points to GVRMesh who's useItem.getGVRMesh Coordinates
                        // were
                        // DEFined earlier. We don't want to share the entire GVRMesh since
                        // the
                        // 2meshes may have different Normals and Texture Coordinates
                        // So as an alternative, copy the normals.
                        gvrMesh.setNormals(useItem.getGVRMesh().getNormals());
                        reorganizeVerts = false;
                    }
                } // end USE Coordinate
                else {
                    // Not a 'Normal USE="..." node
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        // This is a 'Normal DEF="..." case, so save the item
                        DefinedItem definedItem = new DefinedItem(attributeValue);
                        definedItem.setGVRMesh(gvrMesh);
                        // Array list of DEFined items clones objects with USE
                        mDefinedItems.add(definedItem);
                    }
                    String vectorAttribute = attributes.getValue("vector");
                    if (vectorAttribute != null) {
                        parseNumbersString(vectorAttribute, X3Dobject.normalsComponent, 3);

                        // initialize the list
                        char[] ifs = gvrMesh.getIndices();

                        float[] normalVectorList = new float[ifs.length * 3];


                        // check if an indexedVertexNormals list is present
                        normalIndices.clear();
                        if (indexedVertexNormals.size() != 0) {
                            // current indexedFaceSet has a normalIndex.
                            // We may need to reorganize the order of the texture coordinates

                            normalIndices.ensureCapacity(indexedVertexNormals.size() * 3);
                            for (int i = 0; i < indexedVertexNormals.size(); i++) {
                                Coordinates vnIndex = GetIndexedVertexNormals(i);
                                for (int j = 0; j < 3; j++) {
                                    normalIndices.add((int) vnIndex.getCoordinate(j));
                                }
                            }
                        } else {
                            // use the coordIndex if there is no normalIndex.
                            normalIndices.ensureCapacity(indexedFaceSet.size() * 3);
                            for (int i = 0; i < indexedFaceSet.size(); i++) {
                                Coordinates coordinate = indexedFaceSet.get(i);
                                for (int j = 0; j < 3; j++) {
                                    normalIndices.add((int) coordinate.getCoordinate(j));
                                }
                            }
                        }
                    }
                } // end NOT a USE Normals condition
            } // end <Normal> node


            /********** LIGHTS **********/
            /********** PointLight **********/
            else if (qName.equalsIgnoreCase("PointLight")) {
                if (UNIVERSAL_LIGHTS) {
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
                if (UNIVERSAL_LIGHTS) {
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
                if (UNIVERSAL_LIGHTS) {
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
                    boolean solid = true; // cone visible from inside

                    attributeValue = attributes.getValue("size");
                    if (attributeValue != null) {
                        size = parseFixedLengthFloatString(attributeValue, 3, false, true);
                    }
                    attributeValue = attributes.getValue("solid");
                    if (attributeValue != null) {
                        solid = parseBooleanString(attributeValue);
                        Log.e(TAG, "Box solid not currently implemented. ");
                    }
                    GVRCubeSceneObject gvrCubeSceneObject = new GVRCubeSceneObject(
                            gvrContext);
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
                        Log.e(TAG, "Cone solid not currently implemented. ");
                    }
                    GVRCylinderSceneObject.CylinderParams params = new GVRCylinderSceneObject.CylinderParams();
                    params.BottomRadius = bottomRadius;
                    params.TopRadius = 0;
                    params.Height = height;
                    params.FacingOut = true;
                    params.HasTopCap = false;
                    params.HasBottomCap = bottom;
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
                        Log.e(TAG, "Cylinder solid not currently implemented. ");
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
                    params.FacingOut = true;
                    GVRCylinderSceneObject gvrCylinderSceneObject = new GVRCylinderSceneObject(
                            gvrContext, params);
                    currentSceneObject.addChildObject(gvrCylinderSceneObject);
                    meshAttachedSceneObject = gvrCylinderSceneObject;

                } // end <Cylinder> node


                /********** Sphere **********/
                else if (qName.equalsIgnoreCase("Sphere")) {
                    float radius = 1;
                    boolean solid = true; // cylinder visible from inside
                    attributeValue = attributes.getValue("radius");
                    if (attributeValue != null) {
                        radius = parseSingleFloatString(attributeValue, false, true);
                    }
                    attributeValue = attributes.getValue("solid");
                    if (attributeValue != null) {
                        solid = parseBooleanString(attributeValue);
                        Log.e(TAG, "Sphere solid not currently implemented. ");
                    }
                    GVRSphereSceneObject gvrSphereSceneObject = new GVRSphereSceneObject(
                            gvrContext);
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
                    String name = "";
                    String[] string = {};
                    String[] mfStrings = null;
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("string");
                    if (attributeValue != null) {
                        mfStrings = parseMFString(attributeValue);

                    }
                    gvrTextViewSceneObject = new GVRTextViewSceneObject(gvrContext);
                    String text = "";
                    if (mfStrings != null) {
                        for (int i = 0; i < mfStrings.length; i++) {
                            if (i > 0)
                                text += " ";
                            text += mfStrings[i];
                        }
                    }
                    gvrTextViewSceneObject.setText(text);

                    Matrix4f matrix4f = currentSceneObject.getTransform()
                            .getModelMatrix4f();

                    gvrTextViewSceneObject.setTextColor(Color.WHITE); // default
                    gvrTextViewSceneObject.setBackgroundColor(Color.TRANSPARENT); // default

                    currentSceneObject.addChildObject(gvrTextViewSceneObject);
                    // Mark that this object does not require a gvrRenderingData
                    // nor gvrMesh attached.
                    // meshAttachedSceneObject = gvrTextViewSceneObject;

                } // end <Text> node


                /********** FontStyle **********/
                //TODO: FontStyle not currently implemented
                else if (qName.equalsIgnoreCase("FontStyle")) {
                    String name = "";
                    String[] family =
                            {
                                    "SERIF"
                            };
                    boolean horizontal = true;
                    String[] justify =
                            {
                                    "BEGIN"
                            }; // BEGIN, END, FIRST, MIDDLE
                    String language = "";
                    boolean leftToRight = true;
                    float size = 1;
                    float spacing = 1;
                    String[] style =
                            {
                                    "PLAIN"
                            }; // PLAIN | BOLD | ITALIC | BOLDITALIC
                    boolean topToBottom = true;

                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("family");
                    if (attributeValue != null) {
                        family = parseMFString(attributeValue);
                        Log.e(TAG, "FontStyle family attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("justify");
                    if (attributeValue != null) {
                        justify = parseMFString(attributeValue);
                        Log.e(TAG, "FontStyle justify attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("language");
                    if (attributeValue != null) {
                        language = attributeValue;
                        Log.e(TAG, "FontStyle language attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("leftToRight");
                    if (attributeValue != null) {
                        leftToRight = parseBooleanString(attributeValue);
                        Log.e(TAG, "FontStyle leftToRight attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("size");
                    if (attributeValue != null) {
                        size = parseSingleFloatString(attributeValue, false, true);
                    }
                    attributeValue = attributes.getValue("spacing");
                    if (attributeValue != null) {
                        spacing = parseSingleFloatString(attributeValue, false, true);
                        Log.e(TAG, "FontStyle spacing attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("style");
                    if (attributeValue != null) {
                        style = parseMFString(attributeValue);
                        Log.e(TAG, "FontStyle style attribute not implemented. ");
                    }
                    attributeValue = attributes.getValue("topToBottom");
                    if (attributeValue != null) {
                        topToBottom = parseBooleanString(attributeValue);
                        Log.e(TAG, "FontStyle topToBottom attribute not implemented. ");
                    }
                    // not clear how gravity and textSize will be used.
                    // currently, just using a default value

                    gvrTextViewSceneObject.setTextSize(size * 10);
                } // end <FontStyle> node


                /********** Billboard **********/
                else if (qName.equalsIgnoreCase("Billboard")) {
                    Log.e(TAG, "Billboard currently not implemented. ");
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
                    String[] url = {};
                    attributeValue = attributes.getValue("DEF");
                    if (attributeValue != null) {
                        name = attributeValue;
                    }
                    attributeValue = attributes.getValue("url");
                    if (attributeValue != null) {
                        url = parseMFString(attributeValue);
                        GVRSceneObject inlineGVRSceneObject = currentSceneObject; // preserve
                        // the
                        // currentSceneObject
                        if (lodManager.isActive()) {
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

                } // end <LOD> Level-of-Detail node


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
                    currentSensor = sensor;
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

                    GVRSceneObject gvrSensorSceneObject = new GVRSceneObject(gvrContext);
                    gvrSensorSceneObject.setName(name);
                    Sensor sensor = new Sensor(name, Sensor.Type.TOUCH,
                            gvrSensorSceneObject);
                    sensors.add(sensor);
                    currentSensor = sensor;
                    // attach any existing child objects of the parent to the new
                    // gvrSensorSceneObject
                    for (int i = (currentSceneObject.getChildrenCount() - 1); i >= 0; i--) {
                        // detach the children of the parent and re-attach them to the new
                        // sensor object
                        GVRSceneObject childObject = currentSceneObject.getChildByIndex(i);

                        currentSceneObject.removeChildObject(childObject);
                        gvrSensorSceneObject.addChildObject(childObject);
                    }
                    currentSceneObject.addChildObject(gvrSensorSceneObject);
                    currentSceneObject = gvrSensorSceneObject;
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

                        ArrayList<Future<GVRTexture>> futureTextureList = new ArrayList<Future<GVRTexture>>(6);
                        GVRAssetLoader loader = gvrContext.getAssetLoader();
                        String urlAttribute = backUrl[0].substring(0,
                                backUrl[0].indexOf("."));
                        int assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            futureTextureList
                                    .add(loader.loadFutureTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = rightUrl[0].substring(0, rightUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            futureTextureList
                                    .add(loader.loadFutureTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = frontUrl[0].substring(0, frontUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            futureTextureList
                                    .add(loader.loadFutureTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = leftUrl[0].substring(0, leftUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            futureTextureList
                                    .add(loader.loadFutureTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = topUrl[0].substring(0, topUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            futureTextureList
                                    .add(loader.loadFutureTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        urlAttribute = bottomUrl[0].substring(0, bottomUrl[0].indexOf("."));
                        assetID = activityContext.getResources()
                                .getIdentifier(urlAttribute, "drawable",
                                        activityContext.getPackageName());
                        if (assetID != 0) {
                            futureTextureList
                                    .add(loader.loadFutureTexture(new GVRAndroidResource(
                                            gvrContext, assetID)));
                        }

                        GVRCubeSceneObject mCubeEvironment = new GVRCubeSceneObject(
                                gvrContext, false, futureTextureList);
                        mCubeEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                                CUBE_WIDTH);

                        root.addChildObject(mCubeEvironment);
                    } else {
                        // Not cubemapping, then set default skyColor
                        cameraRigAtRoot.getLeftCamera()
                                .setBackgroundColor(skycolor[0], skycolor[1], skycolor[2], 1);
                        cameraRigAtRoot.getRightCamera()
                                .setBackgroundColor(skycolor[0], skycolor[1], skycolor[2], 1);
                    }

                } // end <Background> node

                // These nodes are once per file commands and thus moved to the end of the
                //  end of the parsing's if-then-else statement

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
                    if (currentSensor != null) {
                        // A GVRScene object was added between the parent and it's children.
                        // If the currentSensor points to the currentSceneObject, go up to
                        //   it's parent thus skipping past the sensor's sensor's GVRSceneObject
                        if (currentSensor.getGVRSceneObject() == currentSceneObject) {
                            currentSensor = null;
                            currentSceneObject = currentSceneObject.getParent();
                        }
                    }

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
                if (currentSensor != null) {
                    // A GVRScene object was added between the parent and it's children.
                    // If the currentSensor points to the currentSceneObject, go up to
                    //   it's parent thus skipping past the sensor's sensor's GVRSceneObject
                    if (currentSensor.getGVRSceneObject() == currentSceneObject) {
                        currentSensor = null;
                        currentSceneObject = currentSceneObject.getParent();
                    }
                }

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

                    if (!UNIVERSAL_LIGHTS) {
                        gvrMaterial = shaderSettings.material;
                        gvrMaterial.setVec2(mX3DTandLShaderTest.TEXTURECENTER_KEY,
                                shaderSettings.textureCenter[0],
                                shaderSettings.textureCenter[1]);
                        gvrMaterial.setVec2(mX3DTandLShaderTest.TEXTURESCALE_KEY,
                                shaderSettings.textureScale[0],
                                shaderSettings.textureScale[1]);
                        gvrMaterial.setFloat(mX3DTandLShaderTest.TEXTUREROTATION_KEY,
                                shaderSettings.textureRotation);
                        gvrMaterial.setVec2(mX3DTandLShaderTest.TEXTURETRANSLATION_KEY,
                                shaderSettings.textureTranslation[0],
                                shaderSettings.textureTranslation[1]);

                        gvrMaterial.setVec3(mX3DTandLShaderTest.DIFFUSECOLOR_KEY,
                                shaderSettings.diffuseColor[0],
                                shaderSettings.diffuseColor[1],
                                shaderSettings.diffuseColor[2]);
                        gvrMaterial.setVec3(mX3DTandLShaderTest.EMISSIVECOLOR_KEY,
                                shaderSettings.emissiveColor[0],
                                shaderSettings.emissiveColor[1],
                                shaderSettings.emissiveColor[2]);
                        gvrMaterial.setVec3(mX3DTandLShaderTest.SPECULARCOLOR_KEY,
                                shaderSettings.specularColor[0],
                                shaderSettings.specularColor[1],
                                shaderSettings.specularColor[2]);
                        gvrMaterial.setFloat(mX3DTandLShaderTest.SHININESS_KEY,
                                shaderSettings.shininess);

                        gvrMaterial.setTexture(mX3DTandLShaderTest.TEXTURE_KEY, gvrTexture);


                        float[] modelMatrix = currentSceneObject.getTransform()
                                .getModelMatrix();
                        gvrMaterial
                                .setMat4(mX3DTandLShaderTest.MODELMATRIX_KEY, modelMatrix[0],
                                        modelMatrix[1], modelMatrix[2], 0, modelMatrix[4],
                                        modelMatrix[5], modelMatrix[6], 0, modelMatrix[8],
                                        modelMatrix[9], modelMatrix[10], 0, modelMatrix[12],
                                        modelMatrix[13], modelMatrix[14], 1);


                        gvrRenderData.setMaterial(gvrMaterial);
                        gvrRenderData.setShaderTemplate(GVRPhongShader.class);
                    } // end !UNIVERSAL_LIGHTS
                    else {
                        // UNIVERSAL_LIGHTS

                        if (!gvrMaterialUSEd) { // if GVRMaterial is NOT set by a USE statement.

                            if (meshAttachedSceneObject == null) {
                                gvrMaterial = shaderSettings.material;
                                gvrRenderData.setMaterial(gvrMaterial);
                                gvrRenderData.setShaderTemplate(GVRPhongShader.class);
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
                                // reset the DEF item to now point to
                                if (definedGRRenderingData != null)
                                    definedGRRenderingData.setGVRRenderData(gvrRenderData);
                                gvrRenderData.setShaderTemplate(GVRPhongShader.class); // set
                                // the
                                // shader


                                gvrMaterial = gvrRenderData.getMaterial();
                            }
                            // X3D doesn't have an ambient color so need to do color
                            // calibration tests on how to set this.
                            // gvrMaterial.setVec4("ambient_color", 1.0f, 1.0f, 1.0f, 1.0f);
                            gvrMaterial.setVec4("diffuse_color",
                                    shaderSettings.diffuseColor[0],
                                    shaderSettings.diffuseColor[1],
                                    shaderSettings.diffuseColor[2], 1.0f);
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
                            }

                            // Appearance node thus far contains properties of GVRMaterial
                            // node
                            if (!shaderSettings.getAppearanceName().isEmpty()) {
                                DefinedItem definedItem = new DefinedItem(
                                        shaderSettings.getAppearanceName());
                                definedItem.setGVRMaterial(gvrMaterial);
                                mDefinedItems.add(definedItem); // Add gvrMaterial to Array list
                                // of DEFined items Clones
                                // objects with USE
                            }

                            float transparency = shaderSettings.getTransparency();
                            gvrMaterial.setOpacity(transparency);
                            if ((transparency != 0) && (transparency != 1)) {
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

                if (shapeLODSceneObject != null) {
                    // if this GVRSceneObject is part of a Level-of-Detail
                    // then restore bck to the parent object
                    currentSceneObject = currentSceneObject.getParent();
                    shapeLODSceneObject = null;
                    lodManager.increment();
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
                if (reorganizeVerts) {
                    organizeVertices(gvrMesh);
                    reorganizeVerts = false;
                }
                gvrRenderData.setMesh(gvrMesh);
                gvrMesh = null;
                indexedFaceSet.clear(); // clean up this Vector<coordinates> list.
                indexedVertexNormals.clear(); // clean up this Vector<coordinates> list
                indexedTextureCoord.clear(); // clean up this Vector<textureCoordinates>
                texcoordIndices.clear();
                normalIndices.clear();
                vertices.clear();
                vertexNormal.clear();
                textureCoord.clear();
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
                currentSensor = null;
            } else if (qName.equalsIgnoreCase("Text")) {
                ;
            } else if (qName.equalsIgnoreCase("FontStyle")) {
                ;
            } else if (qName.equalsIgnoreCase("Billboard")) {
                ;
            } else if (qName.equalsIgnoreCase("Anchor")) {
                if (currentSceneObject.getParent() == root)
                    currentSceneObject = null;
                else
                    currentSceneObject = currentSceneObject.getParent();
                currentSensor = null;
            } else if (qName.equalsIgnoreCase("Inline")) {
                ;
            } else if (qName.equalsIgnoreCase("LOD")) {
                ;
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

                // Set up the camera / Viewpoint
                // The camera rig is indirectly attached to the root

                if (cameraRigAtRoot != null) {
                    GVRTransform cameraTransform = cameraRigAtRoot.getTransform();

                    if (viewpoints.isEmpty()) {

                        // No <Viewpoint> node(s) included in X3D file,

                        // so use default viewpoint values
                        cameraTransform.setPosition(0, 0, 10);
                        AxisAngle4f axisAngle4f = new AxisAngle4f(0, 0, 1, 0);
                        Quaternionf quaternionf = new Quaternionf(axisAngle4f);
                        cameraTransform.setRotation(quaternionf.w, quaternionf.x,
                                quaternionf.y, quaternionf.z);
                        mainCamera.setName("DefaultCamera");
                    } else {
                        // X3D file contained a <Viewpoint> node.
                        // Per spec., grab the first viewpoint from the X3D file
                        Viewpoint viewpoint = viewpoints.firstElement();
                        viewpoint.setIsBound(true);
                        float[] position = viewpoint.getPosition();
                        cameraTransform.setPosition(position[0], position[1], position[2]);
                        float[] orientation = viewpoint.getOrientation();
                        AxisAngle4f axisAngle4f = new AxisAngle4f(orientation[3],
                                orientation[0], orientation[1], orientation[2]);
                        Quaternionf quaternionf = new Quaternionf(axisAngle4f);
                        float[] centerOfRotation = viewpoint.getCenterOfRotation();
                        cameraTransform
                                .rotateWithPivot(quaternionf.w, quaternionf.x, quaternionf.y,
                                        quaternionf.z, centerOfRotation[0],
                                        centerOfRotation[1], centerOfRotation[2]);
                        if (viewpoint.getParent() != null) {
                            Matrix4f cameraMatrix4f = cameraTransform.getLocalModelMatrix4f();
                            Matrix4f parentMatrix4x4f = viewpoint.getParent().getTransform()
                                    .getModelMatrix4f();
                            parentMatrix4x4f.mul(cameraMatrix4f);
                            cameraTransform.setModelMatrix(parentMatrix4x4f);
                        }
                    } // <Viewpoint> node existed
                } // end setting based on new camera rig

                animationInteractivityManager.initAnimationsAndInteractivity();
                // Need to build a JavaScript function that constructs the
                // X3D data type objects used with a SCRIPT.
                // Scripts can also have an initialize() method.
                animationInteractivityManager.InitializeScript();

            } // end </scene>
            else if (qName.equalsIgnoreCase("x3d")) {
                ;
            } // end </x3d>
        }


        // if the mesh contains no normals, then they must be generated.
        // First generate the polygon normal from the cross product of any
        // 2 lines of the polygon.  Second, for each vertex, sum the polygon
        // normals shared by this vertex
        private void generateNormals() {
            try {
                Vector3f[] polygonNormal = new Vector3f[indexedFaceSet.size()];
                for (int i = 0; i < polygonNormal.length; i++) {
                    polygonNormal[i] = new Vector3f();
                }
                Coordinates[] polygonVertices = new Coordinates[3];
                short[] polygonIFS = new short[3];
                try {
                    Vertex[] polygonVertex = new Vertex[3];
                    for (int f = 0; f < indexedFaceSet.size(); f++) {
                        Coordinates coordinate = indexedFaceSet.get(f);
                        polygonIFS = coordinate.getCoordinates();
                        for (int i = 0; i < 3; i++) {
                            polygonVertex[i] = vertices.get(polygonIFS[i]);
                        }
                        // get the sides of 2 lines of this polygons
                        Vector3f side0 = new Vector3f();
                        Vector3f side1 = new Vector3f();
                        ;
                        for (int i = 0; i < 3; i++) {
                            side0.setComponent(i, (polygonVertex[0].getVertexCoord(i) - polygonVertex[1].getVertexCoord(i)));
                            side1.setComponent(i, (polygonVertex[1].getVertexCoord(i) - polygonVertex[2].getVertexCoord(i)));
                        }
                        side0.cross(side1, polygonNormal[f]);
                        polygonNormal[f].normalize();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                // Calculate the vertex normals by summing & normalizing all the
                // polygon normals who share this vertex.
                Vector3f[] vertexNormals = new Vector3f[vertices.size()];
                try {
                    for (int i = 0; i < vertexNormals.length; i++) {
                        vertexNormals[i] = new Vector3f();
                        try {
                            for (char f = 0; f < indexedFaceSet.size(); f++) {
                                Coordinates coordinate = indexedFaceSet.get(f);
                                polygonIFS = coordinate.getCoordinates();
                                for (int j = 0; j < 3; j++) {
                                    if (i == polygonIFS[j]) {
                                        // this polygon touches the currentvertex
                                        //   we are finding the normal
                                        vertexNormals[i].add(polygonNormal[f]);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                        vertexNormals[i].normalize();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                // Calculate the vertex normals by summing & normalizing all the
                // Add the vertex normals to the existing 'vertexNormal' array list
                //   and add the normal indices to the IndexedFaceSet
                for (Vector3f vns : vertexNormals) {
                    float[] vn = new float[]{vns.x, vns.y, vns.z};
                    AddVertexNormal(vn);
                }
                for (char f = 0; f < indexedFaceSet.size(); f++) {
                    Coordinates coordinate = indexedFaceSet.get(f);
                    polygonIFS = coordinate.getCoordinates();
                    for (int i = 0; i < 3; i++) {
                        normalIndices.add((int) polygonIFS[i]);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }  //  end generateNormals

        private void organizeVertices(GVRMesh mesh) {
            boolean hasNormals = normalIndices.size() > 0;
            boolean hasTexcoords = texcoordIndices.size() > 0;
            Map<String, Integer> vertexMap = new LinkedHashMap<String, Integer>();
            char[] newIndices = new char[indexedFaceSet.size() * 3];
            List<Float> gvrVerts = new ArrayList<Float>();
            List<Float> gvrNormals = new ArrayList<Float>();
            List<Float> gvrTexcoords = new ArrayList<Float>();
            float minYtextureCoordinate = Float.MAX_VALUE;
            float maxYtextureCoordinate = Float.MIN_VALUE;

            if (!hasNormals) {
                generateNormals();
                hasNormals = true;
            }
            //
            // Scan all the faces and compose the set of unique vertices
            //
            for (char f = 0; f < indexedFaceSet.size(); f++) {
                Coordinates coordinate = indexedFaceSet.get(f);
                float x;
                float y;
                float z;
                float nx = 0;
                float ny = 0;
                float nz = 0;
                float u = 0;
                float v = 0;

                for (char j = 0; j < 3; j++) {
                    int findex = f * 3 + j;
                    int vindex = coordinate.getCoordinate(j);
                    Vertex srcVert = vertices.get(vindex);
                    String key = "";

                    x = srcVert.getVertexCoord(0);
                    y = srcVert.getVertexCoord(1);
                    z = srcVert.getVertexCoord(2);
                    key += String.valueOf(x) + String.valueOf(y) + String.valueOf(z);
                    if (hasNormals) {
                        int nindex = normalIndices.get(findex);
                        VertexNormal nml = vertexNormal.get(nindex);
                        nx = nml.getVertexNormalCoord(0);
                        ny = nml.getVertexNormalCoord(1);
                        nz = nml.getVertexNormalCoord(2);
                        key += String.valueOf(nx) + String.valueOf(ny) + String.valueOf(nz);
                    }
                    if (hasTexcoords) {
                        int tindex = texcoordIndices.get(findex);
                        TextureValues tv = textureCoord.get(tindex);
                        u = tv.coord[0];
                        v = tv.coord[1];
                        key += String.valueOf(u) + String.valueOf(v);
                    }
                    Integer newindex = vertexMap.get(key);
                    if (newindex == null) {
                        newindex = vertexMap.size();
                        vertexMap.put(key, newindex);
                        gvrVerts.add(x);
                        gvrVerts.add(y);
                        gvrVerts.add(z);
                        if (hasNormals) {
                            gvrNormals.add(nx);
                            gvrNormals.add(ny);
                            gvrNormals.add(nz);
                        }
                        if (hasTexcoords) {
                            gvrTexcoords.add(u);
                            gvrTexcoords.add(v);
                            if (v < minYtextureCoordinate) {
                                minYtextureCoordinate = v;
                            }
                            if (u > maxYtextureCoordinate) {
                                maxYtextureCoordinate = u;
                            }
                        }
                    }
                    newIndices[findex] = (char) (int) newindex;
                }
            }
            //
            // Copy the new vertex data into the GVRMesh
            //
            int nverts = gvrVerts.size() / 3;
            float[] newVertices = new float[nverts * 3];
            float[] newNormals = hasNormals ? new float[nverts * 3] : null;
            float[] newTexcoords = hasTexcoords ? new float[nverts * 2] : null;
            int maxMinDiff = (int) Math.round((float) Math
                    .ceil(maxYtextureCoordinate - minYtextureCoordinate));

            for (int i = 0; i < nverts; ++i) {
                int t = 3 * i;
                newVertices[t] = gvrVerts.get(t++);
                newVertices[t] = gvrVerts.get(t++);
                newVertices[t] = gvrVerts.get(t++);
                if (hasNormals) {
                    t = 3 * i;
                    newNormals[t] = gvrNormals.get(t++);
                    newNormals[t] = gvrNormals.get(t++);
                    newNormals[t] = gvrNormals.get(t++);
                }
                if (hasTexcoords) // flip the Y texture coordinate
                {
                    t = 2 * i;
                    newTexcoords[t] = gvrTexcoords.get(t++);
                    newTexcoords[t] = maxMinDiff - gvrTexcoords.get(t);
                }
            }
            mesh.setVertices(newVertices);
            if (newNormals != null) {
                mesh.setNormals(newNormals);
            }
            if (newTexcoords != null) {
                mesh.setTexCoords(newTexcoords);
            }
            mesh.setIndices(newIndices);
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
                        try {
                            gvrAndroidResource = new GVRAndroidResource(gvrContext, urls[j]);
                            inputStream = gvrAndroidResource.getStream();
                            currentSceneObject = inlineObject.getInlineGVRSceneObject();
                            saxParser.parse(inputStream, userhandler);
                        } catch (FileNotFoundException e) {
                            Log.e(TAG,
                                    "Inline file reading: GVRAndroidResource File Not Found Exception: "
                                            + e);
                        } catch (IOException ioException) {
                            Log.e(TAG,
                                    "Inline file reading: GVRAndroidResource IOException url["
                                            + j + "] url " + urls[j]);
                            Log.e(TAG, "Inline file reading: " + ioException.toString());
                        } catch (Exception exception) {
                            Log.e(TAG, "Inline file reading: GVRAndroidResource Exception: "
                                    + exception);
                        }
                    }
                }
            }
        } catch (Exception exception) {
            Log.e(TAG, "Parse call: Exception = " + exception);
            exception.printStackTrace();
        }

    } // end Parse

}
