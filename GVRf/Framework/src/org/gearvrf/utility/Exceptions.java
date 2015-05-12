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

import java.util.Arrays;

/**
 * Factory class to generate exceptions using
 * {@link String#format(String, Object...) String.format()}
 */
public abstract class Exceptions {

    /**
     * Generate an {@link IllegalArgumentException}
     * 
     * @param pattern
     *            {@link String#format(String, Object...) String.format()}
     *            pattern
     * @param parameters
     *            {@code String.format()} parameters
     * @return A new {@code IllegalArgumentException}
     */
    public static IllegalArgumentException IllegalArgument(String pattern,
            Object... parameters) {
        return strip(new IllegalArgumentException(String.format(pattern,
                parameters)));
    }

    /**
     * Generate an {@link IllegalArgumentException}
     * 
     * @param cause
     *            Existing {@link Throwable} to wrap in a new
     *            {@code IllegalArgumentException}
     * @param pattern
     *            {@link String#format(String, Object...) String.format()}
     *            pattern
     * @param parameters
     *            {@code String.format()} parameters
     * @return A new {@code IllegalArgumentException}
     */
    public static IllegalArgumentException IllegalArgument(Throwable cause,
            String pattern, Object... parameters) {
        return strip(new IllegalArgumentException(String.format(pattern,
                parameters), cause));
    }

    /**
     * Generate a {@link RuntimeAssertion}
     * 
     * @param pattern
     *            {@link String#format(String, Object...) String.format()}
     *            pattern
     * @param parameters
     *            {@code String.format()} parameters
     * @return A new {@code IllegalArgumentException}
     */
    public static RuntimeAssertion RuntimeAssertion(String pattern,
            Object... parameters) {
        return strip(new RuntimeAssertion(String.format(pattern, parameters)));
    }

    /**
     * Generate a {@link RuntimeAssertion}
     * 
     * @param cause
     *            Existing {@link Throwable} to wrap in a new
     *            {@code IllegalArgumentException}
     * @param pattern
     *            {@link String#format(String, Object...) String.format()}
     *            pattern
     * @param parameters
     *            {@code String.format()} parameters
     * @return A new {@code IllegalArgumentException}
     */
    public static RuntimeAssertion RuntimeAssertion(Throwable cause,
            String pattern, Object... parameters) {
        return strip(new RuntimeAssertion(String.format(pattern, parameters),
                cause));
    }

    private static <T extends Exception> T strip(T e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        e.setStackTrace(Arrays.copyOfRange(stackTrace, 1, stackTrace.length));
        return e;
    }
}
