
package com.samsung.android.gvr.app.base.viewmanager;

import com.samsung.android.gvr.app.R;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;

public class SolarViewManager extends TestDefaultGVRViewManager {

    GVRAnimationEngine mAnimationEngine;

    @Override
    public void onInitTest() {

        mAnimationEngine = mGVRContext.getAnimationEngine();

        mGVRContext.getMainScene().getMainCameraRig().getLeftCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);
        mGVRContext.getMainScene().getMainCameraRig().getRightCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);

        mGVRContext.getMainScene().getMainCameraRig().getOwnerObject()
                .getTransform().setPosition(0.0f, 0.0f, 0.0f);

        GVRAndroidResource androidResource = new GVRAndroidResource(mGVRContext, R.mipmap.sphere);
        GVRMesh sphereMesh = mGVRContext.loadMesh(androidResource);
        GVRTexture sunTexture = mGVRContext.loadTexture("sunmap.jpg");
        GVRTexture mercuryTexture = mGVRContext.loadTexture("mercurymap.jpg");
        GVRTexture venusTexture = mGVRContext.loadTexture("venusmap.jpg");
        GVRTexture earthTexture = mGVRContext.loadTexture("earthmap1k.jpg");
        GVRTexture marsTexture = mGVRContext.loadTexture("mars_1k_color.jpg");

        GVRSceneObject solarSystemObject = new GVRSceneObject(mGVRContext);
        mGVRContext.getMainScene().addSceneObject(solarSystemObject);

        GVRSceneObject sunRotationObject = new GVRSceneObject(mGVRContext);
        solarSystemObject.addChildObject(sunRotationObject);

        GVRSceneObject sunMeshObject = new GVRSceneObject(mGVRContext,
                sphereMesh, sunTexture);
        sunMeshObject.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        sunMeshObject.getTransform().setScale(10.0f, 10.0f, 10.0f);
        sunRotationObject.addChildObject(sunMeshObject);

        GVRSceneObject mercuryRevolutionObject = new GVRSceneObject(mGVRContext);
        mercuryRevolutionObject.getTransform().setPosition(14.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(mercuryRevolutionObject);

        GVRSceneObject mercuryRotationObject = new GVRSceneObject(mGVRContext);
        mercuryRevolutionObject.addChildObject(mercuryRotationObject);

        GVRSceneObject mercuryMeshObject = new GVRSceneObject(mGVRContext,
                sphereMesh, mercuryTexture);
        mercuryMeshObject.getTransform().setScale(0.3f, 0.3f, 0.3f);
        mercuryRotationObject.addChildObject(mercuryMeshObject);

        GVRSceneObject venusRevolutionObject = new GVRSceneObject(mGVRContext);
        venusRevolutionObject.getTransform().setPosition(17.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(venusRevolutionObject);

        GVRSceneObject venusRotationObject = new GVRSceneObject(mGVRContext);
        venusRevolutionObject.addChildObject(venusRotationObject);

        GVRSceneObject venusMeshObject = new GVRSceneObject(mGVRContext,
                sphereMesh, venusTexture);
        venusMeshObject.getTransform().setScale(0.8f, 0.8f, 0.8f);
        venusRotationObject.addChildObject(venusMeshObject);

        GVRSceneObject earthRevolutionObject = new GVRSceneObject(mGVRContext);
        earthRevolutionObject.getTransform().setPosition(22.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(earthRevolutionObject);

        GVRSceneObject earthRotationObject = new GVRSceneObject(mGVRContext);
        earthRevolutionObject.addChildObject(earthRotationObject);

        GVRSceneObject earthMeshObject = new GVRSceneObject(mGVRContext,
                sphereMesh, earthTexture);
        earthMeshObject.getTransform().setScale(1.0f, 1.0f, 1.0f);
        earthRotationObject.addChildObject(earthMeshObject);

        GVRSceneObject moonRevolutionObject = new GVRSceneObject(mGVRContext);
        moonRevolutionObject.getTransform().setPosition(4.0f, 0.0f, 0.0f);
        earthRevolutionObject.addChildObject(moonRevolutionObject);
        moonRevolutionObject.addChildObject(mGVRContext.getMainScene()
                .getMainCameraRig().getOwnerObject());

        GVRSceneObject marsRevolutionObject = new GVRSceneObject(mGVRContext);
        marsRevolutionObject.getTransform().setPosition(30.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(marsRevolutionObject);

        GVRSceneObject marsRotationObject = new GVRSceneObject(mGVRContext);
        marsRevolutionObject.addChildObject(marsRotationObject);

        GVRSceneObject marsMeshObject = new GVRSceneObject(mGVRContext,
                sphereMesh, marsTexture);
        marsMeshObject.getTransform().setScale(0.6f, 0.6f, 0.6f);
        marsRotationObject.addChildObject(marsMeshObject);

        counterClockwise(sunRotationObject, 50f);

        counterClockwise(mercuryRevolutionObject, 150f);
        counterClockwise(mercuryRotationObject, 100f);

        counterClockwise(venusRevolutionObject, 400f);
        clockwise(venusRotationObject, 400f);

        counterClockwise(earthRevolutionObject, 600f);
        counterClockwise(earthRotationObject, 1.5f);

        counterClockwise(moonRevolutionObject, 60f);

        clockwise(mGVRContext.getMainScene().getMainCameraRig().getOwnerObject()
                .getTransform(), 60f);

        counterClockwise(marsRevolutionObject, 1200f);
        counterClockwise(marsRotationObject, 200f);
    }

    @Override
    public void onStep() {
    }

    private void run(GVRAnimation animation) {
        animation
                //
                .setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1)
                .start(mAnimationEngine);
    }

    private void counterClockwise(GVRSceneObject object, float duration) {
        run(new GVRRotationByAxisWithPivotAnimation( //
                object, duration, 360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }

    private void clockwise(GVRSceneObject object, float duration) {
        run(new GVRRotationByAxisWithPivotAnimation( //
                object, duration, -360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }

    private void clockwise(GVRTransform transform, float duration) {
        run(new GVRRotationByAxisWithPivotAnimation( //
                transform, duration, -360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }
}
