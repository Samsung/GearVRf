package org.gearvrf.x3d;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;

public class Sensor {

  public enum Type { ANCHOR, PROXIMITY, TOUCH, VISIBILITY };

  String name = null;
  Type sensorType;
  public GVRSceneObject sensorSceneObject = null;
  private GVRKeyFrameAnimation gvrKeyFrameAnimation = null;
  private String anchorURL = null;

  public Sensor(String name, Type sensorType, GVRSceneObject sensorSceneObject) {
    this.name = name;
    this.sensorType = sensorType;
    this.sensorSceneObject = sensorSceneObject;
  }

  public void setGVRKeyFrameAnimation(GVRKeyFrameAnimation gvrKeyFrameAnimation) {
  	this.gvrKeyFrameAnimation = gvrKeyFrameAnimation;
  }

  public GVRKeyFrameAnimation getGVRKeyFrameAnimation() {
  	return this.gvrKeyFrameAnimation;
  }

  public void setAnchorURL(String anchorURL) {
  	this.anchorURL = anchorURL;
  }

  public String getAnchorURL() {
  	return this.anchorURL;
  }
  
}



