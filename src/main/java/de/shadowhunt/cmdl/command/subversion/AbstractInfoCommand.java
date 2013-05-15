package de.shadowhunt.cmdl.command.subversion;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.SubversionInfo;
import de.shadowhunt.scm.subversion.SubversionRepository;

abstract class AbstractInfoCommand extends AbstractRevisionCommand {

	protected AbstractInfoCommand(final String name) {
		super(name);
	}

	protected SubversionInfo getInfo(final CommandLine cmdl) throws Exception {
		final SubversionRepository repositry = createRepository(cmdl);
		final String resource = getTargetResource(cmdl);
		final Integer revision = getRevision(cmdl);
		if (revision == null) {
			return repositry.info(resource, false);
		}
		return repositry.info(resource, revision, false);
	}
}
