/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Command table entry
 */
public class ShellCommand {

    private String prefix;
    private String name;
    private String description;
    private String abbreviation;
    private String header;
    private Method method;
    private Object handler;
    private ShellCommandParamSpec[] paramSpecs;

    public ShellCommand(Object handler, Method method, String prefix, String name) {
        super();
        assert method != null;
        this.paramSpecs = ShellCommandParamSpec.forMethod(method);
        assert paramSpecs.length == method.getParameterTypes().length;
        this.method = method;
        this.prefix = prefix;
        this.name = name;
        this.handler = handler;

        this.description = makeCommandDescription(method, paramSpecs);
    }

    private static String makeCommandDescription(Method method, ShellCommandParamSpec[] paramSpecs) {
        StringBuilder result = new StringBuilder();
        result.append(method.getName());
        result.append('(');
        Class[] paramTypes = method.getParameterTypes();
        assert paramTypes.length == paramSpecs.length;
        boolean first = true;
        for (int i = 0; i < paramTypes.length; i++) {
            if (!first) {
                result.append(", ");
            }
            first = false;
            if (paramSpecs[i] != null) {
                result.append(paramSpecs[i].getName());
                result.append(":");
                result.append(paramTypes[i].getSimpleName());
            } else {
                result.append(paramTypes[i].getSimpleName());
            }
        }
        result.append(") : ");
        result.append(method.getReturnType().getSimpleName());
        return result.toString();
    }

    public Object invoke(Object[] parameters)
            throws CLIException {
        assert method != null;
        try {
            Object result = method.invoke(handler, parameters);
            return result;
        } catch (InvocationTargetException ite) {
            return ite.getCause();
        } catch (Exception ex) {
            throw new CLIException(ex);
        }
    }

    public boolean canBeDenotedBy(String commandName) {
        return commandName.equals(prefix + name) || commandName.equals(prefix + abbreviation);
    }

    public int getArity() {
        return method.getParameterTypes().length;
    }

    public String getDescription() {
        return description;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }

    public Method getMethod() {
        return method;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHeader(String header) {
        this.header = header;
    }



    public boolean startsWith(String prefix) {
        return (this.prefix + abbreviation).startsWith(prefix) ||
                (this.prefix + name).startsWith(prefix);
    }

    @Override
    public String toString() {
        return prefix + name + "\t" + (abbreviation != null ? prefix + abbreviation : "") + "\t" +
                method.getParameterTypes().length + (method.isVarArgs() ? "+" : "") + "\t" + description;
    }

    public String getHeader() {
        return header;
    }

    public ShellCommandParamSpec[] getParamSpecs() {
        return paramSpecs;
    }

}
