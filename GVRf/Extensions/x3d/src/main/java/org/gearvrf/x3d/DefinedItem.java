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

import java.util.concurrent.Future;

import org.gearvrf.GVRIndexBuffer;
import org.gearvrf.GVRMaterial;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
/*
 * Used in Array List for items DEFined in X3D (using the DEF="...." parameter).
 * Another item using the USE="...." parameter will search the array list for the 
 * matching item.
 */

import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRVertexBuffer;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.joml.Vector2f;
import org.gearvrf.x3d.data_types.SFVec2f;
import org.gearvrf.x3d.data_types.SFFloat;


/**
 * @author m1.williams DefinedItem is an array list of each X3D node that has a
 *         DEF="some_name" between the < > brackets. The name is saved, and a
 *         reference to the GVR structure
 *         <p/>
 *         For example <IndexedFaceSet DEF="myIFS"> will create a DefinedItem
 *         setting name to "myIFS" and gvrMesh will point to a GVRmesh object.
 *         Later, if an <IndexedFaceSet USE="myIFS">, both IndexedFaceSet will
 *         point to the same GVRmesh.
 *         <p/>
 *         This also allows implementation of X3D's working with with HTML5
 *         Document Object Model (DOM) getElementByTagName() method since every
 *         DEF name will be in this array list containing DEFined values.
 */
public class DefinedItem {

    private String name = "";
    private GVRMesh gvrMesh = null;
    private GVRIndexBuffer gvrIndexBuffer = null;
    private GVRVertexBuffer gvrVertexBuffer = null;
    private GVRSceneObject gvrSceneObject = null;
    private GVRTexture gvrTexture = null;
    private GVRRenderData gvrRenderData = null;
    private GVRMaterial gvrMaterial = null;
    private GVRVideoSceneObject gvrVideoSceneObject = null;
    private Viewpoint viewpoint = null;
    private GVRTextViewSceneObject gvrTextViewSceneObject = null;
    /**
     * X3D Transforms use AxisAngle format for rotations,
     * and float3 for the SpotLight, DirectionalLight direction
     * Using SCRIPT node need these raw values while GearVR
     * saves these values as Quaternions
     */
    private AxisAngle4f rotationAxisAngle = new AxisAngle4f();
    private Vector3f direction = new Vector3f();

    private SFVec2f textureTranslation = new SFVec2f(0, 0);
    private SFVec2f textureCenter = new SFVec2f(0, 0);
    private SFVec2f textureScale = new SFVec2f(1, 1);
    private SFFloat textureRotation = new SFFloat(0);

    public DefinedItem() {
    }

    public DefinedItem(String name) {
        this.name = name;
    }

    // used by directional and spot light
    public DefinedItem(String name, Vector3f direction) {
        this.name = name;
        this.direction.set(direction);
    }

    public DefinedItem(String name, float x, float y, float z) {
        this.name = name;
        this.direction.set(x, y, z);
    }

    // used by rotations
    public DefinedItem(String name, AxisAngle4f axisAngle) {
        this.name = name;
        this.rotationAxisAngle.set(axisAngle);
    }

    public DefinedItem(String name, float angle, float x, float y, float z) {
        this.name = name;
        this.rotationAxisAngle.set(angle, x, y, z);  // order: angle, x, y, z
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDirection(Vector3f direction) {
        this.direction.set(direction);
    }

    public void setDirection(float[] direction) {
        setDirection(direction[0], direction[1], direction[2]);
    }

    public void setDirection(float x, float y, float z) {
        this.direction.set(x, y, z);
    }

    public void setAxisAngle(AxisAngle4f axisAngle) {
        this.rotationAxisAngle.set(axisAngle);
    }

    public void setAxisAngle(float angle, float x, float y, float z) {
        this.rotationAxisAngle.set(angle, x, y, z);  // order: angle, x, y, z
    }

    public Vector3f getDirection() {
        return this.direction;
    }

    public AxisAngle4f getAxisAngle() {
        return this.rotationAxisAngle;
    }

    public void setGVRRenderData(GVRRenderData gvrRenderData) {
        this.gvrRenderData = gvrRenderData;
    }

    public GVRRenderData getGVRRenderData() {
        return this.gvrRenderData;
    }

    public void setGVRTexture(GVRTexture gvrTexture) {
        this.gvrTexture = gvrTexture;
    }

    public GVRTexture getGVRTexture() {
        return this.gvrTexture;
    }
    public void setGVRVideoSceneObject(GVRVideoSceneObject gvrVideoSceneObject) {
        this.gvrVideoSceneObject = gvrVideoSceneObject;
    }

    public GVRVideoSceneObject getGVRVideoSceneObject() {
        return this.gvrVideoSceneObject;
    }

    public void setGVRMesh(GVRMesh gvrMesh) {
        this.gvrMesh = gvrMesh;
    }

    public GVRMesh getGVRMesh() {
        return this.gvrMesh;
    }

    public void setVertexBuffer(GVRVertexBuffer vbuf) { gvrVertexBuffer = vbuf; }

    public GVRVertexBuffer getVertexBuffer() { return gvrVertexBuffer; }

    public void setIndexBuffer(GVRIndexBuffer ibuf) { gvrIndexBuffer = ibuf; }

    public GVRIndexBuffer getIndexBuffer() { return gvrIndexBuffer; }

    public void setGVRMaterial(GVRMaterial gvrMaterial) {
        this.gvrMaterial = gvrMaterial;
    }

    public GVRMaterial getGVRMaterial() {
        return this.gvrMaterial;
    }


    public void setGVRTextViewSceneObject(GVRTextViewSceneObject gvrTextViewSceneObject) {
        this.gvrTextViewSceneObject = gvrTextViewSceneObject;
    }

    public GVRTextViewSceneObject getGVRTextViewSceneObject() {
        return this.gvrTextViewSceneObject;
    }

    public void setGVRSceneObject(GVRSceneObject gvrSceneObject) {
        this.gvrSceneObject = gvrSceneObject;
    }

    public GVRSceneObject getGVRSceneObject() {
        return this.gvrSceneObject;
    }


    public void setViewpoint(Viewpoint viewpoint) {
        this.viewpoint = viewpoint;
    }

    public Viewpoint getViewpoint() {
        return this.viewpoint;
    }

    public void setTextureTranslation (float x, float y) {
        textureTranslation.setValue( x, y );
    }
    public void setTextureTranslation (SFVec2f textureTranslation) {
        this.textureTranslation.setValue( textureTranslation );
    }
    public SFVec2f getTextureTranslation () {
        return this.textureTranslation;
    }

    public void setTextureCenter (float x, float y) {
        textureCenter.setValue( x, y );
    }
    public void setTextureCenter (SFVec2f textureCenter) {
        this.textureCenter.setValue( textureCenter );
    }
    public SFVec2f getTextureCenter () {
        return this.textureCenter;
    }

    public void setTextureScale (float x, float y) {
        textureScale.setValue( x,  y );
    }
    public void setTextureScale (SFVec2f textureScale) {
        this.textureScale.setValue( textureScale );
    }
    public SFVec2f getTextureScale () {
        return this.textureScale;
    }

    public void setTextureRotation (float rotation) {
        textureRotation.setValue( rotation);
    }
    public void setTextureRotation (SFFloat rotation) {
        this.textureRotation.setValue( rotation.getValue() );
    }
    public SFFloat getTextureRotation () {
        return textureRotation;
    }

}
