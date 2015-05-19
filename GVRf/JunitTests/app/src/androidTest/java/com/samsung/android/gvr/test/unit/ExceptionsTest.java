package com.samsung.android.gvr.test.unit;


import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;
import org.gearvrf.utility.Exceptions;
import org.gearvrf.utility.RuntimeAssertion;

import java.security.InvalidParameterException;

/**
 * Created by j.elidelson on 5/14/2015.
 */
public class ExceptionsTest extends ActivityInstrumentationGVRf {

    public void testIllegalArgument() {
        IllegalArgumentException IA = Exceptions.IllegalArgument("%f\n", 2.0f);
        assertNotNull(IA);
    }

    public void testIllegalArgumentThrowable() {
        Throwable aux = new Throwable("*",null);
        IllegalArgumentException IA = Exceptions.IllegalArgument(aux,"%f\n", 2.0f);
        assertNotNull(IA);
    }

    public void testRuntimeAssertion() {
        RuntimeAssertion RA = Exceptions.RuntimeAssertion("%f\n", 2.0f);
        assertNotNull(RA);
    }

    public void testRuntimeAssertionThrowable() {
        Throwable aux = new Throwable("*",null);
        RuntimeAssertion RA = Exceptions.RuntimeAssertion(aux,"%f\n", 2.0f);
        assertNotNull(RA);
    }
}
