package org.gearvrf.scene_objects;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GVRTextViewSceneObject extends GVRSceneObject implements
		GVRDrawFrameListener {
	private static final int REFRESH_INTERVAL = 30; // frames

	private final Surface mSurface;
	private final SurfaceTexture mSurfaceTexture;
	private final LinearLayout mTextViewContainer;
	private final TextView mTextView;
	private static int DEFAULT_WIDTH = 2000;
	private static int DEFAULT_HEIGHT = 1000;
	private static float DEFAULT_QUAD_WIDTH = 2.0f;
	private static float DEFAULT_QUAD_HEIGHT = 2.0f;
	private int mCount = 0;

	/**
	 * Shows a {@link TextView} on a {@linkplain GVRSceneObject scene object}.
	 * 
	 * @param gvrContext
	 *            current {@link GVRContext}
	 * @param mesh
	 *            a {@link GVRMesh} - see
	 *            {@link GVRContext#loadMesh(org.gearvrf.GVRAndroidResource)}
	 *            and {@link GVRContext#createQuad(float, float)}
	 * @param gvrActivity
	 *            a {@link GVRActivity}
	 * @param viewWidth
	 *            Width of the {@link TextView}
	 * @param viewHeight
	 *            Height of the {@link TextView}
	 * @param text
	 *            {@link String} to show on the textView
	 */

	public GVRTextViewSceneObject(GVRContext gvrContext, GVRMesh mesh,
			GVRActivity gvrActivity, int viewWidth, int viewHeight, String text) {
		super(gvrContext, mesh);
		mTextView = new TextView(gvrActivity);
		mTextView.setLayoutParams(new LayoutParams(viewWidth, viewHeight));
		mTextView.measure(viewWidth, viewHeight);
		mTextView.setBackgroundColor(Color.TRANSPARENT);
		mTextView.setText(text);
		mTextView.setVisibility(View.VISIBLE);
		mTextViewContainer = new LinearLayout(gvrActivity);
		mTextViewContainer.addView(mTextView);
		mTextViewContainer.measure(viewWidth, viewHeight);
		mTextViewContainer.layout(0, 0, viewWidth, viewHeight);
		mTextViewContainer.setVisibility(View.VISIBLE);
		gvrContext.registerDrawFrameListener(this);
		GVRTexture texture = new GVRExternalTexture(gvrContext);
		GVRMaterial material = new GVRMaterial(gvrContext, GVRShaderType.OES.ID);
		material.setMainTexture(texture);
		getRenderData().setMaterial(material);

		mSurfaceTexture = new SurfaceTexture(texture.getId());
		mSurface = new Surface(mSurfaceTexture);
		mSurfaceTexture.setDefaultBufferSize(mTextViewContainer.getWidth(),
				mTextViewContainer.getHeight());
	}

	/**
	 * Shows a {@link TextView} on a {@linkplain GVRSceneObject scene object}.
	 * 
	 * @param gvrContext
	 *            current {@link GVRContext}
	 * @param gvrActivity
	 *            a {@link GVRActivity}
	 * @param width
	 *            width for createQuad to get a new {@link GVRMesh}
	 * @param height
	 *            height for createQuad to get a new {@link GVRMesh}
	 * @param viewWidth
	 *            Width of the {@link TextView}
	 * @param viewHeight
	 *            Height of the {@link TextView}
	 * @param text
	 *            {@link String} to show on the textView
	 */

	public GVRTextViewSceneObject(GVRContext gvrContext,
			GVRActivity gvrActivity, float width, float height, int viewWidth,
			int viewHeight, String text) {
		this(gvrContext, gvrContext.createQuad(width, height), gvrActivity,
				viewWidth, viewHeight, text);
	}

	/**
	 * Shows a {@link TextView} on a {@linkplain GVRSceneObject scene object}
	 * with view default height and width.
	 * 
	 * @param gvrContext
	 *            current {@link GVRContext}
	 * @param mesh
	 *            a {@link GVRMesh} - see
	 *            {@link GVRContext#loadMesh(org.gearvrf.GVRAndroidResource)}
	 *            and {@link GVRContext#createQuad(float, float)}
	 * @param gvrActivity
	 *            a {@link GVRActivity}
	 * @param text
	 *            {@link String} to show on the textView
	 */

	public GVRTextViewSceneObject(GVRContext gvrContext, GVRMesh mesh,
			GVRActivity gvrActivity, String text) {
		this(gvrContext, mesh, gvrActivity, DEFAULT_WIDTH, DEFAULT_HEIGHT, text);
	}

	/**
	 * Shows a {@link TextView} on a {@linkplain GVRSceneObject scene object}
	 * with view's default height and width.
	 * 
	 * @param gvrContext
	 *            current {@link GVRContext}
	 * @param gvrActivity
	 *            a {@link GVRActivity}
	 * @param width
	 *            width for createQuad to get a new {@link GVRMesh}
	 * @param height
	 *            height for createQuad to get a new {@link GVRMesh}
	 * @param text
	 *            {@link String} to show on the textView
	 */

	public GVRTextViewSceneObject(GVRContext gvrContext,
			GVRActivity gvrActivity, float width, float height, String text) {
		this(gvrContext, gvrContext.createQuad(width, height), gvrActivity,
				text);
	}

	/**
	 * Shows a {@link TextView} on a {@linkplain GVRSceneObject scene object}
	 * with both view's default height and width and quad's default height and
	 * width.
	 * 
	 * @param gvrContext
	 *            current {@link GVRContext}
	 * @param gvrActivity
	 *            a {@link GVRActivity}
	 * @param text
	 *            {@link String} to show on the textView
	 */

	public GVRTextViewSceneObject(GVRContext gvrContext,
			GVRActivity gvrActivity, String text) {
		this(gvrContext, gvrActivity, DEFAULT_QUAD_WIDTH, DEFAULT_QUAD_HEIGHT,
				text);
	}

	/**
	 * Shows a {@link TextView} on a {@linkplain GVRSceneObject scene object}
	 * with both view's default height and width and quad's default height and
	 * width. And the default text of this TextView will be "Hello World!"
	 * 
	 * @param gvrContext
	 *            current {@link GVRContext}
	 * @param gvrActivity
	 *            a {@link GVRActivity}
	 */
	public GVRTextViewSceneObject(GVRContext gvrContext, GVRActivity gvrActivity) {
		this(gvrContext, gvrActivity, "Hello World!");
	}

	/**
	 * Set the text size.
	 * 
	 * @param newSize
	 *            The new text size.
	 */
	public void setTextSize(float size) {
		mTextView.setTextSize(size);
	}

	/**
	 * Get the current text size.
	 * 
	 * @return The current text size.
	 */
	public float getTextSize() {
		return mTextView.getTextSize();
	}

	/**
	 * Set the text color.
	 * 
	 * @param color
	 *            The text color, in Android {@link Color} format. The
	 *            {@linkplain Color#alpha(int) alpha component} is ignored.
	 */
	public void setTextColor(int color) {
		mTextView.setTextColor(color);
	}

	/**
	 * Set the text string to be displayed.
	 * 
	 * @param string
	 *            The new text string to be displayed.
	 */
	public void setText(String str) {
		mTextView.setText(str);
	}

	/**
	 * Set the view's background color.
	 * 
	 * @param color
	 *            The view's background color, in Android {@link Color} format.
	 *            The {@linkplain Color#alpha(int) alpha component} is ignored.
	 */
	public void setBackgroundColor(int color) {
		mTextViewContainer.setBackgroundColor(color);
	}

	/**
	 * Set the view's background with resource drawable.
	 * 
	 * @param drawable
	 *            The view's background.
	 */
	public void setBackGround(Drawable drawable) {
		mTextViewContainer.setBackground(drawable);
	}

	/**
	 * Get the current text string.
	 * 
	 * @return The current text string that is being displayed.
	 */
	public String getText() {
		return mTextView.getText().toString();
	}
	/**
	 * Set the current view's gravity.
	 * 
	 * @param gravity
	 * 			The gravity of this TextView;
	 */
	public void setGravity(int gravity){
		mTextView.setGravity(gravity);
	}
	
	/**
	 * This is actually a callback for GVRDrawFrameListener. It shouldn't get
	 * called from app side. The only reason for this to be public is for
	 * inheritance.
	 * 
	 * @param Current
	 *            frame time.
	 */
	@Override
	public void onDrawFrame(float frameTime) {
		if (++mCount > REFRESH_INTERVAL) {
			refresh();
			mCount = 0;
		}
	}

	/** Draws the {@link WebView} onto {@link #mSurfaceTexture} */
	private void refresh() {
		try {
			Canvas canvas = mSurface.lockCanvas(null);
			mTextViewContainer.draw(canvas);
			mSurface.unlockCanvasAndPost(canvas);
		} catch (Surface.OutOfResourcesException t) {
			Log.e("GVRTextViewObject", "lockCanvas failed");
		}
		mSurfaceTexture.updateTexImage();
	}

}
