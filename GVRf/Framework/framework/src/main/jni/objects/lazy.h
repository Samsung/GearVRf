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


/***************************************************************************
 * Does lazy initialization.
 ***************************************************************************/

#ifndef LAZY_H_
#define LAZY_H_

namespace gvr {
template<class T>
class Lazy {
public:
    explicit Lazy(T element, bool valid = false) :
            element_(element), valid_(valid) {
    }

    Lazy(const Lazy& lazy) :
            element_(lazy.element_), valid_(lazy.valid_) {
    }

    Lazy(Lazy&& lazy) :
            element_(lazy.element_), valid_(lazy.valid_) {
    }

    ~Lazy() {
    }

    void validate(T element) {
        element_ = element;
        valid_ = true;
    }

    void invalidate() {
        valid_ = false;
    }

    bool isValid() {
        return valid_;
    }

    const T& element() const {
        return element_;
    }

private:
    Lazy& operator=(const Lazy& lazy);
    Lazy& operator=(Lazy&& lazy);

private:
    T element_;
    bool valid_;
};
}
#endif
