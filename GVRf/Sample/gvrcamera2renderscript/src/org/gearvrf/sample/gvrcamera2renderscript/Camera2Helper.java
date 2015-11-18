package org.gearvrf.sample.gvrcamera2renderscript;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;

public class Camera2Helper {

	private Activity mActivity;
	private Size[] mAvailableSizes;
	private Size mPreferredSize;
	private CameraManager mCameraManager;
	private String mCameraId;
	private CameraDevice mCameraDevice;
	private List<Surface> mTargetSurfaces;
	private CaptureRequest.Builder mPreviewBuilder;
	private CameraCaptureSession mPreviewSession;
	
	public Camera2Helper(Activity activity, int cameraIndex) throws CameraAccessException
	{
		mActivity = activity;
		mTargetSurfaces = new ArrayList<Surface>();
		
		mCameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
		mCameraId = mCameraManager.getCameraIdList()[cameraIndex];
		CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
		StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
		mAvailableSizes = map.getOutputSizes(SurfaceTexture.class);
	}
	
	public Size[] getOutputSizes()
	{
		return mAvailableSizes;
	}
	
	public Size setPreferredSize(int width, int height)
	{
		int pixels = width * height;
		int candidateIndex = 0;
		int candidatePixels = 0;
		for(int i=0; i<mAvailableSizes.length; i++)
		{
			Size s = mAvailableSizes[i];
			int product = s.getWidth() * s.getHeight();
			if( pixels == product )
			{
				mPreferredSize = s;
				return mPreferredSize;
			}
			else if( pixels > product && product > candidatePixels )
			{
				candidateIndex = i;
				candidatePixels = product;
			}
		}
		return mAvailableSizes[candidateIndex];
	}
	
	public Size getPreferredSize()
	{
		return mPreferredSize;
	}
	
	public void startCapture(Surface surface) throws CameraAccessException
	{
		mTargetSurfaces.add(surface);
		
		HandlerThread thread = new HandlerThread("CameraOpen");
		thread.start();
		Handler openHandler = new Handler(thread.getLooper());
		mCameraManager.openCamera(mCameraId, mStateCallback, openHandler);
	}
	
	public void startCapture(List<Surface> surfaces) throws CameraAccessException
	{
		mTargetSurfaces = surfaces;
		
		HandlerThread thread = new HandlerThread("CameraOpen");
		thread.start();
		Handler openHandler = new Handler(thread.getLooper());
		mCameraManager.openCamera(mCameraId, mStateCallback, openHandler);
	}
	
	private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

		@Override
		public void onOpened(CameraDevice camera) {
			mCameraDevice = camera;
			startPreview();
		}

		@Override
		public void onDisconnected(CameraDevice camera) {
		}

		@Override
		public void onError(CameraDevice camera, int error) {
		}

	};
	
	protected void startPreview() 
	{	
		try {
			mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,	CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
			for(Surface s : mTargetSurfaces)
			{
				mPreviewBuilder.addTarget(s);
			}
			mCameraDevice.createCaptureSession(mTargetSurfaces, mCaptureCallback, null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}
	
	private CameraCaptureSession.StateCallback mCaptureCallback = new CameraCaptureSession.StateCallback() 
	{
		@Override
		public void onConfigured(CameraCaptureSession session) 
		{	
			mPreviewSession = session;
			updatePreview();
		}

		@Override
		public void onConfigureFailed(CameraCaptureSession session) 
		{

		}
	};
	
	protected void updatePreview() {
		mPreviewBuilder.set(CaptureRequest.CONTROL_MODE,CameraMetadata.CONTROL_MODE_AUTO);
		
		HandlerThread thread = new HandlerThread("CameraPreview");
		thread.start();
		Handler backgroundHandler = new Handler(thread.getLooper());

		try 
		{
			mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
		} catch (CameraAccessException e) 
		{
			e.printStackTrace();
		}
	}

	public void closeCamera() 
	{
		try 
		{
			if (null != mCameraDevice) 
			{
				mCameraDevice.close();
				mCameraDevice = null;
			}
		} 
		catch (IllegalStateException ie) 
		{
			ie.printStackTrace();
		}

	}
}
