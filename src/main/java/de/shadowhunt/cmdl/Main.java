package de.shadowhunt.cmdl;

import de.shadowhunt.cmdl.command.Command;
import de.shadowhunt.cmdl.command.subversion.HelpCommand;

public class Main {

	static Command getCommandForName(final String name) {
		return new HelpCommand();
	}

	static String getCommandName(final String[] args) {
		if ((args.length == 0) || (args[0].charAt(0) == '-')) {
			return "help";
		}
		return args[0];
	}

	static String[] getCommandOptions(final String[] args) {
		final int length = Math.max(0, args.length - 1);
		final String[] options = new String[length];
		if (length > 0) {
			System.arraycopy(args, 1, options, 0, length);
		}
		return options;
	}

	public static void main(final String[] args) throws Exception {
		final String commandName = getCommandName(args);
		final Command command = getCommandForName(commandName);
		final String[] options = getCommandOptions(args);

		command.execute(options);
	}
}
