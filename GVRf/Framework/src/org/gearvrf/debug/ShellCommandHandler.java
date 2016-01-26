package org.gearvrf.debug;

import java.util.List;

import javax.script.ScriptEngine;

import org.gearvrf.GVRContext;
import org.gearvrf.debug.cli.Command;
import org.gearvrf.debug.cli.HelpCommandHandler;
import org.gearvrf.debug.cli.Shell;
import org.gearvrf.debug.cli.ShellDependent;
import org.gearvrf.script.GVRScriptManager;

/*package*/ class ShellCommandHandler implements ShellDependent {
    protected Shell mShell;
    protected GVRContext mGVRContext;
    protected HelpCommandHandler mHelpHandler = new HelpCommandHandler();

    protected ScriptHandler mScriptHandler;
    protected List<String> mSavedPath;

    public ShellCommandHandler(GVRContext gvrContext) {
        mGVRContext = gvrContext;
    }

    @Command
    public void lua() {
        enterLanguage(GVRScriptManager.LANG_LUA);
    }

    @Command
    public void js() {
        enterLanguage(GVRScriptManager.LANG_JAVASCRIPT);
    }

    private void enterLanguage(String language) {
        ScriptEngine engine = mGVRContext.getScriptManager().getEngine(language);

        if (engine == null) {
            mShell.getOutputConverter().convertOutput("Cannot find the language engine for " + language);
            return;
        }

        mScriptHandler = new ScriptHandler(mGVRContext, language, engine);
        mShell.setLineProcessor(mScriptHandler);
    }

    @Command
    public Object help() {
        return mHelpHandler.help();
    }

    @Command
    public void exit() {
        // empty
    }

    @Override
    public void cliSetShell(Shell theShell) {
        mShell = theShell;
        mHelpHandler.cliSetShell(mShell);
    }
}
