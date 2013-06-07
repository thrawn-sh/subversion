package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.subversion.Path;
import de.shadowhunt.subversion.Repository;

/**
 * Subversion lock command
 */
public class LockCommand extends AbstractSubversionCommand {

	/**
	 * Create a new Subversion lock command
	 * @param out {@link PrintWriter} to write the complete command output to (will not be closed)
	 */
	public LockCommand(final PrintWriter out) {
		super("lock", out);
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final Repository repositry = createRepository(cmdl);
		final Path resource = getTargetResource(cmdl);
		repositry.lock(resource);
		final String user = getUser(cmdl);
		out.println("'" + resource + "' locked by user '" + user + "'.");
	}
}
