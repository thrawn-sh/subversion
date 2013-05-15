package de.shadowhunt.cmdl.command.subversion;

import java.io.InputStream;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.SubversionRepository;

abstract class AbstractContentCommand extends AbstractRevisionCommand {

	protected AbstractContentCommand(final String name) {
		super(name);
	}

	protected InputStream getContent(final CommandLine cmdl) throws Exception {
		final SubversionRepository repositry = createRepository(cmdl);
		final String resource = getTargetResource(cmdl);
		final Integer revision = getRevision(cmdl);
		if (revision == null) {
			return repositry.download(resource);
		}
		return repositry.download(resource, revision);
	}

}
