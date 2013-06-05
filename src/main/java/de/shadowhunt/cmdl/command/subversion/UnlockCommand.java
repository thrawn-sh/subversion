package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.Path;
import de.shadowhunt.scm.subversion.SubversionRepository;

/**
 * Subversion unlock command
 */
public class UnlockCommand extends AbstractSubversionCommand {

	/**
	 * Create a new Subversion unlock command
	 * @param out {@link PrintWriter} to write the complete command output to (will not be closed)
	 */
	public UnlockCommand(final PrintWriter out) {
		super("unlock", out);
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final SubversionRepository repositry = createRepository(cmdl);
		final Path resource = getTargetResource(cmdl);
		repositry.unlock(resource);
		out.println("'" + resource + "' unlocked.");
	}
}
