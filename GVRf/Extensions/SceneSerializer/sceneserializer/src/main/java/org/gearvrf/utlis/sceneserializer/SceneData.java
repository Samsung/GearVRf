package org.gearvrf.utlis.sceneserializer;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SceneData {
    private transient static final String TAG = SceneData.class.getSimpleName();
    private EnvironmentData environmentData;
    private List<SceneObjectData> sceneObjectDataList;
    private transient Set<String> sceneObjectNames;
    private transient int modelCounter;

    SceneData() {
    }

    List<SceneObjectData> getSceneObjectDataList() {
        return sceneObjectDataList;
    }

    void setSceneObjectDataList(List<SceneObjectData> sceneObjectDataList) {
        this.sceneObjectDataList = sceneObjectDataList;
    }

    EnvironmentData getEnvironmentData() {
        return environmentData;
    }

    void setEnvironmentData(EnvironmentData environmentData) {
        this.environmentData = environmentData;
    }

    void addToSceneData(GVRSceneObject gvrSceneObject, String filePath) {
        if (sceneObjectDataList == null) {
            sceneObjectDataList = new ArrayList<SceneObjectData>();
        }
        if(sceneObjectNames == null) {
            sceneObjectNames = new HashSet<String>();
            for(SceneObjectData data: sceneObjectDataList) {
                sceneObjectNames.add(data.getName());
            }
        }
        int end = filePath.lastIndexOf(".");
        int start = filePath.lastIndexOf(File.separator, end) + 1;
        String name = null;
        do {
            name = filePath.substring(start, end) + "_" +modelCounter++;
        } while(sceneObjectNames.contains(name));
        sceneObjectNames.add(name);
        Log.d(TAG, "Setting model name to:%s", name);
        gvrSceneObject.setName(name);
        SceneObjectData sod = SceneObjectData.createSceneObjectData(gvrSceneObject, filePath);

        sceneObjectDataList.add(sod);
    }

    void removeFromSceneData(GVRSceneObject gvrSceneObject) {
        Iterator<SceneObjectData> iterator = sceneObjectDataList.iterator();
        while (iterator.hasNext()) {
            SceneObjectData sod = iterator.next();
            if (sod.getGvrSceneObject() == gvrSceneObject) {
                iterator.remove();
                return;
            }
        }
    }

    void prepareForExport() {
        if(sceneObjectDataList == null) {
            return;
        }
        for (SceneObjectData sod : sceneObjectDataList) {
            GVRSceneObject so = sod.getGvrSceneObject();
            if(so != null) {
                sod.setModelMatrix(so.getTransform().getModelMatrix());
                sod.setName(sod.getGvrSceneObject().getName());
            }
        }
    }
}
