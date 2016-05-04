package org.gearvrf;

class NativeGLDelete {
    static native long ctor();
    static native void dtor(long ptr);
    static native void processQueues(long ptr);
    static native void createTlsKey();
}
