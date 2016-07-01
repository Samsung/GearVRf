package org.gearvrf.x3d;

import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;

public class RouteAnimation extends Route {

   // If a ROUTE is associated with GVRKeyFrameAnimation, then
   // this value will point to it.
   // Assists with Touch Sensors
   private GVRKeyFrameAnimation gvrKeyFrameAnimation = null;

  public RouteAnimation(String fromNode, String fromField, String toNode, String toField) {
    super(fromNode, fromField, toNode, toField);
  }


  public void setGVRKeyFrameAnimation(GVRKeyFrameAnimation gvrKeyFrameAnimation) {
  	this.gvrKeyFrameAnimation = gvrKeyFrameAnimation;
  }

  public GVRKeyFrameAnimation getGVRKeyFrameAnimation() {
  	return this.gvrKeyFrameAnimation;
  }

}



