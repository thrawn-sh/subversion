package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

abstract class AbstractRevisionCommand extends AbstractCommand {

	private static final String REVISION_OPTION = "r";

	protected AbstractRevisionCommand(final String name, final PrintWriter out) {
		super(name, out);
	}

	@Override
	protected Options getOptions() {
		final Options options = super.getOptions();
		options.addOption(REVISION_OPTION, "revision", true, "The revision number");
		return options;
	}

	protected Integer getRevision(final CommandLine cmdl) {
		final String revision = cmdl.getOptionValue(REVISION_OPTION);
		return Integer.valueOf(revision);
	}
}
