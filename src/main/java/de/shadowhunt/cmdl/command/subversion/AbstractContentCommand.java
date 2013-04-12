package de.shadowhunt.cmdl.command.subversion;

import java.io.InputStream;
import java.net.URI;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.ServerVersion;
import de.shadowhunt.scm.subversion.SubversionFactory;
import de.shadowhunt.scm.subversion.SubversionRepository;

abstract class AbstractContentCommand extends AbstractRevisionCommand {

	protected AbstractContentCommand(final String name) {
		super(name);
	}

	protected InputStream getContent(final CommandLine cmdl) throws Exception {
		final URI root = getRepositoryRoot(cmdl);
		final String user = getUser(cmdl);
		final String password = getPassword(cmdl);
		final String workstation = getWorkstation(cmdl);
		final ServerVersion version = getServerVersion(cmdl);

		final SubversionRepository repositry = SubversionFactory.getInstance(root, user, password, workstation, version);
		final String resource = getTargetResource(cmdl);
		final Long revision = getRevision(cmdl);
		if (revision == null) {
			return repositry.download(resource);
		}
		return repositry.download(resource, revision);
	}

}
