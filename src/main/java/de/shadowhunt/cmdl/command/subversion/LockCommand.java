package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.Path;
import de.shadowhunt.scm.subversion.SubversionRepository;

/**
 * Subversion lock command
 */
public class LockCommand extends AbstractCommand {

	/**
	 * Create a new Subversion lock command
	 * @param out {@link PrintWriter} to write the complete command output to (will not be closed)
	 */
	public LockCommand(final PrintWriter out) {
		super("lock", out);
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final SubversionRepository repositry = createRepository(cmdl);
		final Path resource = getTargetResource(cmdl);
		repositry.lock(resource);
		final String user = getUser(cmdl);
		out.println("'" + resource + "' locked by user '" + user + "'.");
	}
}
