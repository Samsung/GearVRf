package org.gearvrf.animation.keyframe;

/** 
 * Defines how an animation channel behaves outside the defined time range.
 */
public enum GVRAnimationBehavior {
    /** 
     * The value from the default node transformation is taken.
     */
    DEFAULT,  

    
    /** 
     * The nearest key value is used without interpolation. 
     */
    CONSTANT,

    
    /** 
     * The value of the nearest two keys is linearly extrapolated for the 
     * current time value.
     */
    LINEAR,

    
    /** 
     * The animation is repeated.<p>
     *
     * If the animation key go from n to m and the current time is t, use the 
     * value at (t-n) % (|m-n|).
     */
    REPEAT;
}
