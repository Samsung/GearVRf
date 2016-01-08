package org.gearvrf;

/**
 * This interface defines life-cycle and general per-frame
 * events that are handled by an application.
 */
public interface IScriptEvents extends IEvents {
    void onInit(GVRContext gvrContext) throws Throwable;
    void onStep();
}
