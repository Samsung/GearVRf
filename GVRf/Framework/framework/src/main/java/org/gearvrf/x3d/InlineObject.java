package org.gearvrf.x3d;
import org.gearvrf.GVRSceneObject;

public class InlineObject {

  private GVRSceneObject inlineGVRSceneObject = null;
  private String[] url = {};

  public InlineObject() { }

  public InlineObject(GVRSceneObject inlineGVRSceneObject, String[] url) {
     this.inlineGVRSceneObject = inlineGVRSceneObject;
     this.url = url;
  }

  public String[] getURL() {
     return this.url;
  }

  public int getTotalURL() {
     return url.length;
  }

  public GVRSceneObject getInlineGVRSceneObject() {
     return this.inlineGVRSceneObject;
  }

}



