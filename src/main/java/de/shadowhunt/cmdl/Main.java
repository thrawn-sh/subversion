package de.shadowhunt.cmdl;

import java.util.Locale;
import java.util.ServiceLoader;

import javax.annotation.CheckForNull;

import de.shadowhunt.cmdl.command.Command;

/**
 * Main class for command-line clients
 */
public final class Main {

	private static final String HELP = "help";

	@CheckForNull
	static Command getCommandForName(final String name) {
		final String lowerCase = name.toLowerCase(Locale.US);
		for (final Command provider : ServiceLoader.load(Command.class)) {
			if (lowerCase.equals(provider.getName())) {
				return provider;
			}
		}
		return null;
	}

	static String getCommandName(final String[] args) {
		if ((args.length == 0) || (args[0].charAt(0) == '-')) {
			return HELP;
		}
		return args[0];
	}

	static String[] getCommandOptions(final String[] args) {
		final int length = Math.max(0, args.length - 1);
		final String[] options = new String[length];
		if (length > 0) {
			// remove command name from arguments
			System.arraycopy(args, 1, options, 0, length);
		}
		return options;
	}

	static Command getHelpCommand() {
		final Command help = getCommandForName(HELP);
		if (help == null) {
			throw new UnsupportedOperationException("can't create " + HELP + " command");
		}
		return help;
	}

	/**
	 * Start command-line client
	 * @param args command-line arguments
	 * @throws Exception in case of arbitrary errors
	 */
	public static void main(final String[] args) throws Exception {
		final String commandName = getCommandName(args);
		final Command command = getCommandForName(commandName);
		if (command != null) {
			final String[] options = getCommandOptions(args);
			command.execute(options);
			return;
		}
		getHelpCommand().execute();
	}

	private Main() {
		// prevent instantiation
	}
}
