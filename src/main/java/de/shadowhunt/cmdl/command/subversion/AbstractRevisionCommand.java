package de.shadowhunt.cmdl.command.subversion;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

abstract class AbstractRevisionCommand extends AbstractCommand {

	private static final String REVISION_OPTION = "revision";

	protected AbstractRevisionCommand(final String name) {
		super(name);
	}

	@Override
	protected Options getOptions() {
		final Options options = super.getOptions();
		options.addOption("r", REVISION_OPTION, true, "The revision number");
		return options;
	}

	protected Long getRevision(final CommandLine cmdl) {
		final String revision = cmdl.getOptionValue(REVISION_OPTION);
		return Long.valueOf(revision);
	}
}
