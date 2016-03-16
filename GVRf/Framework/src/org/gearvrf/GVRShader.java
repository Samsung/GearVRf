/* Copyright 2015 Samsung Electronics Co., LTD
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
package org.gearvrf;
      
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gearvrf.GVRLight;
import org.gearvrf.GVRShaderSegment;

/**
 * Manages a set of variants on vertex and fragment shaders from
 * the same source code.
 */
public class GVRShader 
{
	public enum ShaderType { VERTEX_TEMPLATE, FRAGMENT_TEMPLATE, VERTEX, SURFACE, LIGHT };
	private String VERTEX_SHADER_TEMPLATE = 
		    "//\n" +
    	    "// GearVRF managed constants\n" +
    	    "//\n" +
    	    "uniform mat4 WorldMatrix;\n" +
    	    "uniform mat4 ViewMatrix;\n" +
    	    "uniform mat4 ProjMatrix;\n" +
    	    "attribute vec4	position;\n" +
    	    "#ifdef $normal\n" +
    	    "    attribute vec4	normal;\n" +
    	    "#endif\n" +
    	    "#ifdef $texcoord\n" +
    	    "    attribute vec2	texcoord;\n" +
    	    "#endif\n" +
   	        "//\n" +
    	    "// Common vertex output structure\n" +
    	    "//\n" +
    	    "varying vec4 world_position;\n" +
    	    "varying vec4 world_normal;\n" +
    	    "varying vec4 texcoord0;\n" +
    	    "//\n" +
    	    "// vertex shader template\n" +
    	    "//\n" +
    	    "void main() { $VERTEXSHADER }\n";
	
	private String FRAGMENT_SHADER_TEMPLATE = 
		    "precision mediump float;\n" +
    	    "// per object constants\n" +
    	    "uniform mat4 WorldMatrix;\n" +
    	    "// per frame constants\n" +
    	    "uniform mat4	ViewMatrix;\n" +
    	    "uniform mat4	ViewProjMatrix;\n" +
    	    "uniform vec3	CameraPos;\n" +
    	    "struct Radiance\n" +
    	    "{\n" +
    	    "   vec4 intensity;\n" +
    	    "   vec4 direction;\n" +
    	    "};\n" +
    	    "vec3 AddLight(Surface s, Radiance r)\n" +
    	    "{\n" +
    	    "	vec3 L = r.direction.xyz;   // From surface towards light, unit length, world-space\n" +
    	    "	vec3 I = r.intensity.xyz;   // N . L already accounted for\n" +
    	    "	vec3 campos = CameraPos;\n" +
    	    "\n" +
    	    "	// Vector from surface location to the camera's eye\n" +
    	    "	vec3 E = normalize(campos - s.position.xyz);\n" +
    	    "	vec3 H = normalize(L + E);\n" +
    	    "	vec3 kD = s.diffuse.xyz * I;\n" +
    	    "	//vec3 kS = s.specular.xyz * I * pow(max(dot(s.normal.xyz, H), 0.0), s.specular.w);\n" +
    	    "	vec3 kS = s.specular.xyz * I * pow(max(dot(s.normal.xyz, H), 0.0), 5.0);\n" +
    	    "	return kD + kS;\n" +
    	    "}\n" +
    	    "@SURFACESHADER\n" +
    	    "@LIGHTSOURCES\n" +
    	    "void main()\n" +
    	    "{\n" +
    	    "	Surface s;\n" +
    	    "	vec3 color = vec3(0.0, 0.0, 0.0);\n" +
    	    "\n" +
    	    "	s = @SHADERNAME();\n" +
    	    "	color = LightPixel(s);\n" +
    	    "	gl_FragColor = vec4(color, 1.0);\n" +
    	    "}\n";
	
	public GVRShader(String name)
	{
		GVRShaderSegment segment = new GVRShaderSegment("FragmentTemplate");
		mName = name;
		mShaderSegments = new GVRShaderSegment[5];
		segment.setShaderCode(FRAGMENT_SHADER_TEMPLATE);
		mShaderSegments[ShaderType.FRAGMENT_TEMPLATE.ordinal()] = segment;
		segment = new GVRShaderSegment("VertexTemplate");
		segment.setShaderCode(VERTEX_SHADER_TEMPLATE);
		mShaderSegments[ShaderType.VERTEX_TEMPLATE.ordinal()] = segment;
	}
	
	/**
	 * Gets the names of all defines in the shader source that may
	 * be substituted to create a new variant.
	 * These are all the names that being with "$" in the source.
	 * @return names representing defines
	 */
	public Set<String> getDefines()
	{
		Set<String> allVariants = new HashSet<String>();
		GVRShaderSegment segment = mShaderSegments[ShaderType.VERTEX.ordinal()];
		if (segment!= null)
		{
			Set<String> vertexVariants = segment.getDefines();
			if (vertexVariants != null)
				allVariants.addAll(vertexVariants);
		}
		segment = mShaderSegments[ShaderType.SURFACE.ordinal()];
		if (segment!= null)
		{
			Set<String> surfaceVariants = segment.getDefines();
			if (surfaceVariants != null)
				allVariants.addAll(surfaceVariants);
		}
		return allVariants;
	}

	/**
	 * Get the shader segment of the specified type
	 * @param type VERTEX, SURFACE or LIGHT
	 * @return GVRShaderSegment or null if no segment defined for that type
	 */
	public GVRShaderSegment getSegment(ShaderType type)
	{
		return mShaderSegments[type.ordinal()];
	}
	
	/**
	 * Set the shader segment for the specified type
	 * @param type VERTEX, SURFACE or LIGHT
	 * @param segment GVRShaderSegment defining shader segment
	 */
	public void setSegment(ShaderType type, GVRShaderSegment segment)
	{
		mShaderSegments[type.ordinal()] = segment;
	}
		
	/**
	 * Create a unique signature for this shader variant.
	 * @param variantValues key / value pairs
	 * @return string signature for shader
	 */
	public String	generateSignature(Map<String, Integer> variantValues)
	{
		String sig = mName + ":";
		
		for (Map.Entry<String, Integer> entry : variantValues.entrySet())
		{
		    String key = entry.getKey();
		    Integer value = entry.getValue();
		    if (value > 0)
		    	sig += key + ":" + value.toString();
		}
		return sig;
	}
	
	/**
	 * Construct the source code for a GL fragment shader based on the
	 * variant parameters passed.
	 * The FRAGMENT_TEMPLATE shader segment is
	 * combined with the SURFACE shader segment and the
	 * shader code from each scene light to make the pixel shader.
	 * The $ parameters are replaced with their corresponding values from variantValues.
	 * 
	 * @param variantValues table of named integer values
	 * @return GL vertex shader code with parameters substituted
	 */
	public String	generateFragmentShader(Map<String, Integer> variantValues, GVRLight[] lightlist)
	{
		GVRShaderSegment surface = mShaderSegments[ShaderType.SURFACE.ordinal()];
		GVRShaderSegment template = mShaderSegments[ShaderType.FRAGMENT_TEMPLATE.ordinal()];
		
		if ((surface == null) || (template == null))
			return null;
		
		String fragTemplate = template.getShaderCode();
		if (fragTemplate == null)
			return null;
		
		String surfaceShader = surface.generateVariant(variantValues);
		String fragmentShader = fragTemplate.replace("@SURFACESHADER", surfaceShader);
		fragmentShader = fragmentShader.replace("@LIGHTSOURCES", generateLightShader(lightlist));
		fragmentShader = fragmentShader.replace("@SHADERNAME", surface.getName());
		return fragmentShader;				
	}
	
	/**
	 * Construct the source code for a GL vertex shader based on the
	 * variant parameters passed.
	 * The VERTEX_TEMPLATE shader segment is
	 * combined with the VERTEX shader segment to create
	 * the vertex shader. The $ parameters are then replaced
	 * with their corresponding values from variantValues.
	 * 
	 * @param variantValues table of named integer values
	 * @return GL vertex shader code with parameters substituted
	 */
	public String	generateVertexShader(Map<String, Integer> variantValues)
	{
		GVRShaderSegment segment = mShaderSegments[ShaderType.VERTEX.ordinal()];
		GVRShaderSegment template = mShaderSegments[ShaderType.VERTEX_TEMPLATE.ordinal()];
		
		if ((segment == null) || (template == null))
			return null;
		String vertexTemplate = template.getShaderCode();
		if (vertexTemplate == null)
			return null;
		String vertexShader = segment.generateVariant(variantValues);
		vertexShader = vertexTemplate.replace("$VERTEXSHADER", vertexShader);
		return vertexShader;		
	}

	public GVRMaterialShaderId generateGLShaders(GVRMesh mesh, GVRMaterial material, GVRLight[] lightlist) {
		Map<String, Integer> params = new HashMap<String, Integer>();
		Set<String> names = getDefines();
		for (String name : names)
		{
			if (mesh.hasAttribute(name) ||
				material.hasAttribute(name)) {
				params.put(name, 1);
			}
		}
		String vertexShaderSource = generateVertexShader(params);
		String fragShaderSource = generateFragmentShader(params, lightlist);
		GVRMaterialShaderId shaderID = addShader(vertexShaderSource, fragShaderSource);
		return shaderID;
	}

	/***
	 * Determine if the source code for the shader has been loaded.
	 * @return true if loaded, false if not
	 */
	public	boolean isLoaded()
	{
		return	(mShaderSegments[ShaderType.FRAGMENT_TEMPLATE.ordinal()] != null) &&
				(mShaderSegments[ShaderType.VERTEX_TEMPLATE.ordinal()] != null) &&
				(mShaderSegments[ShaderType.VERTEX.ordinal()] != null) &&
				(mShaderSegments[ShaderType.SURFACE.ordinal()] != null);
	}

	/**
	 * Generates the shader code to compute lighting for each light source
	 * @param lightlist list of lights in the scene
	 * @return string with shader source code for lighting
	 */
	protected String generateLightShader(GVRLight[] lightlist)
	{
		String lightSources = "";
		String lightFunction =
			"vec3 LightPixel(Surface s) {\n" +
            "   vec3 color = vec3(0.0, 0.0, 0.0);\n";
		String lightDefs = "";
		Integer lightid = 0;

		for (GVRLight light : lightlist)
		{
			GVRShaderSegment lightShader = light.getShader();
			if (lightShader != null)
			{
				lightSources += lightShader.getName() + "Data light" + lightid.toString() + ";\n";
				lightFunction += "   Radiance r" + lightid.toString() + " = " + lightShader.getName() +  ";\n";
				lightFunction += "   color =+ AddLight(s, r" + lightid.toString() + ");\n";
				lightDefs += lightShader.getShaderCode();
			}
			++lightid;
		}
	    lightFunction += "   return color; }\n";
		return lightDefs + lightSources + lightFunction;
	}
	
	protected String mName;
	protected String mFragmentShaderTemplate;
	protected String mVertexShaderTemplate;
	protected String mLightShaderTemplate;
	protected GVRShaderSegment[] mShaderSegments;
}
