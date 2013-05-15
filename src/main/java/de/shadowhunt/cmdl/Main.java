package de.shadowhunt.cmdl;

import java.util.Locale;
import java.util.ServiceLoader;

import de.shadowhunt.cmdl.command.Command;

public class Main {

	static Command getCommandForName(final String name) {
		final String lowerCase = name.toLowerCase(Locale.US);
		for (final Command provider : ServiceLoader.load(Command.class)) {
			if (lowerCase.equals(provider.getName())) {
				return provider;
			}
		}

		throw new IllegalArgumentException("no provider for found.");
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
