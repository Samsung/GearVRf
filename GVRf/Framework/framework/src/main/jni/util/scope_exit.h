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

#ifndef UTIL_SCOPE_EXIT_H_
#define UTIL_SCOPE_EXIT_H_

#include <functional>

/**
 * This class allows user to specify a block of code to be
 * executed when it exits the scope. It is similary to Java's
 * "finally" block but follows C++ RAII (resource allocation
 * is initialization) principle.
 */
class ScopeExit {
public:
    ScopeExit (const std::function<void()> &func)
    : m_func(func)
    { }

    ~ScopeExit () {
        m_func();
    }

private:
    std::function<void()> m_func;
};

#define STR_JOIN(arg1, arg2) STR_JOIN_(arg1, arg2)
#define STR_JOIN_(arg1, arg2) arg1 ## arg2

// Equivalent of a Java "finally" block
#define SCOPE_EXIT(code) \
    ScopeExit STR_JOIN(scope_exit_, __LINE__)([=]() { \
        code; \
    })

#endif /* UTIL_SCOPE_EXIT_H_ */
