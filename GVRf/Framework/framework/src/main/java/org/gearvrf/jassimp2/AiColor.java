/*
---------------------------------------------------------------------------
Open Asset Import Library - Java Binding (jassimp)
---------------------------------------------------------------------------

Copyright (c) 2006-2012, assimp team

All rights reserved.

Redistribution and use of this software in source and binary forms, 
with or without modification, are permitted provided that the following 
conditions are met:

* Redistributions of source code must retain the above
  copyright notice, this list of conditions and the
  following disclaimer.

* Redistributions in binary form must reproduce the above
  copyright notice, this list of conditions and the
  following disclaimer in the documentation and/or other
  materials provided with the distribution.

* Neither the name of the assimp team, nor the names of its
  contributors may be used to endorse or promote products
  derived from this software without specific prior
  written permission of the assimp team.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
---------------------------------------------------------------------------
*/
package org.gearvrf.jassimp2;

import java.nio.ByteBuffer;

import static java.lang.Math.max;


/**
 * Wrapper for colors.<p>
 * 
 * The wrapper is writable, i.e., changes performed via the set-methods will
 * modify the underlying mesh.
 */
public final class AiColor {
    /**
     * Constructor.
     * 
     * @param buffer the buffer to wrap
     * @param offset offset into buffer
     */
    public AiColor(ByteBuffer buffer, int offset) {
        m_buffer = buffer;
        m_offset = offset;
    }

    public void getColor(float[] color)
    {
        color[0] = getRed();
        color[1] = getGreen();
        color[2] = getBlue();
        float scale = max(max(color[0], color[1]), color[2]);
        if (scale > 1)
        {
            color[0] /= scale;
            color[1] /= scale;
            color[2] /= scale;
        }
    }
    
    /**
     * Returns the red color component as a float between 0 and 255.
     *
     * @return the red component
     */
    public float getRed() {
        return m_buffer.getFloat(m_offset);
    }


    /**
     * Returns the green color component as a float between 0 and 255..
     *
     * @return the green component
     */
    public float getGreen() {
        return m_buffer.getFloat(m_offset + 4);
    }


    /**
     * Returns the blue color component as a float between 0 and 255.
     *
     * @return the blue component
     */
    public float getBlue() {
        return m_buffer.getFloat(m_offset + 8);
    }

    /**
     * Returns the alpha color component as a float between 0 and 255.
     *
     * @return the alpha component
     */
    public float getAlpha() {
        return m_buffer.getFloat(m_offset + 12);
    }

    /**
     * Sets the red color component.
     * 
     * @param red the new value
     */
    public void setRed(float red) {
        m_buffer.putFloat(m_offset, red);
    }

    /**
     * Sets the green color component.
     * 
     * @param green the new value
     */
    public void setGreen(float green) {
        m_buffer.putFloat(m_offset + 4, green);
    }
    
    
    /**
     * Sets the blue color component.
     * 
     * @param blue the new value
     */
    public void setBlue(float blue) {
        m_buffer.putFloat(m_offset + 8, blue);
    }
    
    
    /**
     * Sets the alpha color component.
     * 
     * @param alpha the new value
     */
    public void setAlpha(float alpha) {
        m_buffer.putFloat(m_offset + 12, alpha);
    }
    

    @Override
    public String toString() {
        return "[" + getRed() + ", " + getGreen() + ", " + getBlue() + ", " + 
                getAlpha() + "]";
    }


    /**
     * Wrapped buffer.
     */
    private final ByteBuffer m_buffer;
    
    
    /**
     * Offset into m_buffer. 
     */
    private final int m_offset;
}
