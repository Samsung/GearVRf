package smcl.samsung.com.debugwebserver;

import org.gearvrf.GVRContext;
import org.gearvrf.debug.GVRConsoleFactory;
import org.gearvrf.debug.ShellCommandHandler;
import org.gearvrf.debug.cli.Shell;
import org.gearvrf.utility.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

class DebugShellRunnable implements Runnable {
    public static final String TAG = DebugShellRunnable.class.getSimpleName();
    private static final String PROMPT = "gvrf";
    private static final String APP_NAME = "GearVR Framework";

    Shell shell;

    DebugShellRunnable(GVRContext gvrContext, InputStream inputStream, OutputStream
            outputStream) {
        PrintStream printStream = new PrintStream(outputStream);
        shell = GVRConsoleFactory.createConsoleShell(PROMPT, APP_NAME, new ShellCommandHandler
                        (gvrContext), new BufferedReader(new InputStreamReader(inputStream)),
                printStream, printStream, null);
    }

    @Override
    public void run() {
        try {
            shell.commandLoop();
        } catch (IOException e) {
            Log.d(TAG,"Exception in shell commandLoop:",e);
        }
    }
}