
package org.gearvrf.keyboard.keyboard.model;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import java.util.ArrayList;
import java.util.List;

public class KeyboardBase extends GVRSceneObject {

    public float softLineWidth = 0;
    private List<GVRSceneObject> objects = null;
    private List<KeyboardLine> mListKeyboardLine = new ArrayList<>();

    public KeyboardBase(GVRContext gvrContext) {
        super(gvrContext);
        setName("KEYBOARD_BASE");
    }
    
    public List<KeyboardLine> getListKeyboardLine() {
        return mListKeyboardLine;
    }
    
    public void addLine(KeyboardLine keyboardLine){
        mListKeyboardLine.add(keyboardLine);
    }

    public void setListKeyboardLine(List<KeyboardLine> listKeyboardLine) {
        this.mListKeyboardLine = listKeyboardLine;
    }

 	public List<GVRSceneObject> getObjects() {

        if (objects == null) {

            objects = new ArrayList<GVRSceneObject>();

            for (KeyboardLine line : mListKeyboardLine) {
                objects.addAll(line.getChildren());
            }
        }

        return objects;
    }	
}
