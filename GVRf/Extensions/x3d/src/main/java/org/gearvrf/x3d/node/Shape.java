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

package org.gearvrf.x3d.node;

public class Shape extends X3DNode
{

    private static final String TAG = Shape.class.getSimpleName();

    private Proto mProto = null;

    public Shape( Proto _proto ) {
        mProto = _proto;
    }

    public Shape(Proto _proto, String _DEF) {
        mProto = _proto;
        setDEF(_DEF);
    }

    /**
     * Provide X3DAppearanceNode instance (using a properly typed node) from inputOutput SFNode field material.
     * @param newValue
     */
    public Appearance getAppearance() {
        return mProto.getAppearance();
    }

    /**
     * Provide X3DGeometryNode instance (using a properly typed node) from inputOutput SFNode field material.
     * @param newValue
     */
    public Geometry getGeometry() {
        return mProto.getGeometry();
    }

    /**
     * Assign String value to inputOutput SFString field named DEF.
     * @param newValue
     */
    public void setDEF(String newValue) {
        super.setDEF(newValue);
    }

    /**
     * Assign String value to inputOutput SFString field named USE.
     * @param newValue
     */
    public void setUSE(String newValue) {
        super.setUSE(newValue);
    }

    /**
     * Assign X3DAppearanceNode instance (using a properly typed node) to inputOutput SFNode field material.
     */
    public void setAppearance(Appearance newValue) {
        mProto.setAppearance(newValue);
    }

    /**
     * Assign X3DGeometryNode instance (using a properly typed node) to inputOutput SFNode field material.
     */
    public void setGeometry(Geometry newValue) {
        mProto.setGeometry(newValue);
    }


    //TODO: the following methods are not implemented
    /*
    getBboxCenter()
    getBboxSize()
    getMetadata()
    setBboxCenter(float[] newValue)
    setBboxSize(float[] newValue)
    setCssClass(java.lang.String newValue)
    setMetadata(X3DMetadataObject newValue)
    */
} // end Shape
