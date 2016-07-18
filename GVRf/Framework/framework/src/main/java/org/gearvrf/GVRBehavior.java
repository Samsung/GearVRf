/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf;

import java.lang.reflect.Method;

/**
 * Base class for adding user-defined behaviors to a scene object.
 * You can override callbacks for initialization and per-frame updates.
 * 
 * This class listens for draw frame events when it is attached to a scene object.
 * You can override these callbacks to implement custom components.
 * - onDrawFrame(float frametime) called once every frame before rendering.
 * - onAttach(GVRSceneObject) called when this behavior is attached to a scene object.
 * - onDetach(GVRSceneObject) called when this behavior is detached from a scene object.
 * 
 * @see GVRComponent
 * @see GVRSceneObject.attachComponent
 * @see GVRSceneObject.getComponent
 * @see GVRSceneObject.attachComponent
 * @see GVRSceneObject.detachComponent
 */
public class GVRBehavior extends GVRComponent implements GVRDrawFrameListener
{
    protected boolean mIsListening;
    protected boolean mHasFrameCallback;
    static private long TYPE_BEHAVIOR = newComponentType(GVRBehavior.class);

    /**
     * Constructor for a behavior.
     *
     * @param gvrContext    The current GVRF context
     */
    protected GVRBehavior(GVRContext gvrContext)
    {
        this(gvrContext, 0);
        mType = getComponentType();
    }
    
    /**
     * Constructor for a behavior.
     *
     * @param gvrContext    The current GVRF context
     * @param nativePointer Pointer to the native object, returned by the native constructor
     */
    protected GVRBehavior(GVRContext gvrContext, long nativePointer)
    {
        super(gvrContext, nativePointer);
        mIsListening = false;
        mHasFrameCallback = isImplemented("onDrawFrame", float.class);
    }    

    static public long getComponentType() { return TYPE_BEHAVIOR; }
    
    static protected long newComponentType(Class<? extends GVRBehavior> clazz)
    {
        long hash = (long) clazz.hashCode() << 32;
        long t = ((long) System.currentTimeMillis() & 0xfffffff);
        long result = hash | t;
        return result;
    }
    
    @Override
    public void onEnable()
    {
        startListening();
    }

    @Override
    public void onDisable()
    {
        stopListening();
    }
    
    /**
     * Called when this behavior is attached to a scene object.
     * 
     * Attaching a behavior to a scene object will cause it
     * to start listening to scene events.
     * 
     * @param newOwner  GVRSceneObject the behavior is attached to.
     */
    @Override
    public void onAttach(GVRSceneObject newOwner)
    {
        startListening();
    }
    
    /**
     * Called when this behavior is detached from a scene object.
     * 
     * Detaching a behavior from a scene object will cause it
     * to stop listening to scene events (onStep won't be called).
     *
     * @param oldOwner  GVRSceneObject the behavior was detached from.
     */
    @Override
    public void onDetach(GVRSceneObject oldOwner)
    {
        stopListening();
    }
    
    /**
     * Called each frame before rendering the scene.
     * It is not called if this behavior is not attached
     * to a {@link GVRSceneObject}.
     */
    public void onDrawFrame(float frameTime) { }
    
    protected void startListening()
    {
        if (mHasFrameCallback && !mIsListening)
        {
            getGVRContext().registerDrawFrameListener(this);
            mIsListening = true;            
        }
    }
    
    protected void stopListening()
    {
        if (mIsListening)
        {
            getGVRContext().unregisterDrawFrameListener(this);
            mIsListening = false;
        }        
    }
    
    protected boolean isImplemented(String methodName, Class<?> ...paramTypes)
    {
        try
        {
            Class<? extends Object> clazz = getClass();
            String name1 = clazz.getSimpleName();
            Method method = clazz.getMethod(methodName, paramTypes);
            Class<? extends Object> declClazz = method.getDeclaringClass();
            String name2 = declClazz.getSimpleName();
            return declClazz.equals(clazz);
        }
        catch (SecurityException e)
        {
            return false;
        }
        catch (NoSuchMethodException e)
        {
            return false;
        }
    }

}


