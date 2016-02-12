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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.gearvrf.GVRTime;

/**
 * FPS tracer with smoothing. It accumulates frames in latest T seconds, and compute the frames per second in this
 * time window.
 */
public class GVRFPSTracer {
    protected GVRStatsLine.GVRStandardColumn<Float> mStatColumn;
    protected List<Long> mTimestamps;

    private static int BUFFER_SECONDS = 3;

    /**
     * Constructor.
     * @param name The name of the FPS tracer.
     */
    public GVRFPSTracer(String name) {
        mStatColumn = new GVRStatsLine.GVRStandardColumn<Float>(name);
        mTimestamps = new LinkedList<Long>();
    }

    /**
     * Gets the statistic column object.
     * @return the column object.
     */
    public GVRStatsLine.GVRColumnBase<Float> getStatColumn() {
        return mStatColumn;
    }

    /**
     * Should be called each frame.
     */
    public synchronized void tick() {
        long currentTime = GVRTime.getMilliTime();
        long cutoffTime = currentTime - BUFFER_SECONDS * 1000;
        ListIterator<Long> it = mTimestamps.listIterator();
        while (it.hasNext()) {
            Long timestamp = it.next();
            if (timestamp < cutoffTime) {
                it.remove();
            } else {
                break;
            }
        }

        mTimestamps.add(currentTime);
        mStatColumn.addValue(((float)mTimestamps.size()) / BUFFER_SECONDS);
    }
}
