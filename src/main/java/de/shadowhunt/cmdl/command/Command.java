package de.shadowhunt.cmdl.command;

/**
 * {@link Command} can be executed with parameters
 */
public interface Command {

	/**
	 * Executes the {@link Command} with the given parameters
	 * @param arguments parameters to execute the command with
	 * @throws Exception if any error occurs
	 */
	public void execute(String... arguments) throws Exception;

	/**
	 * Returns the name of the {@link Command}
	 * @return the name of the {@link Command}
	 */
	public String getName();
}
