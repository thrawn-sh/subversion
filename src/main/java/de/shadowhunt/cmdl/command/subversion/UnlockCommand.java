package de.shadowhunt.cmdl.command.subversion;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.SubversionRepository;

public class UnlockCommand extends AbstractCommand {

	public UnlockCommand() {
		super("unlock");
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final SubversionRepository repositry = createRepository(cmdl);
		final String resource = getTargetResource(cmdl);
		repositry.unlock(resource);
		System.out.println("'" + resource + "' unlocked.");
	}
}
