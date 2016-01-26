package org.gearvrf.debug;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.debug.cli.CommandTable;
import org.gearvrf.debug.cli.ConsoleIO;
import org.gearvrf.debug.cli.DashJoinedNamer;
import org.gearvrf.debug.cli.HelpCommandHandler;
import org.gearvrf.debug.cli.Shell;
import org.gearvrf.debug.cli.util.ArrayHashMultiMap;
import org.gearvrf.debug.cli.util.MultiMap;

class ConsoleFactory {
    /**
     * Facade method for operating the Shell allowing specification of auxiliary
     * handlers (i.e. handlers that are to be passed to all subshells).
     *
     * Run the obtained Shell with commandLoop().
     *
     * @see org.gearvrf.debug.cli.Shell#Shell(org.gearvrf.debug.cli.Shell.Settings, org.gearvrf.debug.cli.CommandTable, java.util.List)
     *
     * @param prompt Prompt to be displayed
     * @param appName The app name string
     * @param mainHandler Main command handler
     * @param auxHandlers Aux handlers to be passed to all subshells.
     * @return Shell that can be either further customized or run directly by calling commandLoop().
     */
    static Shell createConsoleShell(String prompt, String appName, Object mainHandler,
            BufferedReader in, PrintStream out, PrintStream err) {
        ConsoleIO io = new ConsoleIO(in, out, err);

        List<String> path = new ArrayList<String>(1);
        path.add(prompt);

        MultiMap<String, Object> modifAuxHandlers = new ArrayHashMultiMap<String, Object>();
        modifAuxHandlers.put("!", io);

        Shell theShell = new Shell(new Shell.Settings(io, io, modifAuxHandlers, false),
                new CommandTable(new DashJoinedNamer(true)), path);
        theShell.setAppName(appName);

        theShell.addMainHandler(theShell, "!");
        theShell.addMainHandler(new HelpCommandHandler(), "?");
        theShell.addMainHandler(mainHandler, "");

        return theShell;
    }
}
