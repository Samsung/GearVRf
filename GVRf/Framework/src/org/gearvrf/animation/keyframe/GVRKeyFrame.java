package org.gearvrf.animation.keyframe;

public interface GVRKeyFrame<T> {
    float getTime();
    T getValue();
    void setValue(T value);
}
