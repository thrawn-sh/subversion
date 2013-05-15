package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.SubversionRepository;

public class UnlockCommand extends AbstractCommand {

	public UnlockCommand(final PrintWriter out) {
		super("unlock", out);
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final SubversionRepository repositry = createRepository(cmdl);
		final String resource = getTargetResource(cmdl);
		repositry.unlock(resource);
		out.println("'" + resource + "' unlocked.");
	}
}
