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

    // Cache for Java handler methods
    private final Map<Object, Map<String, Method>> mHandlerMethodCache;

    GVREventManager(GVRContext gvrContext) {
        mGvrContext = gvrContext;
        mHandlerMethodCache = new HashMap<Object, Map<String, Method>>();
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
        // Set to true if an event is handled.
        boolean handledSuccessful = false;

        // Check if the target directly handles the event by implementing the
        // eventsClass interface.
        Method method = findHandlerMethod(target, eventsClass, eventName, params);

        if (method != null) {
            // Try invoking the method in target
            invokeMethod(target, method, params);
            handledSuccessful = true;
        }

        // Try to deliver to the event receiver (if any)
        if (target instanceof IEventReceiver) {
            IEventReceiver receivingTarget = (IEventReceiver) target;
            GVREventReceiver receiver = receivingTarget.getEventReceiver();
            List<IEvents> listeners = receiver.getListeners();

            for (IEvents listener : listeners) {
                // Skip the listener due to different type
                if (!eventsClass.isInstance(listener))
                    continue;

                try {
                    Method listenerMethod = findHandlerMethod(listener, eventsClass, eventName, params);
                    // This may throw RuntimeException if the handler does so.
                    invokeMethod(listener, listenerMethod, params);
                    handledSuccessful = true;
                } catch(Exception e) {
                    // Requested method does not exist. Probably because of a caller error.
                    continue;
                }
            }
        }

        // Try invoking the handler in the script
        if (target instanceof IScriptable) {
            handledSuccessful |= tryInvokeScript((IScriptable)target, eventName, params);
        }

        return handledSuccessful;
    }

    /*
     * Return the method in the target by signature. It throws if the method is not found.
     */
    private Method findHandlerMethod(Object target, Class<? extends IEvents> eventsClass,
            String eventName, Object[] params) {
        // Use cached method if available
        Method cachedMethod = getCachedMethod(target, eventName);
        if (cachedMethod != null) {
            return cachedMethod;
        }

        // Check target event interface
        if (!eventsClass.isInstance(target)) {
            // The target object does not implement interface
            return null;
        }

        for (Method method : eventsClass.getMethods()) {
            // Match method name and event name
            if (method.getName().equals(eventName)) {
                // Check number of parameters
                Class<?>[] types = method.getParameterTypes();
                if (types.length != params.length)
                    continue;

                // Check parameter types
                int i = 0;
                boolean foundMatchedMethod = true;
                for (Class<?> type : types) {
                    Object param = params[i++];
                    if (!type.isInstance(param)) {
                        foundMatchedMethod = false;
                        break;
                    }
                }

                if (foundMatchedMethod) {
                    addCachedMethod(target, eventName, method);
                    return method;
                }
            }
        }

        // No matching method
        return null;
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
