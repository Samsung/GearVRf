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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRTime;
import org.gearvrf.debug.Stats.DescriptiveResult;
import org.gearvrf.utility.Log;

/**
 * This class generates statistic lines for an application for
 * debugging purposes.
 */
public class GVRStatsLine {
    protected String mLineTag;
    protected long mLastPrintTimeMS;
    protected boolean mAnalysisModeHeader;

    /**
     * Formats for statistics
     */
    public enum FORMAT {
        DEFAULT,
        MULTILINE,
        ANALYSIS,
    }

    /**
     * Current format.
     */
    public static FORMAT sFormat = FORMAT.DEFAULT;

    /**
     * Constructor.
     * @param lingTag
     *         The line tag printed at beginning of each statistics line.
     */
    public GVRStatsLine(String lineTag) {
        mLineTag = lineTag;
        mLastPrintTimeMS = -1;

        // Print a header in analysis mode
        mAnalysisModeHeader = true;
    }

    protected List<GVRColumnBase<? extends Number>> mColumns =
            new ArrayList<GVRColumnBase<? extends Number>>();

    /**
     * Adds a column into the statistic line.
     * @param column The {@link GVRColumnBase} object.
     */
    public void addColumn(GVRColumnBase<? extends Number> column) {
        mColumns.add(column);
    }

    /**
     * Start a new line of statistics.
     */
    public synchronized void startLine() {
        for (GVRColumnBase<? extends Number> col : mColumns) {
            col.onStartLine();
        }
    }

    /**
     * Prints the statistic line, including all added columns.
     * @return The line to be printed.
     */
    public void printLine() {
        Log.d(mLineTag, "%s", getStats(sFormat));
    }

    /**
     * Log a line with a specified frequency. If the method is invoked before
     * {@code periodMS} millisecond has passed since last printing, the new line
     * is suppressed. The {@code periodMS} can be set to 0 to print every line.
     *
     * @param period The log period in milliseconds.
     */
    public void printLine(long periodMS) {
        long currentTime = GVRTime.getMilliTime();

        if (mLastPrintTimeMS < 0) {
            mLastPrintTimeMS = currentTime;
            return;
        }

        if (currentTime - mLastPrintTimeMS >= periodMS) {
            mLastPrintTimeMS = currentTime;
            printLine();
        }
    }

    public String getStats(FORMAT format) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        switch (format) {
            case DEFAULT: {
                for (GVRColumnBase<? extends Number> col : mColumns) {
                    if (!first) {
                        sb.append(", ");
                    } else {
                        first = false;
                    }
                    sb.append(col.getName());
                    sb.append("=");
                    sb.append(col.getStat());
                }
                break;
            }

            case MULTILINE:
                for (GVRColumnBase<? extends Number> col : mColumns) {
                    String line = String.format("%s: %s", col.getName(), col.getStat());
                    sb.append(line);
                    sb.append(System.lineSeparator());
                }
                break;

            case ANALYSIS:
                if (mAnalysisModeHeader) {
                    // Print header
                    mAnalysisModeHeader = false; // once
                    boolean firstHeaderCol = true;
                    for (GVRColumnBase<? extends Number> col : mColumns) {
                        if (!firstHeaderCol) {
                            sb.append(";");
                        } else {
                            firstHeaderCol = false;
                        }
                        sb.append(col.getName());
                    }
                    return sb.toString(); // skip the data once for simplicity
                }

                for (GVRColumnBase<? extends Number> col : mColumns) {
                    if (!first) {
                        sb.append(";");
                    } else {
                        first = false;
                    }
                    sb.append(col.getStat());
                }
                break;
        }

        return sb.toString();
    }

    /* **********************************************************************************
     * Standard columns are defined here
     * **********************************************************************************/

    /**
     * This class represents a column in the statistic line.
     */
    public static abstract class GVRColumnBase<T> {
        protected String mName;

        /**
         * Constructor for the statistic column.
         *
         * @param name The name of the column.
         */
        public GVRColumnBase(String name) {
            this.mName = name;
        }

        /**
         * Rests the column
         */
        public void reset() {
        }

        /**
         * Gets the name of the column.
         * @return The name of the column.
         */
        public String getName() {
            return mName;
        }

        /**
         * This is called to start a new row.
         */
        protected abstract void onStartLine();

        /**
         * Adds a data point to the statistics.
         * @param value The value to be added.
         */
        public abstract void addValue(T value);

        /**
         * Process the data accumulated since {@link #onStartLine()} has been called.
         *
         * @return The value to be printed.
         */
        public abstract Object getStat();
    }

    /**
     * This class represents a simple statistic column. It prints a summary of the data collected
     * during a period. If the data size is 1, it prints the value itself. If the data size is > 1,
     * it prints the mean, the count and the standard deviation.
     */
    public static class GVRStandardColumn<T extends Number> extends GVRColumnBase<T> {
        protected static String sDefaultDecimalFormat = "0.##";
        protected DecimalFormat mDecimalFormat = new DecimalFormat(sDefaultDecimalFormat);

        protected List<T> mData = new ArrayList<T>();

        /**
         * Constructor.
         * @param name
         *         The name of the statistic column. It will be printed in the log line.
         */
        public GVRStandardColumn(String name) {
            super(name);
        }

        @Override
        public synchronized void reset() {
            super.reset();
            mData.clear();
        }

        @Override
        protected synchronized void onStartLine() {
            mData.clear();
        }

        @Override
        public synchronized void addValue(T value) {
            if (value == null) {
                return;
            }

            mData.add(value);
        }

        @Override
        public synchronized Object getStat() {
            switch (mData.size()) {
                case 0:
                    return "n/a";
                case 1: {
                    return formatDecimal(mData.get(0).doubleValue());
                }
                default: {
                    DescriptiveResult res = Stats.computeDescriptive(mData);
                    return String.format("%s (n=%d, sd=%s)",
                                         formatDecimal(res.mean), mData.size(),
                                         formatDecimal(res.stdev));
                }
            }
        }

        protected String formatDecimal(double value) {
            return mDecimalFormat.format(value);
        }

        /**
         * Sets the format string for decimals.
         * @param fmt The format string. See {@link DecimalFormat}.
         */
        public void setNumberFormat(String fmt) {
            mDecimalFormat = new DecimalFormat(fmt);
        }
    }
}
