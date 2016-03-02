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

package org.gearvrf.debug;

import java.util.List;

public class Stats {
    public static class DescriptiveResult {
        public int n;
        public double mean;
        public double stdev;
    }

    public static <T extends Number> DescriptiveResult computeDescriptive(List<T> data) {
        DescriptiveResult desc = new DescriptiveResult();

        desc.n = data.size();
        if (desc.n == 0)
            return desc;

        for (T val : data) {
            desc.mean += val.doubleValue();
        }

        desc.mean /= desc.n;

        for (T val : data) {
            desc.stdev += (val.doubleValue() - desc.mean) * (val.doubleValue() - desc.mean);
        }

        desc.stdev /= (desc.n - 1);
        desc.stdev = Math.sqrt(desc.stdev);

        return desc;
    }
}
