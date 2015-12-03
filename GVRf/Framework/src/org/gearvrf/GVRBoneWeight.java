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
package org.gearvrf;


/**
 * A single influence of a bone on a vertex.
 */
public final class GVRBoneWeight extends GVRHybridObject {
    /**
     * Constructor.
     */
    public GVRBoneWeight(GVRContext gvrContext) {
        super(gvrContext, NativeBoneWeight.ctor());
    }

    /**
     * Sets the index of the vertex which is influenced by the bone.
     *
     * @param vertexId the vertex index
     */
    public void setVertexId(int vertexId) {
        NativeBoneWeight.setVertexId(getNative(), vertexId);
    }

    /**
     * Gets the index of the vertex which is influenced by the bone.
     *
     * @return the vertex index
     */
    public int getVertexId() {
        return NativeBoneWeight.getVertexId(getNative());
    }

    /**
     * Sets the strength of the influence in the range (0...1). <p>
     * The influence from all bones at one vertex amounts to 1.
     *
     * @param weight the influence
     */
    public void setWeight(float weight) {
        NativeBoneWeight.setWeight(getNative(), weight);
    }

    /**
     * The strength of the influence in the range (0...1).<p>
     *
     * The influence from all bones at one vertex amounts to 1
     *
     * @return the influence
     */
    public float getWeight() {
        return NativeBoneWeight.getWeight(getNative());
    }

    @Override
    public String toString() {
        return "[vid=" + getVertexId() + ", wt=" + getWeight() + "]";
    }
}

class NativeBoneWeight {
    static native long ctor();
    static native void setVertexId(long ptr, int vertexId);
    static native int getVertexId(long ptr);
    static native void setWeight(long ptr, float weight);
    static native float getWeight(long ptr);
}