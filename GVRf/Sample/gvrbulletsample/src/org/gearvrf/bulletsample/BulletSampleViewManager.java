package org.gearvrf.bulletsample;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.siprop.bullet.Bullet;
import org.siprop.bullet.Geometry;
import org.siprop.bullet.MotionState;
import org.siprop.bullet.RigidBody;
import org.siprop.bullet.Transform;
import org.siprop.bullet.shape.BoxShape;
import org.siprop.bullet.shape.SphereShape;
import org.siprop.bullet.shape.StaticPlaneShape;
import org.siprop.bullet.util.Point3;
import org.siprop.bullet.util.ShapeType;
import org.siprop.bullet.util.Vector3;

import android.graphics.Color;

public class BulletSampleViewManager extends GVRScript {

    private GVRContext mGVRContext = null;

    private Bullet mBullet = null;

    private Map<RigidBody, GVRSceneObject> rigidBodiesSceneMap = new HashMap<RigidBody, GVRSceneObject>();

    private static final float CUBE_MASS = 0.5f;

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getNextMainScene();

        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);

        mainCameraRig.getTransform().setPosition(0.0f, 6.0f, 0.0f);

        /*
         * Create new Bullet instance.
         * 
         * The Bullet Physics JNI used is from
         * http://www.badlogicgames.com/wordpress/?p=248 Its compiled as a jar
         * and native so file, sources can be found at
         * https://github.com/deepakrawat22/BulletJniFramework
         */
        mBullet = new Bullet();
        /*
         * Create the physics world.
         */
        mBullet.createPhysicsWorld(new Vector3(-480.0f, -480.0f, -480.0f),
                new Vector3(480.0f, 480.0f, 480.0f), 1024, new Vector3(0.0f,
                        -9.8f, 0.0f));

        /*
         * Create the ground. A simple textured quad. In bullet it will be a
         * plane shape with 0 mass
         */
        GVRSceneObject groundScene = quadWithTexture(100.0f, 100.0f,
                "floor.jpg");
        groundScene.getTransform().setRotationByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
        groundScene.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        scene.addSceneObject(groundScene);

        StaticPlaneShape floorShape = new StaticPlaneShape(new Vector3(0.0f,
                1.0f, 0.0f), 0.0f);
        Geometry floorGeometry = mBullet.createGeometry(floorShape, 0.0f,
                new Vector3(0.0f, 0.0f, 0.0f));
        MotionState floorState = new MotionState();
        mBullet.createAndAddRigidBody(floorGeometry, floorState);

        /*
         * Create Some cubes in Bullet world and hit it with a sphere
         */
        addCube(scene, 0.0f, 1.0f, -9.0f, CUBE_MASS);
        addCube(scene, 0.0f, 1.0f, -10.0f, CUBE_MASS);
        addCube(scene, 0.0f, 1.0f, -11.0f, CUBE_MASS);
        addCube(scene, 1.0f, 1.0f, -9.0f, CUBE_MASS);
        addCube(scene, 1.0f, 1.0f, -10.0f, CUBE_MASS);
        addCube(scene, 1.0f, 1.0f, -11.0f, CUBE_MASS);
        addCube(scene, 2.0f, 1.0f, -9.0f, CUBE_MASS);
        addCube(scene, 2.0f, 1.0f, -10.0f, CUBE_MASS);
        addCube(scene, 2.0f, 1.0f, -11.0f, CUBE_MASS);

        addCube(scene, 0.0f, 2.0f, -9.0f, CUBE_MASS);
        addCube(scene, 0.0f, 2.0f, -10.0f, CUBE_MASS);
        addCube(scene, 0.0f, 2.0f, -11.0f, CUBE_MASS);
        addCube(scene, 1.0f, 2.0f, -9.0f, CUBE_MASS);
        addCube(scene, 1.0f, 2.0f, -10.0f, CUBE_MASS);
        addCube(scene, 1.0f, 2.0f, -11.0f, CUBE_MASS);
        addCube(scene, 2.0f, 2.0f, -9.0f, CUBE_MASS);
        addCube(scene, 2.0f, 2.0f, -10.0f, CUBE_MASS);
        addCube(scene, 2.0f, 2.0f, -11.0f, CUBE_MASS);

        addCube(scene, 0.0f, 3.0f, -9.0f, CUBE_MASS);
        addCube(scene, 0.0f, 3.0f, -10.0f, CUBE_MASS);
        addCube(scene, 0.0f, 3.0f, -11.0f, CUBE_MASS);
        addCube(scene, 1.0f, 3.0f, -9.0f, CUBE_MASS);
        addCube(scene, 1.0f, 3.0f, -10.0f, CUBE_MASS);
        addCube(scene, 1.0f, 3.0f, -11.0f, CUBE_MASS);
        addCube(scene, 2.0f, 3.0f, -9.0f, CUBE_MASS);
        addCube(scene, 2.0f, 3.0f, -10.0f, CUBE_MASS);
        addCube(scene, 2.0f, 3.0f, -11.0f, CUBE_MASS);

        /*
         * Throw a sphere from top
         */
        addSphere(scene, 1.0f, 1.5f, 100.0f, -10.0f, 20.0f);
    }

    @Override
    public void onStep() {
        mBullet.doSimulation(1.0f / 60.0f, 10);
        for (RigidBody body : rigidBodiesSceneMap.keySet()) {
            if (body.geometry.shape.getType() == ShapeType.SPHERE_SHAPE_PROXYTYPE
                    || body.geometry.shape.getType() == ShapeType.BOX_SHAPE_PROXYTYPE) {
                rigidBodiesSceneMap
                        .get(body)
                        .getTransform()
                        .setPosition(
                                body.motionState.resultSimulation.originPoint.x,
                                body.motionState.resultSimulation.originPoint.y,
                                body.motionState.resultSimulation.originPoint.z);
            }
        }
    }

    private GVRSceneObject quadWithTexture(float width, float height,
            String texture) {
        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
                mGVRContext.createQuad(width, height));
        GVRSceneObject object = null;
        try {
            object = new GVRSceneObject(mGVRContext, futureMesh,
                    mGVRContext.loadFutureTexture(new GVRAndroidResource(
                            mGVRContext, texture)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    private GVRSceneObject meshWithTexture(String mesh, String texture) {
        GVRSceneObject object = null;
        try {
            object = new GVRSceneObject(mGVRContext, new GVRAndroidResource(
                    mGVRContext, mesh), new GVRAndroidResource(mGVRContext,
                    texture));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    /*
     * Function to add a cube of unit size with mass at the specified position
     * in Bullet physics world and scene graph.
     */
    private void addCube(GVRScene scene, float x, float y, float z, float mass) {
        BoxShape boxShape = new BoxShape(new Vector3(0.5f, 0.5f, 0.5f));
        Geometry boxGeometry = mBullet.createGeometry(boxShape, mass,
                new Vector3(0.0f, 0.0f, 0.0f));
        MotionState boxState = new MotionState();
        boxState.worldTransform = new Transform(new Point3(x, y, z));
        RigidBody boxBody = mBullet
                .createAndAddRigidBody(boxGeometry, boxState);

        GVRSceneObject cubeObject = meshWithTexture("cube.obj", "cube.jpg");

        cubeObject.getTransform().setPosition(x, y, z);
        scene.addSceneObject(cubeObject);
        rigidBodiesSceneMap.put(boxBody, cubeObject);
    }

    /*
     * Function to add a sphere of dimension and position specified in the
     * Bullet physics world and scene graph
     */
    private void addSphere(GVRScene scene, float radius, float x, float y,
            float z, float mass) {
        SphereShape sphereShape = new SphereShape(radius);
        Geometry sphereGeometry = mBullet.createGeometry(sphereShape, mass,
                new Vector3(0.0f, 0.0f, 0.0f));
        MotionState sphereState = new MotionState();
        sphereState.worldTransform = new Transform(new Point3(x, y, z));
        RigidBody sphereBody = mBullet.createAndAddRigidBody(sphereGeometry,
                sphereState);

        GVRSceneObject sphereObject = meshWithTexture("sphere.obj",
                "sphere.jpg");

        sphereObject.getTransform().setPosition(x, y, z);
        scene.addSceneObject(sphereObject);
        rigidBodiesSceneMap.put(sphereBody, sphereObject);
    }

}
