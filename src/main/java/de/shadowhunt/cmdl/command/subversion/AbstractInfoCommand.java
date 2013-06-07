package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.subversion.InfoEntry;
import de.shadowhunt.subversion.Path;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Revision;

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
