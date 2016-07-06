/* Copyright 2016 Samsung Electronics Co., LTD
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

import org.gearvrf.script.GVRScriptFile;
import org.gearvrf.script.IScriptable;

/**
 * This class provides API for event-related operations in the
 * framework. In the framework, events are categorized into
 * groups, each of which is represented by an interface extending
 * the tag interface {@link IEvents}. For example,
 * the event group {@link IScriptEvents} contain life-cycle events
 * {@link IScriptEvents#onInit(GVRContext)}, and per-frame callbacks
 * {@link IScriptEvents#onStep()}.<p>
 *
 * Other event groups can be defined in similar ways. As seen above,
 * we don't necessarily use classes to represent events themselves,
 * but we may create event classes (such as {@code MouseEvent}) to
 * represent details of an event, and pass it to an event handler.<p>
 *
 * An event handler can take one of the two forms. 1) It can be a
 * class implementing an event interface in Java. 2) It can be a
 * script in a scripting language, such as Lua and Javascript. In
 * the scripting case, the prototype of the function mirrors their
 * Java counterpart. For example, a handler function in Lua for
 * {@link IScriptEvents#onInit(GVRContext)} is<p>
 *
 * <pre>
 * {@code
 * function onInit(gvrf)
 *    ...
 * end
 * }
 * </pre>
 */
public class GVREventManager {
    private static final String TAG = GVREventManager.class.getSimpleName();
    private GVRContext mGvrContext;

    // Cache for Java handler methods; keys *must* be weakly referenced
    private final WeakHashMap<Object, Map<String, Method>> mHandlerMethodCache;

    protected static final int SEND_MASK_OBJECT = 0x1;
    protected static final int SEND_MASK_LISTENERS = 0x2;
    protected static final int SEND_MASK_SCRIPTS = 0x4;
    protected static final int SEND_MASK_ALL = SEND_MASK_OBJECT | SEND_MASK_LISTENERS | SEND_MASK_SCRIPTS;

    GVREventManager(GVRContext gvrContext) {
        mGvrContext = gvrContext;
        mHandlerMethodCache = new WeakHashMap<Object, Map<String, Method>>();
    }

    /**
     * Delivers an event to a handler object. An event is sent in the following
     * way: <p>
     *
     * <ol>
     * <li> If the {@code target} defines the interface of the class object
     * {@code eventsClass}, the event is delivered to it first, by invoking
     * the corresponding method in the {@code target}. </li>
     * <li> If the {@code target} implements interface {@link IEventReceiver}, the
     *    event is delivered to listeners added to the {@link GVREventReceiver} object.
     *    See {@link GVREventReceiver} for more information.
     *    </li>
     * <li> If the target is bound with scripts, the event is delivered to the scripts.
     * A script can be attached to the target using {@link GVRScriptManager#attachScriptFile}.</li>
     * </ol>
     *
     * @param target
     *     The object which handles the event.
     * @param eventsClass
     *     The interface class object representing an event group, such
     *     as {@link IScriptEvents}.class.
     * @param eventName
     *     The name of the event, such as "onInit".
     * @param params
     *     Parameters of the event. It should match the parameter list
     * of the corresponding method in the interface, specified by {@code
     * event class}
     * @return
     *     {@code true} if the event is handled successfully, {@code false} if not handled
     *     or any error occurred.
     */
    public boolean sendEvent(Object target, Class<? extends IEvents> eventsClass,
            String eventName, Object... params) {
        return sendEventWithMask(SEND_MASK_ALL, target, eventsClass, eventName, params);
    }

    protected boolean sendEventWithMask(int sendMask, Object target, Class<? extends IEvents> eventsClass,
            String eventName, Object... params) {
        return sendEventWithMaskParamArray(sendMask, target, eventsClass, eventName, params);
    }

    protected boolean sendEventWithMaskParamArray(int sendMask, Object target, Class<? extends IEvents> eventsClass,
            String eventName, Object[] params) {
        // Set to true if an event is handled.
        boolean handledSuccessful = false;

        // Verify the event name and parameters (cached)
        Method method = findHandlerMethod(target, eventsClass, eventName, params);
        if ((sendMask & SEND_MASK_OBJECT) != 0) {
            // Invoke the method if the target implements the interface
            if (eventsClass.isInstance(target)) {
                invokeMethod(target, method, params);
                handledSuccessful = true;
            }
        }

        if ((sendMask & SEND_MASK_LISTENERS) != 0) {
            // Try to deliver to the event receiver (if any)
            if (target instanceof IEventReceiver) {
                IEventReceiver receivingTarget = (IEventReceiver) target;
                GVREventReceiver receiver = receivingTarget.getEventReceiver();

                List<IEvents> listeners = receiver.getListeners();

                for (IEvents listener : listeners) {
                    // Skip the listener due to different type, or has been removed
                    if (!eventsClass.isInstance(listener) || receiver.getOwner() != target)
                        continue;

                    Method listenerMethod = findHandlerMethod(listener, eventsClass, eventName, params);
                    if (listenerMethod != null) {
                        // This may throw RuntimeException if the handler does so.
                        invokeMethod(listener, listenerMethod, params);
                        handledSuccessful = true;
                    }
                }
            }
        }

        if ((sendMask & SEND_MASK_SCRIPTS) != 0) {
            // Try invoking the handler in the script
            if (target instanceof IScriptable) {
                handledSuccessful |= tryInvokeScript((IScriptable)target, eventName, params);
            }
        }

        return handledSuccessful;
    }

    /*
     * Return the method in eventsClass by checking the signature.
     * RuntimeException is thrown if the event is not found in the eventsClass interface,
     * or the parameter types don't match.
     */
    private Method findHandlerMethod(Object target, Class<? extends IEvents> eventsClass,
            String eventName, Object[] params) {
        // Use cached method if available. Note: no further type checking is done if the
        // method has been cached. It will be checked by JRE when the method is invoked.
        Method cachedMethod = getCachedMethod(target, eventName);
        if (cachedMethod != null) {
            return cachedMethod;
        }

        // Check the event and params against the eventsClass interface object.
        Method nameMatch = null;
        Method signatureMatch = null;
        for (Method method : eventsClass.getMethods()) {
            // Match method name and event name
            if (method.getName().equals(eventName)) {
                nameMatch = method;

                // Check number of parameters
                Class<?>[] types = method.getParameterTypes();
                if (types.length != params.length)
                    continue;

                // Check parameter types
                int i = 0;
                boolean foundMatchedMethod = true;
                for (Class<?> type : types) {
                    Object param = params[i++];
                    if (!isInstanceWithAutoboxing(type, param)) {
                        foundMatchedMethod = false;
                        break;
                    }
                }

                if (foundMatchedMethod) {
                    signatureMatch = method;
                    break;
                }
            }
        }

        // Error
        if (nameMatch == null) {
            throw new RuntimeException(String.format("The interface contains no method %s", eventName));
        } else if (signatureMatch == null ){
            throw new RuntimeException(String.format("The interface contains a method %s but "
                    + "parameters don't match", eventName));
        }

        // Cache the method for the target, even if it doesn't implement the interface. This is
        // to avoid always verifying the event.
        addCachedMethod(target, eventName, signatureMatch);

        return signatureMatch;
    }

    private boolean isInstanceWithAutoboxing(Class<?> type, Object value) {
        if (type.isInstance(value)) {
            return true;
        }

        // Allow null value for subtypes of Object but not int, float, etc.
        if (value == null) {
            return Object.class.isAssignableFrom(type);
        }

        // Return false if auto-boxing is not possible
        if (!(value instanceof Number || value instanceof Boolean)) {
            return false;
        }

        // Check auto-boxing of numeric and boolean values
        if (type.equals(boolean.class) && Boolean.class.isInstance(value)) {
            return true;
        }

        if (type.equals(int.class) && Integer.class.isInstance(value)) {
            return true;
        }

        if (type.equals(long.class) && Long.class.isInstance(value)) {
            return true;
        }

        if (type.equals(short.class) && Short.class.isInstance(value)) {
            return true;
        }

        if (type.equals(byte.class) && Byte.class.isInstance(value)) {
            return true;
        }

        if (type.equals(float.class) && Float.class.isInstance(value)) {
            return true;
        }

        if (type.equals(double.class) && Double.class.isInstance(value)) {
            return true;
        }

        return false;
    }

    private Method getCachedMethod(Object target, String eventName) {
        // Lock the whole cache for both first- and second-level lookup
        synchronized (mHandlerMethodCache) {
            Map<String, Method> targetCache = mHandlerMethodCache.get(target);
            if (targetCache == null) {
                return null;
            }

            return targetCache.get(eventName);
        }
    }

    private void addCachedMethod(Object target, String eventName, Method method) {
        // Lock the whole cache for both first- and second-level lookup
        synchronized (mHandlerMethodCache) {
            Map<String, Method> targetCache = mHandlerMethodCache.get(target);
            if (targetCache == null) {
                targetCache = new TreeMap<String, Method>();
                mHandlerMethodCache.put(target, targetCache);
            }

            targetCache.put(eventName, method);
        }
    }

    private boolean tryInvokeScript(IScriptable target, String eventName,
            Object[] params) {
        GVRScriptFile script = mGvrContext.getScriptManager().getScriptFile(target);
        if (script == null)
            return false;

        return script.invokeFunction(eventName, params);
    }

    private void invokeMethod(Object target, Method method, Object[] params) {
        try {
            method.invoke(target, params);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Throwable throwable = e.getCause();
            // rethrow the RuntimeException back to the application
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else {
                e.printStackTrace();
            }
        }
    }
}
