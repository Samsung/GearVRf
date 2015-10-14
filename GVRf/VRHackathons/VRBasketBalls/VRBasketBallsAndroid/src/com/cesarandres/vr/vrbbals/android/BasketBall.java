package com.cesarandres.vr.vrbbals.android;

import java.io.IOException;
import java.util.Random;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;

public class BasketBall {

	protected GVRSceneObject vrObject;
	protected GVRMesh mesh;
	protected GVRTexture texture;

	protected GVRSceneObject vrpObject;
	protected GVRMesh pmesh;
	protected GVRTexture ptexture;

	protected static Random rnd = new Random();
	protected GVRAnimationEngine mAnimationEngine;

	public float duration;

	public BasketBall() {
		this.duration = 1f;
	}

	public void init(GVRContext vrcontext, GVRAnimationEngine mAnimationEngine,
			int index) {
		try {
			this.mAnimationEngine = mAnimationEngine;
			mesh = vrcontext.loadMesh(new GVRAndroidResource(vrcontext,
					"sphere.obj"));
			texture = vrcontext.loadTexture(new GVRAndroidResource(vrcontext,
					"basketbal.jpg"));
			vrObject = new GVRSceneObject(vrcontext, mesh, texture);
			vrObject.getTransform().setScale(0.2f, 0.2f, 0.2f);

			pmesh = vrcontext.loadMesh(new GVRAndroidResource(vrcontext,
					"cube.obj"));
			ptexture = vrcontext.loadTexture(new GVRAndroidResource(vrcontext,
					"mars_1k_color.jpg"));
			vrpObject = new GVRSceneObject(vrcontext, pmesh, ptexture);
			vrpObject.getTransform().setScale(0.1f, 4f, 0.1f);
			vrpObject.getTransform().setPosition(0f, -4f, 0f);

			vrObject.attachEyePointeeHolder();
			vrObject.addChildObject(vrpObject);

			this.setX((float) index / 2f);
			this.setY(0.5f);
			this.setZ(-1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public float getX() {
		return this.vrObject.getTransform().getPositionX();
	}

	public void setX(float x) {
		this.vrObject.getTransform().setPositionX(x);
	}

	public float getY() {
		return this.vrObject.getTransform().getPositionY();
	}

	public void setY(float y) {
		this.vrObject.getTransform().setPositionY(y);
	}

	public float getZ() {
		return this.vrObject.getTransform().getPositionZ();
	}

	public void setZ(float z) {
		this.vrObject.getTransform().setPositionZ(z);
	}

	public GVRSceneObject getVrObject() {
		return vrObject;
	}

	public void setup(GVRAnimation animation) {
		animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
		mAnimationEngine.start(animation);
	}

	public void counterClockwise(GVRSceneObject object, float duration) {
		setup(new GVRRotationByAxisWithPivotAnimation( //
				object, duration, 360.0f, //
				0.0f, 1.0f, 0.0f, //
				object.getTransform().getPositionX(), object.getTransform()
						.getPositionY(), object.getTransform().getPositionZ()));
	}

	public void clockwise(GVRSceneObject object, float duration) {
		setup(new GVRRotationByAxisWithPivotAnimation( //
				object, duration, -360.0f, //
				0.0f, 1.0f, 0.0f, //
				object.getTransform().getPositionX(), object.getTransform()
						.getPositionY(), object.getTransform().getPositionZ()));
	}
}
