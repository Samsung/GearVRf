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

import org.gearvrf.GVRTime;

public class GVRMethodCallTracer {
    protected GVRStatsLine.GVRStandardColumn<Float> mStatColumn;

    private long mEnterTime;
    private long mLeaveTime;

    private static final float NANO_TO_MILLIS = 1000000.0f;

    public GVRMethodCallTracer(String name) {
        mStatColumn = new GVRStatsLine.GVRStandardColumn<Float>(name);
        mEnterTime = -1;
    }

    public void enter() {
        mEnterTime = getTime();
    }

    public void leave() {
        mLeaveTime = getTime();
        long timeDiff = mEnterTime != -1 ? mLeaveTime - mEnterTime : 0;
        mStatColumn.addValue(timeDiff / NANO_TO_MILLIS);
    }

    protected long getTime() {
        return GVRTime.getNanoTime();
    }

    public GVRStatsLine.GVRStandardColumn<Float> getStatColumn() {
        return mStatColumn;
    }
}
