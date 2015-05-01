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

public abstract class Preconditions {

    /**
     * The goal of this class is to improve the readability and to provide some
     * common checking method to improve the robustness of GVRF. But in some
     * cases, this may be a significant performance cost. Try to use and further
     * extend this class carefully with concern on performance issue.
     * 
     * Actually, one purpose of this class may help us avoid from using
     * GVRJniException which is not a proper way to throw/simulate an exception
     * with JNI crash.
     */

    /**
     * Check that the parameter is not {@code null}.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param object
     *            the parameter object can be any Java reference type, including
     *            arrays.
     * @throws IllegalArgumentException
     *             if the object is null pointer.
     */
    public static void checkNotNull(String parameterName, Object object) {
        if (object == null) {
            throw Exceptions.IllegalArgument("Input %s can't be null.",
                    parameterName);
        }
    }

    /**
     * This function is to ensure that any input string value is not null or
     * empty
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
     * This function is to ensure that the length of input array is legal
     * according to the expected components' number.
     * 
     * @param parameterName
     *            The name of the user-supplied parameter that we are validating
     *            so that the user can easily find the error in their code.
     * @param dataLength
     *            Length of array data.
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
     * This is checkNotNaN for only single float.
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
