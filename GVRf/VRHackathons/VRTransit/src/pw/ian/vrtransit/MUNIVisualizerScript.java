package pw.ian.vrtransit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import pw.ian.vrtransit.data.BusUpdate;
import pw.ian.vrtransit.data.TransitDataAccessor;
import android.graphics.Color;
import android.util.Log;

public class MUNIVisualizerScript extends GVRScript {

	private MainActivity core;

	private GVRContext mCtx;

	private GVRMesh busMesh;

	private GVRTexture busTex;

	private GVRTexture trainTex;

	private GVRTexture mapTex;

	private TransitDataAccessor tda;

	private GVRSceneObject root;

	private GVRSceneObject map;

	private Map<String, GVRSceneObject> vehicles = new HashMap<>();

	public MUNIVisualizerScript(MainActivity core) {
		this.core = core;
	}

	@Override
	public void onInit(GVRContext ctx) throws Throwable {

		mCtx = ctx;

		GVRScene scene = ctx.getMainScene();
		scene.setFrustumCulling(true);

		float r = 126f / 255f;
		float g = 192 / 255f;
		float b = 238 / 255f;

		scene.getMainCameraRig().getLeftCamera()
				.setBackgroundColor(r, g, b, 1.0f);
		scene.getMainCameraRig().getRightCamera()
				.setBackgroundColor(r, g, b, 1.0f);
		scene.getMainCameraRig().getTransform()
				.setPosition(0f, 0f, 0f);

		busMesh = ctx.loadMesh(new GVRAndroidResource(ctx, "cube.obj"));

		busTex = ctx.loadTexture(new GVRAndroidResource(ctx, "bus.jpg"));
		busTex.setKeepWrapper(true);

		trainTex = ctx.loadTexture(new GVRAndroidResource(ctx, "train.jpg"));
		trainTex.setKeepWrapper(true);

		mapTex = ctx.loadTexture(new GVRAndroidResource(ctx, "map.jpg"));

		root = new GVRSceneObject(ctx);
		scene.addSceneObject(root);

		map = new GVRSceneObject(ctx, 10f, 10f, mapTex);
		map.getTransform().setPosition(0f, 0f, -5f);
		root.addChildObject(map);

		initBusObjectPool(Constants.MAX_OBJECTS);
		initVehicles();
	}

	@Override
	public void onStep() {
		List<BusUpdate> bs = tda.nextUpdates();
		for (BusUpdate bu : bs) {
			if (bu.getRoute().equals("25"))
				continue;
			if (vehicles.containsKey(bu.getId())) {

				if (bu.remove) {
					GVRSceneObject bus = vehicles.remove(bu.getId());
				} else {
					GVRSceneObject bus = vehicles.get(bu.getId());
					smoothSetBusPos(bus, bu.getLat(), bu.getLon());
				}

			} else {
				GVRSceneObject bus = setBusPos(nextBus(), bu.getLat(),
						bu.getLon());
				if (vehicles.containsValue(bus)) {
					String key = null;
					for (Entry<String, GVRSceneObject> e : vehicles.entrySet()) {
						if (e.getValue().equals(bus)) {
							key = e.getKey();
							break;
						}
					}
					vehicles.remove(key);
				}
				if (bu.getType().equals("train")) {
					bus.getTransform().setScale(0.15f, 0.05f, 0.05f);
					bus.getRenderData().getMaterial().setMainTexture(trainTex);
				} else {
					bus.getTransform().setScale(0.05f, 0.05f, 0.05f);
					bus.getRenderData().getMaterial().setMainTexture(busTex);
				}
				vehicles.put(bu.getId(), bus);
			}
		}
	}

	private Queue<GVRSceneObject> busPool = new LinkedList<>();

	private void initBusObjectPool(int amt) {
		for (int i = 0; i < amt; i++) {
			busPool.add(resetPos(constructBus(mCtx)));
		}
	}

	private GVRSceneObject resetPos(GVRSceneObject bus) {
		bus.getTransform().setPosition(-1000f, -1000f, -1000f);
		return bus;
	}

	private GVRSceneObject nextBus() {
		GVRSceneObject bus = busPool.poll();
		busPool.add(bus);
		return bus;
	}

	private GVRSceneObject constructBus(GVRContext ctx) {
		GVRSceneObject bus = new GVRSceneObject(ctx, busMesh, busTex);
		root.addChildObject(bus);
		return bus;
	}

	public GVRSceneObject smoothSetBusPos(GVRSceneObject bus, double lat,
			double lon) {

		// 37.809607, -122.387515
		// 37.734027, -122.514716

		float dx = scaleCoordX((float) lat, 5f)
				- bus.getTransform().getPositionX();
		float dy = scaleCoordY((float) lon, 5f)
				- bus.getTransform().getPositionY();

		GVRAnimation anim = new GVRRelativeMotionAnimation(bus, 1.0f, dx, dy,
				0f);
		anim.start(mCtx.getAnimationEngine());
		return bus;
	}

	public GVRSceneObject setBusPos(GVRSceneObject bus, double lat, double lon) {

		// 37.809607, -122.387515
		// 37.734027, -122.514716

		lat = scaleCoordX((float) lat, 5f);
		lon = scaleCoordY((float) lon, 5f);
		bus.getTransform().setPosition((float) lat, (float) lon, -5f);
		return bus;
	}

	/**
	 * Scales a coordinate
	 * 
	 * @param min
	 * @param max
	 * @param val
	 * @param extent
	 * @return
	 */
	private float scaleCoord(float min, float max, float val, float extent) {
		float fmin = Math.min(min, max);
		float fmax = Math.max(min, max);

		float diff = fmax - fmin;
		float scale = (val - fmin) / diff;
		return ((extent * 2 * scale) - extent) * 0.8f;
	}

	private float scaleCoordX(float val, float extent) {
		return scaleCoord(37.702100f, 37.814604f, val, extent);
	}

	private float scaleCoordY(float val, float extent) {
		return scaleCoord(-122.553643f, -122.35528f, val, extent);
	}

	boolean zoom = true;

	float xc;
	float yc;
	float zc;

	GVRAnimation zoomAnim;

	public void handleTap() {
		if (zoomAnim != null && !zoomAnim.isFinished()) {
			return;
		}

		Log.i("VRTransit", "Event fired");
		GVRCameraRig rig = mCtx.getMainScene().getMainCameraRig();
		if (zoom) {
			float[] look = rig.getLookAt();

			if (look[2] >= 0) {
				Log.i("VRTransit", "Not facing: " + Arrays.toString(look));
				return;
			}
			float ratio = Constants.DIST / look[2];

			float xct = look[0] * ratio * Constants.ZOOM_FACTOR;
			float yct = look[1] * ratio * Constants.ZOOM_FACTOR;
			float zct = Constants.DIST * Constants.ZOOM_FACTOR;

			float distSq = xct * xct + yct * yct + zct * zct;
			if (distSq > Constants.MAX_DIST_SQ) {
				Log.i("VRTransit", "Too far: " + distSq + " vs "
						+ Constants.MAX_DIST_SQ);
				return;
			}

			xc = xct;
			yc = yct;
			zc = zct;

			Log.i("VRTransit", "Move to " + xc + " " + yc + " " + zc);

			zoomAnim = new GVRRelativeMotionAnimation(rig.getTransform(),
					1.0f, xc, yc, zc);
			zoomAnim.start(mCtx.getAnimationEngine());
		} else {
			Log.i("VRTransit", "Move to " + -xc + " " + -yc + " " + -zc);
			zoomAnim = new GVRRelativeMotionAnimation(rig.getTransform(),
					1.0f, -xc, -yc, -zc);
			zoomAnim.start(mCtx.getAnimationEngine());
		}
		zoom = !zoom;
	}

	String type = "bus";

	public void handleLongPress() {
		if (type.equals("bus")) {
			type = "train";
		} else {
			type = "bus";
		}
		initVehicles();
	}

	public void initVehicles() {
		for (Entry<String, GVRSceneObject> e : vehicles.entrySet()) {
			resetPos(e.getValue());
		}
		vehicles.clear();
		tda = new TransitDataAccessor(type);
	}
}
