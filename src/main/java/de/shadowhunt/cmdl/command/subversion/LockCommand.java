package de.shadowhunt.cmdl.command.subversion;

import java.net.URI;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.ServerVersion;
import de.shadowhunt.scm.subversion.SubversionFactory;
import de.shadowhunt.scm.subversion.SubversionRepository;

public class LockCommand extends AbstractCommand {

	public LockCommand() {
		super("lock");
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
		repositry.lock(resource);
		System.out.println("'" + resource + "' locked by user '" + user + "'.");
	}
}
