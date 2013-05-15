package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import de.shadowhunt.scm.subversion.SubversionProperty;
import de.shadowhunt.scm.subversion.SubversionRepository;

public class PropertyDeleteCommand extends AbstractCommand {

	private static final String PROPERTY_OPTION = "p";

	public PropertyDeleteCommand(final PrintWriter out) {
		super("propdel", out);
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final String property = getProperty(cmdl);

		final SubversionRepository repositry = createRepository(cmdl);

		final String resource = getTargetResource(cmdl);
		repositry.deleteProperties(resource, "TODO", SubversionProperty.createCustomProperty(property, null));
	}

	@Override
	protected Options getOptions() {
		final Options options = super.getOptions();
		options.addOption(new Option(PROPERTY_OPTION, "property", false, "property name"));
		return options;
	}

	protected String getProperty(final CommandLine cmdl) {
		return cmdl.getOptionValue(PROPERTY_OPTION);
	}
}
