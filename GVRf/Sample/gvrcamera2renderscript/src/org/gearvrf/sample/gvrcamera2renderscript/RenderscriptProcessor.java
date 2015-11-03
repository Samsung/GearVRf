package org.gearvrf.sample.gvrcamera2renderscript;

import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.view.Surface;

public class RenderscriptProcessor 
{
	private Allocation mInputAllocation;
    private Allocation mOutputAllocation;

    private HandlerThread mProcessingThread;
    private Handler mProcessingHandler;

    private ScriptC_mono mScript;
    public ProcessingTask mTask;
    
    public RenderscriptProcessor(RenderScript rs, int width, int height)
    {
    	Type.Builder yuvTypeBuilder = new Type.Builder(rs, Element.RGBA_8888(rs));
        yuvTypeBuilder.setX(width);
        yuvTypeBuilder.setY(height);
        mInputAllocation = Allocation.createTyped(rs, yuvTypeBuilder.create(),
                Allocation.USAGE_IO_INPUT | Allocation.USAGE_SCRIPT);

        Type.Builder rgbTypeBuilder = new Type.Builder(rs, Element.RGBA_8888(rs));
        rgbTypeBuilder.setX(width);
        rgbTypeBuilder.setY(height);
        mOutputAllocation = Allocation.createTyped(rs, rgbTypeBuilder.create(),
                Allocation.USAGE_IO_OUTPUT | Allocation.USAGE_SCRIPT);

        mScript = new ScriptC_mono(rs);
        
        mProcessingThread = new HandlerThread("EffectProcessor");
        mProcessingThread.start();
        mProcessingHandler = new Handler(mProcessingThread.getLooper());

        mTask = new ProcessingTask(mInputAllocation);
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

        public ProcessingTask(Allocation input) {
            mInputAllocation = input;
            mInputAllocation.setOnBufferAvailableListener(this);
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

            mScript.forEach_root(mInputAllocation, mOutputAllocation);

            mOutputAllocation.ioSend();
        }
    }
}
