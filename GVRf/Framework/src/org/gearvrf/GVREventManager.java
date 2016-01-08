package org.gearvrf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.gearvrf.script.GVRScriptFile;
import org.gearvrf.script.IScriptable;

public class GVREventManager {
    private static final String TAG = GVREventManager.class.getSimpleName();
    private GVRContext mGvrContext;

    GVREventManager(GVRContext gvrContext) {
        mGvrContext = gvrContext;
    }

    public void sendEvent(Object target, Class<? extends IEvents> eventsClass,
            String eventName, Object... params) {
        // Validate the event
        Method method = validateEvent(target, eventsClass, eventName, params);

        // Try invoking the handler in the script
        if (target instanceof IScriptable) {
            tryInvokeScript((IScriptable)target, eventName, params);
        }

        // Try invoking the method in target
        invokeMethod(target, method, params);
    }

    private Method validateEvent(Object target, Class<? extends IEvents> eventsClass,
            String eventName, Object[] params) {
        // Check target event interface
        if (!eventsClass.isInstance(target)) {
            throw new RuntimeException(String.format("The target object does not implement interface %s",
                    eventsClass.getSimpleName()));
        }

        Method nameMatch = null;
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
                    if (!type.isInstance(param)) {
                        foundMatchedMethod = false;
                        break;
                    }
                }

                if (foundMatchedMethod)
                    return method;
            }
        }

        // Error
        if (nameMatch != null) {
            throw new RuntimeException(String.format("The target object contains a method %s but parameters don't match",
                    eventName));
        } else {
            throw new RuntimeException(String.format("The target object has no method %s",
                    eventName));
        }
    }

    private void tryInvokeScript(IScriptable target, String eventName,
            Object[] params) {
        GVRScriptFile script = mGvrContext.getScriptManager().getScriptFile(target);
        if (script == null)
            return;

        script.invokeFunction(eventName, params);
    }

    private void invokeMethod(Object target, Method method, Object[] params) {
        try {
            method.invoke(target, params);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
