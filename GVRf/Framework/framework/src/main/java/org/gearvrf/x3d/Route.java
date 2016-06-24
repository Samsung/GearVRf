package org.gearvrf.x3d;

import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;

public abstract class  Route {
  private String fromNode = null;
  private String fromField = null;
  private String toNode = null;
  private String toField = null;

  public Route(String fromNode, String fromField, String toNode, String toField) {
    this.fromNode = fromNode;
    this.fromField = fromField;
    this.toNode = toNode;
    this.toField = toField;
  }

    public String getRouteFromNode() {
      return this.fromNode;
    }

    public String getRouteFromField() {
        return this.fromField;
    }

    public String getRouteToNode() {
        return this.toNode;
    }

    public String getRouteToField() {
        return this.toField;
    }
}



