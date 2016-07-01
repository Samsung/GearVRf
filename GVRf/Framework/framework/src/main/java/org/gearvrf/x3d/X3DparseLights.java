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

//import android.content.Context;
//import android.content.res.AssetManager;
import android.util.Log;

//import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
//import org.gearvrf.GVRMaterial;
//import org.gearvrf.GVRMesh;
//import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRModelSceneObject;
//import org.gearvrf.GVRTexture;
//import org.gearvrf.GVRTextureParameters;
//import org.gearvrf.GVRTransform;
//import org.gearvrf.GVRTextureParameters.TextureWrapType;
import org.gearvrf.x3d.x3dTandLShaderTest;
//import org.gearvrf.modelviewer.x3dTextureShader;

public class X3DparseLights {

	private static final String TAG = "X3D Parse Lights";
	
	private GVRContext gvrContext = null;
	//private Context activityContext = null;

	private GVRSceneObject root = null;
	private GVRSceneObject currentSceneObject = null;

    //private x3dTextureShader mX3DTextureShader = null;   
    private x3dTandLShaderTest mX3DTandLShaderTest = null;
    //private GVRTextureParameters gvrTextureParameters = null;
	//private GVRTexture gvrTexture = null;
	
	
	//private ShaderSettings shaderSettings = new ShaderSettings();;
	private ShaderSettings shaderSettings = null;

	
    public X3DparseLights(GVRContext gvrContext, GVRModelSceneObject root) {
    	this.gvrContext = gvrContext;
    	//this.activityContext = gvrContext.getContext();
    	//this.assetManager = activityContext.getAssets();
    	this.root = root;
    }


	public void Parse(InputStream inputStream, ShaderSettings shaderSettings) {
	      try {
		      //System.out.println("X3DparseLights BEGIN");
	    	  this.shaderSettings = shaderSettings;
	          SAXParserFactory factory = SAXParserFactory.newInstance();
	          SAXParser saxParser = factory.newSAXParser();
	          UserHandler userhandler = new UserHandler();
	          saxParser.parse(inputStream, userhandler);
		      //System.out.println("X3DparseLights END");
	       } catch (Exception e) {
	          e.printStackTrace();
	       }
	       
	}  // end Parse

	
	class UserHandler extends DefaultHandler {

		   String attributeValue = null;

		   public float[] parseFixedLengthFloatString(String numberString, int componentCount, boolean constrained0to1, boolean zeroOrGreater) {
			   StringReader sr = new StringReader(numberString);
			   StreamTokenizer st = new StreamTokenizer(sr);
			   st.parseNumbers();
			   int tokenType;
			   float componentFloat[] = new float[componentCount];
			   try {
				   for (int i = 0; i < componentCount; i++) {
					   if ((tokenType = st.nextToken()) == StreamTokenizer.TT_NUMBER) {
						   componentFloat[i] = (float) st.nval;
						   if (constrained0to1) {
							   if (componentFloat[i] < 0) componentFloat[i] = 0;
							   else if  (componentFloat[i] > 1) componentFloat[i] = 1;
						   }
						   else if (zeroOrGreater) {
							   if (componentFloat[i] < 0) componentFloat[i] = 0;
						   }
					   }
				   }
			   }
			   catch (IOException e) {
				   Log.d("Parse X3D", "Error: " + e);
			   }
			   return componentFloat;
		   }

		   public boolean parseBooleanString(String booleanString) {
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
		   
		   
	/*********** Parse the X3D File **************/
	   @Override
	   public void startElement(String uri,
	      String localName, String qName, Attributes attributes)
	         throws SAXException {

		      /********** Transform **********/
		      if (qName.equalsIgnoreCase("transform")) {
		        System.out.print("<" + qName + " ");
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	System.out.print("DEF='" + attributeValue + "' ");
		        	//currentSceneObject.setName(attributeValue);
		        }
		        String translationAttribute = attributes.getValue("translation");
		        if (translationAttribute != null) {
		        	System.out.print("translation='" + translationAttribute + "' ");
		        	float[] translation = parseFixedLengthFloatString(translationAttribute, 3, false, false);
		        	//transform.setPosition( translation[0], translation[1], translation[2] );
		        }
		        String rotationAttribute = attributes.getValue("rotation");
		        if (rotationAttribute != null) {
		        	System.out.print("rotation='" + rotationAttribute + "' ");
		        	float[] rotation = parseFixedLengthFloatString(rotationAttribute, 4, false, false);
		        	//transform.setRotationByAxis( (float)Math.toDegrees(rotation[3]), rotation[0], rotation[1], rotation[2] );
		        }
		        String centerAttribute = attributes.getValue("center");
		        if (centerAttribute != null) System.out.print("center='" + centerAttribute + "' ");
		        System.out.println(">");
		      }

		      /********** Group **********/
		      if (qName.equalsIgnoreCase("Group")) {
		        System.out.print("<" + qName + " ");
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) System.out.print("DEF='" + attributeValue + "' ");
		        System.out.println(">");
		        //indent++;
		      }

		      /********** PointLight **********/
		      if (qName.equalsIgnoreCase("PointLight")) {
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
		        	System.out.print("DEF='" + attributeValue + "'  NOT IMPLEMENTED YET");
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
		        
		        shaderSettings.appendFragmentShaderLights(
		        		" if (" + on + ") fragmentcolor += pointlight(v_position, pixelcolor, pixelnormal"
		        		+ ", vec3(" + location[0] + ", " + location[1] + ", " + location[2] + ")"
		        		+ ", vec3(" + attenuation[0] + ", " + attenuation[1] + ", " + attenuation[2] + ")"
				        + ", " + radius[0]
						+ ", " + intensity[0]
		        		+ ", vec3(" + color[0] + ", " + color[1] + ", " + color[2] + "));\n"); //
		      }
		      
		      /********** DirectionalLight **********/
		      if (qName.equalsIgnoreCase("DirectionalLight")) {
				float ambientIntensity = 0;
				float[] color = {1, 1, 1};
			    float[] direction = {0, 0, -1};
				boolean global = false;
				float[] intensity = {1};
				boolean on = true;
				
		        attributeValue = attributes.getValue("DEF");
		        if (attributeValue != null) {
		        	System.out.print("DEF='" + attributeValue + "' NOT IMPLEMENTED YET");
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
		        
		        shaderSettings.appendFragmentShaderLights(
		        		" if (" + on + ") fragmentcolor += directionallight( pixelcolor, pixelnormal"
		        		+ ", vec3(" + direction[0] + ", " + direction[1] + ", " + direction[2] + ")"
						+ ", " + intensity[0]
		        		+ ", vec3(" + color[0] + ", " + color[1] + ", " + color[2] + "));\n"); //
		      }
		      
		      /********** SpotLight **********/
		      if (qName.equalsIgnoreCase("SpotLight")) {
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
		        	System.out.print("DEF='" + attributeValue + "' NOT IMPLEMENTED YET");
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
		        
		        shaderSettings.appendFragmentShaderLights(
		        		" if (" + on + ") fragmentcolor += spotlight(v_position, pixelcolor, pixelnormal"
				        + ", vec3(" + location[0] + ", " + location[1] + ", " + location[2] + ")"
		        		+ ", vec3(" + direction[0] + ", " + direction[1] + ", " + direction[2] + ")"
		        		+ ", vec3(" + attenuation[0] + ", " + attenuation[1] + ", " + attenuation[2] + ")"
				        + ", " + radius[0]
						+ ", " + intensity[0]
						+ ", " + beamWidth[0]
				        + ", " + cutOffAngle[0]
		        		+ ", vec3(" + color[0] + ", " + color[1] + ", " + color[2] + ") );\n"); //
		      }
		      if (qName.equalsIgnoreCase("x3d")) {
				;
			  }
			  if (qName.equalsIgnoreCase("scene")) {
			    ;
			  }
	   }
	   

	   @Override
	   public void endElement(String uri,
	      String localName, String qName) throws SAXException {
	      if (qName.equalsIgnoreCase("Transform")) {
	    	  ;
	    	  // NEXT LINE PROPBABLY WONT GO HERE IN THE END.  JUST FOR INITIAL TEST
	    	  //scene.addSceneObject(currentSceneObject);
	    	  //currentSceneObject = currentSceneObject.getParent();
	      }
	      if (qName.equalsIgnoreCase("Group")) {
	    	  ;
	      }
	      if (qName.equalsIgnoreCase("DirectionalLight")) {
	    	  ;
	      }
	      if (qName.equalsIgnoreCase("PointLight")) {
	    	  ;
	      }
	      if (qName.equalsIgnoreCase("SpotLight")) {
	    	  ;
	      }
	      if (qName.equalsIgnoreCase("x3d")) {
	    	  ;
	      }
	      if (qName.equalsIgnoreCase("scene")) {
	    	  ;
	      }
	   }

	   @Override
	   public void characters(char ch[],
	      int start, int length) throws SAXException {
	   }
	   
	} // end UserHandler




}
