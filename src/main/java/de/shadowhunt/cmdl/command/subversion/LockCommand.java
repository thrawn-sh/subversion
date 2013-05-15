package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.SubversionRepository;

public class LockCommand extends AbstractCommand {

	public LockCommand(final PrintWriter out) {
		super("lock", out);
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final SubversionRepository repositry = createRepository(cmdl);
		final String resource = getTargetResource(cmdl);
		repositry.lock(resource);
		final String user = getUser(cmdl);
		out.println("'" + resource + "' locked by user '" + user + "'.");
	}
}
