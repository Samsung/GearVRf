package org.gearvrf;

import org.gearvrf.GVRHybridObject.NativeCleanupHandler;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class GVRContextPrivate {
    /**
     * Our {@linkplain GVRReference references} are placed on this queue, once
     * they've been finalized
     */
    private final ReferenceQueue<GVRHybridObject> mReferenceQueue = new ReferenceQueue<GVRHybridObject>();
    /**
     * We need hard references to {@linkplain GVRReference our references} -
     * otherwise, the references get garbage collected (usually before their
     * objects) and never get enqueued.
     */
    private final Set<GVRReference> mReferenceSet = new HashSet<GVRReference>();
    private final GVRFinalizeThread mFinalizeThread = new GVRFinalizeThread();

    final class GVRFinalizeThread extends Thread {
        private GVRFinalizeThread() {
            setName("GVRF Finalize Thread-" + Integer.toHexString(hashCode()));
            setPriority(MAX_PRIORITY);
            start();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    GVRReference reference = (GVRReference)mReferenceQueue.remove();
                    reference.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            android.util.Log.i("GVRContextPrivate", getName() + " shutting down");
        }
    }

    final class GVRReference extends PhantomReference<GVRHybridObject> {
        private long mNativePointer;
        private final List<NativeCleanupHandler> mCleanupHandlers;

        private GVRReference(GVRHybridObject object, long nativePointer,
                List<NativeCleanupHandler> cleanupHandlers) {
            super(object, mReferenceQueue);

            mNativePointer = nativePointer;
            mCleanupHandlers = cleanupHandlers;
        }

        private void close() {
            close(true);
        }

        private void close(boolean removeFromSet) {
            synchronized (mReferenceSet) {
                if (mNativePointer != 0) {
                    if (mCleanupHandlers != null) {
                        for (NativeCleanupHandler handler : mCleanupHandlers) {
                            handler.nativeCleanup(mNativePointer);
                        }
                    }
                    NativeHybridObject.delete(mNativePointer);
                    mNativePointer = 0;
                }

                if (removeFromSet) {
                    mReferenceSet.remove(this);
                }
            }
        }
    }

    void registerHybridObject(GVRHybridObject gvrHybridObject, long nativePointer, List<NativeCleanupHandler> cleanupHandlers) {
        synchronized (mReferenceSet) {
            mReferenceSet.add(new GVRReference(gvrHybridObject, nativePointer, cleanupHandlers));
        }
    }

    /**
     * Explicitly close()ing an object is going to be relatively rare - most
     * native memory will be freed when the owner-objects are garbage collected.
     * Doing a lookup in these rare cases means that we can avoid giving every @link
     * {@link GVRHybridObject} a hard reference to its {@link GVRReference}.
     */
    GVRReference findReference(long nativePointer) {
        for (GVRReference reference : mReferenceSet) {
            if (reference.mNativePointer == nativePointer) {
                return reference;
            }
        }
        return null;
    }

    final void releaseNative(GVRHybridObject hybridObject) {
        synchronized (mReferenceSet) {
            if (hybridObject.getNative() != 0L) {
                GVRReference reference = findReference(hybridObject.getNative());
                if (reference != null) {
                    reference.close();
                }
            }
        }
    }

    @Override
    public void finalize() throws Throwable {
        try {
            mFinalizeThread.interrupt();
        } catch (final Exception ignored) {
        } finally {
            super.finalize();
        }
    }
}
