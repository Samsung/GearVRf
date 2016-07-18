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

import java.nio.Buffer;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;



/**
 * {@hide}
 */
public class TextureValues {
	public float[] coord = { 0, 0 };

	public TextureValues (float u, float v) {
		//Log.d("Texture.constr", ": " + u + ", " + v ); 		    					
		coord[0] = u;
		coord[1] = v;
	}
	public TextureValues (float[] tv) {
		coord[0] = tv[0];
		coord[1] = tv[1];
	}

    public int loadTexture(GL10 gl, int textureUnit, int minFilter, int magFilter, int wrapS, int wrapT, int mode, int width, int height, int dataType, Buffer data)
    {
        int[] texture = new int[1];
        gl.glGenTextures(1, texture, 0);

        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glClientActiveTexture(textureUnit);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, minFilter);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, magFilter);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, wrapS);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, wrapT);
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, mode);

        gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGB, width, height, 0, GL10.GL_RGB, dataType, data);

        return texture[0];
    }

    /*
    public void setTextureParameters(GL10 gl)
    {
        if (name < 0)
        {
            name = loadTexture(gl, GL10.GL_TEXTURE0, GL10.GL_NEAREST, GL10.GL_NEAREST, GL10.GL_REPEAT, GL10.GL_REPEAT, GL10.GL_MODULATE, width, height, GL10.GL_UNSIGNED_SHORT_5_6_5, data);
        }

        gl.glBindTexture(GL10.GL_TEXTURE_2D, name);
    }
    */
}
