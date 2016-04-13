package com.cesarandres.vr.vrbbals.android;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;

import android.graphics.Color;

import com.cesarandres.vr.vrbbals.android.MainActivity.COMMANDS;

public class BallSpinnerScript extends GVRScript {

	private static int GAME_OVER_REVOLUTION_DURATION = 3;
	private static int STARTING_BALLS = 3;
	private static final float CUBE_WIDTH = 200.0f;
	private static float LOOKAT_COLOR_MASK_R = 1.0f;
	private static float LOOKAT_COLOR_MASK_G = 0.6f;
	private static float LOOKAT_COLOR_MASK_B = 0.6f;

	private MainActivity core;

	private GVRTextViewSceneObject textPanel;
	private GVRContext context;
	private GVRScene scene;
	private GVRSceneObject root;
	private GVRCameraRig mainCamera;
	private GVRAnimationEngine animationEngine;

	private boolean connected = false;

	private ConcurrentLinkedQueue<COMMANDS> commandQueue;
	private Queue<BasketBall> ballPool;
	private StringBuilder messageBuilder;
	private COMMANDS lastCommand;
	private boolean lost;
	private int frameCounter;
	private int lastIndex;
	private int score;

	public BallSpinnerScript(MainActivity core) {
		this.core = core;
	}

	@Override
	public void onInit(GVRContext ctx) throws Throwable {

		context = ctx;
		scene = ctx.getMainScene();
		scene.setFrustumCulling(true);

		float r = 5f / 255f;
		float g = 5f / 255f;
		float b = 55f / 255f;

		FutureWrapper<GVRMesh> futureQuadMesh = new FutureWrapper<GVRMesh>(
				ctx.createQuad(CUBE_WIDTH, CUBE_WIDTH));

		Future<GVRTexture> futureCubemapTexture = ctx
				.loadFutureCubemapTexture(new GVRAndroidResource(ctx,
						R.raw.beach));

		GVRMaterial cubemapMaterial = new GVRMaterial(ctx,
				GVRMaterial.GVRShaderType.Cubemap.ID);
		cubemapMaterial.setMainTexture(futureCubemapTexture);

		// surrounding cube
		GVRSceneObject frontFace = new GVRSceneObject(ctx, futureQuadMesh,
				futureCubemapTexture);
		frontFace.getRenderData().setMaterial(cubemapMaterial);
		frontFace.setName("front");
		scene.addSceneObject(frontFace);
		frontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

		GVRSceneObject backFace = new GVRSceneObject(ctx, futureQuadMesh,
				futureCubemapTexture);
		backFace.getRenderData().setMaterial(cubemapMaterial);
		backFace.setName("back");
		scene.addSceneObject(backFace);
		backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
		backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

		GVRSceneObject leftFace = new GVRSceneObject(ctx, futureQuadMesh,
				futureCubemapTexture);
		leftFace.getRenderData().setMaterial(cubemapMaterial);
		leftFace.setName("left");
		scene.addSceneObject(leftFace);
		leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
		leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

		GVRSceneObject rightFace = new GVRSceneObject(ctx, futureQuadMesh,
				futureCubemapTexture);
		rightFace.getRenderData().setMaterial(cubemapMaterial);
		rightFace.setName("right");
		scene.addSceneObject(rightFace);
		rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
		rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

		GVRSceneObject topFace = new GVRSceneObject(ctx, futureQuadMesh,
				futureCubemapTexture);
		topFace.getRenderData().setMaterial(cubemapMaterial);
		topFace.setName("top");
		scene.addSceneObject(topFace);
		topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
		topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

		GVRSceneObject bottomFace = new GVRSceneObject(ctx, futureQuadMesh,
				futureCubemapTexture);
		bottomFace.getRenderData().setMaterial(cubemapMaterial);
		bottomFace.setName("bottom");
		scene.addSceneObject(bottomFace);
		bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f, 0.0f);
		bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);

		animationEngine = context.getAnimationEngine();

		// head-tracking pointer
		GVRTexture pTexture = ctx.loadTexture(new GVRAndroidResource(ctx,
				"headtrackingpointer.png"));
		GVRSceneObject headTracker = new GVRSceneObject(ctx, 0.05f, 0.05f,
				pTexture);

		headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
		headTracker.getRenderData().setDepthTest(false);
		headTracker.getRenderData().setRenderingOrder(100000);
		mainCamera = scene.getMainCameraRig();
		mainCamera.addChildObject(headTracker);

		mainCamera.getLeftCamera().setBackgroundColor(r, g, b, 1.0f);
		mainCamera.getRightCamera().setBackgroundColor(r, g, b, 1.0f);
		mainCamera.getTransform().setPosition(0f, 0f, 0f);

		root = new GVRSceneObject(ctx);
		scene.addSceneObject(root);

		textPanel = new GVRTextViewSceneObject(context, 7, 4, "");

		// set the scene object position
		textPanel.setTextColor(Color.GREEN);
		textPanel.setTextSize(textPanel.getTextSize() * 0.55f);
		textPanel.setRefreshFrequency(IntervalFrequency.MEDIUM);
		// add the scene object to the scene graph
		scene.addSceneObject(textPanel);
		textPanel.getTransform().setPositionZ(-1.5f);
		textPanel.getTransform().setPositionY(-2.5f);
		textPanel.getTransform().setPositionX(1.5f);
		textPanel.getTransform().setRotationByAxis(-45, 1, 0, 0);

		messageBuilder = new StringBuilder();
		commandQueue = new ConcurrentLinkedQueue<COMMANDS>();
		ballPool = new LinkedList<>();

		clearValues();
	}

	private void clearValues() {
		commandQueue.clear();
		for (BasketBall ball : ballPool) {
			root.removeChildObject(ball.getVrObject());
		}
		ballPool.clear();
		lastCommand = COMMANDS.NONE;
		frameCounter = 0;
		lastIndex = 0;
		score = 0;
		lost = false;
		initBusObjectPool();
	}

	@Override
	public void onStep() {

		frameCounter++;

		if (frameCounter % 50 == 0) {
			for (BasketBall vel : ballPool) {
				if (vel.duration >= GAME_OVER_REVOLUTION_DURATION) {
					lost = true;
					break;
				}
				if (connected) {
					vel.duration *= 1.1;
				} else {
					vel.duration *= 1.1;
				}
				vel.counterClockwise(vel.getVrObject(), vel.duration);
				if (!lost) {
					score += ((GAME_OVER_REVOLUTION_DURATION * 10) - (vel.duration * 10f)) / 5;
				}
			}
		}

		COMMANDS command = this.commandQueue.poll();
		if (command != null) {
			this.lastCommand = command;
		} else {
			command = COMMANDS.NONE;
		}

		if (lost) {
			messageBuilder
					.append("GameOver\nScore: " + Integer.toString(score));
		} else {
			messageBuilder.append("Score: " + Integer.toString(score));
		}
		messageBuilder.append("\nLast Command: " + lastCommand.toString());
		messageBuilder.append("\nServer Status: "
				+ (connected ? "Connected" : "Disconnected"));

		textPanel.setText(messageBuilder.toString());
		messageBuilder.setLength(0);

		switch (command) {
		case LEFT:
			for (GVREyePointeeHolder eph : GVRPicker.pickScene(context
					.getMainScene())) {
				for (BasketBall vel : ballPool) {
					if (eph.getOwnerObject().equals(vel.getVrObject())) {
						if (vel.duration >= 1) {
							if (connected) {
								vel.duration *= 0.075;
							} else {
								vel.duration *= 0.75;
							}
							vel.counterClockwise(vel.getVrObject(),
									vel.duration);
						}
						break;
					}
				}
			}
			break;
		case UP:
			createBall();
			break;
		case RESET:
			clearValues();
			break;
		case CONNECTED:
			connected = true;
			break;
		case DISCONNECTED:
			connected = false;
			break;
		case NONE:
			break;
		}

		if (!lost && ((frameCounter + 1) % 350) == 0) {
			createBall();
		}

		for (BasketBall vel : ballPool) {
			vel.getVrObject().getRenderData().getMaterial()
					.setColor(1.0f, 1.0f, 1.0f);
		}

		for (GVREyePointeeHolder eph : GVRPicker.pickScene(context
				.getMainScene())) {
			eph.getOwnerObject()
					.getRenderData()
					.getMaterial()
					.setColor(LOOKAT_COLOR_MASK_R, LOOKAT_COLOR_MASK_G,
							LOOKAT_COLOR_MASK_B);
			break;
		}
	}

	private void initBusObjectPool() {
		for (int i = 0; i < STARTING_BALLS; i++) {
			createBall();
		}
	}

	void createBall() {
		BasketBall ball = new BasketBall();
		ball.init(context, animationEngine, lastIndex);
		ballPool.add(ball);
		root.addChildObject(ball.getVrObject());
		ball.counterClockwise(ball.getVrObject(), ball.duration);
		if (lastIndex <= 0) {
			lastIndex--;
		}
		lastIndex *= -1;
	}

	public void handleLongPress() {
		commandQueue.add(COMMANDS.RESET);
	}

	public void setCommand(COMMANDS command) {
		if (COMMANDS.DISCONNECTED == command && !this.connected) {
			// We may want to use this for something later.
		} else if (COMMANDS.CONNECTED == command && this.connected) {
			// We may want to use this for something later too.
		} else {
			commandQueue.add(command);
		}
	}
}
