package de.shadowhunt.cmdl.command.subversion;

import java.net.URI;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.ServerVersion;
import de.shadowhunt.scm.subversion.SubversionFactory;
import de.shadowhunt.scm.subversion.SubversionInfo;
import de.shadowhunt.scm.subversion.SubversionRepository;

public class UnlockCommand extends AbstractCommand {

	public UnlockCommand() {
		super("unlock");
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final URI root = getRepositoryRoot(cmdl);
		final String user = getUser(cmdl);
		final String password = getPassword(cmdl);
		final String workstation = getWorkstation(cmdl);
		final ServerVersion version = getServerVersion(cmdl);

		final SubversionRepository repositry = SubversionFactory.getInstance(root, user, password, workstation, version);
		final String resource = getTargetResource(cmdl);
		final SubversionInfo info = repositry.info(resource, false);
		repositry.unlock(resource, info);
		System.out.println("'" + resource + "' unlocked.");
	}
}
