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

/**
 * The root structure of the imported data.
 * <p>
 * 
 * Everything that was imported from the given file can be accessed from here.
 * <p>
 * Jassimp copies all data into "java memory" during import and frees resources
 * allocated by native code after scene loading is completed. No special care
 * has to be taken for freeing resources, unreferenced jassimp objects
 * (including the scene itself) are eligible to garbage collection like any
 * other java object.
 */
public final class AiScene {
    /**
     * Constructor.
     */
    AiScene() {
        /* nothing to do */
    }

    /**
     * Returns the scene graph root.
     * 
     * This method is part of the wrapped API (see {@link AiWrapperProvider} for
     * details on wrappers).
     * <p>
     * 
     * The built-in behavior is to return a {@link AiVector}.
     * 
     * @param wrapperProvider
     *            the wrapper provider (used for type inference)
     * @return the scene graph root
     */
    @SuppressWarnings("unchecked")
    public <V3, M4, C, N, Q> N getSceneRoot(
            AiWrapperProvider<V3, M4, C, N, Q> wrapperProvider) {

        return (N) m_sceneRoot;
    }

    /**
     * Scene graph root.
     */
    private Object m_sceneRoot;
}
