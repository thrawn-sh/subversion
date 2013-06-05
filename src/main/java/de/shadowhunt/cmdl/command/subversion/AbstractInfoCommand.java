package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.Path;
import de.shadowhunt.scm.subversion.Revision;
import de.shadowhunt.scm.subversion.SubversionInfo;
import de.shadowhunt.scm.subversion.SubversionRepository;

abstract class AbstractInfoCommand extends AbstractRevisionCommand {

	protected AbstractInfoCommand(final String name, final PrintWriter out) {
		super(name, out);
	}

	protected SubversionInfo getInfo(final CommandLine cmdl) throws Exception {
		final SubversionRepository repositry = createRepository(cmdl);
		final Path resource = getTargetResource(cmdl);
		final Revision revision = getRevision(cmdl);
		if (revision == null) {
			return repositry.info(resource, false);
		}
		return repositry.info(resource, revision, false);
	}
}
