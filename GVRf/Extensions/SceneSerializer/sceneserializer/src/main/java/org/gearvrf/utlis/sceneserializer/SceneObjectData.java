package org.gearvrf.utlis.sceneserializer;

import org.gearvrf.GVRSceneObject;

public class SceneObjectData {
    private float[] modelMatrix;
    private String src;
    private String name;
    private transient GVRSceneObject gvrSceneObject;

    public SceneObjectData() {
    }

    public static SceneObjectData createSceneObjectData(GVRSceneObject gvrSceneObject, String
            source) {
        SceneObjectData sceneObjectData = new SceneObjectData();
        sceneObjectData.setSrc(source);
        sceneObjectData.setGvrSceneObject(gvrSceneObject);
        sceneObjectData.setModelMatrix(gvrSceneObject.getTransform().getModelMatrix());
        sceneObjectData.setName(gvrSceneObject.getName());
        return sceneObjectData;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public float[] getModelMatrix() {
        return modelMatrix;
    }

    public void setModelMatrix(float[] modelMatrix) {
        this.modelMatrix = modelMatrix;
    }

    public GVRSceneObject getGvrSceneObject() {
        return gvrSceneObject;
    }

    public void setGvrSceneObject(GVRSceneObject gvrSceneObject) {
        this.gvrSceneObject = gvrSceneObject;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
