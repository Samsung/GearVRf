/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Output conversion engine is responsible for converting objects after they are returned
 * by command but before they are sent to the Output.
 * As with InputConversionEngine, it can automatically retrieve all converters declared inside
 * an object.
 *
 * All converters are applied to all objects, first-registered--last-applied.
 *
 * Used by Shell.
 *
 * @author ASG
 */
public class OutputConversionEngine {

    private List<OutputConverter> outputConverters = new ArrayList<OutputConverter>();

    public void addConverter(OutputConverter converter) {
        if (converter == null ) {
            throw new IllegalArgumentException("Converter == null");
        }
        outputConverters.add(converter);
    }

    public boolean removeConverter(OutputConverter converter) {
        return outputConverters.remove(converter);
    }

    public Object convertOutput(Object anObject) {
        Object convertedOutput = anObject;
        for (ListIterator<OutputConverter> it = outputConverters.listIterator(outputConverters.size()); it.hasPrevious();) {
            OutputConverter outputConverter = it.previous(); // last in --- first called.
            Object conversionResult = outputConverter.convertOutput(convertedOutput);
            if (conversionResult != null) {
                convertedOutput = conversionResult;
            }
        }
        return convertedOutput;
    }

    public void addDeclaredConverters(Object handler) {
        Field[] fields = handler.getClass().getFields();
        final String PREFIX = "CLI_OUTPUT_CONVERTERS";
        for (Field field : fields) {
            if (field.getName().startsWith(PREFIX)
                    && field.getType().isArray()
                    && OutputConverter.class.isAssignableFrom(field.getType().getComponentType())) {
                try {
                    Object convertersArray = field.get(handler);
                    for (int i = 0; i < Array.getLength(convertersArray); i++) {
                        addConverter((OutputConverter)Array.get(convertersArray, i));
                    }
                } catch (Exception ex) {
                    throw new RuntimeException("Error getting converter from field " + field.getName(), ex);
                }
            }
        }
    }


}
