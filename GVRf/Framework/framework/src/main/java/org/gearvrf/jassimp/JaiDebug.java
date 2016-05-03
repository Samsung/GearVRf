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
package org.gearvrf.jassimp;

import java.nio.ByteBuffer;

/**
 * Debug/utility methods.
 */
public final class JaiDebug {

    /**
     * Pure static class, no accessible constructor.
     */
    private JaiDebug() {
        /* nothing to do */
    }

    /**
     * Dumps a single material property to stdout.
     * 
     * @param property
     *            the property
     */
    public static void dumpMaterialProperty(AiMaterial.Property property) {
        System.out.print(property.getKey() + " " + property.getSemantic() + " "
                + property.getIndex() + ": ");
        Object data = property.getData();

        if (data instanceof ByteBuffer) {
            ByteBuffer buf = (ByteBuffer) data;
            for (int i = 0; i < buf.capacity(); i++) {
                System.out.print(Integer.toHexString(buf.get(i) & 0xFF) + " ");
            }

            System.out.println();
        } else {
            System.out.println(data.toString());
        }
    }

    /**
     * Dumps all properties of a material to stdout.
     * 
     * @param material
     *            the material
     */
    public static void dumpMaterial(AiMaterial material) {
        for (AiMaterial.Property prop : material.getProperties()) {
            dumpMaterialProperty(prop);
        }
    }
}
