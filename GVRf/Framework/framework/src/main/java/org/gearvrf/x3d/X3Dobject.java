package org.gearvrf.x3d;

/*
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.lang.ArrayIndexOutOfBoundsException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
*/
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.FloatBuffer;
//import java.nio.IntBuffer;
//import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCamera;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCubemapTexture;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.GVRSpotLight;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.GVRTransform;
//import org.gearvrf.NativeLight;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.GVRTransform;
import org.gearvrf.PrettyPrint;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.keyframe.GVRAnimationBehavior;
import org.gearvrf.animation.keyframe.GVRAnimationChannel;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;
import org.gearvrf.animation.keyframe.GVRPositionKey;
import org.gearvrf.animation.keyframe.GVRRotationKey;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;
import org.gearvrf.GVRTextureParameters.TextureWrapType;
import org.gearvrf.x3d.x3dTandLShaderTest;
import org.gearvrf.scene_objects.GVRConeSceneObject;
//import org.gearvrf.sample.R;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
//import org.gearvrf.sample.R;
//import com.example.gen.R;
//import com.utilities.Box;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class X3Dobject {

	public final static boolean UNIVERSAL_LIGHTS = true;
	public static final String X3D_ROOT_NODE = "x3d_root_node_";
	
	private static final String TAG = "X3DObject";

	// Strings appended to GVRScene names
	private static final String KEY_FRAME_ANIMATION = "KeyFrameAnimation_";

	private static final String TRANSFORM_CENTER_ = "_Transform_Center_";
	private static final String TRANSFORM_NEGATIVE_CENTER_ = "_Transform_Neg_Center_";
	private static final String TRANSFORM_ROTATION_ = "_Transform_Rotation_";
	private static final String TRANSFORM_TRANSLATION_ = "_Transform_Translation_";
	private static final String TRANSFORM_SCALE_ = "_Transform_Scale_";
	private static final String TRANSFORM_SCALE_ORIENTATION_ = "_Transform_Scale_Orientation_";
	private static final String TRANSFORM_NEGATIVE_SCALE_ORIENTATION_ = "_Transform_Neg_Scale_Orientation_";


	// Append this incremented value to GVRScene names to insure unique GVRSceneObjects
	// when new GVRScene objects are generated to support animation
	private static int animationCount = 1;

	public final static int verticesComponent = 1;
	public final static int normalsComponent = 2;
	public final static int textureCoordComponent = 3;
	public final static int indexedFaceSetComponent = 4;
	public final static int normalIndexComponent = 5;
	public final static int textureIndexComponent = 6;
	public final static int interpolatorKeyComponent = 7;
	public final static int interpolatorKeyValueComponent = 8;
	public final static int LODComponent = 9;
	public final static int elevationGridHeight = 10;
	
	private final static float framesPerSecond = 60.0f;
    private static final float CUBE_WIDTH = 20.0f; // used for cube maps, based on gvrcubemap [GearVRf-Demos master]

	
	private GVRContext gvrContext = null;
	private Context activityContext = null;
	//private AssetManager assetManager = null;
	private GVRSceneObject root = null;
	
	private List<GVRAnimation> mAnimations;

	// When Translation object has multiple properties (center, scale, rotation plus translation)
	//  the mesh must be attached to the bottom of these multiply attached GVRSceneObject's but
	//  the currentSceneObject's parent must be with the original Transform's parent. 
	private GVRSceneObject currentSceneObject = null;
	// Since GVRShapeObject contains LOD range, 'shapeLODSceneObj' is used only when it's embedded
	// into a Level-of-Detail
	private GVRSceneObject shapeLODSceneObject = null;

	// points to a sensor that wraps around other nodes.
	private Sensor currentSensor = null;
	//public static Sensor currentSensor = null;

	private GVRSceneObject meshAttachedSceneObject = null;
    private GVRRenderData gvrRenderData = null;
    private GVRMesh gvrMesh = null;
    private GVRMaterial gvrMaterial = null;

    private x3dTandLShaderTest mX3DTandLShaderTest = null;   
    private GVRTextureParameters gvrTextureParameters = null;
	private GVRTexture gvrTexture = null;
	
	private static int Integer16bit = 0x10000; //65536;
	
	private Vector<Vertex> vertices = new Vector<Vertex>(); // vertices
	private Vector<VertexNormal> vertexNormal = new Vector<VertexNormal>(); 
	private Vector<TextureValues> textureCoord = new Vector<TextureValues>(); 
	private Vector<coordinates> indexedFaceSet = new Vector<coordinates>(); 
	private Vector<coordinates> indexedVertexNormals = new Vector<coordinates>(); 
	private Vector<textureCoordinates> indexedTextureCoord = new Vector<textureCoordinates>(); 

	private Vector<Key> keys = new Vector<Key>(); 
	private Vector<KeyValue> keyValues = new Vector<KeyValue>(); 
	private Vector<Float> floatArray = new Vector<Float>(); 

	private Vector<TimeSensor> timeSensors = new Vector<TimeSensor>(); 
	private Vector<Interpolator> interpolators = new Vector<Interpolator>(); 
	private Vector<RouteAnimation> routeAnimations = new Vector<RouteAnimation>(); 
	private Vector<RouteSensor> routeSensors = new Vector<RouteSensor>(); 

	private Vector<InlineObject> inlineObjects = new Vector<InlineObject>(); 

	// public since camera (i.e. Viewpoint) can change in onStep() real-time function
	public Vector<Viewpoint> viewpoints = new Vector<Viewpoint>(); 

	// public since onStep() will need to know these sensors
	public Vector<Sensor> sensors = new Vector<Sensor>(); 
	
	//private ShaderSettings shaderSettings = new ShaderSettings();
	private ShaderSettings shaderSettings = null;
    private GVRTextViewSceneObject gvrTextViewSceneObject = null;
    
    private LODmanager lodManager = null;
    private GVRCameraRig cameraRigAtRoot = null;


	
    public X3Dobject(GVRContext gvrContext, GVRModelSceneObject root) {
    	this.gvrContext = gvrContext;
    	this.activityContext = gvrContext.getContext();
    	this.root = root;
    	// this will need to be referenced in the X3DparserScript
    	this.root.setName(X3D_ROOT_NODE);
    	
    	// code copied from GVRScene::init()
        GVRCamera leftCamera = new GVRPerspectiveCamera(gvrContext);
        leftCamera.setRenderMask(GVRRenderMaskBit.Left);

        GVRCamera rightCamera = new GVRPerspectiveCamera(gvrContext);
        rightCamera.setRenderMask(GVRRenderMaskBit.Right);

        GVRPerspectiveCamera centerCamera = new GVRPerspectiveCamera(gvrContext);
        centerCamera.setRenderMask(GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);

        cameraRigAtRoot = new GVRCameraRig(gvrContext, root);
        cameraRigAtRoot.attachLeftCamera(leftCamera);
        cameraRigAtRoot.attachRightCamera(rightCamera);
        cameraRigAtRoot.attachCenterCamera(centerCamera);
    	
        cameraRigAtRoot.getLeftCamera().setBackgroundColor(Color.DKGRAY);
        cameraRigAtRoot.getRightCamera().setBackgroundColor(Color.DKGRAY);
        // attach the camera rig to the root instead of the GVRscene
        root.addChildObject(cameraRigAtRoot);
		
    	this.mAnimations = root.getAnimations();
    	lodManager = new LODmanager();
    }

	public void AddKeys (float key) {
		Key newKey = new Key(key);
		keys.add(newKey);
	}
	public void AddKeyValues (float[] values) {
		KeyValue newKeyValue = new KeyValue(values);
		keyValues.add(newKeyValue);
	}

	public void AddVertex (int x, int y, int z) {
		Vertex newVertex = new Vertex(x, y, z);
		vertices.add(newVertex);
	}
	public void AddVertex (float[] values) {
		Vertex newVertex = new Vertex(values);
		vertices.add(newVertex);
	}
	public Vertex GetVertex (int index) {
		return vertices.get(index);
	}
	
	public void AddVertexNormal(float x, float y, float z) {
		VertexNormal newVertex = new VertexNormal(x, y, z);
		vertexNormal.add(newVertex);
	}
	public void AddVertexNormal(float[] vn) {
		VertexNormal newVertex = new VertexNormal(vn);
		vertexNormal.add(newVertex);
	}
	public VertexNormal GetVertexNormal (int index) {
		return vertexNormal.get(index);
	}
	
	public void AddTextureCoord(float u, float v) {
		TextureValues newTextureCoord = new TextureValues(u, v);
		textureCoord.add(newTextureCoord);
	}
	public void AddTextureCoord(float[] tc) {
		TextureValues newTextureCoord = new TextureValues(tc);
		textureCoord.add(newTextureCoord);
	}	
	public TextureValues GetTextureCoord (int index) {
		return textureCoord.get(index);
	}

	public void AddIndexedFaceSet (short x, short y, short z) {
		coordinates newCoordinates = new coordinates(x, y, z);
		indexedFaceSet.add(newCoordinates);
	}
	public void AddIndexedFaceSet (short[] coord) {
		coordinates newCoordinates = new coordinates(coord);
		indexedFaceSet.add(newCoordinates);
	}
	
	public coordinates GetIndexedFaceSet (int index) {
		return indexedFaceSet.get(index);
	}

	public void AddTextureCoordinateSet (short x, short y, short z) {
		textureCoordinates newCoordinates = new textureCoordinates(x, y, z);
		indexedTextureCoord.add(newCoordinates);
	}
	public void AddTextureCoordinateSet (short[] tc) {
		textureCoordinates newCoordinates = new textureCoordinates(tc);
		indexedTextureCoord.add(newCoordinates);
	}
	
	public textureCoordinates GetTexturedCoordSet (int index) {
		return indexedTextureCoord.get(index);
	}

	//public Vector<coordinates> indexedVertexNormals = new Vector<coordinates>(); // vertices
	public void AddIndexedVertexNormals (short x, short y, short z) {
		coordinates newCoordinates = new coordinates(x, y, z);
		indexedVertexNormals.add(newCoordinates);
	}
	public void AddIndexedVertexNormals (short[] normalIndex) {
		coordinates newCoordinates = new coordinates(normalIndex);
		indexedVertexNormals.add(newCoordinates);
	}
	public coordinates GetIndexedVertexNormals (int index) {
		return indexedVertexNormals.get(index);
	}
	

	class UserHandler extends DefaultHandler {

		   String attributeValue = null;

		   private float[] parseFixedLengthFloatString(String numberString, int componentCount, boolean constrained0to1, boolean zeroOrGreater) {
			   StringReader sr = new StringReader(numberString);
			   StreamTokenizer st = new StreamTokenizer(sr);
			   st.parseNumbers();
			   int tokenType;
			   float componentFloat[] = new float[componentCount];
			   try {
				   for (int i = 0; i < componentCount; i++) {
					   if ((tokenType = st.nextToken()) == StreamTokenizer.TT_NUMBER) {
						   componentFloat[i] = (float) st.nval;
					   }
					   else { // check for an exponent 'e'
						   if (tokenType == StreamTokenizer.TT_WORD) {
							   String word = st.sval;
							   if ( word.startsWith("e-")) { // negative exponent
								   String exponentString = word.substring(2, word.length());
								   try {
									   --i; // with this exponent, we are still working with the previous number
									   Integer exponentInt = Integer.parseInt(exponentString);
									   componentFloat[i] *= (float)Math.pow(10, -exponentInt.intValue());
								   } catch (NumberFormatException e) {
									   Log.e(TAG, "parsing fixed length string, exponent number conversion error: " + exponentString);
								   }
							   }
							   else if ( word.equalsIgnoreCase("e")) { // exponent with plus sign
								   tokenType = st.nextToken();
								   if (tokenType == 43) {  // "+" plus sign
									   if ((tokenType = st.nextToken()) == StreamTokenizer.TT_NUMBER) {
										   --i; // with this exponent, we are still working with the previous number
										   float exponent = (float) st.nval;
										   componentFloat[i] *= (float)Math.pow(10, exponent);
									   }
									   else {
										   st.pushBack();
										   Log.e(TAG, "Error: exponent in X3D parser with fixed length float");
									   }
								   }
								   else st.pushBack();
							   }
							   else st.pushBack();
						   }
					   } // end  check for 'e' exponent
					   if (constrained0to1) {
						   if (componentFloat[i] < 0) componentFloat[i] = 0;
						   else if  (componentFloat[i] > 1) componentFloat[i] = 1;
					   }
					   else if (zeroOrGreater) {
						   if (componentFloat[i] < 0) componentFloat[i] = 0;
					   }
				   }  //  end for-loop
			   } //  end 'try'
			   catch (IOException e) {
				   Log.d(TAG, "Error parsing fixed length float string: " + e);
			   }
			   return componentFloat;
		   }  //  end parseFixedLengthFloatString

		   private boolean parseBooleanString(String booleanString) {
			   StringReader sr = new StringReader(booleanString);
			   StreamTokenizer st = new StreamTokenizer(sr);
			   boolean value = false;
			   int tokenType;
				try {
					tokenType = st.nextToken();
					if ( tokenType == StreamTokenizer.TT_WORD) {
						if ( st.sval.equalsIgnoreCase("true")) value = true;
					}
				} catch (IOException e) {
					   Log.d("Parse X3D", "Boolean Error: " + e);
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
				   while ( (tokenType = st.nextToken()) != StreamTokenizer.TT_EOF) {
						//if ( tokenType == StreamTokenizer.TT_WORD) {
							strings.add(st.sval);
						//}
				   }
				} catch (IOException e) {
					Log.d("Parse X3D", "String parsing Error: " + e);
					e.printStackTrace();
				}
				mfStrings = new String[strings.size()];
				for (int i = 0; i < strings.size(); i++) {
					mfStrings[i] = strings.get(i);
				}
			    return mfStrings;
		   }  //  end parseMFString

		   private void parseNumbersString(String numberString, int componentType, int componentCount) {
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
						   // first parse for short values whih will be integers
						   //    and have no exponents
						   if (componentType == X3Dobject.indexedFaceSetComponent) {
							   if ( (short) st.nval != -1) {
								   componentShort[index] = (short)st.nval;
								   index++;
								   if (index == componentCount) {
									   AddIndexedFaceSet(componentShort);
									   index = 0;
								   }
							   }
						   }
						   else if (componentType == X3Dobject.textureIndexComponent) {
							   if ( (short) st.nval != -1) {
								   componentShort[index] = (short)st.nval;
								   index++;
								   if (index == componentCount) {
									   AddTextureCoordinateSet(componentShort);
									   index = 0;
								   }
							   }
						   }
						   else if (componentType == X3Dobject.normalIndexComponent) {
							   if ( (short) st.nval != -1) {
								   componentShort[index] = (short)st.nval;
								   index++;
								   if (index == componentCount) {
									   AddIndexedVertexNormals(componentShort);
									   index = 0;
								   }
							   }
						   }
						   else if (componentType == X3Dobject.verticesComponent) {
								componentFloat[index] = (float)(st.nval);
								index++;
								if (index == componentCount) {
									AddVertex (componentFloat);
									index = 0;
								}
						   }
						   else if (componentType == X3Dobject.textureCoordComponent) {
								componentFloat[index] = (float)st.nval;
								index++;
								if (index == componentCount) {
									AddTextureCoord(componentFloat);
									index = 0;
								}
						   }
						   else if (componentType == X3Dobject.normalsComponent) {
								componentFloat[index] = (float)st.nval;
								index++;
								if (index == componentCount) {
									AddVertexNormal(componentFloat);
									index = 0;
								}
						   }
						   else if (componentType == X3Dobject.interpolatorKeyComponent) {
								componentFloat[index] = (float)st.nval;
								index++;
								if (index == componentCount) {
									AddKeys(componentFloat[0]);
									index = 0;
								}
						   }
						   else if (componentType == X3Dobject.interpolatorKeyValueComponent) {
								componentFloat[index] = (float)st.nval;
								index++;
								if (index == componentCount) {
									AddKeyValues(componentFloat);
									index = 0;
								}
						   }
						   else if (componentType == X3Dobject.LODComponent) {
								componentFloat[index] = (float)st.nval;
								AddKeys(componentFloat[0]);
						   }
						   else if (componentType == X3Dobject.elevationGridHeight) {
							   floatArray.add(new Float((float)st.nval));
						   }

					   }  //  end if token = number
				   }  //  end while loop 
			   }  //  end try statement
			   catch (IOException e) {
				   Log.d(TAG, "Error: parseNumbersString - " + e);
			   }
			}  //  parseNumbersString

		   
		   private GVRSceneObject AddGVRSceneObject() {
			    GVRSceneObject newObject = new GVRSceneObject(gvrContext);
		    	if (currentSceneObject == null) root.addChildObject(newObject);
		    	else currentSceneObject.addChildObject(newObject);
	    		return newObject;
		   }
		   
	/*********** Parse the X3D File **************/
	   @Override
	   public void startElement(String uri,
	      String localName, String qName, Attributes attributes)
	         throws SAXException {

		      /********** Transform **********/
		      if (qName.equalsIgnoreCase("transform")) {
		    	  // initialize default values
		    	  String name = "";
				  float[] center = {0, 0, 0};
				  float[] rotation = {0, 0, 1, 0};
				  float[] scaleOrientation = {0, 0, 1, 0};
				  float[] scale = {1, 1, 1};
				  float[] translation = {0, 0, 0};
		    	
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	name = attributeValue;
		        }
		        // Order for Transformations:
		        //    P' = T * C * R * SR * S * -SR * -C * P
		        //		T=Translation, C=Center, R=Rotation, SR=ScaleOrientation, S=Scale, and P will the Point
		        // Parsing Center value must occur before Rotation
		        String translationAttribute = attributes.getValue("translation");
		        if (translationAttribute != null) {
		        	translation = parseFixedLengthFloatString(translationAttribute, 3, false, false);
		        }
		        String centerAttribute = attributes.getValue("center");
		        if (centerAttribute != null) {
		        	center = parseFixedLengthFloatString(centerAttribute, 3, false, false);
				}
		        String rotationAttribute = attributes.getValue("rotation");
		        if (rotationAttribute != null) {
		        	rotation = parseFixedLengthFloatString(rotationAttribute, 4, false, false);
		        }
		        String scaleOrientationAttribute = attributes.getValue("scaleOrientation");
		        if (scaleOrientationAttribute != null) {
		        	scaleOrientation = parseFixedLengthFloatString(scaleOrientationAttribute, 4, false, false);
		        }
		        attributeValue = attributes.getValue("scale");
		        if (attributeValue != null) {
		        	scale = parseFixedLengthFloatString(attributeValue, 3, false, true);
		        }
		        
		        if (name.isEmpty()) {
		        	// There is no DEF, thus no animation or interactivity applied to this Transform
		        	// Therefore, just set the values in a single GVRSceneObject
		    		currentSceneObject = AddGVRSceneObject();
		    		GVRTransform transform = currentSceneObject.getTransform();
		        	transform.setPosition( translation[0], translation[1], translation[2] );
		        	transform.rotateByAxisWithPivot( (float)Math.toDegrees(rotation[3]), rotation[0], rotation[1], rotation[2],
		        			center[0], center[1], center[2]);
		    		transform.setScale( scale[0], scale[1], scale[2] );
		        }
		        else {
		        	// This transform may be animated later, which means we must have separate GVRSceneObjects
		        	// for each transformation plus center and scaleOrientation if needed
			        // Order for Transformations:
			        //    P' = T * C * R * SR * S * -SR * -C * P
				    // First add the translation
		    		currentSceneObject = AddGVRSceneObject();
		    		currentSceneObject.getTransform().setPosition( translation[0], translation[1], translation[2] );
		        	currentSceneObject.setName(name + TRANSFORM_TRANSLATION_);
		        	// now check if we have a center value.
		        	if ( (center[0] != 0) || (center[1] != 0) || (center[2] != 0) ) {
			    		currentSceneObject = AddGVRSceneObject();
			    		currentSceneObject.getTransform().setPosition( center[0], center[1], center[2] );
			    		currentSceneObject.setName(name + TRANSFORM_CENTER_);
		        	}
		        	// add rotation
		    		currentSceneObject = AddGVRSceneObject();
		    		currentSceneObject.getTransform().setRotationByAxis( (float)Math.toDegrees(rotation[3]), rotation[0], rotation[1], rotation[2] );
		        	currentSceneObject.setName(name + TRANSFORM_ROTATION_);
		        	// now check if we have a scale orientation value.
		        	if ( (scaleOrientation[0] != 0) || (scaleOrientation[1] != 0) || (scaleOrientation[2] != 1) || (scaleOrientation[3] != 0) ) {
			    		currentSceneObject = AddGVRSceneObject();
			    		currentSceneObject.getTransform().setRotationByAxis( (float)Math.toDegrees(scaleOrientation[3]), scaleOrientation[0], scaleOrientation[1], scaleOrientation[2] );
			    		currentSceneObject.setName(name + TRANSFORM_SCALE_ORIENTATION_);
		        	}
		        	// add rotation
		    		currentSceneObject = AddGVRSceneObject();
		    		currentSceneObject.getTransform().setScale( scale[0], scale[1], scale[2] );
		        	currentSceneObject.setName(name + TRANSFORM_SCALE_);
		        	// if we had a scale orientation, now we have to negate it.
		        	if ( (scaleOrientation[0] != 0) || (scaleOrientation[1] != 0) || (scaleOrientation[2] != 1) || (scaleOrientation[3] != 0) ) {
			    		currentSceneObject = AddGVRSceneObject();
			    		currentSceneObject.getTransform().setRotationByAxis( (float)Math.toDegrees( -scaleOrientation[3]), scaleOrientation[0], scaleOrientation[1], scaleOrientation[2] );
			    		currentSceneObject.setName(name + TRANSFORM_NEGATIVE_SCALE_ORIENTATION_);
		        	}
		        	// now check if we have a center value.
		        	if ( (center[0] != 0) || (center[1] != 0) || (center[2] != 0) ) {
			    		currentSceneObject = AddGVRSceneObject();
			    		currentSceneObject.getTransform().setPosition( -center[0], -center[1], -center[2] );
			    		currentSceneObject.setName(name + TRANSFORM_NEGATIVE_CENTER_);
		        	}
		        	// Actual object that will have GVRendering and GVRMesh attached
		    		currentSceneObject = AddGVRSceneObject();
		        	currentSceneObject.setName(name);
		        } // end if DEF name and thus possible animation / interactivity
		      } //  end Transform

		      /********** Group **********/
		      else if (qName.equalsIgnoreCase("Group")) {
	    		currentSceneObject = AddGVRSceneObject();
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	currentSceneObject.setName(attributeValue);
		        }
		      }

		      /********** Shape **********/
		      else if (qName.equalsIgnoreCase("shape")) {
		         gvrRenderData = new GVRRenderData(gvrContext);
		         //gvrRenderData.setCullFace(GVRCullFaceEnum.None);
		         gvrRenderData.setCullFace(GVRCullFaceEnum.Back);
		         shaderSettings.initializeTextureMaterial();

		         if (UNIVERSAL_LIGHTS) gvrRenderData.setShaderTemplate(GVRPhongShader.class);
		         		         
		         if (lodManager.isActive()) {
		        	shapeLODSceneObject = AddGVRSceneObject();
		        	shapeLODSceneObject.setLODRange(lodManager.getMinRange(), lodManager.getMaxRange());
		        	currentSceneObject = shapeLODSceneObject;
		         }

		      }

		      /********** Appearance **********/
		      else if (qName.equalsIgnoreCase("appearance")) {
		         
		         /* This gives the X3D-only Shader */
		        if (!UNIVERSAL_LIGHTS ) mX3DTandLShaderTest = new x3dTandLShaderTest(gvrContext);
		      }

		      /********** Material **********/
		      else if (qName.equalsIgnoreCase("material")) {
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	System.out.print("DEF='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        }
		        String diffuseColorAttribute = attributes.getValue("diffuseColor");
		        if (diffuseColorAttribute != null) {
		        	float diffuseColor[] = parseFixedLengthFloatString(diffuseColorAttribute, 3, true, false);
		        	shaderSettings.setDiffuseColor(diffuseColor);
		        }
		        String specularColorAttribute = attributes.getValue("specularColor");
		        if (specularColorAttribute != null) {
		        	float specularColor[] = parseFixedLengthFloatString(specularColorAttribute, 3, true, false);
		        	shaderSettings.setSpecularColor(specularColor);
		        }
		        String emissiveColorAttribute = attributes.getValue("emissiveColor");
		        if (emissiveColorAttribute != null) {
		        	float emissiveColor[] = parseFixedLengthFloatString(emissiveColorAttribute, 3, true, false);
		        	shaderSettings.setEmmissiveColor(emissiveColor);
		        }
		        String ambientIntensityAttribute = attributes.getValue("ambientIntensity");
		        if (ambientIntensityAttribute != null) {
				    Log.e(TAG, "ambientIntensity currently not implemented.");
			        float ambientIntensity[] = parseFixedLengthFloatString(ambientIntensityAttribute, 1, true, false);
			        shaderSettings.setAmbientIntensity(ambientIntensity[0]);
		        }
		        String shininessAttribute = attributes.getValue("shininess");
		        if (shininessAttribute != null) {
		        	float shinniness[] = parseFixedLengthFloatString(shininessAttribute, 1, true, false);
			        shaderSettings.setShininess(shinniness[0]);
		        }
		        String transparencyAttribute = attributes.getValue("transparency");
		        if (transparencyAttribute != null) {
		        	System.out.print("transparency='" + transparencyAttribute + "'  NOT IMPLEMENTED YET");
		        	float transparency[] = parseFixedLengthFloatString(transparencyAttribute, 1, true, false);
			        shaderSettings.setTransparency(transparency[0]);
		        }
		      }
		      /********** ImageTexture **********/
		      else if (qName.equalsIgnoreCase("ImageTexture")) {

		        gvrTextureParameters = new GVRTextureParameters(gvrContext);
		        gvrTextureParameters.setWrapSType( TextureWrapType.GL_REPEAT);
		        gvrTextureParameters.setWrapTType( TextureWrapType.GL_REPEAT);
		        
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	System.out.print("DEF='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        }
		        String urlAttribute = attributes.getValue("url");
		        if (urlAttribute != null) {
		        	  //System.out.print("url ='" + urlAttribute + "' ");
		        	  //urlAttribute = parseWord(urlAttribute, true);
		        	  urlAttribute = urlAttribute.replace("\"", "");  // remove double and single quotes
		        	  urlAttribute = urlAttribute.replace("\'", "");
		        	  urlAttribute = urlAttribute.toLowerCase();
		        	  urlAttribute = urlAttribute.substring(0, urlAttribute.indexOf("."));
			          int assetID = activityContext.getResources().getIdentifier(urlAttribute, "drawable", activityContext.getPackageName());
			          if (assetID != 0) {
				          gvrTexture = gvrContext.loadTexture(
				        		  new GVRAndroidResource(gvrContext, assetID));
			          }
			          else Log.i(TAG, urlAttribute + " NOT found");
		        }
		        String repeatSAttribute = attributes.getValue("repeatS");
		        if (repeatSAttribute != null) {
		        	if ( !parseBooleanString(repeatSAttribute) )  {
		        		gvrTextureParameters.setWrapSType( TextureWrapType.GL_CLAMP_TO_EDGE);
				    }
		        }
		        String repeatTAttribute = attributes.getValue("repeatT");
		        if (repeatTAttribute != null) {
		        	if ( !parseBooleanString(repeatTAttribute) ) {
		        		gvrTextureParameters.setWrapTType( TextureWrapType.GL_CLAMP_TO_EDGE);		        		
		        	}
		        }
		        if (gvrTexture != null) {
		        	gvrTexture.updateTextureParameters(gvrTextureParameters);
		        	shaderSettings.setTexture(gvrTexture);
		        }
		      }  //  end ImageTexture
		      
		      /********** TextureTransform **********/
		      else if (qName.equalsIgnoreCase("TextureTransform")) {
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	System.out.print("DEF='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        }
		        String centerAttribute = attributes.getValue("center");
		        if (centerAttribute != null) {
		        	float[] center = parseFixedLengthFloatString(centerAttribute, 2, false, false);
		        	shaderSettings.setTextureCenter(center);
		        }
		        String rotationAttribute = attributes.getValue("rotation");
		        if (rotationAttribute != null) {
		        	float[] rotation = parseFixedLengthFloatString(rotationAttribute, 1, false, false);
		        	shaderSettings.setTextureRotation(rotation[0]);
		        }
		        String scaleAttribute = attributes.getValue("scale");
		        if (scaleAttribute != null) {
		        	float[] scale = parseFixedLengthFloatString(scaleAttribute, 2, false, true);
		        	shaderSettings.setTextureScale(scale);
		        }
		        String translationAttribute = attributes.getValue("translation");
		        if (translationAttribute != null) {
		        	float[] translation = parseFixedLengthFloatString(translationAttribute, 2, false, false);
		        	shaderSettings.setTextureTranslation(translation);
		        }
		      }


		      /********** IndexedFaceSet **********/
		      /********** and its children **********/
		      /********** eventually include IndexedLineSet **********/
		      else if (qName.equalsIgnoreCase("IndexedFaceSet")) {
		        gvrMesh = new GVRMesh(gvrContext);
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	System.out.print("DEF='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        }
		        String solidAttribute = attributes.getValue("solid");
		        if (solidAttribute != null) {
		        	System.out.print("solid='" + solidAttribute + "'  NOT IMPLEMENTED YET");
		        }
		        String coordIndexAttribute = attributes.getValue("coordIndex");
		        if (coordIndexAttribute != null) {
		          parseNumbersString(coordIndexAttribute, X3Dobject.indexedFaceSetComponent, 3);

		          char[] ifs = new char[indexedFaceSet.size()*3] ;
		          
		          for (int i = 0; i < indexedFaceSet.size(); i++) {
		        	  coordinates coordinate = indexedFaceSet.get(i);
		        	  for (int j = 0; j < 3; j++ ) {
		        		  ifs[i*3+j] = (char) coordinate.coords[j];
		        	  }
		          }
		          gvrMesh.setIndices(ifs);
		        }
		        String normalIndexAttribute = attributes.getValue("normalIndex");
		        if (normalIndexAttribute != null) {
		          parseNumbersString(normalIndexAttribute, X3Dobject.normalIndexComponent, 3);
		        }
		        String texCoordIndexAttribute = attributes.getValue("texCoordIndex");
		        if (texCoordIndexAttribute != null) {
		          parseNumbersString(texCoordIndexAttribute, X3Dobject.textureIndexComponent, 3);
		        }
		      }  // end IndexedFaceSet 

		      /********** Coordinate **********/
		      else if (qName.equalsIgnoreCase("Coordinate")) {
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	System.out.print("DEF='" + attributeValue + "' NOT IMPLEMENTED YET");
		        }
		        String pointAttribute = attributes.getValue("point");
		        if (pointAttribute != null) {
					parseNumbersString(pointAttribute, X3Dobject.verticesComponent, 3);
					
			        float[] vertexList = new float[vertices.size()*3] ;
			          for (int i = 0; i < vertices.size(); i++) {
			        	  Vertex vertex = vertices.get(i);
			        	  for (int j = 0; j < 3; j++ ) {
			        		  vertexList[i*3+j] = vertex.point[j];
			        	  }
			          }
			          gvrMesh.setVertices(vertexList);
		        }
		      }

		      /********** TextureCoordinate **********/
		      else if (qName.equalsIgnoreCase("TextureCoordinate")) {
		        
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	System.out.print("DEF='" + attributeValue + "' NOT YET IMPLEMENTED");
		        }
		        // Have to flip the y texture coordinates because the image will be upside down
		        String pointAttribute = attributes.getValue("point");
		        if (pointAttribute != null) {
					parseNumbersString(pointAttribute, X3Dobject.textureCoordComponent, 2);
					
					// initialize the list
		        	// We may need to reorganize the order of the texture coordinates if there
					// isn't a 1-to-1 match of coordinates, and texture coordinates.
			        char[] ifs = gvrMesh.getIndices();

			        float[] textureCoordinateList = new float[ifs.length * 2] ;
			        int[] indexedTextureCoordList = null;

			        // check if an indexedTextureCoordinat list is present
			        if ( indexedTextureCoord.size() != 0) {
			        	// current indexedFaceSet has a textureCoordIndex.
				        indexedTextureCoordList = new int[ indexedTextureCoord.size()*3 ];
				        for (int i = 0; i < indexedTextureCoord.size(); i++){
				            textureCoordinates tcIndex = GetTexturedCoordSet(i);
				        	for (int j = 0; j < 3; j++) {
				        		indexedTextureCoordList[i*3+j] = tcIndex.coords[j];
				        	}
				        }
			        }
			        
			        else {
			        	// use the coordIndex if there is no indexedTextureCoord.
				        indexedTextureCoordList = new int[ indexedFaceSet.size()*3 ];
				        for (int i = 0; i < indexedFaceSet.size(); i++){
				        	coordinates coordinate = indexedFaceSet.get(i);
				        	for (int j = 0; j < 3; j++ ) {
				        		indexedTextureCoordList[i*3+j] = (char) coordinate.coords[j];
				        	}
				        }
			        }					
				        
				    try {
					    for (int i = 0; i < ifs.length; i++) {
					        int index = ifs[i];
					        int tcIndex = indexedTextureCoordList[ i ];
					        TextureValues textureValues = textureCoord.get(tcIndex);
					        for (int j = 0; j < 2; j++ ) {
					        	textureCoordinateList[index*2+j] = textureValues.coord[j];
					        }
					     }
				    }
				    catch (ArrayIndexOutOfBoundsException e) {
				        Log.e(TAG, "Texture Coordinates array indexed out of bounds exception");
				        Log.e(TAG, "error: " + e);
				    }
			        
				    // Flip the Y texture coordinate since y-axis 'up' is positive in X3D, and down in GearVR
			        float minYtextureCoordinate = Float.MAX_VALUE;
			        float maxYtextureCoordinate = Float.MIN_VALUE;
			        for (int i = 0; i < textureCoord.size(); i++) {
			        	  if (textureCoordinateList[i*2+1] > maxYtextureCoordinate) maxYtextureCoordinate = textureCoordinateList[i*2+1];
			        	  else if (textureCoordinateList[i*2+1] < minYtextureCoordinate) minYtextureCoordinate = textureCoordinateList[i*2+1];
			        }
			        // flip the Y vertices
			        //float maxMinDiff = maxYtextureCoordinate - minYtextureCoordinate;
			        int maxMinDiff = (int)Math.round( (float)Math.ceil(maxYtextureCoordinate - minYtextureCoordinate));
			        for (int i = 1; i < textureCoordinateList.length; i+=2) {
			        //for ( i = 1; i < textureCoordinateList.length; i+=2) {
			        	  textureCoordinateList[i] = -textureCoordinateList[i] + maxMinDiff;
			        }
			        gvrMesh.setTexCoords(textureCoordinateList);
		        }
		      }

		      /********** Normal **********/
		      else if (qName.equalsIgnoreCase("Normal")) {
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	System.out.print("DEF='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        }
		        String vectorAttribute = attributes.getValue("vector");
		        if (vectorAttribute != null) {
					parseNumbersString(vectorAttribute, X3Dobject.normalsComponent, 3);

					// initialize the list
			        char[] ifs = gvrMesh.getIndices();
			        
			        float[] normalVectorList = new float[ifs.length * 3] ;
			        
			        // check if an indexedVertexNormals list is present
			        int[] indexedVertexNormalsList = null;
			        if ( indexedVertexNormals.size() != 0) {
			        	// current indexedFaceSet has a normalIndex.
			        	// We may need to reorganize the order of the texture coordinates
				        indexedVertexNormalsList = new int[ indexedVertexNormals.size()*3 ];
				        for (int i = 0; i < indexedVertexNormals.size(); i++){
				        	coordinates vnIndex = GetIndexedVertexNormals(i);
				        	for (int j = 0; j < 3; j++) {
				        		indexedVertexNormalsList[i*3+j] = vnIndex.coords[j];
				        	}
				        }
			        }
			        else {
			        	// use the coordIndex if there is no normalIndex.
			        	indexedVertexNormalsList = new int[ indexedFaceSet.size()*3 ];
				        for (int i = 0; i < indexedFaceSet.size(); i++){
				        	coordinates coordinate = indexedFaceSet.get(i);
				        	for (int j = 0; j < 3; j++ ) {
				        		indexedVertexNormalsList[i*3+j] = (char) coordinate.coords[j];
				        	}
				        }
			        	
			        }
				    
			        try {
						for (int i = 0; i < ifs.length; i++) {
					        int index = ifs[i];
					        int vnIndex = indexedVertexNormalsList[ i ];
						     VertexNormal vertexNormals = vertexNormal.get(vnIndex);
					         for (int j = 0; j < 3; j++ ) {
					        		  normalVectorList[index*3+j] = vertexNormals.vector[j];
					         }
				        }
			        }
				    catch (ArrayIndexOutOfBoundsException e) {
				        Log.e(TAG, "Normals array indexed out of bounds exception");
				        Log.e(TAG, "error: " + e);
				    }
			        
			        gvrMesh.setNormals(normalVectorList);
		        }
		      }

		      /********** LIGHTS **********/
		      /********** PointLight **********/
		      else if (qName.equalsIgnoreCase("PointLight")) {
			    if (UNIVERSAL_LIGHTS ) {
		    	
			    	String name = "";
					float ambientIntensity = 0;
				    float[] attenuation = {1, 0, 0};
				    float[] color = {1, 1, 1};
				    boolean global = true;
					float[] intensity = {1};
				    float[] location = {0, 0, 0};
				    boolean on = true;
					float[] radius = {100};
					
			        attributeValue = attributes.getValue("DEF");
			        if (attributeValue != null) {
			        	name = attributeValue;
			        }
			        attributeValue = attributes.getValue("ambientIntensity");
			        if (attributeValue != null) {
			        	System.out.print("ambientIntensity='" + attributeValue + "'  NOT IMPLEMENTED YET");
			        }
			        attributeValue = attributes.getValue("attenuation");
			        if (attributeValue != null) {
			        	attenuation = parseFixedLengthFloatString(attributeValue, 3, false, true);
			        	if ( (attenuation[0] == 0) &&  (attenuation[1] == 0) &&  (attenuation[2] == 0) ) attenuation[0] = 1;
			        }
			        attributeValue = attributes.getValue("color");
			        if (attributeValue != null) {
			        	color = parseFixedLengthFloatString(attributeValue, 3, true, false);
			        }
			        attributeValue = attributes.getValue("global");
			        if (attributeValue != null) {
			        	System.out.print("global='" + attributeValue + "'  NOT IMPLEMENTED YET");
			        }
			        attributeValue = attributes.getValue("intensity");
			        if (attributeValue != null) {
			        	intensity = parseFixedLengthFloatString(attributeValue, 1, true, false);
			        }
			        attributeValue = attributes.getValue("location");
			        if (attributeValue != null) {
			        	location = parseFixedLengthFloatString(attributeValue, 3, false, false);
			        }
			        attributeValue = attributes.getValue("on");
			        if (attributeValue != null) {
			 		    on = parseBooleanString(attributeValue);
			        }
			        attributeValue = attributes.getValue("radius");
			        if (attributeValue != null) {
			        	radius = parseFixedLengthFloatString(attributeValue, 1, false, true);
			        }

			        GVRSceneObject newPtLightSceneObj = AddGVRSceneObject();
			        GVRPointLight newPtLight = new GVRPointLight(gvrContext);
			        newPtLightSceneObj.attachLight(newPtLight);
			        
			        //newPtLight.setAmbientIntensity(0, 0, 0, 1);
			        newPtLight.setAmbientIntensity(color[0]*intensity[0], color[1]*intensity[0], color[2]*intensity[0], 1);
			        newPtLight.setDiffuseIntensity(color[0]*intensity[0], color[1]*intensity[0], color[2]*intensity[0], 1);
			        //newPtLight.setSpecularIntensity(0, 0, 0, 1);
			        newPtLight.setSpecularIntensity(color[0]*intensity[0], color[1]*intensity[0], color[2]*intensity[0], 1);
			        newPtLight.setAttenuation(attenuation[0], attenuation[1], attenuation[2]);
			        if (on) newPtLight.enable();
			        else newPtLight.disable();

			        GVRTransform newPtLightSceneObjTransform = newPtLightSceneObj.getTransform();
			        newPtLightSceneObjTransform.setPosition(location[0], location[1], location[2]);
			        newPtLightSceneObj.setName(name);
			    }  // end if UNIVERSAL_LIGHTS
		      } //  end PointLight
		      /********** DirectionalLight **********/
		      else if (qName.equalsIgnoreCase("DirectionalLight")) {
				 if (UNIVERSAL_LIGHTS ) {
			    	String name = "";
					float ambientIntensity = 0;
				    float[] color = {1, 1, 1};
				    float[] direction = {0, 0, -1};
				    boolean global = true;
					float[] intensity = {1};
				    boolean on = true;
					
			        attributeValue = attributes.getValue("DEF");
			        if (attributeValue != null) {
			        	name = attributeValue;
			        }
			        attributeValue = attributes.getValue("ambientIntensity");
			        if (attributeValue != null) {
			        	System.out.print("ambientIntensity='" + attributeValue + "'  NOT IMPLEMENTED YET");
			        }
			        attributeValue = attributes.getValue("color");
			        if (attributeValue != null) {
			        	color = parseFixedLengthFloatString(attributeValue, 3, true, false);
			        }
			        attributeValue = attributes.getValue("direction");
			        if (attributeValue != null) {
			        	direction = parseFixedLengthFloatString(attributeValue, 3, false, false);
			        }
			        attributeValue = attributes.getValue("global");
			        if (attributeValue != null) {
			        	System.out.print("global='" + attributeValue + "'  NOT IMPLEMENTED YET");
			        }
			        attributeValue = attributes.getValue("intensity");
			        if (attributeValue != null) {
			        	intensity = parseFixedLengthFloatString(attributeValue, 1, true, false);
			        }
			        attributeValue = attributes.getValue("on");
			        if (attributeValue != null) {
			 		    on = parseBooleanString(attributeValue);
			        }
			        GVRSceneObject newDirectionalLightSceneObj = AddGVRSceneObject();
			        GVRDirectLight newDirectionalLight = new GVRDirectLight(gvrContext);
			        newDirectionalLightSceneObj.attachLight(newDirectionalLight);

			        newDirectionalLight.setAmbientIntensity(0, 0, 0, 1);
			        newDirectionalLight.setDiffuseIntensity(color[0]*intensity[0], color[1]*intensity[0], color[2]*intensity[0], 1);
			        newDirectionalLight.setSpecularIntensity(0, 0, 0, 1);

			        if (on) newDirectionalLight.enable();
			        else newDirectionalLight.disable();
			        
			        // Vectors3f: D = light direction; s = Side; u = up = re-crossed
			        Vector3f d = new Vector3f(-direction[0], -direction[1], -direction[2]);
			        // check for exception condition
			        Quaternionf q = new Quaternionf();
			        if ( (d.x == 0) && (d.z == 0) ) {
			        	// exception condition
			        	if (d.y > 0) {
				        	AxisAngle4f angleAxis = new AxisAngle4f( -(float)Math.PI/2, 1, 0, 0);
				        	q.set(angleAxis);
			        	}
			        	else if (d.y < 0) {
				        	AxisAngle4f angleAxis = new AxisAngle4f( (float)Math.PI/2, 1, 0, 0);
				        	q.set(angleAxis);
			        	}
			        	else { // zero's.  Just set to identity quaternion
			        		q.identity();
			        	}
			        }
			        else {
				        d.normalize();
				        Vector3f up = new Vector3f(0, 1, 0);
				        Vector3f s = new Vector3f();
				        d.cross(up, s);
				        s.normalize();
				        Vector3f u = new Vector3f();
				        d.cross(s, u);
				        u.normalize();
				        Matrix4f matrix = new Matrix4f(
				        		s.x, s.y, s.z, 0,
				        		u.x, u.y, u.z, 0,
				        		d.x, d.y, d.z, 0,
				        		0, 0, 0, 1 );
				        q.setFromNormalized(matrix);
			        }
			        
			        // set direction in the Light's GVRScene
			        GVRTransform newDirectionalLightSceneObjTransform = newDirectionalLightSceneObj.getTransform();
			        newDirectionalLightSceneObjTransform.setRotation(q.w, q.x, q.y, q.z);
			        newDirectionalLightSceneObj.setName(name);
				 }
		      }  //  end Directional Light
		      
		      /********** SpotLight **********/
		      else if (qName.equalsIgnoreCase("SpotLight")) {
				    if (UNIVERSAL_LIGHTS ) {
				    	String name = "";
						float ambientIntensity = 0;
					    float[] attenuation = {1, 0, 0};
						float[] beamWidth = { (float)Math.PI/4}; // range is 0 to 180 degrees
					    float[] color = {1, 1, 1};
						float[] cutOffAngle = { (float)Math.PI/2}; // range is 0 to 180 degrees
					    float[] direction = {0, 0, -1};
					    boolean global = true;
						float[] intensity = {1};
					    float[] location = {0, 0, 0};
					    boolean on = true;
						float[] radius = {100};
						
				        attributeValue = attributes.getValue("DEF");
				        if (attributeValue != null) {
				        	name = attributeValue;
				        }
				        attributeValue = attributes.getValue("ambientIntensity");
				        if (attributeValue != null) {
				        	System.out.print("ambientIntensity='" + attributeValue + "'  NOT IMPLEMENTED YET");
				        }
				        attributeValue = attributes.getValue("attenuation");
				        if (attributeValue != null) {
				        	attenuation = parseFixedLengthFloatString(attributeValue, 3, false, true);
				        	if ( (attenuation[0] == 0) &&  (attenuation[1] == 0) &&  (attenuation[2] == 0) ) attenuation[0] = 1;
				        }
				        attributeValue = attributes.getValue("beamWidth");
				        if (attributeValue != null) {
				        	beamWidth = parseFixedLengthFloatString(attributeValue, 1, false, true);
				        	if ( beamWidth[0] > (float) Math.PI/2 ) beamWidth[0] = (float) Math.PI/2;
				        }
				        attributeValue = attributes.getValue("color");
				        if (attributeValue != null) {
				        	color = parseFixedLengthFloatString(attributeValue, 3, true, false);
				        }
				        attributeValue = attributes.getValue("cutOffAngle");
				        if (attributeValue != null) {
				        	cutOffAngle = parseFixedLengthFloatString(attributeValue, 1, false, true);
				        	if ( cutOffAngle[0] > (float) Math.PI/2 ) cutOffAngle[0] = (float) Math.PI/2;
				        }
				        attributeValue = attributes.getValue("direction");
				        if (attributeValue != null) {
				        	direction = parseFixedLengthFloatString(attributeValue, 3, false, false);
				        }
				        attributeValue = attributes.getValue("global");
				        if (attributeValue != null) {
				        	System.out.print("global='" + attributeValue + "'  NOT IMPLEMENTED YET");
				        }
				        attributeValue = attributes.getValue("intensity");
				        if (attributeValue != null) {
				        	intensity = parseFixedLengthFloatString(attributeValue, 1, true, false);
				        }
				        attributeValue = attributes.getValue("location");
				        if (attributeValue != null) {
				        	location = parseFixedLengthFloatString(attributeValue, 3, false, false);
				        }
				        attributeValue = attributes.getValue("on");
				        if (attributeValue != null) {
				 		    on = parseBooleanString(attributeValue);
				        }
				        attributeValue = attributes.getValue("radius");
				        if (attributeValue != null) {
				        	radius = parseFixedLengthFloatString(attributeValue, 1, false, true);
				        }
				        
				        GVRSceneObject newSpotLightSceneObj = AddGVRSceneObject();
				        GVRSpotLight newSpotLight = new GVRSpotLight(gvrContext);
				        newSpotLightSceneObj.attachLight(newSpotLight);

				        newSpotLight.setAmbientIntensity(color[0]*intensity[0], color[1]*intensity[0], color[2]*intensity[0], 1);
				        newSpotLight.setDiffuseIntensity(color[0]*intensity[0], color[1]*intensity[0], color[2]*intensity[0], 1);
				        newSpotLight.setSpecularIntensity(0, 0, 0, 1);
				        newSpotLight.setAttenuation(attenuation[0], attenuation[1], attenuation[2]);
				        	 
				        if (on) newSpotLight.enable();
				        else newSpotLight.disable();
				        newSpotLight.setInnerConeAngle(beamWidth[0] * 180 / (float)Math.PI);
				        newSpotLight.setOuterConeAngle(cutOffAngle[0] * 180 / (float)Math.PI);
				        
				        // Vectors3f: D = light direction; s = Side; u = up = re-crossed
				        Vector3f d = new Vector3f(-direction[0], -direction[1], -direction[2]);
				        // check for exception condition
				        Quaternionf q = new Quaternionf();
				        if ( (d.x == 0) && (d.z == 0) ) {
				        	// exception condition
				        	if (d.y > 0) {
					        	AxisAngle4f angleAxis = new AxisAngle4f( -(float)Math.PI/2, 1, 0, 0);
					        	q.set(angleAxis);
				        	}
				        	else if (d.y < 0) {
					        	AxisAngle4f angleAxis = new AxisAngle4f( (float)Math.PI/2, 1, 0, 0);
					        	q.set(angleAxis);
				        	}
				        	else { // zero's.  Just set to identity quaternion
				        		q.identity();
				        	}
				        }
				        else {
					        d.normalize();
					        Vector3f up = new Vector3f(0, 1, 0);
					        Vector3f s = new Vector3f();
					        d.cross(up, s);
					        s.normalize();
					        Vector3f u = new Vector3f();
					        d.cross(s, u);
					        u.normalize();
					        Matrix4f matrix = new Matrix4f(
					        		s.x, s.y, s.z, 0,
					        		u.x, u.y, u.z, 0,
					        		d.x, d.y, d.z, 0,
					        		0, 0, 0, 1 );
					        q.setFromNormalized(matrix);
				        }
				        
				        // set position and direction in the SpotLight's GVRScene
				        GVRTransform newSpotLightSceneObjTransform = newSpotLightSceneObj.getTransform();
				        newSpotLightSceneObjTransform.setPosition(location[0], location[1], location[2]);
				        newSpotLightSceneObjTransform.setRotation(q.w, q.x, q.y, q.z);
				        newSpotLightSceneObj.setName(name);
				    }  // end if UNIVERSAL_LIGHTS
		      }

		      /********** TimeSensor **********/
		      else if (qName.equalsIgnoreCase("TimeSensor")) {
			    String name = null;
		    	float[] cycleInterval = {1};
		    	boolean enabled = true;
		    	boolean loop = false;
		    	float[] pauseTime = {0};
		    	float[] resumeTime = {0};
		    	float[] startTime = {0};
		    	float[] stopTime = {0};
		    	
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	name = attributeValue;
		        }
		        attributeValue = attributes.getValue("cycleInterval");
		        if (attributeValue != null) {
		        	cycleInterval = parseFixedLengthFloatString(attributeValue, 1, false, true);
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
		        	pauseTime = parseFixedLengthFloatString(attributeValue, 1, false, true);
		        }
		        attributeValue = attributes.getValue("resumeTime");
		        if (attributeValue != null) {
		        	resumeTime = parseFixedLengthFloatString(attributeValue, 1, false, true);
		        }
		        attributeValue = attributes.getValue("startTime");
		        if (attributeValue != null) {
		        	startTime = parseFixedLengthFloatString(attributeValue, 1, false, true);
		        }
		        attributeValue = attributes.getValue("stopTime");
		        if (attributeValue != null) {
		        	stopTime = parseFixedLengthFloatString(attributeValue, 1, false, true);
		        }
		        
		        TimeSensor newTimeSensor = new TimeSensor(name, cycleInterval[0], enabled,
		        		loop, pauseTime[0], resumeTime[0], startTime[0], stopTime[0]);
		        timeSensors.add(newTimeSensor);
		      }
		      
		      /********** ROUTE **********/
		      else if (qName.equalsIgnoreCase("ROUTE")) {
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

		         Interpolator routeToInterpolator = null;
		         Interpolator routeFromInterpolator = null;		         
		         for (int j = 0; j < interpolators.size(); j++) {
	    			  Interpolator interpolator = interpolators.get(j);
	    			  if (interpolator.name.equalsIgnoreCase(toNode) ) {
	    				  routeToInterpolator = interpolator;
	    			  }
	    			  else if (interpolator.name.equalsIgnoreCase(fromNode) ) {
	    				  routeFromInterpolator = interpolator;
	    			  }
	    		  }
		         if ( (routeToInterpolator != null) || (routeFromInterpolator != null) ) {
				    RouteAnimation newRoute = new RouteAnimation(fromNode, fromField, toNode, toField);
				    routeAnimations.add(newRoute);		        	 
		         }
		         else {
		        	 Sensor routeSensor = null;
		        	 for (int j = 0; j < sensors.size(); j++){
		        		 Sensor sensor = sensors.get(j);
		        		 if ( sensor.name.equalsIgnoreCase(fromNode)) {
		        			 routeSensor = sensor; 
		        		 }
		        	 }
		        	 if (routeSensor != null ) {
		        		 RouteSensor newRoute = new RouteSensor(fromNode, fromField, toNode, toField);
						 routeSensors.add(newRoute);		        	 
		        	 }
		         }        
		      }  //  end ROUTES
		      
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
					parseNumbersString(attributeValue, X3Dobject.interpolatorKeyComponent, 1);

			        keysList = new float[keys.size()] ;
			        for (int i = 0; i < keysList.length; i++) {
			        	  Key keyObject = keys.get(i);
			        	  keysList[i] = keyObject.key;
			         }
			        keys.clear();
		        }
		        attributeValue = attributes.getValue("keyValue");
		        if (attributeValue != null) {
					parseNumbersString(attributeValue, X3Dobject.interpolatorKeyValueComponent, 3);

			        keyValuesList = new float[keyValues.size() * 3] ;
			        for (int i = 0; i < keyValues.size(); i++) {
			        	KeyValue keyValueObject = keyValues.get(i);
			        	for (int j = 0; j < 3; j++) {
			        		keyValuesList[i*3+j] = keyValueObject.keyValues[j];			        		
			        	}
			         }
			        keyValues.clear();
		        }
		        Interpolator newInterporlator = new Interpolator(name, keysList, keyValuesList);
		        interpolators.add(newInterporlator);
		      }

		      
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
					parseNumbersString(attributeValue, X3Dobject.interpolatorKeyComponent, 1);

			        keysList = new float[keys.size()] ;
			        for (int i = 0; i < keysList.length; i++) {
			        	  Key keyObject = keys.get(i);
			        	  keysList[i] = keyObject.key;
			         }
			        keys.clear();
		        }
		        attributeValue = attributes.getValue("keyValue");
		        if (attributeValue != null) {
					parseNumbersString(attributeValue, X3Dobject.interpolatorKeyValueComponent, 4);

			        keyValuesList = new float[keyValues.size() * 4] ;
			        for (int i = 0; i < keyValues.size(); i++) {
			        	KeyValue keyValueObject = keyValues.get(i);
			        	for (int j = 0; j < 4; j++) {
			        		keyValuesList[i*4+j] = keyValueObject.keyValues[j];			        		
			        	}
			         }
			        keyValues.clear();
		        }
		        Interpolator newInterporlator = new Interpolator(name, keysList, keyValuesList);
		        interpolators.add(newInterporlator);
		      }

		      
		      /********** Box **********/
		      else if (qName.equalsIgnoreCase("Box")) {
				float[] size = {2, 2, 2};
				boolean solid = true; // cone visible from inside
				
		        attributeValue = attributes.getValue("size");
		        if (attributeValue != null) {
		        	size = parseFixedLengthFloatString(attributeValue, 3, false, true);
		        }
		        attributeValue = attributes.getValue("solid");
		        if (attributeValue != null) {
		        	solid = parseBooleanString(attributeValue);
		        }
		        GVRCubeSceneObject gvrCubeSceneObject = new GVRCubeSceneObject(gvrContext);
		        currentSceneObject.addChildObject(gvrCubeSceneObject);
		        meshAttachedSceneObject = gvrCubeSceneObject;
		        System.out.println("<" + qName + "> NOT IMPLEMENTED YET");
		        //BoxVertices
		        
				//this.AddVertex( (int)(f[0]*Integer16bit), (int)(f[1]*Integer16bit), (int)(f[2]*Integer16bit) );
		        Log.d(TAG, "Box setup:"); 
		        Log.d(TAG, "   Box.BoxVertices.length = " + Box.BoxVertices.length + ", Box.BoxIndexedFaceSet.length = " + Box.BoxIndexedFaceSet.length); 
		        Log.d(TAG, "   Box.BoxVertexNormals.length = " + Box.BoxVertexNormals.length + ", Box.BoxVertexNormalIndex.length = " + Box.BoxVertexNormalIndex.length); 
		        Log.d(TAG, "   Box.TextureCoordinates.length = " + Box.TextureCoordinates.length + ", Box.BoxTextureCoordinateIndex.length = " + Box.BoxTextureCoordinateIndex.length); 
		        for (int i = 0; i < Box.BoxVertices.length/3; i++) {
					AddVertex( (int)(Box.BoxVertices[i*3]*Integer16bit), (int)(Box.BoxVertices[i*3+1]*Integer16bit), (int)(Box.BoxVertices[i*3+2]*Integer16bit) );
		        }
		        for (int i = 0; i < Box.BoxIndexedFaceSet.length/3; i++) {
		        	AddIndexedFaceSet( Box.BoxIndexedFaceSet[i*3], Box.BoxIndexedFaceSet[i*3+1], Box.BoxIndexedFaceSet[i*3+2] );
		        }
		        for (int i = 0; i < Box.BoxVertexNormals.length/3; i++) {
		        	AddVertexNormal( Box.BoxVertexNormals[i*3], Box.BoxVertexNormals[i*3+1], Box.BoxVertexNormals[i*3+2] );
		        }
		        for (int i = 0; i < Box.TextureCoordinates.length/2; i++) {
		        	AddTextureCoord( Box.TextureCoordinates[i*2], Box.TextureCoordinates[i*2+1] );
		        }
		        for (int i = 0; i < Box.BoxVertexNormalIndex.length/3; i++) {
		        	AddIndexedVertexNormals( Box.BoxVertexNormalIndex[i*3], Box.BoxVertexNormalIndex[i*3+1], Box.BoxVertexNormalIndex[i*3+2] );
		        }
		        for (int i = 0; i < Box.BoxTextureCoordinateIndex.length/3; i++) {
		        	AddTextureCoordinateSet( Box.BoxTextureCoordinateIndex[i*3], Box.BoxTextureCoordinateIndex[i*3+1], Box.BoxTextureCoordinateIndex[i*3+2] );
		        }

		      } // end Box
		      
		      /********** Cone **********/
		      else if (qName.equalsIgnoreCase("Cone")) {
				boolean bottom = true;
				float[] bottomradius = {1};
				float[] height = {2};
				boolean side = true;
				boolean solid = true; // cone visible from inside
				
		        attributeValue = attributes.getValue("bottom");
		        if (attributeValue != null) {
		        	bottom = parseBooleanString(attributeValue);
		        }
		        attributeValue = attributes.getValue("bottomradius");
		        if (attributeValue != null) {
		        	bottomradius = parseFixedLengthFloatString(attributeValue, 1, false, true);
		        }
		        attributeValue = attributes.getValue("height");
		        if (attributeValue != null) {
		        	height = parseFixedLengthFloatString(attributeValue, 1, false, true);
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
		        params.BottomRadius = bottomradius[0];
		        params.TopRadius = 0;
		        params.Height = height[0];
		        params.FacingOut = true;
		        params.HasTopCap = false;
		        params.HasBottomCap = bottom;
		        GVRCylinderSceneObject cone = new GVRCylinderSceneObject(gvrContext, params);

		        currentSceneObject.addChildObject(cone);
		        meshAttachedSceneObject = cone;
		      }
		      
		      /********** Cylinder **********/
		      else if (qName.equalsIgnoreCase("Cylinder")) {
				boolean bottom = true;
				float[] height = {2};
				float[] radius = {1};
				boolean side = true;
				boolean solid = true; // cylinder visible from inside
				boolean top = true;
				
		        attributeValue = attributes.getValue("bottom");
		        if (attributeValue != null) {
		        	bottom = parseBooleanString(attributeValue);
		        }
		        attributeValue = attributes.getValue("height");
		        if (attributeValue != null) {
		        	height = parseFixedLengthFloatString(attributeValue, 1, false, true);
		        }
		        attributeValue = attributes.getValue("radius");
		        if (attributeValue != null) {
		        	radius = parseFixedLengthFloatString(attributeValue, 1, false, true);
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
		        params.BottomRadius = radius[0];
		        params.TopRadius = radius[0];
		        params.Height = height[0];
		        params.HasBottomCap = bottom;
		        params.HasTopCap = top;
		        params.FacingOut = true;
		        GVRCylinderSceneObject gvrCylinderSceneObject = new GVRCylinderSceneObject(gvrContext, params);
		        currentSceneObject.addChildObject(gvrCylinderSceneObject);
		        meshAttachedSceneObject = gvrCylinderSceneObject;
		      }
		      
		      /********** Sphere **********/
		      else if (qName.equalsIgnoreCase("Sphere")) {
					float[] radius = {1};
					boolean solid = true; // cylinder visible from inside
			        attributeValue = attributes.getValue("radius");
			        if (attributeValue != null) {
			        	radius = parseFixedLengthFloatString(attributeValue, 1, false, true);
			        }
			        attributeValue = attributes.getValue("solid");
			        if (attributeValue != null) {
			        	solid = parseBooleanString(attributeValue);
			        }
			        GVRSphereSceneObject gvrSphereSceneObject = new GVRSphereSceneObject(gvrContext);
			        currentSceneObject.addChildObject(gvrSphereSceneObject);
			        meshAttachedSceneObject = gvrSphereSceneObject;
		        System.out.println("<" + qName + ">  NOT IMPLEMENTED YET");
		      }
		      
		      /********* Infrequent commands and thus moved to end of if-then-else.
		         May not be used, often just once or rarely a few times in a file ********/
		      /********** Viewpoint **********/
		      else if (qName.equalsIgnoreCase("Viewpoint")) {
				float[] centerOfRotation = {0, 0, 0};
				String description = "";
				float[] fieldOfView = {(float) Math.PI / 4};
				boolean jump = true;
				String name = "";
				float[] orientation = {0, 0, 1, 0};
				float[] position = {0, 0, 10};
				boolean retainUserOffsets = false;
				
				attributeValue = attributes.getValue("DEF");
			    if (attributeValue != null) {
			        name = attributeValue;
			    }
		        attributeValue = attributes.getValue("centerOfRotation");
		        if (attributeValue != null) {
		        	centerOfRotation = parseFixedLengthFloatString(attributeValue, 3, false, false);
		        }
		        attributeValue = attributes.getValue("description");
		        if (attributeValue != null) {
		        	description = attributeValue;
		        }
		        attributeValue = attributes.getValue("fieldOfView");
		        if (attributeValue != null) {
		        	fieldOfView = parseFixedLengthFloatString(attributeValue, 1, false, true);
		        	if ( fieldOfView[0] > (float) Math.PI ) fieldOfView[0] = (float) Math.PI;
		        }
		        attributeValue = attributes.getValue("jump");
		        if (attributeValue != null) {
		        	jump = parseBooleanString(attributeValue);
		        }
		        attributeValue = attributes.getValue("orientation");
		        if (attributeValue != null) {
		        	orientation = parseFixedLengthFloatString(attributeValue, 4, false, false);
		        }
		        attributeValue = attributes.getValue("position");
		        if (attributeValue != null) {
		        	position = parseFixedLengthFloatString(attributeValue, 3, false, false);
		        }
		        attributeValue = attributes.getValue("retainUserOffsets");
		        if (attributeValue != null) {
		        	System.out.print("retainUserOffsets='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        }
		        // Add viewpoint to the list.
		        // Since viewpoints can be under a Transform, save the parent.
		        Viewpoint viewpoint = new Viewpoint(centerOfRotation, description, fieldOfView[0], 
		      		  jump, name, orientation, position, retainUserOffsets, currentSceneObject);
		        viewpoints.add(viewpoint);
		      } // end Viewpoint

		      
		      
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
			    //gvrTextViewSceneObject.setText("Hello X3D");
			    String text = "";
			    if ( mfStrings != null ) {
				    for (int i = 0; i < mfStrings.length; i++) {
				    	if (i > 0) text += " ";
				    	text += mfStrings[i];
				    }
			    }
			    gvrTextViewSceneObject.setText(text);

				 Matrix4f matrix4f = currentSceneObject.getTransform().getModelMatrix4f();
				 
				 gvrTextViewSceneObject.setTextColor(Color.WHITE); // default
				 gvrTextViewSceneObject.setBackgroundColor(Color.TRANSPARENT);  // default
				 
				 currentSceneObject.addChildObject(gvrTextViewSceneObject);
		      }
		      
		      /********** FontStyle **********/
		      else if (qName.equalsIgnoreCase("FontStyle")) {
				String name = "";
				String[] family = {"SERIF"};
				boolean horizontal = true;
				String[] justify = {"BEGIN"};  // BEGIN, END, FIRST, MIDDLE
				String language = "";
				boolean leftToRight = true;
				float[] size = {1.0f};
				float[] spacing = {1.0f};
				String[] style = {"PLAIN"};  // PLAIN | BOLD | ITALIC | BOLDITALIC
				boolean topToBottom = true;
				

				attributeValue = attributes.getValue("DEF");
			    if (attributeValue != null) {
			        name = attributeValue;
			    }
				attributeValue = attributes.getValue("family");
			    if (attributeValue != null) {
			    	family = parseMFString(attributeValue);
			    }
				attributeValue = attributes.getValue("justify");
			    if (attributeValue != null) {
			    	justify = parseMFString(attributeValue);
			    }
				attributeValue = attributes.getValue("language");
			    if (attributeValue != null) {
			    	language = attributeValue;
			    }
				attributeValue = attributes.getValue("leftToRight");
			    if (attributeValue != null) {
			    	leftToRight = parseBooleanString(attributeValue);
			    }
				attributeValue = attributes.getValue("size");
			    if (attributeValue != null) {
				    size = parseFixedLengthFloatString(attributeValue, 1, false, true);
			    }
				attributeValue = attributes.getValue("spacing");
			    if (attributeValue != null) {
			    	spacing = parseFixedLengthFloatString(attributeValue, 1, false, true);
			    }
				attributeValue = attributes.getValue("style");
			    if (attributeValue != null) {
			    	style = parseMFString(attributeValue);
			    }
				attributeValue = attributes.getValue("topToBottom");
			    if (attributeValue != null) {
			    	topToBottom = parseBooleanString(attributeValue);
			    }
			    
			    float textSize = gvrTextViewSceneObject.getTextSize();
			    int gravity = gvrTextViewSceneObject.getGravity();
			    gvrTextViewSceneObject.setTextSize(size[0]*10);
		      }  // end FonstStyle
		      
		      /********** Billboard **********/
		      else if (qName.equalsIgnoreCase("Billboard")) {
		    	  	String name = "";
		    	  	float[] axisOrRotation = {0, 1, 0};
					attributeValue = attributes.getValue("DEF");
				    if (attributeValue != null) {
				        name = attributeValue;
				    }
			        attributeValue = attributes.getValue("axisOrRotation");
			        if (attributeValue != null) {
			        	//System.out.print("version='" + attributeValue + "'");
			        	axisOrRotation = parseFixedLengthFloatString(attributeValue, 3, true, false);
			        }
			   }
		      
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
			        	GVRSceneObject inlineGVRSceneObject = currentSceneObject;  // preserve the currentSceneObject
				        if (lodManager.isActive()) {
				        	 inlineGVRSceneObject = AddGVRSceneObject();
				        	 inlineGVRSceneObject.setName("inlineGVRSceneObject" + lodManager.getCurrentRangeIndex());
				        	 inlineGVRSceneObject.setLODRange(lodManager.getMinRange(), lodManager.getMaxRange());
				        	 lodManager.increment();
					    }
			        	InlineObject inlineObject = new InlineObject(inlineGVRSceneObject, url);
			        	inlineObjects.add(inlineObject);
			        }
			   }
		      
		      /********** LOD **********/
		      else if (qName.equalsIgnoreCase("LOD")) {
		    	  	String name = "";
		    	  	float[] center = {0, 0, 0};
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
				        	  range[i+1] = keyObject.key;
				         }
				    	range[range.length-1] = Float.MAX_VALUE;
				        keys.clear();
			        }
			        lodManager.set( range, center );
			   }
		      
		      
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
		    		  //url = parseMFString(attributeValue); // issues with parsing multiple strings with special chars
		    		  url = attributeValue;
		    	  }
		    	  // Set the currentSensor pointer so that child objects will be added 
		    	  // to the list of eye pointer objects.
		    	  currentSceneObject = AddGVRSceneObject();
		    	  currentSceneObject.setName(name);
		    	  Sensor sensor = new Sensor(name, Sensor.Type.ANCHOR, currentSceneObject);
		    	  sensor.setAnchorURL(url);
		    	  sensors.add(sensor);
		    	  currentSensor = sensor;
		    	  currentSceneObject.attachEyePointeeHolder();
		      }  //  end Anchor
		      
		      
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
		    	  Sensor sensor = new Sensor(name, Sensor.Type.TOUCH, gvrSensorSceneObject);
		    	  sensors.add(sensor);
		    	  currentSensor = sensor;
		    	  // attach any existing child objects of the parent to the new gvrSensorSceneObject
		    	  for (int i = (currentSceneObject.getChildrenCount()-1); i >= 0; i--) {
		    		  // detach the children of the parent and re-attach them to the new sensor object
		    		  GVRSceneObject childObject = currentSceneObject.getChildByIndex(i);
		    		  //childObject.attachEyePointeeHolder();
		    		  attachDefaultEyePointee(childObject);
		    		  currentSceneObject.removeChildObject(childObject);
		    		  gvrSensorSceneObject.addChildObject(childObject);
		    	  }
		    	  currentSceneObject.addChildObject(gvrSensorSceneObject);
		    	  currentSceneObject = gvrSensorSceneObject;
		    	  currentSceneObject.attachEyePointeeHolder();
		    	  /*
			    	  currentSceneObject = AddGVRSceneObject();
			    	  currentSceneObject.setName(name);
			    	  Sensor sensor = new Sensor(name, Sensor.Type.TOUCH, currentSceneObject);
			    	  sensors.add(sensor);
			    	  currentSensor = sensor;
			    	  currentSceneObject.attachEyePointeeHolder();
		    	   */
		      }  //  end TouchSensor
		      
		      
		      /********** ProximitySensor **********/
		      else if (qName.equalsIgnoreCase("ProximitySensor")) {
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
		      }

		      
		      
		      /********** ElevationGrid **********/
		      else if (qName.equalsIgnoreCase("ElevationGrid")) {
		    	  String name = "";
		    	  float[] creaseAngle = {0};
		    	  float[] height = null;
		    	  boolean solid = true;
		    	  int xDimension = 0;
		    	  float[] xSpacing = {1};
		    	  int zDimension = 0;
		    	  float[] zSpacing = {1};
		    	  
		    	  attributeValue = attributes.getValue("DEF");
				  if (attributeValue != null) {
				     name = attributeValue;
				  }
		    	  attributeValue = attributes.getValue("xDimension");
		    	  if (attributeValue != null) {
		    		  float[] xDimensionFloat = parseFixedLengthFloatString(attributeValue, 1, false, true);
		    		  xDimension = (int)xDimensionFloat[0];
		    	  }
		    	  attributeValue = attributes.getValue("xSpacing");
		    	  if (attributeValue != null) {
		    		  xSpacing = parseFixedLengthFloatString(attributeValue, 1, false, true);
		    	  }
		    	  attributeValue = attributes.getValue("zDimension");
		    	  if (attributeValue != null) {
		    		  float[] zDimensionFloat = parseFixedLengthFloatString(attributeValue, 1, false, true);
		    		  zDimension = (int)zDimensionFloat[0];
		    	  }
		    	  attributeValue = attributes.getValue("zSpacing");
		    	  if (attributeValue != null) {
		    		  zSpacing = parseFixedLengthFloatString(attributeValue, 1, false, true);
		    	  }
		    	  attributeValue = attributes.getValue("height");
		    	  if (attributeValue != null) {
					parseNumbersString(attributeValue, X3Dobject.elevationGridHeight, xDimension*zDimension);
					height = new float[xDimension*zDimension];
					for (int i = 0; i < height.length; i++) {
						height[i] = floatArray.get(i);
					}
					floatArray.clear();
		    	  }
		    	  
		    	  if (height != null) {
		    		  
			          float[] vertices = new float[height.length*3]; 
			          
			          for (int i = 0; i < xDimension; i++) {
			        	  for (int j = 0; j < zDimension; j++) {
			        		  vertices[i*3+j*xDimension] = (i * xSpacing[0]);   // vertex x value
			        		  vertices[i*3+j*xDimension + 1] = (height[i+j*xDimension]);  // vertex y value
			        		  vertices[i*3+j*xDimension + 2] = (j * zSpacing[0]);   // vertex z value
			        	  }
			          }
			          char[] ifs = new char[(xDimension-1)*(zDimension-1)*6];  // dimensions * 2 polygons per 4 vertices * 3 for x,y,z vertices per polygon to create a face.
			          float[] polygonNormals = new float[xDimension*zDimension*2];
			          // NOT COMPLETED
			       	  //gvrMesh = new GVRMesh(gvrContext);
			          //gvrMesh.setIndices(ifs);

		    	  }


		      } //  end ElevationGrid

		      
		      /********** Navigation Info **********/
		      else if (qName.equalsIgnoreCase("NavigationInfo")) {
		    	String name = "";
			    float[] avatarSize = {0.25f, 1.6f, 0.75f};
		    	boolean headlight = true;
		    	float[] speed = {1.0f};
		    	float[] transitionTime = {1.0f};
		    	float[] visibilityLimit = {0.0f};
		    	
		    	attributeValue = attributes.getValue("DEF");
				if (attributeValue != null) {
				   name = attributeValue;
				}
		        attributeValue = attributes.getValue("avatarSize");
		        if (attributeValue != null) {
		        	System.out.print("avatarSize='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        	avatarSize = parseFixedLengthFloatString(attributeValue, 3, false, true);
		        }
		        attributeValue = attributes.getValue("headlight");
		        if (attributeValue != null) {
		        	headlight = parseBooleanString(attributeValue);
		        }
				attributeValue = attributes.getValue("speed");
			    if (attributeValue != null) {
			    	speed = parseFixedLengthFloatString(attributeValue, 1, false, true);
			    }
				attributeValue = attributes.getValue("transitionTime");
			    if (attributeValue != null) {
		        	System.out.print("transitionTime='" + attributeValue + "'  NOT IMPLEMENTED YET");
			    	transitionTime = parseFixedLengthFloatString(attributeValue, 1, false, true);
			    }
		        attributeValue = attributes.getValue("type");
		        if (attributeValue != null) {
		        	System.out.print("type='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        }
		        attributeValue = attributes.getValue("visibilityLimit");
		        if (attributeValue != null) {
		        	System.out.print("visibilityLimit='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        	visibilityLimit = parseFixedLengthFloatString(attributeValue, 1, false, true);
		        }
		        if (headlight) {
			        GVRSceneObject headlightSceneObject = new GVRSceneObject(gvrContext);
			        GVRDirectLight headLight = new GVRDirectLight(gvrContext);
			        headlightSceneObject.attachLight(headLight);
			        headLight.setDiffuseIntensity(1, 1, 1, 1);

		        	cameraRigAtRoot.addChildObject(headlightSceneObject);
		        }
		      }
		      

		      /********** Background **********/
		      else if (qName.equalsIgnoreCase("Background")) {
		    	float[] skycolor = {0, 0, 0};
		    	String[] backUrl = {};
		    	String[] bottomUrl = {};
		    	String[] frontUrl = {};
		    	String[] leftUrl = {};
		    	String[] rightUrl = {};
		    	String[] topUrl = {};
		    	float[] transparency = {0};
		    	float[] groundAngle = {0};
		    	  
		    	attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	System.out.print("DEF='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        }
		        attributeValue = attributes.getValue("groundColor");
		        if (attributeValue != null) {
		        	System.out.print("groundColor='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        }
		        attributeValue = attributes.getValue("skyColor");
		        if (attributeValue != null) {
		        	skycolor = parseFixedLengthFloatString(attributeValue, 3, true, false);
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
		        	System.out.print("Background transparency='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        	transparency = parseFixedLengthFloatString(attributeValue, 1, true, false);
		        }
		        attributeValue = attributes.getValue("groundAngle");
		        if (attributeValue != null) {
		        	System.out.print("Background groundAngle='" + attributeValue + "'  NOT IMPLEMENTED YET");
		        	groundAngle = parseFixedLengthFloatString(attributeValue, 1, false, true);
		        	if ( groundAngle[0] > (float) Math.PI/2 ) groundAngle[0] = (float) Math.PI/2;
		        }
		        
		        // if url's defined, use cube mapping for the background
		        if ((backUrl.length > 0) && (bottomUrl.length > 0) && (frontUrl.length > 0) && 
		        		(leftUrl.length > 0) && (rightUrl.length > 0) && (topUrl.length > 0) ) {

			        ArrayList<Future<GVRTexture>> futureTextureList = new ArrayList<Future<GVRTexture>>(6);
			        
			        String urlAttribute = backUrl[0].substring(0, backUrl[0].indexOf("."));
			        int assetID = activityContext.getResources().getIdentifier(urlAttribute, "drawable", activityContext.getPackageName());
			        if (assetID != 0) {
			        	futureTextureList.add(gvrContext.loadFutureTexture(  new GVRAndroidResource(gvrContext, assetID)));
			        }

			        urlAttribute = rightUrl[0].substring(0, rightUrl[0].indexOf("."));
			        assetID = activityContext.getResources().getIdentifier(urlAttribute, "drawable", activityContext.getPackageName());
			        if (assetID != 0) {
			        	futureTextureList.add(gvrContext.loadFutureTexture(  new GVRAndroidResource(gvrContext, assetID)));
			        }
			        
			        urlAttribute = frontUrl[0].substring(0, frontUrl[0].indexOf("."));
			        assetID = activityContext.getResources().getIdentifier(urlAttribute, "drawable", activityContext.getPackageName());
			        if (assetID != 0) {
			        	futureTextureList.add(gvrContext.loadFutureTexture(  new GVRAndroidResource(gvrContext, assetID)));
			        }
			        
			        urlAttribute = leftUrl[0].substring(0, leftUrl[0].indexOf("."));
			        assetID = activityContext.getResources().getIdentifier(urlAttribute, "drawable", activityContext.getPackageName());
			        if (assetID != 0) {
			        	futureTextureList.add(gvrContext.loadFutureTexture(  new GVRAndroidResource(gvrContext, assetID)));
			        }
			        
			        urlAttribute = topUrl[0].substring(0, topUrl[0].indexOf("."));
			        assetID = activityContext.getResources().getIdentifier(urlAttribute, "drawable", activityContext.getPackageName());
			        if (assetID != 0) {
			        	futureTextureList.add(gvrContext.loadFutureTexture(  new GVRAndroidResource(gvrContext, assetID)));
			        }
			        
			        urlAttribute = bottomUrl[0].substring(0, bottomUrl[0].indexOf("."));
			        assetID = activityContext.getResources().getIdentifier(urlAttribute, "drawable", activityContext.getPackageName());
			        if (assetID != 0) {
			        	futureTextureList.add(gvrContext.loadFutureTexture(  new GVRAndroidResource(gvrContext, assetID)));
			        }

			        GVRCubeSceneObject mCubeEvironment = new GVRCubeSceneObject(
			                    gvrContext, false, futureTextureList);
			        mCubeEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
			                    CUBE_WIDTH);

			        root.addChildObject(mCubeEvironment);
		        }
		        else {
		        	// Not cubemapping, then set default skyColor
		        	cameraRigAtRoot.getLeftCamera().setBackgroundColor(skycolor[0], skycolor[1], skycolor[2], 1);
		        	cameraRigAtRoot.getRightCamera().setBackgroundColor(skycolor[0], skycolor[1], skycolor[2], 1);
		        }
		      } // end Background

		      /********* These are once per file commands and thus moved to the
		         end of the if-then-else statement ********/
		      /********** X3D **********/
		      else if (qName.equalsIgnoreCase("x3d")) {
		        //System.out.print("<" + qName + " ");
		        attributeValue = attributes.getValue("version");
		        if (attributeValue != null) {
		        	//System.out.print("version='" + attributeValue + "'");
		        }
		        attributeValue = attributes.getValue("profile");
		        if (attributeValue != null) {
		        	//System.out.print(" profile='" + attributeValue + "' ");
		        }
		        //System.out.println(">");
		      }
		     /********** Scene **********/
		      else if (qName.equalsIgnoreCase("scene")) {
		        ;
		      }
		      else System.out.println("X3D node " + qName + " not implemented.");
	   }

	   @Override
	   public void endElement(String uri,
	      String localName, String qName) throws SAXException {
	      if (qName.equalsIgnoreCase("Transform")) {
	    	  if (currentSensor != null) {
	    		  attachDefaultEyePointee(currentSceneObject);
	    	  }

    		  if (currentSceneObject.getParent() == root) currentSceneObject = null;
    		  else {
    	    	  String name = currentSceneObject.getName();
    			  currentSceneObject = currentSceneObject.getParent();
    			  while ( currentSceneObject.getName().equals(name + TRANSFORM_ROTATION_) ||
    					  currentSceneObject.getName().equals(name + TRANSFORM_TRANSLATION_) ||
    					  currentSceneObject.getName().equals(name + TRANSFORM_SCALE_) ||
    					  currentSceneObject.getName().equals(name + TRANSFORM_CENTER_) ||
    					  currentSceneObject.getName().equals(name + TRANSFORM_NEGATIVE_CENTER_) ||
    					  currentSceneObject.getName().equals(name + TRANSFORM_SCALE_ORIENTATION_) ||
    					  currentSceneObject.getName().equals(name + TRANSFORM_NEGATIVE_SCALE_ORIENTATION_)
    					) {
    				  currentSceneObject = currentSceneObject.getParent();
    			  }
    		  }
	      }
	      else if (qName.equalsIgnoreCase("Group")) {
    		  //if (currentSceneObject.getParent() == null) currentSceneObject = null;
    		  if (currentSceneObject.getParent() == root) currentSceneObject = null;
    		  else currentSceneObject = currentSceneObject.getParent();
	      }
	      else if (qName.equalsIgnoreCase("Shape")) {
	    	  	// Shape containts Text
	    	  	if (gvrTextViewSceneObject != null ) {
				    gvrTextViewSceneObject.setTextColor( (((0xFF << 8) + (int)(shaderSettings.diffuseColor[0]*255) << 8) + (int)(shaderSettings.diffuseColor[1]*255) << 8) + (int)(shaderSettings.diffuseColor[2]*255)); 
		    	  	gvrTextViewSceneObject = null;    	  		
	    	  	}

	    	  	if (!UNIVERSAL_LIGHTS) {
			         mX3DTandLShaderTest.appendFragmentShaderLights(shaderSettings.fragmentShaderLights);
			         mX3DTandLShaderTest.setCustomShader();
			          
			         gvrMaterial = new GVRMaterial(gvrContext, mX3DTandLShaderTest.getShaderId());
			         gvrMaterial.setVec2(mX3DTandLShaderTest.TEXTURECENTER_KEY, shaderSettings.textureCenter[0], shaderSettings.textureCenter[1] );
			         gvrMaterial.setVec2(mX3DTandLShaderTest.TEXTURESCALE_KEY, shaderSettings.textureScale[0], shaderSettings.textureScale[1] );
			         gvrMaterial.setFloat(mX3DTandLShaderTest.TEXTUREROTATION_KEY, shaderSettings.textureRotation);
			         gvrMaterial.setVec2(mX3DTandLShaderTest.TEXTURETRANSLATION_KEY, shaderSettings.textureTranslation[0], shaderSettings.textureTranslation[1] );
	
			         gvrMaterial.setVec3(mX3DTandLShaderTest.DIFFUSECOLOR_KEY, shaderSettings.diffuseColor[0], shaderSettings.diffuseColor[1], shaderSettings.diffuseColor[2] );
			         gvrMaterial.setVec3(mX3DTandLShaderTest.EMISSIVECOLOR_KEY, shaderSettings.emissiveColor[0], shaderSettings.emissiveColor[1], shaderSettings.emissiveColor[2] );
			         gvrMaterial.setVec3(mX3DTandLShaderTest.SPECULARCOLOR_KEY, shaderSettings.specularColor[0], shaderSettings.specularColor[1], shaderSettings.specularColor[2] );
			         gvrMaterial.setFloat(mX3DTandLShaderTest.SHININESS_KEY, shaderSettings.shininess);
	
			         gvrMaterial.setTexture(mX3DTandLShaderTest.TEXTURE_KEY, gvrTexture);
			         
			         float[] modelMatrix = currentSceneObject.getTransform().getModelMatrix();
			         gvrMaterial.setMat4(mX3DTandLShaderTest.MODELMATRIX_KEY, 
			        		 modelMatrix[ 0], modelMatrix[ 1], modelMatrix[ 2], 0,  
			        		 modelMatrix[ 4], modelMatrix[ 5], modelMatrix[ 6], 0,  
			        		 modelMatrix[ 8], modelMatrix[ 9], modelMatrix[10], 0,  
			        		 modelMatrix[12], modelMatrix[13], modelMatrix[14], 1  
			         );
			         
			     	//public float transparency = 0;  NOT IMPLEMENTED YET
			     	    	  
			         gvrRenderData.setMaterial(gvrMaterial);
	    	  	}  //  end !UNIVERSAL_LIGHTS
	    	  	else {
	    	  		// UNIVERSAL_LIGHTS

	    	  		if (meshAttachedSceneObject == null) {
				         gvrMaterial = new GVRMaterial(gvrContext);
		    	  		 gvrMaterial.setVec4("diffuse_color", shaderSettings.diffuseColor[0], shaderSettings.diffuseColor[1], shaderSettings.diffuseColor[2], 1.0f);
		    	  		 // X3D doesn't have an am
		    	  		 //gvrMaterial.setVec4("ambient_color", 0.0f, 0.0f, 0.0f, 1.0f);
		    	  		 //gvrMaterial.setVec4("ambient_color", 1.0f, 1.0f, 1.0f, 1.0f);
		    	  		 gvrMaterial.setVec4("specular_color", shaderSettings.specularColor[0], shaderSettings.specularColor[1], shaderSettings.specularColor[2], 0.0f);
		    	  		 gvrMaterial.setVec4("emissive_color", shaderSettings.emissiveColor[0], shaderSettings.emissiveColor[1], shaderSettings.emissiveColor[2], 0.0f);
		    	  		 gvrMaterial.setFloat("specular_exponent", shaderSettings.shininess);
		    	  		 if (gvrTexture != null) gvrMaterial.setTexture("diffuseTexture", gvrTexture);
				         gvrRenderData.setMaterial(gvrMaterial);
	    	  		}
	    	  		else {
	    	  			// This GVRSceneObject came with a GVRRenderData and GVRMaterial already attached.
	    	  			//    examples of this are primitives such as the box, cone, cylinder, sphere.
	    		        gvrRenderData = meshAttachedSceneObject.getRenderData();
	    	  			gvrRenderData.setShaderTemplate(GVRPhongShader.class);  // set the shader
	    		        GVRMaterial gvrMaterial = gvrRenderData.getMaterial();
		    	  		gvrMaterial.setVec4("diffuse_color", shaderSettings.diffuseColor[0], shaderSettings.diffuseColor[1], shaderSettings.diffuseColor[2], 1.0f);
		    	  		gvrMaterial.setVec4("specular_color", shaderSettings.specularColor[0], shaderSettings.specularColor[1], shaderSettings.specularColor[2], 0.0f);
		    	  		gvrMaterial.setVec4("emissive_color", shaderSettings.emissiveColor[0], shaderSettings.emissiveColor[1], shaderSettings.emissiveColor[2], 0.0f);
		    	  		gvrMaterial.setFloat("specular_exponent", shaderSettings.shininess);
		    	  		if (gvrTexture != null) gvrMaterial.setTexture("diffuseTexture", gvrTexture);
	    	  		}
			         gvrTexture = null;
			         // this line is also under the Appearance or Shape
			         //gvrRenderData.setShaderTemplate(GVRPhongShader.class);

			         //gvrRenderData.bindShader(scene);
	    	  	}
		         
		      //gvrRenderData.setMaterial(mMaterial);
	    	  if (meshAttachedSceneObject!= null) {
	    		  // mesh i.e. gvrRenderData, will be attached to a different GVRSceneObject
	    		  // since the original Transform created multiple GVRSceneObject's
	    //		  meshAttachedSceneObject.attachRenderData(gvrRenderData);
		    	  meshAttachedSceneObject = null;
	    	  }
	    	  else currentSceneObject.attachRenderData(gvrRenderData);
	    	  
	        if (shapeLODSceneObject != null) {
	        	//float lodmin = shapeLODSceneObject.getLODMinRange();
	        	//float lodmax = shapeLODSceneObject.getLODMaxRange();
	        	currentSceneObject = currentSceneObject.getParent();
	        	shapeLODSceneObject = null;
	        	lodManager.increment();
	        }

	    	  //gvrRenderData = null;
	      }  // end of ending Shape node
	      else if (qName.equalsIgnoreCase("Appearance")) {
	    	  /*
	    	  	if (!UNIVERSAL_LIGHTS) {
			         mX3DTandLShaderTest.appendFragmentShaderLights(shaderSettings.fragmentShaderLights);
			         mX3DTandLShaderTest.setCustomShader();
			          
			         gvrMaterial = new GVRMaterial(gvrContext, mX3DTandLShaderTest.getShaderId());
			         gvrMaterial.setVec2(mX3DTandLShaderTest.TEXTURECENTER_KEY, shaderSettings.textureCenter[0], shaderSettings.textureCenter[1] );
			         gvrMaterial.setVec2(mX3DTandLShaderTest.TEXTURESCALE_KEY, shaderSettings.textureScale[0], shaderSettings.textureScale[1] );
			         gvrMaterial.setFloat(mX3DTandLShaderTest.TEXTUREROTATION_KEY, shaderSettings.textureRotation);
			         gvrMaterial.setVec2(mX3DTandLShaderTest.TEXTURETRANSLATION_KEY, shaderSettings.textureTranslation[0], shaderSettings.textureTranslation[1] );
	
			         gvrMaterial.setVec3(mX3DTandLShaderTest.DIFFUSECOLOR_KEY, shaderSettings.diffuseColor[0], shaderSettings.diffuseColor[1], shaderSettings.diffuseColor[2] );
			         gvrMaterial.setVec3(mX3DTandLShaderTest.EMISSIVECOLOR_KEY, shaderSettings.emissiveColor[0], shaderSettings.emissiveColor[1], shaderSettings.emissiveColor[2] );
			         gvrMaterial.setVec3(mX3DTandLShaderTest.SPECULARCOLOR_KEY, shaderSettings.specularColor[0], shaderSettings.specularColor[1], shaderSettings.specularColor[2] );
			         gvrMaterial.setFloat(mX3DTandLShaderTest.SHININESS_KEY, shaderSettings.shininess);
	
			         gvrMaterial.setTexture(mX3DTandLShaderTest.TEXTURE_KEY, gvrTexture);
			         
			         float[] modelMatrix = currentSceneObject.getTransform().getModelMatrix();
			         gvrMaterial.setMat4(mX3DTandLShaderTest.MODELMATRIX_KEY, 
			        		 modelMatrix[ 0], modelMatrix[ 1], modelMatrix[ 2], 0,  
			        		 modelMatrix[ 4], modelMatrix[ 5], modelMatrix[ 6], 0,  
			        		 modelMatrix[ 8], modelMatrix[ 9], modelMatrix[10], 0,  
			        		 modelMatrix[12], modelMatrix[13], modelMatrix[14], 1  
			         );
			         
			     	//public float transparency = 0;  NOT IMPLEMENTED YET
			     		          
			     	    	  
			     		      gvrRenderData.setMaterial(gvrMaterial);
	    	  	}
	    	  	else {
	    	        //GVRMaterialShaderManager shadermanager = getGVRContext().getMaterialShaderManager();
	    	        //GVRMaterialShaderManager shadermanager = gvrContext.getMaterialShaderManager();
	    	       // mShaderTemplate = shadermanager.retrieveShaderTemplate(GVRPhongPointLight.class);
	    	        //GVRShaderTemplate shaderTemplae = shadermanager.retrieveShaderTemplate(GVRPhongLight.class);

			         //gvrMaterial = new GVRMaterial(gvrContext, mX3DTandLShaderTest.getShaderId());

			         gvrMaterial = new GVRMaterial(gvrContext);
			         gvrMaterial.setMainTexture(gvrTexture);
			         gvrMaterial.setSpecularExponent(shaderSettings.shininess);
			         //gvrMaterial.setFloat("specular_exponent", shaderSettings.shininess);
			         gvrMaterial.setSpecularColor(shaderSettings.specularColor[0], shaderSettings.specularColor[1], shaderSettings.specularColor[2], 1.0f);
			         gvrRenderData.setMaterial(gvrMaterial);
			         //gvrRenderData.bindShader(scene);
	    	  	}
		         
		      //gvrRenderData.setMaterial(mMaterial);
		       
		       */
	    	  	

	      }
	      else if (qName.equalsIgnoreCase("Material")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("ImageTexture")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("TextureTransform")) {
	    	  ;
	      }

	      else if (qName.equalsIgnoreCase("IndexedFaceSet")) {
	    	  gvrRenderData.setMesh(gvrMesh);   
		      //gvrMesh = null;
	          indexedFaceSet.clear();  // clean up this Vector<coordinates> list.
	      	  indexedVertexNormals.clear();  // clean up this  Vector<coordinates> list 
	    	  indexedTextureCoord.clear();  // clean up this Vector<textureCoordinates> ist 
	      }
	      else if (qName.equalsIgnoreCase("Coordinate")) {
	          vertices.clear();  // clean up this Vector<Vertex> list.
	      }
	      else if (qName.equalsIgnoreCase("TextureCoordinate")) {
	    	  textureCoord.clear();  // clean up this Vector<TextureValues> list.
	      }
	      else if (qName.equalsIgnoreCase("Normal")) {
		      vertexNormal.clear();  // clean up this Vector<VertexNormal> list.
	      }
	      else if (qName.equalsIgnoreCase("DirectionalLight")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("PointLight")) {
	    	  System.out.println("end point light")
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("SpotLight")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("TimeSensor")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("PositionInterpolator")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("OrientationInterpolator")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("ROUTE")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("TouchSensor")) {
	    		currentSensor = null;
	      }
	      else if (qName.equalsIgnoreCase("ProximitySensor")) {
	    		currentSensor = null;
	      }
	      else if (qName.equalsIgnoreCase("Text")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("FontStyle")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("Billboard")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("Anchor")) {
    		  if (currentSceneObject.getParent() == root) currentSceneObject = null;
    		  else currentSceneObject = currentSceneObject.getParent();
    		  currentSensor = null;
	      }
	      else if (qName.equalsIgnoreCase("Inline")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("LOD")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("Box")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("Cone")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("Cylinder")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("Sphere")) {
	    	  ;
	      }
	      
	      /********* Infrequent commands and thus moved to end of if-then-else.
	         May not be used, often just once or rarely a few times in a file ********/
	      else if (qName.equalsIgnoreCase("Viewpoint")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("NavigationInfo")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("Background")) {
	    	  ;
	      }
	      else if (qName.equalsIgnoreCase("ElevationGrid")) {
	    	  ;
	      }
	      /********* These are once per file commands and thus moved to the
	         end of the if-then-else statement ********/
	      else if (qName.equalsIgnoreCase("scene")) {
	    	  // Now that the scene is over, we can set construct the animations since we now
	    	  // have all the ROUTES, and set up either the default or an actual camera based
	    	  // on a <Viewpoint> in the scene.
	    	  
	    	  // Set up the camera / Viewpoint
	    	  //   The camera rig is indirectly attached to the root
	  		  
	  		  if ( cameraRigAtRoot != null ) {
			      GVRTransform cameraTransform = cameraRigAtRoot.getTransform();
	
		    	  if (viewpoints.isEmpty()) {
		    		  // No <Viewpoint> included in X3D file,
		    		  //    so use default viewpoint values
				      cameraTransform.setPosition(0, 0, 10);
				      AxisAngle4f axisAngle4f = new AxisAngle4f(0, 0, 1, 0);
					  Quaternionf quaternionf = new Quaternionf(axisAngle4f);
				      cameraTransform.setRotation(quaternionf.w, quaternionf.x, quaternionf.y, quaternionf.z);
		    	  }
		    	  else {
		    		  // X3D file contained a <Viewpoint> node.
		    		  //    Per spec., grab the first viewpoint from the X3D file
		    		  Viewpoint viewpoint = viewpoints.firstElement();
		    		  viewpoint.setIsBound(true);
		    		  float[] position = viewpoint.getPosition();
				      cameraTransform.setPosition(position[0], position[1], position[2]);
				      float[] orientation = viewpoint.getOrientation();
				      AxisAngle4f axisAngle4f = new AxisAngle4f(orientation[3], orientation[0], orientation[1], orientation[2]);
				      Quaternionf quaternionf = new Quaternionf(axisAngle4f);
				      float[] centerOfRotation = viewpoint.getCenterOfRotation();
				      cameraTransform.rotateWithPivot(quaternionf.w, quaternionf.x, quaternionf.y, quaternionf.z, 
				    		  centerOfRotation[0], centerOfRotation[1], centerOfRotation[2]);
					  if (viewpoint.getParent() != null) {
					      Matrix4f cameraMatrix4f = cameraTransform.getLocalModelMatrix4f();
						  Matrix4f parentMatrix4x4f = viewpoint.getParent().getTransform().getModelMatrix4f();
						  parentMatrix4x4f.mul(cameraMatrix4f);
						  cameraTransform.setModelMatrix(parentMatrix4x4f);
					  }
		    	  } // <Viewpoint> node existed
	  		  } // end setting based on new camera rig
	    	  
	    	  // Handle ROUTES
    		  TimeSensor routeTimeSensor = null;
    		  Interpolator routeToInterpolator = null;
    		  Interpolator routeFromInterpolator = null;

    		  // Implement the ROUTES involving with Animations
	    	  for (RouteAnimation route: routeAnimations) {
	    		  String fromNode = route.getRouteFromNode();
	    		  String fromField = route.getRouteFromField();
	    		  String toNode = route.getRouteToNode();
	    		  String toField = route.getRouteToField();
	    		  
	    		  // declared outside the for loop since we set the boolean 'loop' value later
    			  //TimeSensor timeSensor = null;
	    		  for (TimeSensor timeSensor: timeSensors) {
	    			  if (timeSensor.name.equalsIgnoreCase(fromNode) ) {
	    				  routeTimeSensor = timeSensor;
	    				  break;
	    			  }
	    		  }
		    	  for (Interpolator interpolator: interpolators) {

	    			  if (interpolator.name.equalsIgnoreCase(toNode) ) {
	    				  routeToInterpolator = interpolator;
	    			  }
	    			  else if (interpolator.name.equalsIgnoreCase(fromNode) ) {
	    				  routeFromInterpolator = interpolator;
	    			  }
	    		  }
	    		  GVRSceneObject gvrSceneObject = root.getSceneObjectByName(toNode);
	    		  if (gvrSceneObject != null) {
		    		  // Handle "set_translation" or "translation", "rotation" or "set_rotation", etc.
		    		  GVRAnimationChannel gvrAnimationChannel = null;
		    		  GVRKeyFrameAnimation gvrKeyFrameAnimation = null;
		    		  toField = toField.toLowerCase();
	    			  if ( (toField.endsWith("translation") || toField.endsWith("position")) ) {
	    				  GVRSceneObject gvrAnimatedTranslation = root.getSceneObjectByName( (toNode + TRANSFORM_TRANSLATION_) );

		    			  gvrAnimationChannel = new GVRAnimationChannel(
		    				  gvrAnimatedTranslation.getName(), routeToInterpolator.key.length, 0, 0,
		    				  GVRAnimationBehavior.LINEAR, GVRAnimationBehavior.LINEAR);
		
		    			  for (int j = 0; j < routeToInterpolator.key.length; j++) {
		    				  Vector3f vector3f = new Vector3f(routeFromInterpolator.keyValue[j*3], routeFromInterpolator.keyValue[j*3+1], routeFromInterpolator.keyValue[j*3+2]);
		    				  gvrAnimationChannel.setPosKeyVector(j,
		    						  routeToInterpolator.key[j] * routeTimeSensor.cycleInterval * framesPerSecond, vector3f);
		    			  }
		    			  
		    			  gvrKeyFrameAnimation = new GVRKeyFrameAnimation(gvrAnimatedTranslation.getName() + KEY_FRAME_ANIMATION + animationCount,
			    				  gvrAnimatedTranslation, routeTimeSensor.cycleInterval * framesPerSecond, framesPerSecond);
			    		  // Assists in connecting a TouchSensor to an animation
			    		  route.setGVRKeyFrameAnimation(gvrKeyFrameAnimation);
		    		  }  // end translation
		    		  else if ( toField.endsWith("rotation")) {

	    				  GVRSceneObject gvrAnimatedRotation = root.getSceneObjectByName( (toNode + TRANSFORM_ROTATION_) );

		    			  gvrAnimationChannel = new GVRAnimationChannel(
		    				  gvrAnimatedRotation.getName(), 0, routeToInterpolator.key.length, 0,
		    				  GVRAnimationBehavior.DEFAULT, GVRAnimationBehavior.DEFAULT);
		    			  
		    			  for (int j = 0; j < routeToInterpolator.key.length; j++) {
		    				  AxisAngle4f axisAngle4f = new AxisAngle4f(routeFromInterpolator.keyValue[j*4+3], routeFromInterpolator.keyValue[j*4], routeFromInterpolator.keyValue[j*4+1], routeFromInterpolator.keyValue[j*4+2]);
		    				  Quaternionf quaternionf = new Quaternionf(axisAngle4f);
		    				  gvrAnimationChannel.setRotKeyQuaternion(j,
		    						  routeToInterpolator.key[j] * routeTimeSensor.cycleInterval * framesPerSecond, 
		    						  quaternionf);
		    			  }

		    			  gvrKeyFrameAnimation = new GVRKeyFrameAnimation(gvrAnimatedRotation.getName() + KEY_FRAME_ANIMATION + animationCount,
		    					  gvrAnimatedRotation, routeTimeSensor.cycleInterval * framesPerSecond, framesPerSecond);
			    		  // Assists in connecting a TouchSensor to an animation
			    		  route.setGVRKeyFrameAnimation(gvrKeyFrameAnimation);
		    		  } // end rotation animation
	    			  
		    		  else if ( toField.endsWith("scale")) {
	    				  GVRSceneObject gvrAnimatedScale = root.getSceneObjectByName( (toNode + TRANSFORM_SCALE_) );

	    				  gvrAnimationChannel = new GVRAnimationChannel(
	    					  gvrAnimatedScale.getName(), 0, 0, routeToInterpolator.key.length,
		    				  GVRAnimationBehavior.DEFAULT, GVRAnimationBehavior.DEFAULT);
		    			  for (int j = 0; j < routeToInterpolator.key.length; j++) {
		    				  Vector3f vector3f = new Vector3f(routeFromInterpolator.keyValue[j*3], routeFromInterpolator.keyValue[j*3+1], routeFromInterpolator.keyValue[j*3+2]);
		    				  gvrAnimationChannel.setScaleKeyVector(j,
		    						  routeToInterpolator.key[j] * routeTimeSensor.cycleInterval * framesPerSecond, vector3f);
		    			  }

		    			  gvrKeyFrameAnimation = new GVRKeyFrameAnimation(gvrAnimatedScale.getName() + KEY_FRAME_ANIMATION + animationCount,
		    					  gvrAnimatedScale, routeTimeSensor.cycleInterval * framesPerSecond, framesPerSecond);
			    		  // Assists in connecting a TouchSensor to an animation
			    		  route.setGVRKeyFrameAnimation(gvrKeyFrameAnimation);
		    		  }  // end scale animation
	    			  
	    			  if (gvrAnimationChannel != null) {
		    			  gvrKeyFrameAnimation.addChannel(gvrAnimationChannel);
		    			  if (routeTimeSensor.loop) {
			    			  gvrKeyFrameAnimation.setRepeatMode(GVRRepeatMode.REPEATED);
			    			  gvrKeyFrameAnimation.setRepeatCount(-1);
		    			  }
		    			  gvrKeyFrameAnimation.prepare();
		    			  mAnimations.add( (GVRAnimation) gvrKeyFrameAnimation);	    			  
	    			  }
	    			  animationCount++;
	    		  } // end if (gvrSceneObject != null)
	    		  else {
	    			  /*
	    			   *** At this moment, camera animation is not supported.
	    			   * Issues may exist with GVRKeyFrameAnimation not 
	    			   * working inside the Frame Render loop
	    			   * But that might not be a verified assumption.
	    			  // check if the ROUTE is to a Viewpoint
	    			  Viewpoint animatedViewpoint = null;
	    			  for (int j = 0; j < viewpoints.size(); j++) {
	    				  Viewpoint viewpoint = viewpoints.get(j);
	    				  if ( toNode.equalsIgnoreCase(viewpoint.getName())) {
	    					  animatedViewpoint = viewpoint;
	    				  }
	    			  }
	    			  if (animatedViewpoint != null) {
	    				  // Insert a new GVRsceneObject between scene and camera rig
	    				  // so we can animate the scene.
	    			        gvrCameraRig = scene.getMainCameraRig();
	    				    GVRTransform gvrCameraRigTransform = gvrCameraRig.getTransform();
	    				    // set the camera rig to the origin and no rotation since the animation will control it
	    				    gvrCameraRigTransform.setPosition(0, 0, 0);
	    				    AxisAngle4f axisAngle4f = new AxisAngle4f(0, 0, 1, 0);
	    					Quaternionf quaternionf = new Quaternionf(axisAngle4f);
	    					gvrCameraRigTransform.setRotation(quaternionf.w, quaternionf.x, quaternionf.y, quaternionf.z);

	    			        GVRSceneObject gvrSceneObjectCameraOwner = gvrCameraRig.getOwnerObject();
	    					GVRSceneObject gvrAnimatedCameraSceneObject = new GVRSceneObject(gvrContext);
	    					gvrAnimatedCameraSceneObject.setName("animatedViewpointSceneObject");
	    					
	    					// test code
	    					//GVRTransform gvrAnimatedCameraTransform = gvrAnimatedCameraSceneObject.getTransform();
	    					//gvrAnimatedCameraTransform.setPosition(0, 0, -2);

	  	    			  	// ROUTE to a Viewpoint
	  		    		    GVRKeyFrameAnimation gvrKeyFrameAnimation = new GVRKeyFrameAnimation(animatedViewpoint.getName() + KEY_FRAME_ANIMATION + animationCount,
	  		    				  //animatedViewpoint, routeTimeSensor.cycleInterval * framesPerSecond, framesPerSecond);
	  		    				  gvrAnimatedCameraSceneObject, routeTimeSensor.cycleInterval * framesPerSecond, framesPerSecond);
	  		    		  
	  		    		    // Handle "set_position" or "position", "orientation" or "set_orientation", etc.
	  		    		    toField = toField.toLowerCase();
	  	    			    GVRAnimationChannel gvrAnimationChannel = null;
	  	    			    if ( toField.endsWith("translation") || toField.endsWith("position") ) {
	  		    			    gvrAnimationChannel = new GVRAnimationChannel(
	  		    					gvrAnimatedCameraSceneObject.getName(), routeToInterpolator.key.length, 0, 0,
	  		    				  GVRAnimationBehavior.DEFAULT, GVRAnimationBehavior.DEFAULT);
	  		
	  		    			    for (int j = 0; j < routeToInterpolator.key.length; j++) {
	  		    				  Vector3f vector3f = new Vector3f(routeFromInterpolator.keyValue[j*3], routeFromInterpolator.keyValue[j*3+1], routeFromInterpolator.keyValue[j*3+2]);
	  		    				  gvrAnimationChannel.setPosKeyVector(j,
	  		    						  routeToInterpolator.key[j] * routeTimeSensor.cycleInterval * framesPerSecond, vector3f);
	  		    			    }
		    			        if ( gvrSceneObjectCameraOwner.getParent() == null) {
		    					  	scene.removeSceneObject(gvrSceneObjectCameraOwner);
		    					    //scene.addSceneObject(gvrSceneObjectCameraOwner); // test
		    					    scene.addSceneObject(gvrAnimatedCameraSceneObject);
		    					    //gvrAnimatedCameraSceneObject.addChildObject(gvrSceneObjectCameraOwner);
		    			        }
		    			        else {
		    			        	System.out.println("gvrSceneObject.getParent() != null.  STILL NEED TO WORK ON THIS");
		    			        	
		    			        }
		    			        gvrAnimatedCameraSceneObject.addChildObject(gvrSceneObjectCameraOwner);
	  	    			    }  // end if translation or position
		  	    			if (gvrAnimationChannel != null) {
				    			  gvrKeyFrameAnimation.addChannel(gvrAnimationChannel);
				    			  if (timeSensor.loop) {
					    			  gvrKeyFrameAnimation.setRepeatMode(GVRRepeatMode.REPEATED);
					    			  gvrKeyFrameAnimation.setRepeatCount(-1);
				    			  }
				    			  
				    			  gvrKeyFrameAnimation.prepare();
				    			  mAnimations.add(gvrKeyFrameAnimation);	    			  
			    			}
			    			animationCount++;
			    			timeSensor = null;

	    			  }   //  end if (animatedViewpoint != null)
	    			  */
	    			  
	    		  }  // end else statement (animation not  3d mesh translation)

	    	  }  // end for-loop for animation routes
	    	  
    		  // Implement the ROUTES involving Touch Sensors, Anchors, etc.
	    	  // will need to connect the 'sensor' object to the GVRKeyFrameAnimation
	    	  // or other object(s).  This requires parsing the sensor ROUTE based on Touch Sensor
	    	  // the timer-to-interpolator route, then from the interpolator-to-3d mesh
	    	  // route so that the sensor can link to the GVRKeyFrameAnimation

	    	  // 1) get the (Touch) sensor node to the ROUTE with the touch sensor
    		  for (Sensor sensor: sensors) {
    			  if (sensor.sensorType == Sensor.Type.TOUCH) {
    				  
			    	  for (RouteSensor routeSensor : routeSensors) {
			    		  String routeSensor_fromNode = routeSensor.getRouteFromNode();
			    		  String routeSensor_fromField = routeSensor.getRouteFromField();
			    		  String routeSensor_toNode = routeSensor.getRouteToNode();
			    		  String routeSensor_toField = routeSensor.getRouteToField();
			    		  // Now match the sensor-to-timer ROUTE to-node to the from-node
			    		  // of the same name for the Timer-to-Interpolator ROUTE
		    			  if (sensor.name.equalsIgnoreCase(routeSensor_fromNode) ) {
		    				  for (RouteAnimation routeAnim1: routeAnimations) {
		    		    		  String routeAnim1_fromNode = routeAnim1.getRouteFromNode();
		    		    		  String routeAnim1_fromField = routeAnim1.getRouteFromField();
		    		    		  String routeAnim1_toNode = routeAnim1.getRouteToNode();
		    		    		  String routeAnim1_toField = routeAnim1.getRouteToField();
		    	    			  if (routeAnim1_fromNode.equalsIgnoreCase(routeSensor_toNode) ) {
		    	    	    		  // Now match the from-node of the Timer-to-Interpolator
		    	    	    		  // to the to-Node of the Interpolator-to-Object ROUTE
		    	    				  for (RouteAnimation routeAnim2: routeAnimations) {
		    	    		    		  String routeAnim2_fromNode = routeAnim2.getRouteFromNode();
		    	    		    		  String routeAnim2_fromField = routeAnim2.getRouteFromField();
		    	    		    		  String routeAnim2_toNode = routeAnim2.getRouteToNode();
		    	    		    		  String routeAnim2_toField = routeAnim2.getRouteToField();
		    	    	    			  if (routeAnim2_fromNode.equalsIgnoreCase(routeAnim1_toNode) ) {
		    	    	    	    		  // Now match the from-node of the Interpolator-to-Object
		    	    	    	    		  // to the to-Node which is the same name as the Object
		    	    	    				  sensor.setGVRKeyFrameAnimation(routeAnim2.getGVRKeyFrameAnimation());
		    	    	    			  }
		    	    				  }  //  end for routeAnim2 for loop  
		    	    			  }  //  end if routeAnim1_fromNode == routeSensor_toNode
		    				  }  //  end for-loop of routeAnim1
		    			  } // end if sensor.name == routeSensor name
		    		  }  // end for RouteSensor for-loop
    			  }  // end if sensor type = TOUCH
        		  else if (sensor.sensorType == Sensor.Type.ANCHOR) {
        			  /* all the setup was created during the parsing
        			  String url = sensor.getAnchorURL();
        			  GVRSceneObject anchorSceneObject = sensor.sensorSceneObject;
        			  for (GVRSceneObject anchorChildObject: anchorSceneObject.getChildren()) {
        				 // may not need to set anything here.	  
        			  }
        			  */
        		  }
	    	  } // end search on sensors

	      } // end </scene>
	      else if (qName.equalsIgnoreCase("x3d")) {
	    	  System.out.println("end x3d");
	      }
	   }

	    private void attachDefaultEyePointee(GVRSceneObject sceneObject) {
	        GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(
	                gvrContext);
	        GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(gvrContext,
	                sceneObject.getRenderData().getMesh());
	        eyePointeeHolder.addPointee(eyePointee);
	        sceneObject.attachEyePointeeHolder(eyePointeeHolder);
	    }
	    
	    
	   @Override
	   public void characters(char ch[],
	      int start, int length) throws SAXException {
	   }
	   
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
	          if (inlineObjects.size() != 0);{
	        	  for (int i = 0; i < inlineObjects.size(); i++) {
	        		  InlineObject inlineObject = inlineObjects.get(i);
	        		  String[] urls = inlineObject.getURL();
	        		  for (int j = 0; j < urls.length; j++) {
		        		 GVRAndroidResource gvrAndroidResource = null;
		        		 try {
					        	gvrAndroidResource = new GVRAndroidResource(gvrContext, urls[j]);
					        	inputStream = gvrAndroidResource.getStream();
					        	currentSceneObject = inlineObject.getInlineGVRSceneObject();
					        	//float low = currentSceneObject.getLODMinRange();
					        	//float high = currentSceneObject.getLODMaxRange();
				  	          	saxParser.parse(inputStream, userhandler);
		        		  }
		        		  catch (FileNotFoundException e) {
				        		System.out.println("GVRAndroidResource File Not Found Exception: " + e);
		        		  }
		        		  catch (IOException ioException) {
				        		System.out.println("GVRAndroidResource IOException url["+j+"] url " + urls[j]);
				        		System.out.println(ioException);
		        		  }
		        		  catch (Exception exception) {
				        		System.out.println("GVRAndroidResource Exception: " + exception);
		        		  }
	        		  }
	        	  }
	          }
	       } catch (Exception e) {
	          e.printStackTrace();
	       }
	       
	}  // end Parse



}
