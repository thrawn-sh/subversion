package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.Path;
import de.shadowhunt.scm.subversion.Revision;
import de.shadowhunt.scm.subversion.InfoEntry;
import de.shadowhunt.scm.subversion.Repository;

abstract class AbstractInfoCommand extends AbstractRevisionCommand {

	protected AbstractInfoCommand(final String name, final PrintWriter out) {
		super(name, out);
	}

	protected InfoEntry getInfo(final CommandLine cmdl) throws Exception {
		final Repository repositry = createRepository(cmdl);
		final Path resource = getTargetResource(cmdl);
		final Revision revision = getRevision(cmdl);
		return repositry.info(resource, revision, false);
	}
}
