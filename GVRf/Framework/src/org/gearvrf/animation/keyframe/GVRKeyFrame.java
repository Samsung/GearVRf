package org.gearvrf.animation.keyframe;

/*package*/ interface GVRKeyFrame<T> {
    float getTime();
    T getValue();
    void setValue(T value);
}
