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

package org.gearvrf.utility;

import android.text.TextUtils;

/**
 * The goal of this class is to improve the readability and to provide some
 * common checking method to improve the robustness of GVRF.
 * 
 * Additionally, this class may help us avoid using {@link GVRJniException},
 * which is (at best) a sort of fake exception-from-JNI.
 * 
 * <p>
 * Obviously, all parameter checks add runtime cost. Try to use this class
 * sparingly, on user-facing APIs that would otherwise crash: it's probably OK
 * to return meaningless results on meaningless inputs; internal calls should
 * generally rely on careful testing, not parameter checking.
 */
public abstract class Assert {

    /**
     * Check that the parameter is not {@code null}.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param object
     *            The parameter object can be any Java reference type, including
     *            arrays.
     * @throws IllegalArgumentException
     *             If the object is null pointer.
     */
    public static void checkNotNull(String parameterName, Object object) {
        if (object == null) {
            throw Exceptions.IllegalArgument("Input %s can't be null.",
                    parameterName);
        }
    }

    /**
     * Check that the parameter string is not null or empty
     * 
     * @param value
     *            String value to be checked.
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @throws IllegalArgumentException
     *             If the key is null or empty.
     */
    public static void checkStringNotNullOrEmpty(String parameterName,
            String value) {
        if (TextUtils.isEmpty(value)) {
            throw Exceptions.IllegalArgument("Current input string %s is %s.",
                    parameterName, value == null ? "null" : "empty");
        }
    }

    /**
     * Check that the parameter array has exactly the right number of elements.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param expectedLength
     *            The expected array length
     */
    public static void checkArrayLength(String parameterName, int actualLength,
            int expectedLength) {
        if (actualLength != expectedLength) {
            throw Exceptions.IllegalArgument(
                    "Array %s should have %d elements, not %d", parameterName,
                    expectedLength, actualLength);
        }
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has
     * exactly the right number of elements.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param expectedLength
     *            The expected array length
     */
    public static void checkArrayLength(String parameterName, boolean[] array,
            int expectedLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, expectedLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has
     * exactly the right number of elements.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param expectedLength
     *            The expected array length
     */
    public static void checkArrayLength(String parameterName, char[] array,
            int expectedLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, expectedLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has
     * exactly the right number of elements.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param expectedLength
     *            The expected array length
     */
    public static void checkArrayLength(String parameterName, byte[] array,
            int expectedLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, expectedLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has
     * exactly the right number of elements.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param expectedLength
     *            The expected array length
     */
    public static void checkArrayLength(String parameterName, short[] array,
            int expectedLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, expectedLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has
     * exactly the right number of elements.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param expectedLength
     *            The expected array length
     */
    public static void checkArrayLength(String parameterName, int[] array,
            int expectedLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, expectedLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has
     * exactly the right number of elements.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param expectedLength
     *            The expected array length
     */
    public static void checkArrayLength(String parameterName, long[] array,
            int expectedLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, expectedLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has
     * exactly the right number of elements.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param expectedLength
     *            The expected array length
     */
    public static void checkArrayLength(String parameterName, float[] array,
            int expectedLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, expectedLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has
     * exactly the right number of elements.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param expectedLength
     *            The expected array length
     */
    public static void checkArrayLength(String parameterName, double[] array,
            int expectedLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, expectedLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has
     * exactly the right number of elements.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param expectedLength
     *            The expected array length
     */
    public static void checkArrayLength(String parameterName, Object[] array,
            int expectedLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, expectedLength);
    }

    /**
     * Check that the parameter array has at least as many elements as it
     * should.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param minimumLength
     *            The minimum array length
     */
    public static void checkMinimumArrayLength(String parameterName,
            int actualLength, int minimumLength) {
        if (actualLength < minimumLength) {
            throw Exceptions
                    .IllegalArgument(
                            "Array %s should have at least %d elements, but it only has %d",
                            parameterName, minimumLength, actualLength);
        }
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has at
     * least as many elements as it should.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param minimumLength
     *            The minimum array length
     */
    public static void checkMinimumArrayLength(String parameterName,
            boolean[] array, int minimumLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, minimumLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has at
     * least as many elements as it should.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param minimumLength
     *            The minimum array length
     */
    public static void checkMinimumArrayLength(String parameterName,
            char[] array, int minimumLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, minimumLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has at
     * least as many elements as it should.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param minimumLength
     *            The minimum array length
     */
    public static void checkMinimumArrayLength(String parameterName,
            byte[] array, int minimumLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, minimumLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has at
     * least as many elements as it should.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param minimumLength
     *            The minimum array length
     */
    public static void checkMinimumArrayLength(String parameterName,
            short[] array, int minimumLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, minimumLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has at
     * least as many elements as it should.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param minimumLength
     *            The minimum array length
     */
    public static void checkMinimumArrayLength(String parameterName,
            int[] array, int minimumLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, minimumLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has at
     * least as many elements as it should.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param minimumLength
     *            The minimum array length
     */
    public static void checkMinimumArrayLength(String parameterName,
            long[] array, int minimumLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, minimumLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has at
     * least as many elements as it should.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param minimumLength
     *            The minimum array length
     */
    public static void checkMinimumArrayLength(String parameterName,
            float[] array, int minimumLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, minimumLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has at
     * least as many elements as it should.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param minimumLength
     *            The minimum array length
     */
    public static void checkMinimumArrayLength(String parameterName,
            double[] array, int minimumLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, minimumLength);
    }

    /**
     * Check that the parameter array is non-{@code null} and that it has at
     * least as many elements as it should.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param actualLength
     *            The actual array length
     * @param minimumLength
     *            The minimum array length
     */
    public static void checkMinimumArrayLength(String parameterName,
            Object[] array, int minimumLength) {
        checkNotNull(parameterName, array);
        checkArrayLength(parameterName, array.length, minimumLength);
    }

    /**
     * Check arrays of tuples: is the array length a multiple of the tuple size?
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param dataLength
     *            Array parameter length
     * @param expectedComponents
     *            Expected number of components.
     * @throws IllegalArgumentException
     *             If the dataLength is zero or not divisible by the expected
     *             number.
     */
    public static void checkDivisibleDataLength(String parameterName,
            int dataLength, int expectedComponents) {
        if (dataLength == 0 || dataLength % expectedComponents != 0) {
            throw Exceptions
                    .IllegalArgument(
                            "The input array of %s should be an array of %d-component elements whose length is non-zero and divisible by %d. But current data length is %d.",
                            parameterName, expectedComponents,
                            expectedComponents, dataLength);
        }
    }

    /**
     * Check arrays of tuples: is the array length a multiple of the tuple size?
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param array
     *            Array of tuples
     * @param expectedComponents
     *            Expected number of components.
     * @throws IllegalArgumentException
     *             If the dataLength is zero or not divisible by the expected
     *             number.
     */
    public static void checkDivisibleDataLength(String parameterName,
            boolean[] array, int expectedComponents) {
        checkNotNull(parameterName, array);
        checkDivisibleDataLength(parameterName, array.length,
                expectedComponents);
    }

    /**
     * Check arrays of tuples: is the array length a multiple of the tuple size?
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param array
     *            Array of tuples
     * @param expectedComponents
     *            Expected number of components.
     * @throws IllegalArgumentException
     *             If the dataLength is zero or not divisible by the expected
     *             number.
     */
    public static void checkDivisibleDataLength(String parameterName,
            char[] array, int expectedComponents) {
        checkNotNull(parameterName, array);
        checkDivisibleDataLength(parameterName, array.length,
                expectedComponents);
    }

    /**
     * Check arrays of tuples: is the array length a multiple of the tuple size?
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param array
     *            Array of tuples
     * @param expectedComponents
     *            Expected number of components.
     * @throws IllegalArgumentException
     *             If the dataLength is zero or not divisible by the expected
     *             number.
     */
    public static void checkDivisibleDataLength(String parameterName,
            byte[] array, int expectedComponents) {
        checkNotNull(parameterName, array);
        checkDivisibleDataLength(parameterName, array.length,
                expectedComponents);
    }

    /**
     * Check arrays of tuples: is the array length a multiple of the tuple size?
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param array
     *            Array of tuples
     * @param expectedComponents
     *            Expected number of components.
     * @throws IllegalArgumentException
     *             If the dataLength is zero or not divisible by the expected
     *             number.
     */
    public static void checkDivisibleDataLength(String parameterName,
            short[] array, int expectedComponents) {
        checkNotNull(parameterName, array);
        checkDivisibleDataLength(parameterName, array.length,
                expectedComponents);
    }

    /**
     * Check arrays of tuples: is the array length a multiple of the tuple size?
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param array
     *            Array of tuples
     * @param expectedComponents
     *            Expected number of components.
     * @throws IllegalArgumentException
     *             If the dataLength is zero or not divisible by the expected
     *             number.
     */
    public static void checkDivisibleDataLength(String parameterName,
            int[] array, int expectedComponents) {
        checkNotNull(parameterName, array);
        checkDivisibleDataLength(parameterName, array.length,
                expectedComponents);
    }

    /**
     * Check arrays of tuples: is the array length a multiple of the tuple size?
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param array
     *            Array of tuples
     * @param expectedComponents
     *            Expected number of components.
     * @throws IllegalArgumentException
     *             If the dataLength is zero or not divisible by the expected
     *             number.
     */
    public static void checkDivisibleDataLength(String parameterName,
            long[] array, int expectedComponents) {
        checkNotNull(parameterName, array);
        checkDivisibleDataLength(parameterName, array.length,
                expectedComponents);
    }

    /**
     * Check arrays of tuples: is the array length a multiple of the tuple size?
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param array
     *            Array of tuples
     * @param expectedComponents
     *            Expected number of components.
     * @throws IllegalArgumentException
     *             If the dataLength is zero or not divisible by the expected
     *             number.
     */
    public static void checkDivisibleDataLength(String parameterName,
            float[] array, int expectedComponents) {
        checkNotNull(parameterName, array);
        checkDivisibleDataLength(parameterName, array.length,
                expectedComponents);
    }

    /**
     * Check arrays of tuples: is the array length a multiple of the tuple size?
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param array
     *            Array of tuples
     * @param expectedComponents
     *            Expected number of components.
     * @throws IllegalArgumentException
     *             If the dataLength is zero or not divisible by the expected
     *             number.
     */
    public static void checkDivisibleDataLength(String parameterName,
            double[] array, int expectedComponents) {
        checkNotNull(parameterName, array);
        checkDivisibleDataLength(parameterName, array.length,
                expectedComponents);
    }

    /**
     * Check arrays of tuples: is the array length a multiple of the tuple size?
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param array
     *            Array of tuples
     * @param expectedComponents
     *            Expected number of components.
     * @throws IllegalArgumentException
     *             If the dataLength is zero or not divisible by the expected
     *             number.
     */
    public static void checkDivisibleDataLength(String parameterName,
            Object[] array, int expectedComponents) {
        checkNotNull(parameterName, array);
        checkDivisibleDataLength(parameterName, array.length,
                expectedComponents);
    }

    /**
     * In common shader cases, NaN makes little sense. Correspondingly, GVRF is
     * going to use Float.NaN as illegal flag in many cases. Therefore, we need
     * a function to check if there is any setX that is using NaN as input.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param data
     *            A series of input float data.
     * @throws IllegalArgumentException
     *             if the data includes NaN.
     */
    public static void checkFloatNotNaNOrInfinity(String parameterName,
            float... data) {
        for (float element : data) {
            checkFloatNotNaNOrInfinity(parameterName, element);
        }
    }

    /**
     * In common shader cases, NaN makes little sense. Correspondingly, GVRF is
     * going to use Float.NaN as illegal flag in many cases. Therefore, we need
     * a function to check if there is any setX that is using NaN as input.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param data
     *            A single float data.
     * @throws IllegalArgumentException
     *             if the data includes NaN.
     */
    public static void checkFloatNotNaNOrInfinity(String parameterName,
            float data) {
        if (Float.isNaN(data) || Float.isInfinite(data)) {
            throw Exceptions.IllegalArgument(
                    "%s should never be NaN or Infinite.", parameterName);
        }
    }

}
