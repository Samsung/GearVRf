/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.io.cursor3d;

class PriorityIoDeviceTuple implements Comparable<PriorityIoDeviceTuple> {
    private int priority;
    private IoDevice ioDevice;

    PriorityIoDeviceTuple(int priority, IoDevice ioDevice) {
        this.priority = priority;
        this.ioDevice = ioDevice;
    }

    IoDevice getIoDevice() {
        return ioDevice;
    }

    int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(PriorityIoDeviceTuple another) {
        int priorityDiff = priority - another.getPriority();
        if (priorityDiff == 0) {
            return ioDevice.hashCode() - another.getIoDevice().hashCode();
        } else {
            return priorityDiff;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PriorityIoDeviceTuple that = (PriorityIoDeviceTuple) o;

        if (priority != that.priority) return false;
        return ioDevice.equals(that.ioDevice);
    }

    @Override
    public int hashCode() {
        int result = priority;
        result = 31 * result + ioDevice.hashCode();
        return result;
    }
}