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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates variants of a shader source code fragment.
 * 
 * The final vertex and fragment shaders used by GearVRF are
 * composed by combining segments of shader source code
 * and generating the appropriate #define statements to make
 * a particular variant. Whenever a variable starting with "$"
 * is encountered in the shader source, it is substituted with
 * the name of the #define for correspondingly named material
 * property or vertex attribute. This permits
 * multiple variants of the same shader to be created.
 * 
 * {@link GVRShader } { @link GVRMaterial }
 */
public class GVRShaderSegment {
	public GVRShaderSegment(String name)
	{
		mName = name;
		mDefines = null;
	}
	
	/**
	 * Set the code segment to be used for generating shader variants.
	 * Changing the shader source code will invalidate all variants
	 * of this shader, possibly causing them to be recompiled.
	 * @param shadercode string containing shader source code.
	 */
	public void		setShaderCode(String shadercode)
	{
		mShaderSource = shadercode;
		mDefines = new HashSet<String>();		
		if (shadercode != null)
		{
			Pattern pattern = Pattern.compile(".*\\$([a-zA-Z0-9_]+)");
			Matcher matcher = pattern.matcher(mShaderSource);
			
			while (matcher.find())
				mDefines.add(matcher.group(1));
		}
	}
	
	/**
	 * Load the code segment to be used for generating shader variants
	 * from the named file. Changing the shader source code will invalidate
	 * all variants of this shader, possibly causing them to be recompiled.
	 * @param shadercode string containing name of shader source file
	 */
	//public void		loadShaderCode(String shaderfilename);
	
	/**
	 * Gets the source code used to generate variants of this shader.
	 * @return string with shader source code.
	 */
	public String	getShaderCode() { return mShaderSource; }
	
	/**
	 * Gets the name of this shader.
	 * @return String with shader name.
	 */
	public String getName()	{ return mName; }
	
	/**
	 * Gets the list of defines used to create variants
	 * of this shader. These are variable names that
	 * begin with "$" in the shader and they correspond
	 * to GVRMaterial properties or vertex attributes.
	 */
	public Set<String> getDefines() { return mDefines; }
	
	/**
	 * Generates a single variant of the shader by substituting
	 * its $ variables with integer values.
	 * @return shader source code with parameters substituted.
	 */
	public String	generateVariant(Map<String, Integer> variantValues)
	{
		String variantCode = mShaderSource;
		String defines = "";
		if (mShaderSource == null)
			return null;
		for (String key : mDefines)
		{
		    String param = "$" + key;
		    String define = "HAS_" + key;
		    int found = variantCode.indexOf(param);
	    	defines += "#define " + define + " ";
		    if (found >= 0)
		    {
			    if (variantValues.containsKey(key))
				    defines += variantValues.get(key).toString() + "\n";
			    else
			    	defines += "0\n";
			    variantCode = variantCode.replace(param, define);		    	
		    }
		}
		return defines + variantCode;
	}
	
	protected String mName = null;
	protected Set<String> mDefines = null;
	protected String mShaderSource = null;
	protected String[] mVariantParameters = null;
}
