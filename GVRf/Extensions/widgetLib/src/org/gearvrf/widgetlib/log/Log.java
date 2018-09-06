package org.gearvrf.widgetlib.log;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools.SynchronizedPool;
import android.util.SparseArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * Widgetlib Log provides highly configurable logging facilities. The logging subsystem is configured
 * using a system of log categories and log type. Log categories define what messages to capture,
 * and log types define how to deal with those messages (write to disk, send to console).
 *
 * Log levels indicate the nature and severity of a log message. The level of a given log message
 * is specified by the developer using the appropriate methods of their chosen logging framework
 * to send the message. Widgetlib supports the same set of standard android log message priorities:
 * Verbose, Debug, Info, Warn, Error, Assert
 *
 */
public class Log {
    private enum TYPE {
        /**
         * Standard android output
         * Each message has a format: timestamp: PRIORITY/tag (PID): message
         */
        ANDROID,
        /**
         * Save the log messages to the log file(s).
         * These are the log files that get created for the default logging configurations:

         * /storage/emulated/0/Android/data/<app_package_name>/files/Documents/widgetlib
         * It can produce multiple files. Each file should not exceed the size:
         * {@link PersistentLog#FILE_SIZE_LIMIT}
         *
         * The files stay in the memory no longer than {@link PersistentLog#MAXFILEAGE}
         *
         * The file name format is logFile.yyyy-MM-dd_HH:mm:ss
         *
         */
        PERSISTENT
    }

    public enum MODE {
        /**
         * Save full log into the file {@link PersistentLog#FULL_FILE_PATH_BASE}
         */
        DEBUG,
        /**
         * Save widgetLibrary debug only into the file(s)  {@link PersistentLog#FILE_PATH_BASE}
         */
        DEVELOPER,
        /**
         * Print the messages out to the standard android console
         */
        USER
    }

    /**
     * Simple interface for subsystem definition
     */
    public interface Subsystem {
        /**
         * Standard {@code Enum} method.
         */
        String name();
    }

    /**
     * Log categories define a set of log messages to capture. By default only MAIN subsystem is
     * captured. More than one subsystem can be captured at the same time.
     * The set of enabled subsystems can be changed by either {@link #enableSubsystem} or
     * {@link #enableAllSubsystems}
     */
    public enum SUBSYSTEM implements Subsystem {
        MAIN,
        WIDGET,
        LAYOUT,
        FOCUS,
        TRACING,
        JSON,
        INPUT;
    }

    /**
     * Get current debug mode
     */
    public static MODE getMode() {
        return mode;
    }

    /**
     * Initialize the default logging system. {@link MODE#DEVELOPER} is used as a default one, and
     * only {@link SUBSYSTEM#MAIN} category is activated. Other categories might produce a lot of
     * log messages that might affect the performance.
     * In release version for the best performance the logging system should be turned off or at
     * least only the most important categories enabled.
     * @param context
     * @param enable true if the logging system is enabled, false - *All* logging is disabled
     */
    public static void init(Context context, boolean enable) {
        PersistentLog.init(context);

        // `adb shell setprop vrtop.logging.enable true`
        enable_logging = enable;
        android.util.Log.d(TAG, "enable_logging: " + enable_logging);
        rebuild(MODE.DEVELOPER);

        enableSubsystem(SUBSYSTEM.MAIN, true);
//        enableSubsystem(SUBSYSTEM.FOCUS, true);

//        enableSubsystem(SUBSYSTEM.TRACING, true);
//        enableSubsystem(SUBSYSTEM.LAYOUT, true);
//        enableSubsystem(SUBSYSTEM.WIDGET, true);

    }

    /**
     * Enable/disable the logging component
     * @param subsystem The logging component
     * @param enable true - to enable it, false - to disable it
     */
    public static void enableSubsystem(Subsystem subsystem, boolean enable) {
        if (enable_logging) {
            if (enable) {
                mEnabledSubsystems.add(subsystem);
            } else {
                mEnabledSubsystems.remove(subsystem);
            }
        }
    }

    /**
     * Enable/disable all logging component
     * @param enable true - to enable all logging components from the default list; false - disable
     *               all currently enabled logging component including the custom ones
     */
    public static void enableAllSubsystems(boolean enable) {
        if (enable_logging) {
            if (enable) {
                for (Subsystem s : SUBSYSTEM.values()) {
                    mEnabledSubsystems.add(s);
                }
            } else {
                mEnabledSubsystems.clear();
            }
        }
    }

    /**
     * Rebuild logging systems with updated mode
     * @param newMode log mode
     */
    public static void rebuild(final MODE newMode) {
        if (mode != newMode) {
            mode = newMode;
            TYPE type;
            switch (mode) {
            case DEBUG:
                type = TYPE.ANDROID;
                Log.startFullLog();
                break;
            case DEVELOPER:
                type = TYPE.PERSISTENT;
                Log.stopFullLog();
                break;
            case USER:
                type = TYPE.ANDROID;
                break;
            default:
                type =  DEFAULT_TYPE;
                Log.stopFullLog();
                break;
            }
            currentLog = getLog(type);
        }
    }

    /**
     * Check if the logging component is enabled
     * @param subsystem the logging component
     * @return true if it is currently enabled; otherwise - false
     */
    public static boolean isEnabled(Subsystem subsystem) {
        return mEnabledSubsystems.contains(subsystem);
    }

    /**
     * Pause the logging
     */
    public static void pause() {
        currentLog.pause();
    }

    /**
     * Resume the logging
     */
    public static void resume() {
        currentLog.resume();
    }

    /**
     * Send a DEBUG log message with {@link SUBSYSTEM#MAIN} as default one
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return
     */
    public static int d(String tag, String msg) {
        return d(SUBSYSTEM.MAIN, tag, msg);
    }

    /**
     * Send a DEBUG log message with {@link SUBSYSTEM#MAIN} as default one and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     *
     * @return
     */
    public static int d(String tag, String msg, Throwable tr) {
        return d(SUBSYSTEM.MAIN, tag, msg, tr);
    }

    /**
     * Send an ERROR log message with {@link SUBSYSTEM#MAIN} as default one .
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return
     */
    public static int e(String tag, String msg) {
        return e(SUBSYSTEM.MAIN, tag, msg);
    }

    /**
     * Send an ERROR log message with {@link SUBSYSTEM#MAIN} as default one and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     *
     * @return
     */
    public static int e(String tag, String msg, Throwable tr) {
        return e(SUBSYSTEM.MAIN, tag, msg, tr);
    }
    /**
     * Send an INFO log message with {@link SUBSYSTEM#MAIN} as default one .
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return
     */
    public static int i(String tag, String msg) {
        return i(SUBSYSTEM.MAIN, tag, msg);
    }

    /**
     * Send an INFO log message with {@link SUBSYSTEM#MAIN} as default one and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     *
     * @return
     */
    public static int i(String tag, String msg, Throwable tr) {
        return i(SUBSYSTEM.MAIN, tag, msg, tr);
    }

    /**
     * Send a VERBOSE log message with {@link SUBSYSTEM#MAIN} as default one .
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return
     */
    public static int v(String tag, String msg) {
        return v(SUBSYSTEM.MAIN, tag, msg);
    }

    /**
     * Send a VERBOSE log message with {@link SUBSYSTEM#MAIN} as default one and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     *
     * @return
     */
    public static int v(String tag, String msg, Throwable tr) {
        return v(SUBSYSTEM.MAIN, tag, msg, tr);
    }

    /**
     * Send a WARN log message with {@link SUBSYSTEM#MAIN} as default one .
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return
     */
    public static int w(String tag, String msg) {
        return w(SUBSYSTEM.MAIN, tag, msg);
    }

    /**
     * Send a WARN log message with {@link SUBSYSTEM#MAIN} as default one and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     *
     * @return
     */
    public static int w(String tag, String msg, Throwable tr) {
        return w(SUBSYSTEM.MAIN, tag, msg, tr);
    }

    /**
     * Log WARN exception with {@link SUBSYSTEM#MAIN} as default one.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param tr An exception to log.
     * @return
     */
    public static int w(String tag, Throwable tr) {
        return w(SUBSYSTEM.MAIN, tag, tr);
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen. The error will always
     * be logged at level ASSERT with the call stack  and with {@link SUBSYSTEM#MAIN} as default one.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return
     */
    public static int wtf(String tag, String msg) {
        return wtf(SUBSYSTEM.MAIN, tag, msg);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen with
     * {@link SUBSYSTEM#MAIN} as default one. Similar to {@link #wtf(String, String)},
     * with an exception to log.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param tr An exception to log.
     * @return
     */
    public static int wtf(String tag, Throwable tr) {
        return wtf(SUBSYSTEM.MAIN, tag, tr);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen with
     * {@link SUBSYSTEM#MAIN} as default one. Similar to {@link #wtf(String, Throwable)},
     * with a message as well.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     * @return
     */
    public static int wtf(String tag, String msg, Throwable tr) {
        return wtf(SUBSYSTEM.MAIN, tag, msg, tr);
    }

    /**
     * Send a DEBUG log message with specified subsystem. If subsystem is not enabled the message
     * will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return
     */
    public static int d(Subsystem subsystem, String tag, String msg) {
        return isEnabled(subsystem) ?
                currentLog.d(tag, getMsg(subsystem,msg)) : 0;
    }

    /**
     * Send a DEBUG log message with specified subsystem and log the exception.
     * If subsystem is not enabled the message will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     *
     * @return
     */
    public static int d(Subsystem subsystem, String tag, String msg, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.d(tag, getMsg(subsystem,msg), tr) : 0;
    }

    /**
     * Send an ERROR log message with specified subsystem. If subsystem is not enabled the message
     * will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return
     */
    public static int e(Subsystem subsystem, String tag, String msg) {
        return isEnabled(subsystem) ?
                currentLog.e(tag, getMsg(subsystem,msg)) : 0;
    }

    /**
     * Send an ERROR log message with specified subsystem and log the exception.
     * If subsystem is not enabled the message will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     *
     * @return
     */
    public static int e(Subsystem subsystem, String tag, String msg, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.e(tag, getMsg(subsystem,msg), tr) : 0;
    }

    /**
     * Send an INFO log message with specified subsystem. If subsystem is not enabled the message
     * will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return
     */
    public static int i(Subsystem subsystem, String tag, String msg) {
        return isEnabled(subsystem) ?
                currentLog.i(tag, getMsg(subsystem,msg)) : 0;
    }

    /**
     * Send an INFO log message with specified subsystem and log the exception.
     * If subsystem is not enabled the message will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     *
     * @return
     */
    public static int i(Subsystem subsystem, String tag, String msg, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.i(tag, getMsg(subsystem,msg), tr) : 0;
    }

    /**
     * Send a VERBOSE log message with specified subsystem. If subsystem is not enabled the message
     * will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return
     */
    public static int v(Subsystem subsystem, String tag, String msg) {
        return isEnabled(subsystem) ?
                currentLog.v(tag, getMsg(subsystem,msg)) : 0;
    }

    /**
     * Send a VERBOSE log message with specified subsystem and log the exception.
     * If subsystem is not enabled the message will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     *
     * @return
     */
    public static int v(Subsystem subsystem, String tag, String msg, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.v(tag, getMsg(subsystem,msg), tr) : 0;
    }

    /**
     * Log WARN exception with specified subsystem.
     * If subsystem is not enabled the message will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param tr An exception to log.
     * @return
     */
    public static int w(Subsystem subsystem, String tag, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.w(tag, getMsg(subsystem, ""), tr) : 0;
    }

    /**
     * Send a WARN log message with specified subsystem and log the exception.
     * If subsystem is not enabled the message will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     *
     * @return
     */
    public static int w(Subsystem subsystem, String tag, String msg, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.w(tag, getMsg(subsystem,msg), tr) : 0;
    }

    /**
     * Send a WARN log message with specified subsystem. If subsystem is not enabled the message
     * will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return
     */
    public static int w(Subsystem subsystem, String tag, String msg) {
        return isEnabled(subsystem) ?
                currentLog.w(tag, getMsg(subsystem,msg)) : 0;
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen with specified
     * subsystem. If subsystem is not enabled the message will not be logged.
     * Similar to {@link #wtf(String, String)}, with an exception to log.
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param tr An exception to log.
     * @return
     */
    public static int wtf(Subsystem subsystem, String tag, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.wtf(tag, getMsg(subsystem, ""), tr) : 0;
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen with specified
     * subsystem. If subsystem is not enabled the message will not be logged.
     * Similar to {@link #wtf(String, Throwable)}, with a message as well.
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     * @return
     */
    public static int wtf(Subsystem subsystem, String tag, String msg, Throwable tr) {
        return isEnabled(subsystem) ?
                currentLog.wtf(tag, getMsg(subsystem,msg), tr) : 0;
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen. The error will always
     * be logged at level ASSERT with the call stack  and with specified subsystem.
     * If subsystem is not enabled the message will not be logged.
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return
     */
    public static int wtf(Subsystem subsystem, String tag, String msg) {
        return isEnabled(subsystem) ?
                currentLog.wtf(tag, getMsg(subsystem,msg)) : 0;
    }

    /**
     * Send a DEBUG log message with specified subsystem. If subsystem is not enabled the message
     * will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param pattern The message pattern
     * @return
     */
    public static void d(Subsystem subsystem, String tag, String pattern, Object... parameters) {
        if (!isEnabled(subsystem)) return;
        d(subsystem, tag, format(pattern, parameters));
    }

    /**
     * Send an ERROR log message with specified subsystem. If subsystem is not enabled the message
     * will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param pattern The message pattern
     * @return
     */
    public static void e(Subsystem subsystem, String tag, String pattern, Object... parameters) {
        if (!isEnabled(subsystem)) return;
        e(subsystem, tag, format(pattern, parameters));
    }

    /**
     * Send an ERROR log message with specified subsystem and log the exception.
     * If subsystem is not enabled the message will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param tr An exception to log.
     * @param pattern The message pattern
     * @return
     */
    public static void e(Subsystem subsystem, String tag, Throwable tr, String pattern,
                         Object... parameters) {
        if (!isEnabled(subsystem)) return;
        e(subsystem, tag, format(pattern, parameters), tr);
    }
    /**
     * Send an INFO log message with specified subsystem. If subsystem is not enabled the message
     * will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param pattern The message pattern
     * @return
     */
    public static void i(Subsystem subsystem, String tag, String pattern, Object... parameters) {
        if (!isEnabled(subsystem)) return;
        i(subsystem, tag, format(pattern, parameters));
    }

    /**
     * Send a VERBOSE log message with specified subsystem. If subsystem is not enabled the message
     * will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param pattern The message pattern
     * @return
     */
    public static void v(Subsystem subsystem, String tag, String pattern, Object... parameters) {
        if (!isEnabled(subsystem)) return;
        v(subsystem, tag, format(pattern, parameters));
    }

    /**
     * Send a WARN log message with specified subsystem. If subsystem is not enabled the message
     * will not be logged
     * @param subsystem logging subsystem
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param pattern The message pattern
     * @return
     */
    public static void w(Subsystem subsystem, String tag, String pattern, Object... parameters) {
        if (!isEnabled(subsystem)) return;
        w(subsystem, tag, format(pattern, parameters));
    }

    /**
     * String getStackTraceString (Throwable tr) Handy function to get a loggable stack trace from
     * a Throwable
     * @param tr An exception to log
     * @return
     */
    public static String getStackTraceString(Throwable tr ) {
        return LogBase.getStackTraceString(tr);
    }

    /**
     * Send a DEBUG log message with subsystem {@link SUBSYSTEM#MAIN} as a default one.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param pattern The message pattern
     * @return
     */
    public static void d(String tag, String pattern, Object... parameters) {
        d(SUBSYSTEM.MAIN, tag, pattern, parameters);
    }

    /**
     * Send an ERROR log message with subsystem {@link SUBSYSTEM#MAIN} as a default one.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param pattern The message pattern
     * @return
     */
    public static void e(String tag, String pattern, Object... parameters) {
        e(SUBSYSTEM.MAIN, tag, pattern, parameters);
    }

    /**
     * Send an ERROR log message with subsystem {@link SUBSYSTEM#MAIN} as a default one
     * and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param tr An exception to log.
     * @param pattern The message pattern
     * @return
     */
    public static void e(String tag, Throwable tr, String pattern, Object... parameters) {
        e(SUBSYSTEM.MAIN, tag, tr, pattern, parameters);
    }

    /**
     * Send an INFO log message with subsystem {@link SUBSYSTEM#MAIN} as a default one.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param pattern The message pattern
     * @return
     */
    public static void i(String tag, String pattern, Object... parameters) {
        i(SUBSYSTEM.MAIN, tag, pattern, parameters);
    }

    /**
     * Send a VERBOSE log message with subsystem {@link SUBSYSTEM#MAIN} as a default one.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param pattern The message pattern
     * @return
     */
    public static void v(String tag, String pattern, Object... parameters) {
        v(SUBSYSTEM.MAIN, tag, pattern, parameters);
    }

    /**
     * Send a WARN log message with subsystem {@link SUBSYSTEM#MAIN} as a default one.
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param pattern The message pattern
     * @return
     */
    public static void w(String tag, String pattern, Object... parameters) {
        w(SUBSYSTEM.MAIN, tag, pattern, parameters);
    }




    private static void startFullLog() {
        PersistentLog.startFullLog();
    }

    private static void stopFullLog() {
        PersistentLog.stopFullLog();
    }

    private static String format(String pattern, Object... parameters) {
        return parameters == null || parameters.length == 0 ? pattern :
                String.format(pattern, parameters);
    }

    private static LogBase getLog(Log.TYPE type) {
        LogBase log = null;
        switch(type) {
            case ANDROID:
                log = new AndroidLog();
                break;
            case PERSISTENT:
                log = new PersistentLog();
                break;
            default:
                android.util.Log.w(TAG, "Incorrect logger type! type = " + type +
                        " Use default log type: " + DEFAULT_TYPE);
                break;
        }
        return log;
    }

    private static String getMsg(Subsystem subsystem, String msg) {
        return subsystem == SUBSYSTEM.MAIN ? msg :
                String.format(LOG_MSG_FORMAT, subsystem, msg);
    }

    private static final String LOG_MSG_FORMAT = "<%s> %s";

    private static Set<Subsystem> mEnabledSubsystems = new HashSet<>();
    private static MODE mode;
    private static final String TAG = "Log";

    private static final TYPE DEFAULT_TYPE = TYPE.PERSISTENT; // TYPE.ANDROID;
    private static final boolean frequentlyUpdating = false;
    private static boolean enable_logging;
    private static LogBase currentLog;


    private static abstract class LogBase {
        protected static String getStackTraceString (Throwable tr) {
            return android.util.Log.getStackTraceString(tr) + "\n";
        }

        private void pause() {
        }

        private void resume() {
        }

        abstract public int d(String tag, String msg);
        abstract public int d(String tag, String msg, Throwable tr);
        abstract public int e(String tag, String msg);
        abstract public int e(String tag, String msg, Throwable tr);
        abstract public int i(String tag, String msg);
        abstract public int i(String tag, String msg, Throwable tr);
        abstract public int v(String tag, String msg, Throwable tr);
        abstract public int v(String tag, String msg);
        abstract public int w(String tag, Throwable tr);
        abstract public int w(String tag, String msg, Throwable tr);
        abstract public int w(String tag, String msg);
        @SuppressWarnings("unused")
        abstract public int wtf(String tag, Throwable tr);
        abstract public int wtf(String tag, String msg, Throwable tr);
        abstract public int wtf(String tag, String msg);
    }

    private static class AndroidLog extends LogBase {
        public int d(String tag, String msg) {
            return android.util.Log.d(tag, msg);
        }
        public int d(String tag, String msg, Throwable tr) {
            return android.util.Log.d(tag, msg, tr);
        }
        public int e(String tag, String msg) {
            return android.util.Log.e(tag, msg);
        }
        public int e(String tag, String msg, Throwable tr) {
            return android.util.Log.e(tag, msg, tr);
        }
        public int i(String tag, String msg) {
            return android.util.Log.i(tag, msg);
        }
        public int i(String tag, String msg, Throwable tr) {
            return android.util.Log.i(tag, msg, tr);
        }
        public int v(String tag, String msg, Throwable tr)  {
            return android.util.Log.v(tag, msg, tr);
        }
        public int v(String tag, String msg) {
            return android.util.Log.v(tag, msg);
        }
        public int w(String tag, Throwable tr) {
            return android.util.Log.w(tag, tr);
        }
        public int w(String tag, String msg, Throwable tr) {
            return android.util.Log.w(tag, msg, tr);
        }
        public int w(String tag, String msg) {
            return android.util.Log.w(tag, msg);
        }
        public int wtf(String tag, Throwable tr) {
            return android.util.Log.wtf(tag, tr);
        }
        public int wtf(String tag, String msg, Throwable tr) {
            return android.util.Log.wtf(tag, msg, tr);
        }
        public int wtf(String tag, String msg) {
            return android.util.Log.wtf(tag, msg);
        }
    }

    private static class PersistentLog extends LogBase {
        private static final int WRITER_BUFFER_LEN = 8192; // number of chars
        static final String TAG = "PersistentLog";

        private static final SimpleDateFormat FILE_TIMESTAMP_FORMAT =
                new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss",
                java.util.Locale.getDefault());

        private static final SimpleDateFormat MSG_TIMESTAMP_FORMAT =
                new SimpleDateFormat("MM-dd HH:mm:ss.SSS",
                java.util.Locale.getDefault());

        private static final String MSG_FORMAT = "%s %s/%s (%s): %s\n";  // timestamp: PRIORITY/tag (PID): message
        private static final String LOG_FILE_NAME_FORMAT = "%s.%s";  // FILE_PATH_BASE_timestamp
        private static final int FILE_SIZE_LIMIT = 10000000;
        private static final long MAXFILEAGE = 86400000L; // 1 day in milliseconds

        private static File LOG_FILE_DIR;
        private static String FILE_PATH_BASE;
        private static String FULL_FILE_PATH_BASE;
        private BufferedWriter bufferedWriter;
        private File currentLogFile;
        private boolean isLogging = true; // start logging as earlier as possible

        private static final SparseArray<String> PRIORITY_NAME = new SparseArray<>();

        static {
            PRIORITY_NAME.put(android.util.Log.VERBOSE, "V");
            PRIORITY_NAME.put(android.util.Log.DEBUG, "D");
            PRIORITY_NAME.put(android.util.Log.INFO, "I");
            PRIORITY_NAME.put(android.util.Log.WARN, "W");
            PRIORITY_NAME.put(android.util.Log.ERROR, "E");
            PRIORITY_NAME.put(android.util.Log.ASSERT, "F");
        }

        private final LinkedBlockingQueue<LogRequest> requestQueue;
        private ExecutorService executorService;
        private final LogWorker worker;

        static void init(Context context) {
            final File documentsDirs = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            LOG_FILE_DIR = new File(documentsDirs, "widgetlib");
            FILE_PATH_BASE = new File(LOG_FILE_DIR, "logFile").getAbsolutePath();
            FULL_FILE_PATH_BASE = new File(LOG_FILE_DIR, "fullLogFile").getAbsolutePath();
        }

        PersistentLog() {
            requestQueue = new LinkedBlockingQueue<>();
            worker = new LogWorker(requestQueue);
        }

        private void pause() {
            d(TAG, "pause logging!");
            writeToFile(new CloseLogRequest());
        }

        private void resume() {
            // start log request worker thread
            if (executorService == null) {
                executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
                    public Thread newThread(@NonNull Runnable r) {
                        Thread t = new Thread(r);
                        t.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
                        return t;
                    }
                });
                executorService.submit(worker);
            }
            writeToFile(new OpenLogRequest());
            d(TAG, "resume logging!");
            d(TAG, "LOG_FILE_DIR: " + LOG_FILE_DIR);
            d(TAG, "FILE_PATH_BASE: " + FILE_PATH_BASE);
            d(TAG, "FULL_FILE_PATH_BASE: " + FULL_FILE_PATH_BASE);
        }

        public int d(String tag, String msg) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.DEBUG, tag, msg));
            return android.util.Log.d(tag, msg);
        }
        public int d(String tag, String msg, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.DEBUG, tag, msg, tr));
            return android.util.Log.d(tag, msg, tr);
        }
        public int e(String tag, String msg) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.ERROR, tag, msg));
            return android.util.Log.e(tag, msg);
        }
        public int e(String tag, String msg, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.ERROR, tag, msg, tr));
            return android.util.Log.e(tag, msg, tr);
        }
        public int i(String tag, String msg) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.INFO, tag, msg));
            return android.util.Log.i(tag, msg);
        }
        public int i(String tag, String msg, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.INFO, tag, msg, tr));
            return android.util.Log.i(tag, msg, tr);
        }
        public int v(String tag, String msg, Throwable tr)  {
            writeToFile(MsgLogRequest.obtain(android.util.Log.VERBOSE, tag, msg, tr));
            return android.util.Log.v(tag, msg, tr);
        }
        public int v(String tag, String msg) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.VERBOSE, tag, msg));
            return android.util.Log.v(tag, msg);
        }
        public int w(String tag, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.WARN, tag, tr));
            return android.util.Log.w(tag, tr);
        }
        public int w(String tag, String msg, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.WARN, tag, msg, tr));
            return android.util.Log.w(tag, msg, tr);
        }
        public int w(String tag, String msg) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.WARN, tag, msg));
            return android.util.Log.w(tag, msg);
        }
        public int wtf(String tag, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.ASSERT, tag, tr));
            return android.util.Log.wtf(tag, tr);
        }
        public int wtf(String tag, String msg, Throwable tr) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.ASSERT, tag, msg, tr));
            return android.util.Log.wtf(tag, msg, tr);
        }
        public int wtf(String tag, String msg) {
            writeToFile(MsgLogRequest.obtain(android.util.Log.ASSERT, tag, msg));
            return android.util.Log.wtf(tag, msg);
        }

        private void writeToFile(final LogRequest request) {
            try {
                requestQueue.put(request);
            } catch (InterruptedException e) {
                android.util.Log.e(TAG, "writeToFile error: " + e.toString());
            }
        }

        private static String formatMsg(final int priority, final String tag, final String msg) {
            return String.format(MSG_FORMAT, getCurrentTimeStamp(MSG_TIMESTAMP_FORMAT),
                    getPriorityName(priority), tag, android.os.Process.myPid(), msg);
        }

        private static String getPriorityName(int priority) {
            String name = PRIORITY_NAME.get(priority);
            return name == null ? "" : name;
        }

        private interface LogRequest {
            enum TYPE {
                OPEN,
                CLOSE,
                MSG
            }
            void process(final boolean isLogging, final BufferedWriter bufferedWriter);
            TYPE getType();
        }

        private static class MsgLogRequest implements LogRequest {
            private String logMessage;
            private String trMessage;
            private boolean flushRequired;
            private static final int MAX_POOL_SIZE = 5;
            private static final SynchronizedPool<MsgLogRequest> sPool =
                    new SynchronizedPool<>(MAX_POOL_SIZE);

            private static MsgLogRequest obtain(final int priority, final String tag, final String msg, final Throwable tr) {
                MsgLogRequest instance = sPool.acquire();
                if (instance != null) {
                    instance.init(priority, tag, msg, tr);
                } else {
                    instance = new MsgLogRequest(priority, tag, msg, tr);
                }
                return instance;
            }

            private static MsgLogRequest obtain(final int priority, final String tag, final String msg) {
                return obtain(priority, tag, msg, null);
            }

            private static MsgLogRequest obtain(final int priority, final String tag, final Throwable tr) {
                return obtain(priority, tag, null, tr);
            }

            private void init(final int priority, String tag, final String msg, final Throwable tr) {
                if (msg != null) {
                    if (tag == null) {
                        tag = "";
                    }
                    logMessage = formatMsg(priority, tag, msg);
                }
                if (tr != null) {
                    trMessage = getStackTraceString(tr);
                }
                flushRequired = frequentlyUpdating ||
                        tr != null ||
                        priority >= android.util.Log.WARN;
            }

            @Override
            public TYPE getType() {
                return TYPE.MSG;
            }

            private MsgLogRequest(final int priority, final String tag, final String msg, final Throwable tr) {
                init(priority, tag, msg, tr);
            }

            private void recycle() {
                // Clear state if needed.
                sPool.release(this);
            }

            @Override
            public void process(final boolean isLogging, final BufferedWriter writer) {
                if (isLogging) {
                    try {
                        if (logMessage != null) {
                            writer.write(logMessage);
                        }

                        if (trMessage != null) {
                            writer.write(trMessage);
                        }

                        if (flushRequired) {
                            writer.flush();
                        }
                    } catch (IOException e) {
                        android.util.Log.e(TAG, e.toString());
                    } finally {
                        recycle();
                    }
                }
            }
        }

        private class CloseLogRequest implements LogRequest {
            @Override
            public void process(final boolean isLogging, final BufferedWriter bufferedWriter) {
                close();
                deleteOldAndEmptyFiles();
                if (requestQueue != null) {
                    requestQueue.clear();
                }
                executorService.shutdown();
                executorService = null;
                enableLog(false);
            }

            @Override
            public TYPE getType() {
                return TYPE.CLOSE;
            }

        }

        private class OpenLogRequest implements LogRequest {
            @Override
            public void process(final boolean isLogging, final BufferedWriter bufferedWriter) {
                enableLog(true);
            }

            @Override
            public TYPE getType() {
                return TYPE.OPEN;
            }
        }

        private void enableLog(final boolean enable) {
            isLogging = enable;
        }

        private class LogWorker implements Runnable {
            private final LinkedBlockingQueue<LogRequest> queue;

            LogWorker(LinkedBlockingQueue<LogRequest> queue) {
                this.queue = queue;
            }

            @Override
            public void run() {
                while (true) {
                    try {
                        // Get the next log request item off of the queue
                        LogRequest request = queue.take();

                        // Process log request
                        if (request != null) {
                            request.process(isLogging, getWriter());
                        }
                    }
                    catch ( InterruptedException ie ) {
                        break;
                    }
                }
            }
        }

        private BufferedWriter getWriter() {
            try {
                if (currentLogFile != null && currentLogFile.length() > FILE_SIZE_LIMIT) {
                    close();
                }

                if (currentLogFile == null && isLogging) {
                    currentLogFile = new File(nextFileName());
                    File parent = currentLogFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    } else if (currentLogFile.exists()) {
                        currentLogFile.delete();
                    }
                    currentLogFile.createNewFile();
                    bufferedWriter = new BufferedWriter(new FileWriter(currentLogFile, true), WRITER_BUFFER_LEN);
                }
            } catch (IOException e) {
                android.util.Log.e(TAG, "close error: " + e.toString());
            }
            return bufferedWriter;
        }

        private void close() {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                    bufferedWriter = null;
                    currentLogFile = null;
                }
            } catch (IOException e) {
                android.util.Log.e(TAG, "close error: " + e.toString());
            }
        }

        // delete of files more than 1 day old
        private static void deleteOldAndEmptyFiles() {
            File dir = LOG_FILE_DIR;
            if (dir.exists()) {
                File[] files = dir.listFiles();

                for (File f : files) {
                    if (f.length() == 0 ||
                            f.lastModified() + MAXFILEAGE < System.currentTimeMillis()) {
                        f.delete();
                    }
                }
            }
        }

        private static String getCurrentTimeStamp(final SimpleDateFormat dateFormat) {
            String currentTimeStamp = null;
            try {
                currentTimeStamp = dateFormat.format(new Date());
            } catch (Exception e) {
                android.util.Log.e(TAG, "getCurrentTimeStamp error: " + e.toString());
            }

            return currentTimeStamp;
        }

        private static String nextFileName() {
            return String.format(LOG_FILE_NAME_FORMAT, FILE_PATH_BASE, getCurrentTimeStamp(FILE_TIMESTAMP_FORMAT));
        }

        static void stopFullLog() {
            if (fullLogProcess != null) {
                fullLogProcess.destroy();
                fullLogProcess = null;
            }
        }

        private static Process fullLogProcess;
        private static void startFullLog() {
            deleteOldAndEmptyFiles();
            try {
                // clear logcat  buffer
                new ProcessBuilder()
                .command("logcat", "-c")
                .redirectErrorStream(true)
                .start();

                File fullLog = new File(String.format(LOG_FILE_NAME_FORMAT, FULL_FILE_PATH_BASE,
                        getCurrentTimeStamp(FILE_TIMESTAMP_FORMAT)));
                File parent = fullLog.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                } else if (fullLog.exists()) {
                    fullLog.delete();
                }

                // start logcat
                fullLogProcess = Runtime.getRuntime().exec("logcat -v time -f " + fullLog.getAbsolutePath());
            } catch (IOException e) {
               Log.e(TAG, "startLog error: " + e.toString());
            }
        }
    }
}
