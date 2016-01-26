package org.gearvrf.debug.cli;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


/**
 * Specification of command's parameters, such as description given with Param annotation.
 */
public class ShellCommandParamSpec {

    static ShellCommandParamSpec[] forMethod(Method theMethod) {
        Class[] paramTypes = theMethod.getParameterTypes();
        ShellCommandParamSpec[] result = new ShellCommandParamSpec[theMethod.getParameterTypes().length];
        Annotation[][] annotations = theMethod.getParameterAnnotations();
        assert annotations.length == result.length;
        for (int i = 0; i < result.length; i++) {
            Param paramAnnotation = null;
            for (Annotation a : annotations[i]) {
                if (a instanceof Param) {
                    paramAnnotation = (Param)a;
                    break;
                }
            }
            if (paramAnnotation != null) {
                assert !paramAnnotation.name().isEmpty() : "@Param.name mustn\'t be empty";
                result[i] = new ShellCommandParamSpec(paramAnnotation.name(), paramTypes[i],
                        paramAnnotation.description(), i);
            } else {
                result[i] = new ShellCommandParamSpec(String.format("p%d", i + 1), paramTypes[i], "", i);
            }
        }
        return result;
    }
    private String name;
    private String description;
    private int position;
    private Class valueClass;

    public Class getValueClass() {
        return valueClass;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public ShellCommandParamSpec(String name, Class valueClass, String description, int position) {
        super();
        this.name = name;
        this.description = description;
        this.position = position;
        this.valueClass = valueClass;
    }
}
