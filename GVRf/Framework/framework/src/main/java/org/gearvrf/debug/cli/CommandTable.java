/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Command table is responsible for managing a lot of ShellCommands and is like a dictionary,
 * because its main function is to return a command by name.
 *
 * @author ASG
 */
public class CommandTable {

    private List<ShellCommand> commandTable = new ArrayList<ShellCommand>();
    private CommandNamer namer;

    public CommandNamer getNamer() {
        return namer;
    }

    public CommandTable(CommandNamer namer) {
        this.namer = namer;
    }

    public List<ShellCommand> getCommandTable() {
        return Collections.unmodifiableList(commandTable);
    }

    public void addMethod(Method method, Object handler, String prefix) {
        Command annotation = method.getAnnotation(Command.class);
        assert method != null;
        String name;
        String autoAbbrev = null;

        if (annotation != null && annotation.name() != null && ! annotation.name().equals("")) {
            name = annotation.name();
        } else {
            CommandNamer.NamingInfo autoNames = namer.nameCommand(method);
            name = autoNames.commandName;
            for (String abbr : autoNames.possibleAbbreviations) {
                if (!doesCommandExist(prefix + abbr, method.getParameterTypes().length)) {
                    autoAbbrev = abbr;
                    break;
                }
            }
        }
        
        ShellCommand command = new ShellCommand(handler, method, prefix, name);

        if (annotation != null && annotation.abbrev() != null && ! annotation.abbrev().equals("")) {
            command.setAbbreviation(annotation.abbrev());
        } else {
            command.setAbbreviation(autoAbbrev);
        }
        if (annotation != null && annotation.description() != null && !annotation.description().equals("")) {
            command.setDescription(annotation.description());
        }
        if (annotation != null && annotation.header() != null && ! annotation.header().equals("")) {
            command.setHeader(annotation.header());
        }
        
        commandTable.add(command);

    }

    private boolean doesCommandExist(String commandName, int arity) {
        for (ShellCommand cmd : commandTable) {
            if (cmd.canBeDenotedBy(commandName) && cmd.getArity() == arity) {
                return true;
            }
        }
        return false;
    }


    public List<ShellCommand> commandsByName(String discriminator) {
        List<ShellCommand> collectedTable = new ArrayList<ShellCommand>();
        // collection
        for (ShellCommand cs : commandTable) {
            if (cs.canBeDenotedBy(discriminator)) {
                collectedTable.add(cs);
            }
        }
        return collectedTable;
    }

    public ShellCommand lookupCommand(String discriminator, List<Token> tokens) throws CLIException {
        List<ShellCommand> collectedTable = commandsByName(discriminator);
        // reduction
        List<ShellCommand> reducedTable = new ArrayList<ShellCommand>();
        for (ShellCommand cs : collectedTable) {
            if (cs.getMethod().getParameterTypes().length == tokens.size()-1
                    || (cs.getMethod().isVarArgs()
                        && (cs.getMethod().getParameterTypes().length <= tokens.size()-1))) {
                reducedTable.add(cs);
            }
        }
        // selection
        if (collectedTable.size() == 0) {
            throw CLIException.createCommandNotFound(discriminator);
        } else if (reducedTable.size() == 0) {
            throw CLIException.createCommandNotFoundForArgNum(discriminator, tokens.size()-1);
        } else if (reducedTable.size() > 1) {
            throw CLIException.createAmbiguousCommandExc(discriminator, tokens.size()-1);
        } else {
            return reducedTable.get(0);
        }
    }


}
