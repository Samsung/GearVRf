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
import java.nio.IntBuffer;

/**
 * Contains the bitmap data for an embedded texture
 */
public final class AiTexture {
    /**
     * Constructor.
     */
    public AiTexture(int width, int height, String type) {
        int numBytes = width;

        if (height > 0)
        {
            numBytes *= height * 4;
        }
        m_data = ByteBuffer.allocate(numBytes);
        m_width = width;
        m_height = height;
        m_type = type;
    }

    /**
     * Returns the pixel width of the texture
     */
    public int getWidth() {
        return m_width;
    }

    /**
     * Returns the pixel height of the texture
     */
    public int getHeight() {
        return m_height;
    }

    /**
     * Returns the bitmap data as an integer array
     */
    public int[] getIntData()
    {
        IntBuffer intData = m_data.asIntBuffer();
        return intData.array();
    }

    /**
     * Returns the bitmap data as an integer array
     */
    public byte[] getByteData()
    {
        if (m_data != null)
        {
            return m_data.array();
        }
        return null;
    }

    /**
     * Returns the integer buffer for the bitmap data
     */
    public ByteBuffer getBuffer() { return m_data; }

    /**
     * Returns the type of the bitmap data
     */
    public String getType() { return m_type; }

    private String m_type;
    private int m_width;
    private int m_height;
    private ByteBuffer m_data;
}