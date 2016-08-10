package org.gearvrf.utlis.sceneserializer;

public class EnvironmentData {
    private String src;
    private float scale;

    public EnvironmentData() {
    }

    public EnvironmentData(String src, float scale) {
        this.src = src;
        this.scale = scale;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
