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

package org.gearvrf.script;

import java.io.InputStream;

/**
 * This interface represents a script file, which can be attached to an
 * object to handle events delivered to it.
 */
public interface IScriptFile {

    /**
     * Invokes a function defined in the script.
     *
     * @param funcName
     *     The function name.
     * @param params
     *     The parameter array.
     * @return
     *     A boolean value representing whether the function is
     * executed correctly. If the function cannot be found, or
     * parameters don't match, {@code false} is returned.
     */
    boolean invokeFunction(String funcName, Object[] params);

}
