package org.gearvrf.sample.gvrcamera2renderscript;

import android.graphics.ImageFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.view.Surface;

public class RenderscriptProcessor 
{
	private Allocation mInputAllocation;
	private Allocation mInterAllocation;
    private Allocation mOutputAllocation;

    private HandlerThread mProcessingThread;
    private Handler mProcessingHandler;

    private ScriptC_mono mScript;
    private ScriptIntrinsicYuvToRGB mScriptIntrinsic;
    public ProcessingTask mTask;
    
    private boolean mNeedYuvConversion = false;
    
    public RenderscriptProcessor(RenderScript rs, int width, int height)
    {
    	String model = Build.MODEL;
    	Log.d("MODEL",model);
    	if( model.contains("SM-G920") || model.contains("SM-G925") || model.contains("SM-G928") )
    	{
    		mNeedYuvConversion = true;
    	}
    	
    	Type.Builder yuvTypeBuilder = new Type.Builder(rs, Element.U8_4(rs));
        yuvTypeBuilder.setX(width);
        yuvTypeBuilder.setY(height);
        yuvTypeBuilder.setYuvFormat(ImageFormat.YUV_420_888);
        
        Type.Builder rgbTypeBuilder = new Type.Builder(rs, Element.RGBA_8888(rs));
        rgbTypeBuilder.setX(width);
        rgbTypeBuilder.setY(height);
        
        if( mNeedYuvConversion )
        {
        	mInputAllocation = Allocation.createTyped(rs, yuvTypeBuilder.create(),
                Allocation.USAGE_IO_INPUT | Allocation.USAGE_SCRIPT);
        	
            mInterAllocation = Allocation.createTyped(rs, rgbTypeBuilder.create(),
                    Allocation.USAGE_SCRIPT);
            
            mScriptIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        }
        else
        {
        	mInputAllocation = Allocation.createTyped(rs, rgbTypeBuilder.create(),
                    Allocation.USAGE_IO_INPUT | Allocation.USAGE_SCRIPT);
        }
        
        mOutputAllocation = Allocation.createTyped(rs, rgbTypeBuilder.create(),
                Allocation.USAGE_IO_OUTPUT | Allocation.USAGE_SCRIPT);

        mScript = new ScriptC_mono(rs);
        
        mProcessingThread = new HandlerThread("EffectProcessor");
        mProcessingThread.start();
        mProcessingHandler = new Handler(mProcessingThread.getLooper());

        mTask = new ProcessingTask(mInputAllocation, mNeedYuvConversion);
    }
    
    public void release()
    {
        mTask.release();
        mProcessingHandler.removeCallbacks(mTask);
        mProcessingThread.quit();
    }
    
    public Surface getInputSurface() {
        return mInputAllocation.getSurface();
    }

    public void setOutputSurface(Surface output) {
        mOutputAllocation.setSurface(output);
    }
    
    class ProcessingTask implements Runnable, Allocation.OnBufferAvailableListener {
        private int mPendingFrames = 0;
        private Allocation mInputAllocation;
        private boolean mNeedYuvConversion;

        public ProcessingTask(Allocation input, boolean needYuvConversion) {
            mInputAllocation = input;
            mInputAllocation.setOnBufferAvailableListener(this);
            mNeedYuvConversion = needYuvConversion;
        }

        public void release()
        {
            mInputAllocation.setOnBufferAvailableListener(null);
        }

        @Override
        public void onBufferAvailable(Allocation a) {
            synchronized(this) {
                mPendingFrames++;
                mProcessingHandler.post(this);
            }
        }

        @Override
        public void run() {

            // Find out how many frames have arrived
            int pendingFrames;
            synchronized(this) {
                pendingFrames = mPendingFrames;
                mPendingFrames = 0;
                mProcessingHandler.removeCallbacks(this);
            }

            // Get to newest input
            for (int i = 0; i < pendingFrames; i++) {
                mInputAllocation.ioReceive();
            }

            if( mNeedYuvConversion )
            {
	            mScriptIntrinsic.setInput(mInputAllocation);
	            mScriptIntrinsic.forEach(mInterAllocation);
	            
	            mScript.forEach_root(mInterAllocation, mOutputAllocation);
            }
            else
            {
            	mScript.forEach_root(mInputAllocation, mOutputAllocation);
            }

            mOutputAllocation.ioSend();
        }
    }
}
